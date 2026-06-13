package com.dragos.lifebalance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "savings_transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_savings_income",
                        columnNames = {"income_id"}
                )
        }
)
@Getter
@Setter
public class SavingsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "income_id", nullable = false)
    private Income income;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "date_saved", nullable = false)
    private LocalDate dateSaved;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}