package com.project.eventplan.Repository;

import com.project.eventplan.Entity.EventVendor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventVendorRepository extends JpaRepository<EventVendor, Long> {
    boolean existsByEvent_EventIdAndUser_Email(Long eventId, String email);
}