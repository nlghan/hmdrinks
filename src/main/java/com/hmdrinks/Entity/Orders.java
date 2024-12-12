package com.hmdrinks.Entity;

import com.hmdrinks.Enum.CancelReason;
import com.hmdrinks.Enum.Status_Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderId")
    private int orderId;

    @Column(name = "orderDate", nullable = false,columnDefinition = "DATETIME")
    private LocalDateTime orderDate;

    @Column(name = "deliveryDate",columnDefinition = "DATETIME")
    private LocalDateTime deliveryDate;

    @Lob
    @Column(name = "note",columnDefinition = "TEXT")
    private String note;

    @Column(name = "phoneNumber", nullable = false)
    private String phoneNumber;

    @Column(name = "discountPrice", nullable = false)
    private double discountPrice;

    @Column(name = "deliveryFee")
    private double deliveryFee;

    @Column(name = "totalPrice", nullable = false)
    private double totalPrice;

    @Column(name = "address", nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status_Order status;

    @Enumerated(EnumType.STRING)
    @Column(name = "CancelReason")
    private CancelReason cancelReason;

    @Column(name = "is_cancel_reason")
    private Boolean isCancelReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucherId")
    private Voucher voucher;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted",columnDefinition = "DATETIME")
    private LocalDateTime dateDeleted;

    @Column(name = "date_created",columnDefinition = "DATETIME")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated",columnDefinition = "DATETIME")
    private LocalDateTime dateUpdated;

    @Column(name = "date_canceled",columnDefinition = "DATETIME")
    private LocalDateTime dateCanceled;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrderItem orderItem;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;
}
