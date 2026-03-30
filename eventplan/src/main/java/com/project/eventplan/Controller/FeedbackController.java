package com.project.eventplan.Controller;

import com.project.eventplan.Dto.FeedbackRequest;
import com.project.eventplan.Dto.FeedbackLinkDetailsResponse;
import com.project.eventplan.Dto.PublicFeedbackRequest;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.Feedback;
import com.project.eventplan.Entity.Guest;
import com.project.eventplan.Service.GuestService;
import com.project.eventplan.Service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService service;
    private final GuestService guestService;

    public FeedbackController(FeedbackService service, GuestService guestService) {
        this.service = service;
        this.guestService = guestService;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public Feedback addFeedback(@Valid @RequestBody FeedbackRequest request) {
        return service.addFeedback(toFeedback(request));
    }

    @GetMapping("/form")
    public FeedbackLinkDetailsResponse getFeedbackForm(@RequestParam String token) {
        return guestService.getFeedbackFormDetails(token);
    }

    @PostMapping("/submit")
    public Feedback submitFeedback(@Valid @RequestBody PublicFeedbackRequest request) {
        Guest guest = guestService.getGuestForFeedbackToken(request.token());
        Event event = guestService.getEventForFeedbackToken(request.token());

        Feedback feedback = new Feedback();
        feedback.setRating(request.rating());
        feedback.setComments(request.comments());
        feedback.setGuest(guest);
        feedback.setEvent(event);
        return service.addFeedback(feedback);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping
    public List<Feedback> getAllFeedback() {
        return service.getAllFeedback();
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping("/{id}")
    public Feedback getFeedback(@PathVariable Long id) {
        return service.getFeedbackById(id);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping("/event/{eventId}")
    public List<Feedback> getFeedbackByEvent(@PathVariable Long eventId) {
        return service.getFeedbackByEvent(eventId);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public void deleteFeedback(@PathVariable Long id) {
        service.deleteFeedback(id);
    }

    private Feedback toFeedback(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setRating(request.rating());
        feedback.setComments(request.comments());

        Guest guest = new Guest();
        guest.setGuestId(request.guestId());
        feedback.setGuest(guest);

        Event event = new Event();
        event.setEventId(request.eventId());
        feedback.setEvent(event);
        return feedback;
    }
}
