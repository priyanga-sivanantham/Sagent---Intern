package com.project.eventplan.Repository;

import com.project.eventplan.Entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    List<Guest> findByEvent_Organizer_Email(String email);

    List<Guest> findByEventEventId(Long eventId);

    List<Guest> findByEventEventIdAndEvent_Organizer_Email(Long eventId, String email);

}
