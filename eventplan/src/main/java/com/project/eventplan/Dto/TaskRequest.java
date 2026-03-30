package com.project.eventplan.Dto;

import jakarta.validation.constraints.NotBlank;

public record TaskRequest(
        @NotBlank(message = "Task name is required")
        String taskName,
        String description
) {
}
