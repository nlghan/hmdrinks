package com.hmdrinks.Response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ListAllOrderCancelAndPaymentRefund {
    private int total;
    List<OrderCancelPaymentRefund> list;
}
