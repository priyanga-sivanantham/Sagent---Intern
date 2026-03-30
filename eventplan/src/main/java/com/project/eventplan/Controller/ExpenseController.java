package com.project.eventplan.Controller;

import com.project.eventplan.Dto.ExpenseRequest;
import com.project.eventplan.Entity.Budget;
import com.project.eventplan.Entity.Expense;
import com.project.eventplan.Service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public Expense createExpense(@Valid @RequestBody ExpenseRequest request) {
        return expenseService.createExpense(toExpense(request));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/{id}")
    public Expense getExpense(@PathVariable Long id) {
        return expenseService.getExpenseById(id);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PutMapping("/{id}")
    public Expense updateExpense(@PathVariable Long id,
                                 @Valid @RequestBody ExpenseRequest request) {
        return expenseService.updateExpense(id, toExpense(request));
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
    }

    private Expense toExpense(ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setExpenseName(request.expenseName());
        expense.setAmount(request.amount());
        expense.setDescription(request.description());

        Budget budget = new Budget();
        budget.setBudgetId(request.budgetId());
        expense.setBudget(budget);
        return expense;
    }
}
