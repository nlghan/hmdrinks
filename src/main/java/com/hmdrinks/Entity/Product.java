package com.hmdrinks.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proId")
    private int proId;

    @Column(name = "proName", nullable = false)
    private String proName;

    @Column(name = "proImg")
    private String proImg;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted")
    private Date dateDeleted;

    @Column(name = "date_updated")
    private LocalDate dateUpdated;

    @Column(name = "date_created")
    private LocalDate dateCreated;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)  // mappedBy để ánh xạ với thuộc tính user bên UserInfo
    private ProductImage productImage;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    // Sửa mối quan hệ OneToMany để ánh xạ đúng với ProductVariants
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariants> productVariants; // Sử dụng List để ánh xạ với các bản ghi của ProductVariants
}
