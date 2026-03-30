package com.project.eventplan.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseId;

    private String expenseName;

    private double amount;

    private String description;

    @ManyToOne
    @JoinColumn(name = "budget_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "event"})
    private Budget budget;
}
