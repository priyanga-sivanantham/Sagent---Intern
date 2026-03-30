package com.project.eventplan.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExpenseRequest(
        @NotBlank(message = "Expense name is required")
        String expenseName,
        @Min(value = 0, message = "Amount must be non-negative")
        double amount,
        String description,
        @NotNull(message = "Budget ID is required")
        Long budgetId
) {
}
