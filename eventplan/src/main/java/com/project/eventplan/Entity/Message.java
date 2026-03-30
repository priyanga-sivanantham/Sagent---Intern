package com.project.eventplan.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    private String content;

    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User sender;

    @JsonProperty("senderName")
    public String getSenderName() {
        if (sender == null) {
            return null;
        }
        if (sender.getName() != null && !sender.getName().isBlank()) {
            return sender.getName();
        }
        return sender.getEmail();
    }
}
