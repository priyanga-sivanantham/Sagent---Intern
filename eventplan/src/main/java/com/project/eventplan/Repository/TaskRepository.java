package com.project.eventplan.Repository;

import com.project.eventplan.Entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByEvent_Organizer_Email(String email);

    List<Task> findByAssignedTeamMember_Email(String email);

    List<Task> findByAssignedVendor_Email(String email);
}