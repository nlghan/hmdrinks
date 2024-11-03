package com.hmdrinks.Controller;

import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Shipment;
import com.hmdrinks.Request.*;
import com.hmdrinks.Service.ShipmentService;
import com.hmdrinks.Service.UserService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/shipment")
@RequiredArgsConstructor
public class ShipmentController {
    @Autowired
    private ShipmentService shipmentService;
    @Autowired
    private SupportFunction supportFunction;

    @PostMapping("/allocate")
    public ResponseEntity<?> shipmentAllocation(@RequestBody CRUDShipmentReq req) {
        return shipmentService.shipmentAllocation(req);
    }

    @PostMapping("/activate/shipping")
    public ResponseEntity<?> activeShip(@RequestBody AllocationShipmentReq req) {
        return shipmentService.activateShipment(req.getShipmentId(),req.getUserId());
    }

    @PostMapping("/activate/success")
    public ResponseEntity<?> activeSuccess(@RequestBody AllocationShipmentReq req) {
        return shipmentService.successShipment(req.getShipmentId(),req.getUserId());
    }

    @GetMapping("/shipper/listShippment")
    public ResponseEntity<?> getListShipmentStatusByShipper(@RequestParam(name = "page") String page,
                                                            @RequestParam(name = "limit") String limit,
                                                            @RequestParam(name = "userId") Integer userId,
                                                            @RequestParam(name = "status")Status_Shipment statusShipment)
    {
        return  shipmentService.getListShipmentStatusByShipper(page,limit,userId,statusShipment);
    }


    @GetMapping("/view/list-All")
    public ResponseEntity<?> getListShipment(@RequestParam(name = "page") String page,
                                             @RequestParam(name = "limit") String limit
                                                           )
    {
        return  shipmentService.getListAllShipment(page,limit);
    }

    @GetMapping("/view/listByStatus")
    public ResponseEntity<?> getListShipmentByStatus(@RequestParam(name = "page") String page,
                                                     @RequestParam(name = "limit") String limit,
                                                     @RequestParam(name = "status")Status_Shipment statusShipment
    )
    {
        return  shipmentService.getListAllShipmentByStatus(page,limit,statusShipment);
    }
}