package com.project.eventplan.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BudgetRequest(
        @Min(value = 0, message = "Total budget must be non-negative")
        double totalBudget,
        @Min(value = 0, message = "Remaining budget must be non-negative")
        double remainingBudget,
        @NotNull(message = "Event ID is required")
        Long eventId
) {
}
