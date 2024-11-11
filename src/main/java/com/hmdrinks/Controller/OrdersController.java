package com.hmdrinks.Controller;

import com.hmdrinks.Enum.Status_Order;
import com.hmdrinks.Request.ConfirmCancelOrderReq;
import com.hmdrinks.Request.CreateOrdersReq;
import com.hmdrinks.Request.CreatePaymentReq;
import com.hmdrinks.Service.GenerateInvoiceService;
import com.hmdrinks.Service.OrdersService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

@CrossOrigin
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/orders")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private SupportFunction supportFunction;
    @Autowired
    private GenerateInvoiceService generateInvoiceService;


    @PostMapping(value = "/create")
    public ResponseEntity<?> createVoucher(@RequestBody CreateOrdersReq req,HttpServletRequest httpRequest) {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());
        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return ResponseEntity.ok(ordersService.addOrder(req));
    }

    @PostMapping(value = "/confirm")
    public ResponseEntity<?> createVoucher1(@RequestBody ConfirmCancelOrderReq req,HttpServletRequest httpRequest){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());
        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return ResponseEntity.ok(ordersService.confirmOrder(req.getOrderId()));
    }

    @PostMapping(value = "/confirm-cancel")
    public ResponseEntity<?> cancelOrder(@RequestBody ConfirmCancelOrderReq req, HttpServletRequest httpRequest){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());
        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return ResponseEntity.ok(ordersService.confirmCancelOrder(req.getOrderId()));
    }

    @GetMapping("/info-payment")
    public ResponseEntity<?> infoPayment(@RequestParam int orderId){
        return  ordersService.getInformationPayment(orderId);
    }

    @GetMapping("/pdf/invoice")
    public ResponseEntity<?> infoPayment1(@RequestParam int orderId) throws IOException {
        return  generateInvoiceService.createInvoice(orderId);
    }

    @GetMapping("/view/{userId}")
    public ResponseEntity<?> getAllPaymentByUserId(@RequestParam(name = "page") String page,
                                                   @RequestParam(name = "limit") String limit,
                                                   @PathVariable int userId) throws IOException {
        return  ordersService.getAllOrderByUserId(page,limit,userId);
    }

    @GetMapping("/view/{userId}/status")
    public ResponseEntity<?> getAllPaymentByUserIdAndStatus(@RequestParam(name = "page") String page,
                                                            @RequestParam(name = "limit") String limit,
                                                            @RequestParam(name = "status")Status_Order statusOrder,
                                                            @PathVariable int userId) throws IOException {
        return  ordersService.getAllOrderByUserIdAndStatus(page,limit,userId,statusOrder);
    }

    @PutMapping("/cancel-order")
    public ResponseEntity<?> cancelOrder(@RequestBody CreatePaymentReq req, HttpServletRequest httpRequest) {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());
        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return ordersService.cancelOrder(req.getOrderId(), req.getUserId());
    }


}