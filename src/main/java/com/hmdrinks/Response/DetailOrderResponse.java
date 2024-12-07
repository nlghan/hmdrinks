package com.hmdrinks.Response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DetailOrderResponse {
    CreateOrdersResponse order;
    CRUDPaymentResponse payment;
    CRUDShipmentResponse shipment;
    List<CRUDCartItemResponse> listItem;
}
