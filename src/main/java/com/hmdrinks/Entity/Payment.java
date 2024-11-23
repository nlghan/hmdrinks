package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Order;
import com.hmdrinks.Enum.Status_Payment;
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
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentId")
    private int paymentId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "orderIdPayment")
    private String orderIdPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status_Payment status;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentMethod", nullable = false)
    private Payment_Method paymentMethod;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "is_refunded")
    private Boolean isRefund;

    @Column(name = "date_deleted")
    private LocalDateTime dateDeleted; // Changed to LocalDateTime

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private Orders order;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
    private Shippment shipment;
}
