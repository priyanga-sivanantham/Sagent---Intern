package com.project.eventplan.Service;

import com.project.eventplan.Config.JwtUtil;
import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Dto.FeedbackLinkDetailsResponse;
import com.project.eventplan.Dto.GuestInvitationDetailsResponse;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.Guest;
import com.project.eventplan.Exception.BadRequestException;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.EventRepository;
import com.project.eventplan.Repository.GuestRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class GuestService {

    private final GuestRepository guestRepository;
    private final EmailService emailService;
    private final EventRepository eventRepository;
    private final SecurityUtil securityUtil;
    private final JwtUtil jwtUtil;
    private final String publicBaseUrl;

    public GuestService(GuestRepository guestRepository,
                        EmailService emailService,
                        EventRepository eventRepository,
                        SecurityUtil securityUtil,
                        JwtUtil jwtUtil,
                        @Value("${app.public.base-url}") String publicBaseUrl) {
        this.guestRepository = guestRepository;
        this.emailService = emailService;
        this.eventRepository = eventRepository;
        this.securityUtil = securityUtil;
        this.jwtUtil = jwtUtil;
        this.publicBaseUrl = publicBaseUrl;
    }

    public Guest createGuest(Guest guest) {
        if (guest.getEvent() == null || guest.getEvent().getEventId() == null) {
            throw new BadRequestException("Event ID is required");
        }

        Event event = loadEvent(guest.getEvent().getEventId());
        validateOrganizerOwnership(event);
        guest.setEvent(event);
        return guestRepository.save(guest);
    }

    public List<Guest> getAllGuests(Long eventId) {
        String email = securityUtil.getCurrentUserEmail();
        if (eventId != null) {
            Event event = loadEvent(eventId);
            validateOrganizerOwnership(event);
            return guestRepository.findByEventEventIdAndEvent_Organizer_Email(eventId, email);
        }

        return guestRepository.findByEvent_Organizer_Email(email);
    }

    public Guest getGuestById(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
        validateOrganizerOwnership(guest.getEvent());
        return guest;
    }

    public List<Guest> getGuestsByEvent(Long eventId) {
        Event event = loadEvent(eventId);
        validateOrganizerOwnership(event);
        return guestRepository.findByEventEventIdAndEvent_Organizer_Email(eventId, securityUtil.getCurrentUserEmail());
    }

    public Guest updateGuest(Long id, Guest guest) {
        Guest existing = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
        validateOrganizerOwnership(existing.getEvent());

        if (guest.getEvent() == null || guest.getEvent().getEventId() == null) {
            throw new BadRequestException("Event ID is required");
        }

        Event event = loadEvent(guest.getEvent().getEventId());
        validateOrganizerOwnership(event);

        existing.setGuestName(guest.getGuestName());
        existing.setEmail(guest.getEmail());
        existing.setPhone(guest.getPhone());
        existing.setRsvpStatus(guest.getRsvpStatus());
        existing.setEvent(event);
        return guestRepository.save(existing);
    }

    public void sendInvitationsForEvent(Long eventId) {
        Event event = loadEvent(eventId);
        validateOrganizerOwnership(event);

        List<Guest> guests = guestRepository.findByEventEventId(eventId);
        for (Guest guest : guests) {
            String invitationToken = jwtUtil.generateGuestInvitationToken(guest.getGuestId());
            String invitationLink = publicBaseUrl + "/invitation.html?token=" + invitationToken;

            String body = "Hello " + guest.getGuestName() + ",\n\n"
                    + "You are invited to " + guest.getEvent().getEventName() + ".\n\n"
                    + "Date: " + guest.getEvent().getEventDate() + "\n"
                    + "Venue: " + guest.getEvent().getVenue() + "\n\n"
                    + "Open your invitation page to view the event details and choose your RSVP status.\n\n"
                    + "Invitation link: " + invitationLink + "\n\n"
                    + "Available responses: ACCEPTED, DECLINED, PENDING\n\n"
                    + "Thank you.";

            emailService.sendEmail(
                    guest.getEmail(),
                    "Invitation: " + guest.getEvent().getEventName(),
                    body
            );
        }
    }

    public void deleteGuest(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
        validateOrganizerOwnership(guest.getEvent());
        guestRepository.deleteById(id);
    }

    public Guest respondToInvitation(String token) {
        return respondToInvitation(token, "Accepted");
    }

    public GuestInvitationDetailsResponse getInvitationDetails(String token) {
        try {
            if (!jwtUtil.validateGuestResponseToken(token)) {
                throw new AccessDeniedException("Invalid invitation token");
            }
        } catch (JwtException ex) {
            throw new AccessDeniedException("Invalid invitation token");
        }

        Long guestId = jwtUtil.extractGuestId(token);
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
        Event event = guest.getEvent();

        return new GuestInvitationDetailsResponse(
                guest.getGuestId(),
                guest.getGuestName(),
                guest.getEmail(),
                normalizeStoredStatus(guest.getRsvpStatus()),
                event.getEventId(),
                event.getEventName(),
                event.getEventDate(),
                event.getVenue(),
                List.of("ACCEPTED", "DECLINED", "PENDING")
        );
    }

    public Guest respondToInvitation(String token, String status) {
        try {
            if (!jwtUtil.validateGuestResponseToken(token)) {
                throw new AccessDeniedException("Invalid invitation token");
            }
        } catch (JwtException ex) {
            throw new AccessDeniedException("Invalid invitation token");
        }

        Long guestId = jwtUtil.extractGuestId(token);
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
        guest.setRsvpStatus(normalizeIncomingStatus(status));
        return guestRepository.save(guest);
    }

    public void sendFeedbackEmails(Long eventId) {
        Event event = loadEvent(eventId);
        validateOrganizerOwnership(event);

        List<Guest> guests = guestRepository.findByEventEventId(eventId);
        for (Guest guest : guests) {
            if (!"Accepted".equalsIgnoreCase(normalizeStoredStatus(guest.getRsvpStatus()))) {
                continue;
            }

            String token = jwtUtil.generateFeedbackToken(guest.getGuestId(), eventId);
            String link = publicBaseUrl + "/feedback.html?token=" + token;

            String subject = "We value your feedback!";
            String body = "Thank you for attending our event.\n\n"
                    + "Event: " + event.getEventName() + "\n"
                    + "Date: " + event.getEventDate() + "\n"
                    + "Venue: " + event.getVenue() + "\n\n"
                    + "Please share your feedback by clicking the link below:\n"
                    + link;

            emailService.sendEmail(guest.getEmail(), subject, body);
        }
    }

    public FeedbackLinkDetailsResponse getFeedbackFormDetails(String token) {
        try {
            if (!jwtUtil.validateFeedbackToken(token)) {
                throw new AccessDeniedException("Invalid feedback token");
            }
        } catch (JwtException ex) {
            throw new AccessDeniedException("Invalid feedback token");
        }

        Long guestId = jwtUtil.extractGuestId(token);
        Long eventId = jwtUtil.extractEventId(token);
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
        Event event = loadEvent(eventId);
        validateFeedbackGuestAndEvent(guest, event);

        return new FeedbackLinkDetailsResponse(
                guest.getGuestId(),
                guest.getGuestName(),
                guest.getEmail(),
                event.getEventId(),
                event.getEventName(),
                event.getEventDate(),
                event.getVenue()
        );
    }

    private Event loadEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    public Guest getGuestForFeedbackToken(String token) {
        try {
            if (!jwtUtil.validateFeedbackToken(token)) {
                throw new AccessDeniedException("Invalid feedback token");
            }
        } catch (JwtException ex) {
            throw new AccessDeniedException("Invalid feedback token");
        }

        Long guestId = jwtUtil.extractGuestId(token);
        Long eventId = jwtUtil.extractEventId(token);
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
        Event event = loadEvent(eventId);
        validateFeedbackGuestAndEvent(guest, event);
        return guest;
    }

    public Event getEventForFeedbackToken(String token) {
        Event event = loadEvent(jwtUtil.extractEventId(token));
        validateFeedbackGuestAndEvent(getGuestForFeedbackToken(token), event);
        return event;
    }

    private void validateOrganizerOwnership(Event event) {
        String email = securityUtil.getCurrentUserEmail();

        if (event == null || event.getOrganizer() == null || !email.equals(event.getOrganizer().getEmail())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private void validateFeedbackGuestAndEvent(Guest guest, Event event) {
        if (guest.getEvent() == null || !event.getEventId().equals(guest.getEvent().getEventId())) {
            throw new AccessDeniedException("Guest is not linked to this event");
        }
        if (!"Accepted".equalsIgnoreCase(normalizeStoredStatus(guest.getRsvpStatus()))) {
            throw new AccessDeniedException("Feedback is only available for accepted guests");
        }
    }

    private String normalizeIncomingStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new BadRequestException("Status is required");
        }

        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ACCEPTED" -> "Accepted";
            case "DECLINED" -> "Declined";
            case "PENDING" -> "Pending";
            default -> throw new BadRequestException("Status must be ACCEPTED, DECLINED, or PENDING");
        };
    }

    private String normalizeStoredStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Pending";
        }
        return normalizeIncomingStatus(status);
    }
}
