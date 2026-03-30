package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Budget;
import com.project.eventplan.Entity.Expense;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.BudgetRepository;
import com.project.eventplan.Repository.ExpenseRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final SecurityUtil securityUtil;

    public ExpenseService(ExpenseRepository expenseRepository,
                          BudgetRepository budgetRepository,
                          SecurityUtil securityUtil) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.securityUtil = securityUtil;
    }

    public Expense createExpense(Expense expense) {
        Budget budget = loadBudget(expense.getBudget().getBudgetId());
        validateBudgetOwnership(budget);
        expense.setBudget(budget);
        Expense savedExpense = expenseRepository.save(expense);
        refreshBudgetTotals(budget.getBudgetId());
        return savedExpense;
    }

    public List<Expense> getAllExpenses() {
        String email = securityUtil.getCurrentUserEmail();
        return expenseRepository.findAll().stream()
                .filter(expense -> expense.getBudget() != null
                        && expense.getBudget().getEvent() != null
                        && expense.getBudget().getEvent().getOrganizer() != null
                        && email.equals(expense.getBudget().getEvent().getOrganizer().getEmail()))
                .collect(Collectors.toList());
    }

    public Expense getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        validateBudgetOwnership(expense.getBudget());
        return expense;
    }

    public Expense updateExpense(Long id, Expense expense) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        Budget previousBudget = existing.getBudget();
        validateBudgetOwnership(previousBudget);

        Budget budget = loadBudget(expense.getBudget().getBudgetId());
        validateBudgetOwnership(budget);

        existing.setExpenseName(expense.getExpenseName());
        existing.setAmount(expense.getAmount());
        existing.setDescription(expense.getDescription());
        existing.setBudget(budget);
        Expense savedExpense = expenseRepository.save(existing);

        if (previousBudget != null && previousBudget.getBudgetId() != null) {
            refreshBudgetTotals(previousBudget.getBudgetId());
        }
        refreshBudgetTotals(budget.getBudgetId());

        return savedExpense;
    }

    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        validateBudgetOwnership(expense.getBudget());
        Long budgetId = expense.getBudget().getBudgetId();
        expenseRepository.deleteById(id);
        refreshBudgetTotals(budgetId);
    }

    private Budget loadBudget(Long budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    }

    private void validateBudgetOwnership(Budget budget) {
        String email = securityUtil.getCurrentUserEmail();

        if (budget == null || budget.getEvent() == null || budget.getEvent().getOrganizer() == null
                || !email.equals(budget.getEvent().getOrganizer().getEmail())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private void refreshBudgetTotals(Long budgetId) {
        Budget budget = loadBudget(budgetId);
        double spentAmount = expenseRepository.sumAmountByBudgetId(budgetId);
        budget.setRemainingBudget(budget.getTotalBudget() - spentAmount);
        budgetRepository.save(budget);
    }
}
