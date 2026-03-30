package com.project.eventplan.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    private int rating;

    private String comments;
    private LocalDateTime submittedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "guest_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "event"})
    private Guest guest;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "organizer", "eventTeamMembers", "eventVendors"})
    private Event event;

    @JsonProperty("guestName")
    public String getGuestName() {
        return guest != null ? guest.getGuestName() : null;
    }

    @JsonProperty("eventName")
    public String getEventName() {
        return event != null ? event.getEventName() : null;
    }
}
