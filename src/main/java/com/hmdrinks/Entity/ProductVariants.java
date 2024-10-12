package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Size;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    private Product product;  // Mối quan hệ ManyToOne đến bảng Product

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;  // Sử dụng Enum cho kích thước

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int stock;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted")
    private Date dateDeleted;

    @Column(name = "date_updated")
    private LocalDate dateUpdated;

    @Column(name = "date_created")
    private LocalDate dateCreated;

    @OneToMany(mappedBy = "productVariants", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;
}
