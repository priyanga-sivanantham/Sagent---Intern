package com.project.eventplan.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "organizer", "eventTeamMembers", "eventVendors"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    private String eventName;

    private LocalDate eventDate;

    private String venue;

    // Many events can be created by one user (organizer)
    @ManyToOne
    @JoinColumn(name = "organizer_id" , nullable = false)
    @JsonIgnore
    private User organizer;

    @OneToMany(mappedBy = "event")
    @JsonIgnoreProperties({"event"})
    private List<EventTeamMember> eventTeamMembers = new ArrayList<>();

    @OneToMany(mappedBy = "event")
    @JsonIgnoreProperties({"event"})
    private List<EventVendor> eventVendors = new ArrayList<>();


}
