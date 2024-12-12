package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Size;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartItemId", nullable = false)
    private int cartItemId; 

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "proId", referencedColumnName = "proId"),
            @JoinColumn(name = "size", referencedColumnName = "size")
    })
    private ProductVariants productVariants;

    @ManyToOne
    @JoinColumn(name = "cartId", nullable = false)
    private Cart cart;

    @Column(name = "totalPrice")
    private double totalPrice;

    @Column(name = "note")
    private String note;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted",columnDefinition = "DATETIME")
    private LocalDateTime dateDeleted;

}

