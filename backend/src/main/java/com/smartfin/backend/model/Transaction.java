package com.smartfin.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount; // Positive for Income, Negative for Expense

    @Column(nullable = false)
    private String type; // "INCOME", "EXPENSE", "TRANSFER"

    @Column(nullable = false)
    private String category; // e.g., "Food", "Rent", "Salary"

    private String description;

    @Column(nullable = false)
    private java.time.LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    // For transfers: distinct from/to logic could be handled here or by creating
    // two transactions.
    // Keeping it simple for now (single record per movement in an account).
}
