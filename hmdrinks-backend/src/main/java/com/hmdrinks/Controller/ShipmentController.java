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

    @PostMapping("/activate/receiving")
    public ResponseEntity<?> activeReceiving(@RequestBody AllocationShipmentReq req) {
        return shipmentService.ActivateReceiving(req.getShipmentId(),req.getUserId());
    }

    @PostMapping("/activate/success")
    public ResponseEntity<?> activeSuccess(@RequestBody AllocationShipmentReq req) {
        return shipmentService.successShipment(req.getShipmentId(),req.getUserId());
    }

    @PostMapping("/activate/cancel")
    public ResponseEntity<?> activeCancel(@RequestBody AllocationShipmentReq req) {
        return shipmentService.cancelShipment(req.getShipmentId());
    }

    @GetMapping("/shipper/listShippment")
    public ResponseEntity<?> getListShipmentStatusByShipper(@RequestParam(name = "page") String page,
                                                            @RequestParam(name = "limit") String limit,
                                                            @RequestParam(name = "userId") Integer userId,
                                                            @RequestParam(name = "status")Status_Shipment statusShipment)
    {
        return  shipmentService.getListShipmentStatusByShipper(page,limit,userId,statusShipment);
    }

    @GetMapping("/shipper/listShippments")
    public ResponseEntity<?> getListShipmentsByShipper(@RequestParam(name = "page") String page,
                                                            @RequestParam(name = "limit") String limit,
                                                            @RequestParam(name = "userId") Integer userId)

    {
        return  shipmentService.getListAllShipmentByShipper(page,limit,userId);
    }


    @GetMapping("/check-time")
    public ResponseEntity<?> getListShipment()
    {
        return shipmentService.checkTimeDelivery();
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

    @GetMapping("/view/list-waiting/{userId}")
    public ResponseEntity<?> getListShipmentByUserId(@PathVariable int userId
    )
    {
        return  shipmentService.getListShipmentStatusWaitingByUserId(userId);
    }

    @PutMapping("/update-time")
    public ResponseEntity<?> updateTimeShipment(@RequestBody UpdateTimeShipmentReq req)
    {
        return shipmentService.updateTimeShipment(req);
    }

    @GetMapping("/view/{shipmentId}")
    public ResponseEntity<?> getOneShipment(@PathVariable int shipmentId) {
        return shipmentService.getOneShipment(shipmentId);
    }

    @GetMapping("/view/order/{orderId}")
    public ResponseEntity<?> getOneShipmentByOrderId(@PathVariable int orderId) {
        return shipmentService.getInfoShipmentByOrderId(orderId);
    }
    @GetMapping(value = "/search-shipment")
    public ResponseEntity<?> searchShipment(@RequestParam(name = "keyword") String keyword,
                                            @RequestParam(name = "page") String page,
                                            @RequestParam(name = "limit") String limit) {
        return ResponseEntity.ok(shipmentService.searchShipment(keyword, page, limit));
    }

}
