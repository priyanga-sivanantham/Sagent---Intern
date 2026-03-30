package com.project.eventplan.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record GuestRequest(
        @NotBlank(message = "Guest name is required")
        String guestName,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "Phone number is invalid")
        String phone,
        String rsvpStatus,
        @NotNull(message = "Event ID is required")
        Long eventId
) {
}
