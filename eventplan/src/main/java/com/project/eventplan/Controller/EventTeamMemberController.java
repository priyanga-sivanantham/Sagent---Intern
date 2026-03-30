package com.project.eventplan.Controller;

import com.project.eventplan.Dto.EventTeamMemberRequest;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.EventTeamMember;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Service.EventTeamMemberService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eventteammembers")
public class EventTeamMemberController {

    private final EventTeamMemberService service;

    public EventTeamMemberController(EventTeamMemberService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public EventTeamMember addMember(@Valid @RequestBody EventTeamMemberRequest request) {
        return service.addMember(toAssignment(request));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping
    public List<EventTeamMember> getAllMembers() {
        return service.getAllMembers();
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/{id}")
    public EventTeamMember getMember(@PathVariable Long id) {
        return service.getMemberById(id);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{id}")
    public EventTeamMember updateMember(@PathVariable Long id,
                                        @Valid @RequestBody EventTeamMemberRequest request) {
        return service.updateMember(id, toAssignment(request));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Long id) {
        service.deleteMember(id);
    }

    private EventTeamMember toAssignment(EventTeamMemberRequest request) {
        EventTeamMember member = new EventTeamMember();
        Event event = new Event();
        event.setEventId(request.eventId());
        member.setEvent(event);

        User user = new User();
        user.setUserId(request.userId());
        member.setUser(user);
        member.setRoleInEvent(request.roleInEvent());
        return member;
    }
}
