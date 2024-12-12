package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.TypeLogin;
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
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cateId")
    private int cateId;

    @Column(name = "cateName", nullable = false)
    private String cateName;

    @Column(name = "cateImg")
    private String cateImg;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted",columnDefinition = "DATETIME")
    private LocalDateTime dateDeleted;

    @Column(name = "date_updated",columnDefinition = "DATETIME")
    private LocalDateTime dateUpdated;

    @Column(name = "date_created",columnDefinition = "DATETIME")
    private LocalDateTime dateCreated;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "category")
    private List<Product> products;

}
