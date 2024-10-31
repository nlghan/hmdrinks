package com.hmdrinks.Controller;

import com.hmdrinks.Request.CreateOrdersReq;
import com.hmdrinks.Request.CreateVoucherReq;
import com.hmdrinks.Request.CrudVoucherReq;
import com.hmdrinks.Service.OrdersService;
import com.hmdrinks.Service.VoucherService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/orders")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @PostMapping(value = "/create")
    public ResponseEntity<?> createVoucher(@RequestBody CreateOrdersReq req){
        return ResponseEntity.ok(ordersService.addOrder(req));
    }

    @PostMapping(value = "/confirm")
    public ResponseEntity<?> createVoucher1(@RequestBody int orderId){
        return ResponseEntity.ok(ordersService.confirmOrder(orderId));
    }

    @PostMapping(value = "/confirm-cancel")
    public ResponseEntity<?> cancelOrder(@RequestBody int orderId){
        return ResponseEntity.ok(ordersService.confirmCancelOrder(orderId));
    }

    @GetMapping("/info-payment")
    public ResponseEntity<?> infoPayment(@RequestParam int orderId){
        return  ordersService.getInformationPayment(orderId);
    }

}