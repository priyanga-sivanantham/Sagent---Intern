package com.project.eventplan.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    private String taskName;

    private String description;

    private String status; // NOT_STARTED, ONGOING, COMPLETED

    // Task belongs to an event
    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "organizer", "eventTeamMembers", "eventVendors"})
    private Event event;

    // Assigned team member (User with role TEAM_MEMBER)
    @ManyToOne
    @JoinColumn(name = "assigned_team_member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User assignedTeamMember;

    // Assigned vendor (User with role VENDOR)
    @ManyToOne
    @JoinColumn(name = "assigned_vendor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User assignedVendor;
}
