package com.project.eventplan.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guestId;

    private String guestName;

    private String email;

    private String phone;

    // Invited / Accepted / Declined / Attended
    private String rsvpStatus = "Pending";

    // Guest belongs to an Event
    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "organizer", "eventTeamMembers", "eventVendors"})
    private Event event;

    @JsonProperty("eventId")
    public Long getEventId() {
        return event != null ? event.getEventId() : null;
    }

    @JsonProperty("eventName")
    public String getEventName() {
        return event != null ? event.getEventName() : null;
    }
}
