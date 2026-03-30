package com.project.eventplan.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicFeedbackRequest(
        @NotBlank(message = "Token is required")
        String token,
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        int rating,
        @NotBlank(message = "Comments are required")
        @Size(max = 1000, message = "Comments must be at most 1000 characters")
        String comments
) {
}
