package com.project.eventplan.Dto;

import com.project.eventplan.Entity.Enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "Phone number is invalid")
        String phone,
        @NotNull(message = "Role is required")
        Role role
) {
}
