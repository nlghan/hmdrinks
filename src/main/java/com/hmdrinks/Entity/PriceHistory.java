package com.hmdrinks.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "price_history")
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historyId")
    private int historyId;

    @ManyToOne
    @JoinColumn(name = "varId", nullable = false)
    private ProductVariants productVariant;

    @Column(nullable = false)
    private double oldPrice;

    @Column(nullable = false)
    private double newPrice;

    @Column(nullable = false,columnDefinition = "DATETIME")
    private LocalDateTime dateChanged;

    @Column(name = "changeReason")
    private String changeReason;
}
