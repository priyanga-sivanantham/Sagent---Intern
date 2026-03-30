package com.project.eventplan.Dto;

import jakarta.validation.constraints.NotBlank;

public record GuestResponseRequest(
        @NotBlank(message = "Token is required")
        String token,
        @NotBlank(message = "Status is required")
        String status
) {
}
