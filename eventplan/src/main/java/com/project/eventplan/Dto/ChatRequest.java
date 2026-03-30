package com.project.eventplan.Dto;

import com.project.eventplan.Entity.Enums.ChatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(
        @NotBlank(message = "Chat name is required")
        String chatName,
        @NotNull(message = "Chat type is required")
        ChatType chatType,
        Long eventId
) {
}
