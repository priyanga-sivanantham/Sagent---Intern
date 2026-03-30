package com.project.eventplan.Dto;

import com.project.eventplan.Entity.Enums.Role;

import java.time.Instant;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        Instant expiresAt,
        UserSummary user
) {
    public record UserSummary(
            Long userId,
            String name,
            String email,
            String phone,
            Role role
    ) {
    }
}
