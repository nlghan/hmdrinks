// Shipment.java
package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Status_Shipment;
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
@Table(name = "shipment")
public class Shippment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipmentId")
    private int shipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @Column(name = "date_shipped", columnDefinition = "DATETIME")
    private LocalDateTime dateShip;

    @Column(name = "date_delivered", columnDefinition = "DATETIME")
    private LocalDateTime dateDelivered;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status_Shipment status;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted", columnDefinition = "DATETIME")
    private LocalDateTime dateDeleted;

    @Column(name = "date_created", columnDefinition = "DATETIME")
    private LocalDateTime dateCreated;

    @Column(name = "date_canceled",columnDefinition = "DATETIME")
    private LocalDateTime dateCancel;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paymentId", nullable = false)
    private Payment payment;
}
