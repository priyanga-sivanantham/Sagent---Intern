package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.EventRepository;
import com.project.eventplan.Repository.EventTeamMemberRepository;
import com.project.eventplan.Repository.EventVendorRepository;
import com.project.eventplan.Repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventTeamMemberRepository eventTeamMemberRepository;
    private final EventVendorRepository eventVendorRepository;
    private final SecurityUtil securityUtil;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        EventTeamMemberRepository eventTeamMemberRepository,
                        EventVendorRepository eventVendorRepository,
                        SecurityUtil securityUtil) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventTeamMemberRepository = eventTeamMemberRepository;
        this.eventVendorRepository = eventVendorRepository;
        this.securityUtil = securityUtil;
    }

    public Event createEvent(Event event) {
        String email = securityUtil.getCurrentUserEmail();
        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }

    public List<Event> getEventsForCurrentUser() {
        String email = securityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return switch (user.getRole()) {
            case ORGANIZER -> eventRepository.findByOrganizer_Email(email);
            case TEAM_MEMBER -> eventRepository.findDistinctByEventTeamMembers_User_Email(email);
            case VENDOR -> eventRepository.findDistinctByEventVendors_User_Email(email);
        };
    }

    public Event getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        String email = securityUtil.getCurrentUserEmail();
        if (event.getOrganizer().getEmail().equals(email)
                || eventTeamMemberRepository.existsByEvent_EventIdAndUser_Email(id, email)
                || eventVendorRepository.existsByEvent_EventIdAndUser_Email(id, email)) {
            return event;
        }

        throw new AccessDeniedException("Access denied");
    }

    public Event updateEvent(Long id, Event updatedEvent) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        String email = securityUtil.getCurrentUserEmail();
        if (!event.getOrganizer().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not the owner of this event");
        }

        event.setEventName(updatedEvent.getEventName());
        event.setVenue(updatedEvent.getVenue());
        event.setEventDate(updatedEvent.getEventDate());
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        String email = securityUtil.getCurrentUserEmail();
        if (!event.getOrganizer().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not the owner");
        }

        eventRepository.delete(event);
    }
}
