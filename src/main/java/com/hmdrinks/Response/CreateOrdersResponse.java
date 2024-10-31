package com.hmdrinks.Response;

import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Enum.Status_Order;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrdersResponse {
    private int orderId;
    private String address;
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
    private Integer voucherId;
}
