package com.hmdrinks.Response;

import com.hmdrinks.Enum.Status_Order;
import com.hmdrinks.Enum.Status_Shipment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CRUDShipmentResponse {
    private Integer shipmentId;
    private LocalDateTime dateCreated;
    private LocalDateTime dateDeleted;
    private LocalDateTime dateDeliver;
    private LocalDateTime dateShipped;
    private Boolean isDeleted;
    private Status_Shipment status;
    private Integer paymentId;
    private Integer shipperId;
    private String customerName;
    private String address;
    private String phoneNumber;
    private String email;
}
