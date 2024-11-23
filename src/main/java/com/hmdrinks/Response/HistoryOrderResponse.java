package com.hmdrinks.Response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class HistoryOrderResponse {
    CreateOrdersResponse order;
    CRUDShipmentResponse shipment;
}
