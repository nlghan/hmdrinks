package com.hmdrinks.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CRUDShipmentReq {
    private Integer shipmentId;
    private LocalDateTime dateShip;
    private LocalDateTime dateDeliver;
    private int userId;
}
