package com.hmdrinks.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_image")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proImgId")
    private int proId;

    @Column(name = "list_pro_img", columnDefinition = "TEXT")
    private String proImg;


    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted")
    private Date dateDeleted;

    @Column(name = "date_updated")
    private LocalDate dateUpdated;

    @Column(name = "date_created")
    private LocalDate dateCreated;

    @OneToOne
    @JoinColumn(name = "proId", nullable = false)  // userId trong bảng token là khóa ngoại
    private Product product; // Sử dụng List để ánh xạ với các bản ghi của ProductVariants
}
