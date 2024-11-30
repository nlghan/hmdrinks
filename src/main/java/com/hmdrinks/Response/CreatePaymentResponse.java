package com.hmdrinks.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Enum.Status_Payment;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentResponse {
    private int paymentId;
    private double amount;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreated;
    private LocalDateTime dateDeleted;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateRefunded;
    private  Boolean isDeleted;
    private Payment_Method paymentMethod;
    private Status_Payment statusPayment;
    private int orderId;
    private String linkPayment;
    private String note;
}
