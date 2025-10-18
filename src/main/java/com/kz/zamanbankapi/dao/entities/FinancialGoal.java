package com.kz.zamanbankapi.dao.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "financial_goals")
public class FinancialGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String goalName;

    @Column(nullable = false)
    private BigDecimal targetAmount;

    private BigDecimal currentAmount;

    private LocalDate targetDate;
}

