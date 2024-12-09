package com.hmdrinks.Response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class fetchOrdersAwaitingPayment {
    private int total ;
    ListOrderWaiting listOrderWaiting;
    ListAllOrderConfirmAndNotPayment listAllOrderConfirmAndNotPayment ;
    ListAllOrderConfirmAndNotPayment listAllOrderConfirmAndPaymentPending ;
}
