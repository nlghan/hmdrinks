package com.hmdrinks.Response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class OrderCancelPaymentRefund {
    CreateOrdersResponse order;
    CRUDPaymentResponse payment;
}
