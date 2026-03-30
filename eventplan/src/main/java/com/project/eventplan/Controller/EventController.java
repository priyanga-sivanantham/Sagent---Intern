package com.project.eventplan.Controller;

import com.project.eventplan.Dto.EventRequest;
import com.project.eventplan.Dto.PageResponse;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Service.EventService;
import com.project.eventplan.Util.PaginationUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping
    public ResponseEntity<PageResponse<Event>> getEvents(@RequestParam(required = false) String query,
                                                         @RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer size) {
        List<Event> events = eventService.getEventsForCurrentUser();
        if (query != null && !query.isBlank()) {
            String normalized = query.toLowerCase(Locale.ROOT);
            events = events.stream()
                    .filter(event -> contains(event.getEventName(), normalized)
                            || contains(event.getVenue(), normalized))
                    .toList();
        }
        return ResponseEntity.ok(PaginationUtils.paginate(events, page, size));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.createEvent(toEvent(request)));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, toEvent(request)));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully");
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    private Event toEvent(EventRequest request) {
        Event event = new Event();
        event.setEventName(request.eventName());
        event.setEventDate(request.eventDate());
        event.setVenue(request.venue());
        return event;
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
