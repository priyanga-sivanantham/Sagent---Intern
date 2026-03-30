package com.project.eventplan.Controller;

import com.project.eventplan.Dto.GuestRequest;
import com.project.eventplan.Dto.GuestInvitationDetailsResponse;
import com.project.eventplan.Dto.GuestResponseRequest;
import com.project.eventplan.Dto.PageResponse;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.Guest;
import com.project.eventplan.Service.GuestService;
import com.project.eventplan.Util.PaginationUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public Guest createGuest(@Valid @RequestBody GuestRequest request) {
        return guestService.createGuest(toGuest(request));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping
    public PageResponse<Guest> getAllGuests(@RequestParam(required = false) Long eventId,
                                            @RequestParam(required = false) String query,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size) {
        List<Guest> guests = guestService.getAllGuests(eventId);
        if (query != null && !query.isBlank()) {
            String normalized = query.toLowerCase(Locale.ROOT);
            guests = guests.stream()
                    .filter(guest -> contains(guest.getGuestName(), normalized)
                            || contains(guest.getEmail(), normalized)
                            || contains(guest.getPhone(), normalized)
                            || contains(guest.getRsvpStatus(), normalized)
                            || contains(guest.getEventName(), normalized))
                    .toList();
        }
        return PaginationUtils.paginate(guests, page, size);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/{id}")
    public Guest getGuest(@PathVariable Long id) {
        return guestService.getGuestById(id);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/event/{eventId}")
    public List<Guest> getGuestsByEvent(@PathVariable Long eventId) {
        return guestService.getGuestsByEvent(eventId);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{id}")
    public Guest updateGuest(@PathVariable Long id,
                             @Valid @RequestBody GuestRequest request) {
        return guestService.updateGuest(id, toGuest(request));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public void deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/send-invitations/{eventId}")
    public String sendInvitations(@PathVariable Long eventId) {
        guestService.sendInvitationsForEvent(eventId);
        return "Invitations sent successfully!";
    }

    @GetMapping("/invitation")
    public GuestInvitationDetailsResponse getInvitationDetails(@RequestParam String token) {
        return guestService.getInvitationDetails(token);
    }

    @GetMapping("/respond")
    public String respondToInvitation(@RequestParam String token,
                                      @RequestParam(required = false) String status) {
        guestService.respondToInvitation(token, status == null ? "ACCEPTED" : status);
        return "Your response has been recorded. Thank you!";
    }

    @PostMapping("/respond")
    public Guest respondToInvitation(@Valid @RequestBody GuestResponseRequest request) {
        return guestService.respondToInvitation(request.token(), request.status());
    }

    @GetMapping("/feedback")
    public com.project.eventplan.Dto.FeedbackLinkDetailsResponse getFeedbackFormDetails(@RequestParam String token) {
        return guestService.getFeedbackFormDetails(token);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/{eventId}/send-feedback-mail")
    public String sendFeedbackMail(@PathVariable Long eventId) {
        guestService.sendFeedbackEmails(eventId);
        return "Feedback emails sent successfully!";
    }

    private Guest toGuest(GuestRequest request) {
        Guest guest = new Guest();
        guest.setGuestName(request.guestName());
        guest.setEmail(request.email());
        guest.setPhone(request.phone());
        guest.setRsvpStatus(request.rsvpStatus());

        Event event = new Event();
        event.setEventId(request.eventId());
        guest.setEvent(event);
        return guest;
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
