package com.smartfin.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol; // e.g., "AAPL", "BTC"

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // "STOCK", "CRYPTO", "REAL_ESTATE"

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "avg_cost")
    private BigDecimal avgCost; // Average buy price

    @Column(name = "current_price")
    private BigDecimal currentPrice; // Live market price

    private LocalDateTime lastUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
