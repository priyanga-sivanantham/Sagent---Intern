package com.project.eventplan.Dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EventRequest(
        @NotBlank(message = "Event name is required")
        String eventName,
        @NotNull(message = "Event date is required")
        @FutureOrPresent(message = "Event date must be today or later")
        LocalDate eventDate,
        @NotBlank(message = "Venue is required")
        String venue
) {
}
