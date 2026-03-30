package com.project.eventplan.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventTeamMemberRequest(
        @NotNull(message = "Event ID is required")
        Long eventId,
        @NotNull(message = "User ID is required")
        Long userId,
        @NotBlank(message = "Role in event is required")
        String roleInEvent
) {
}
