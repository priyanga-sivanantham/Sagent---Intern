package com.project.eventplan.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_teammembers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventTeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many team members belong to one event
    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "organizer", "eventTeamMembers", "eventVendors"})
    private Event event;

    // Many team members refer to users
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    // Role of the user inside that event
    private String roleInEvent;
}
