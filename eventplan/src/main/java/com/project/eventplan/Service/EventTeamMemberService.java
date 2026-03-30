package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.EventTeamMember;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.EventRepository;
import com.project.eventplan.Repository.EventTeamMemberRepository;
import com.project.eventplan.Repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventTeamMemberService {

    private final EventTeamMemberRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public EventTeamMemberService(EventTeamMemberRepository repository,
                                  EventRepository eventRepository,
                                  UserRepository userRepository,
                                  SecurityUtil securityUtil) {
        this.repository = repository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    public EventTeamMember addMember(EventTeamMember member) {
        Event event = loadEvent(member.getEvent().getEventId());
        validateOrganizerOwnership(event);
        User user = loadUser(member.getUser().getUserId());

        member.setEvent(event);
        member.setUser(user);
        return repository.save(member);
    }

    public List<EventTeamMember> getAllMembers() {
        String email = securityUtil.getCurrentUserEmail();
        return repository.findAll().stream()
                .filter(member -> member.getEvent() != null
                        && member.getEvent().getOrganizer() != null
                        && email.equals(member.getEvent().getOrganizer().getEmail()))
                .collect(Collectors.toList());
    }

    public EventTeamMember getMemberById(Long id) {
        EventTeamMember member = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team member assignment not found"));
        validateOrganizerOwnership(member.getEvent());
        return member;
    }

    public EventTeamMember updateMember(Long id, EventTeamMember member) {
        EventTeamMember existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team member assignment not found"));
        validateOrganizerOwnership(existing.getEvent());

        Event event = loadEvent(member.getEvent().getEventId());
        validateOrganizerOwnership(event);
        User user = loadUser(member.getUser().getUserId());

        existing.setEvent(event);
        existing.setUser(user);
        existing.setRoleInEvent(member.getRoleInEvent());
        return repository.save(existing);
    }

    public void deleteMember(Long id) {
        EventTeamMember member = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team member assignment not found"));
        validateOrganizerOwnership(member.getEvent());
        repository.deleteById(id);
    }

    private Event loadEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateOrganizerOwnership(Event event) {
        String email = securityUtil.getCurrentUserEmail();

        if (event == null || event.getOrganizer() == null || !email.equals(event.getOrganizer().getEmail())) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
