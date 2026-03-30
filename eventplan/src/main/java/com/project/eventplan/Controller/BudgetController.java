package com.project.eventplan.Controller;

import com.project.eventplan.Dto.BudgetRequest;
import com.project.eventplan.Entity.Budget;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public Budget createBudget(@Valid @RequestBody BudgetRequest request) {
        return budgetService.createBudget(toBudget(request));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping
    public List<Budget> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/{id}")
    public Budget getBudget(@PathVariable Long id) {
        return budgetService.getBudgetById(id);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{id}")
    public Budget updateBudget(@PathVariable Long id,
                               @Valid @RequestBody BudgetRequest request) {
        return budgetService.updateBudget(id, toBudget(request));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public void deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
    }

    private Budget toBudget(BudgetRequest request) {
        Budget budget = new Budget();
        budget.setTotalBudget(request.totalBudget());
        budget.setRemainingBudget(request.remainingBudget());

        Event event = new Event();
        event.setEventId(request.eventId());
        budget.setEvent(event);
        return budget;
    }
}
