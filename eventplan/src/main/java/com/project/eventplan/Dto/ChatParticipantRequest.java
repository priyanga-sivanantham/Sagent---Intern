package com.project.eventplan.Dto;

import jakarta.validation.constraints.NotNull;

public record ChatParticipantRequest(
        @NotNull(message = "Chat ID is required")
        Long chatId,
        @NotNull(message = "User ID is required")
        Long userId
) {
}
