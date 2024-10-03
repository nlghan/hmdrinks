package com.hmdrinks.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment primary key for CartItem
    @Column(name = "cartItemId")
    private int cartItemId;

    @ManyToOne
    @JoinColumn(name = "cartId", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "varId", nullable = false)  // Link to ProductVariants via varId
    private ProductVariants productVariant;

    @Column(name = "totalPrice")
    private double totalPrice;

    @Column(name = "quantity")
    private int quantity;
}
