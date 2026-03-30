package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.Feedback;
import com.project.eventplan.Entity.Guest;
import com.project.eventplan.Exception.BadRequestException;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.EventTeamMemberRepository;
import com.project.eventplan.Repository.EventRepository;
import com.project.eventplan.Repository.EventVendorRepository;
import com.project.eventplan.Repository.FeedbackRepository;
import com.project.eventplan.Repository.GuestRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackRepository repository;
    private final EventRepository eventRepository;
    private final GuestRepository guestRepository;
    private final SecurityUtil securityUtil;
    private final EventTeamMemberRepository eventTeamMemberRepository;
    private final EventVendorRepository eventVendorRepository;

    public FeedbackService(FeedbackRepository repository,
                           EventRepository eventRepository,
                           GuestRepository guestRepository,
                           SecurityUtil securityUtil,
                           EventTeamMemberRepository eventTeamMemberRepository,
                           EventVendorRepository eventVendorRepository) {
        this.repository = repository;
        this.eventRepository = eventRepository;
        this.guestRepository = guestRepository;
        this.securityUtil = securityUtil;
        this.eventTeamMemberRepository = eventTeamMemberRepository;
        this.eventVendorRepository = eventVendorRepository;
    }

    public Feedback addFeedback(Feedback feedback) {
        Event event = eventRepository.findById(feedback.getEvent().getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        Guest guest = guestRepository.findById(feedback.getGuest().getGuestId())
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
        validateFeedbackCreation(event, guest);
        feedback.setEvent(event);
        feedback.setGuest(guest);
        return repository.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        String email = securityUtil.getCurrentUserEmail();
        return repository.findAll().stream()
                .filter(feedback -> canAccessFeedback(feedback, email))
                .collect(Collectors.toList());
    }

    public Feedback getFeedbackById(Long id) {
        Feedback feedback = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
        validateOrganizerOwnership(feedback);
        return feedback;
    }

    public List<Feedback> getFeedbackByEvent(Long eventId) {
        List<Feedback> feedbackList = repository.findByEventEventId(eventId);
        feedbackList.forEach(this::validateOrganizerOwnership);
        return feedbackList;
    }

    public void deleteFeedback(Long id) {
        Feedback feedback = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
        validateOrganizerOwnership(feedback);
        repository.deleteById(id);
    }

    private void validateOrganizerOwnership(Feedback feedback) {
        String email = securityUtil.getCurrentUserEmail();

        if (!canAccessFeedback(feedback, email)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private boolean canAccessFeedback(Feedback feedback, String email) {
        if (feedback.getEvent() == null) {
            return false;
        }

        Long eventId = feedback.getEvent().getEventId();
        return (feedback.getEvent().getOrganizer() != null
                && email.equals(feedback.getEvent().getOrganizer().getEmail()))
                || eventTeamMemberRepository.existsByEvent_EventIdAndUser_Email(eventId, email)
                || eventVendorRepository.existsByEvent_EventIdAndUser_Email(eventId, email);
    }

    private void validateFeedbackCreation(Event event, Guest guest) {
        if (guest.getEvent() == null || !event.getEventId().equals(guest.getEvent().getEventId())) {
            throw new BadRequestException("Guest does not belong to the selected event");
        }
        if (!"Accepted".equalsIgnoreCase(guest.getRsvpStatus())) {
            throw new BadRequestException("Only accepted guests can submit feedback");
        }
        if (repository.existsByEventEventIdAndGuestGuestId(event.getEventId(), guest.getGuestId())) {
            throw new BadRequestException("Feedback already submitted for this guest");
        }
    }
}
