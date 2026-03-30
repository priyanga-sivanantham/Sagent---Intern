package com.project.eventplan.Repository;

import com.project.eventplan.Entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByEventEventId(Long eventId);
    boolean existsByEventEventIdAndGuestGuestId(Long eventId, Long guestId);

}
