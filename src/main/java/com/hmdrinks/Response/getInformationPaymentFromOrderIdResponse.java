package com.hmdrinks.Response;

import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Order;
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
public class getInformationPaymentFromOrderIdResponse {
    private int orderId;
    private String address;
    private Double deliveryFee;
    private LocalDateTime dateCreated;
    private LocalDateTime dateDeleted;
    private LocalDateTime dateUpdated;
    private  LocalDateTime dateDelivered;
    private  Double discountPrice;
    private  Boolean isDeleted;
    private String note;
    private  LocalDateTime dateOders;
    private String phone;
    private Status_Order status;
    private  Double TotalPrice;
    private  int userId;
    private int voucherId;
    private CRUDPaymentResponse infoPaymentResponse;

}
