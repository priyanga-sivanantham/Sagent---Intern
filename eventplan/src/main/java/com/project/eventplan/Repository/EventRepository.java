package com.project.eventplan.Repository;

import com.project.eventplan.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer_Email(String email);

    List<Event> findDistinctByEventTeamMembers_User_Email(String email);

    List<Event> findDistinctByEventVendors_User_Email(String email);
}
