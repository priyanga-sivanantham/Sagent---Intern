package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Budget;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.BudgetRepository;
import com.project.eventplan.Repository.EventRepository;
import com.project.eventplan.Repository.ExpenseRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final EventRepository eventRepository;
    private final ExpenseRepository expenseRepository;
    private final SecurityUtil securityUtil;

    public BudgetService(BudgetRepository budgetRepository,
                         EventRepository eventRepository,
                         ExpenseRepository expenseRepository,
                         SecurityUtil securityUtil) {
        this.budgetRepository = budgetRepository;
        this.eventRepository = eventRepository;
        this.expenseRepository = expenseRepository;
        this.securityUtil = securityUtil;
    }

    public Budget createBudget(Budget budget) {
        Event event = loadEvent(budget.getEvent().getEventId());
        validateOrganizerOwnership(event);
        budget.setEvent(event);
        return recalculateAndSave(budget);
    }

    public List<Budget> getAllBudgets() {
        String email = securityUtil.getCurrentUserEmail();
        return budgetRepository.findAll().stream()
                .filter(budget -> budget.getEvent() != null
                        && budget.getEvent().getOrganizer() != null
                        && email.equals(budget.getEvent().getOrganizer().getEmail()))
                .map(this::syncRemainingBudget)
                .toList();
    }

    public Budget getBudgetById(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        validateOrganizerOwnership(budget.getEvent());
        return syncRemainingBudget(budget);
    }

    public Budget updateBudget(Long id, Budget budget) {
        Budget existing = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        validateOrganizerOwnership(existing.getEvent());

        Event event = loadEvent(budget.getEvent().getEventId());
        validateOrganizerOwnership(event);

        existing.setTotalBudget(budget.getTotalBudget());
        existing.setEvent(event);
        return recalculateAndSave(existing);
    }

    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        validateOrganizerOwnership(budget.getEvent());
        budgetRepository.deleteById(id);
    }

    private Event loadEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private void validateOrganizerOwnership(Event event) {
        String email = securityUtil.getCurrentUserEmail();

        if (event == null || event.getOrganizer() == null || !email.equals(event.getOrganizer().getEmail())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private Budget syncRemainingBudget(Budget budget) {
        double spentAmount = budget.getBudgetId() == null ? 0 : expenseRepository.sumAmountByBudgetId(budget.getBudgetId());
        double remainingBudget = budget.getTotalBudget() - spentAmount;

        if (Double.compare(budget.getRemainingBudget(), remainingBudget) != 0) {
            budget.setRemainingBudget(remainingBudget);
            return budgetRepository.save(budget);
        }

        budget.setRemainingBudget(remainingBudget);
        return budget;
    }

    private Budget recalculateAndSave(Budget budget) {
        budget.setRemainingBudget(budget.getTotalBudget());
        Budget savedBudget = budgetRepository.save(budget);
        return syncRemainingBudget(savedBudget);
    }
}
