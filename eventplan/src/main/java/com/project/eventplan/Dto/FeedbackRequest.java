package com.project.eventplan.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        int rating,
        @NotBlank(message = "Comments are required")
        String comments,
        @NotNull(message = "Guest ID is required")
        Long guestId,
        @NotNull(message = "Event ID is required")
        Long eventId
) {
}
