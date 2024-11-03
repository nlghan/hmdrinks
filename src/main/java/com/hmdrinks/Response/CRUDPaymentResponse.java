package com.hmdrinks.Response;

import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CRUDPaymentResponse {
    private int paymentId;
    private double amount;
    private LocalDateTime dateCreated;
    private LocalDateTime dateDeleted;
    private  Boolean isDeleted;
    private Payment_Method paymentMethod;
    private Status_Payment statusPayment;
    private int orderId;
}
