package com.hmdrinks.Request;

import com.hmdrinks.Enum.Status_Shipment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminActivateShipmentReq {
    private int shipmentId;
    Status_Shipment status;
}
