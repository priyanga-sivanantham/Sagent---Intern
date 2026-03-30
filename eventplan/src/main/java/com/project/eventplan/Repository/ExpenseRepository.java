package com.project.eventplan.Repository;

import com.project.eventplan.Entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByBudget_BudgetId(Long budgetId);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.budget.budgetId = :budgetId")
    double sumAmountByBudgetId(Long budgetId);
}
