package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Size;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_variants")
public class ProductVariants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "varId")
    private int varId;

    @ManyToOne
    @JoinColumn(name = "proId", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;  // Sử dụng Enum cho kích thước

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int stock;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted",columnDefinition = "DATETIME")
    private LocalDateTime dateDeleted;

    @Column(name = "date_updated",columnDefinition = "DATETIME")
    private LocalDateTime dateUpdated;

    @Column(name = "date_created",columnDefinition = "DATETIME")
    private LocalDateTime dateCreated;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceHistory> priceHistories;
}
