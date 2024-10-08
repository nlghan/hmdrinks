package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Status_Cart;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartId")
    private int cartId;

    @Column(name = "totalPrice")
    private double totalPrice;

    @Column(name = "totalProduct")
    private int totalProduct;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status_Cart status;

    @OneToOne(mappedBy = "cart", cascade = CascadeType.ALL)  // mappedBy để ánh xạ với thuộc tính user bên UserInfo
    private OrderItem orderItem;

    @OneToMany(mappedBy = "cart")
    private List<CartItem> cartItems;
}
