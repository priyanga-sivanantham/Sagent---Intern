package com.project.eventplan.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageRequest(
        @NotBlank(message = "Message content is required")
        String content,
        @NotNull(message = "Chat ID is required")
        Long chatId
) {
}
