package com.hmdrinks.Response;

import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Enum.Status_Payment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentResponse {
    private int paymentId;
    private double amount;
    private LocalDateTime dateCreated;
    private LocalDateTime dateDeleted;
    private  Boolean isDeleted;
    private Payment_Method paymentMethod;
    private Status_Payment statusPayment;
    private int orderId;
    private String linkPayment;
}
