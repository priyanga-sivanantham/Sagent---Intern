package com.project.eventplan.Repository;

import com.project.eventplan.Entity.EventTeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventTeamMemberRepository extends JpaRepository<EventTeamMember, Long> {
    boolean existsByEvent_EventIdAndUser_Email(Long eventId, String email);
}