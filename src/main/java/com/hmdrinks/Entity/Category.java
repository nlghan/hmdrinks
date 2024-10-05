package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.TypeLogin;
import jakarta.persistence.*;
import lombok.*;

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

    @OneToMany(mappedBy = "category")
    private List<Product> products;

}