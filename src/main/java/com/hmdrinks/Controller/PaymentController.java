package com.hmdrinks.Controller;

import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Payment;
import com.hmdrinks.Request.CreatePaymentReq;
import com.hmdrinks.Request.CreatePaymentVNPayReq;
import com.hmdrinks.Response.IpnResponse;
import com.hmdrinks.Service.PaymentService;
import com.hmdrinks.Service.VNPayIpnHandler;
import com.hmdrinks.Service.ZaloPayService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private SupportFunction supportFunction;

    @Autowired
    private VNPayIpnHandler vnPayIpnHandler;

    @Autowired
    private ZaloPayService zaloPayService;

    public static String getIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            String remoteAddr = request.getRemoteAddr();
            if (remoteAddr == null) {
                remoteAddr = "127.0.0.1";
            }
            return remoteAddr;
        }
        return xForwardedForHeader.split(",")[0].trim();
    }

    @PostMapping("/create/credit/vnPay")
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentVNPayReq req, HttpServletRequest httpRequest) {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        var ipAddress = getIpAddress(httpRequest);
        req.setIpAddress(ipAddress);
        return paymentService.createVNPay(req);
    }

    @PostMapping("/create/credit/zaloPay")
    public ResponseEntity<?> createPaymentZaloPay(@RequestBody CreatePaymentReq req, HttpServletRequest httpRequest) throws Exception {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return paymentService.createZaloPay(req);
    }

    @GetMapping("/vnpay_ipn")
    IpnResponse processIpn(@RequestParam Map<String, String> params) {
        return vnPayIpnHandler.process(params);
    }

    @PostMapping("/create/credit/momo")
    public ResponseEntity<?> createPaymentVNPay(@RequestBody CreatePaymentReq req, HttpServletRequest httpRequest) {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }

        return paymentService.createPaymentMomo(req.getOrderId());
    }

    @PostMapping("/create/credit/payOs")
    public ResponseEntity<?> createPaymentPayOs(@RequestBody CreatePaymentReq req, HttpServletRequest httpRequest) {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }

        return paymentService.createPaymentATM(req.getOrderId());
    }

    @PostMapping("/create/cash")
    public ResponseEntity<?> createPaymentCash(@RequestBody CreatePaymentReq req, HttpServletRequest httpRequest) {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());
        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return paymentService.createPaymentCash(req.getOrderId());
    }

    @GetMapping("/zalo/callback")
    public ResponseEntity<?> handleCallbackZalo(
            @RequestParam String app_trans_id)
    {
        return  zaloPayService.handleCallBack(app_trans_id);
    }

    @GetMapping("/payOS/callback")
    public ResponseEntity<?> handleCallbackPayOS(
            @RequestParam int orderCode) throws Exception {

        return  paymentService.handleCallBackPayOS(orderCode);
    }

    @GetMapping("/momo/callback")
    public ResponseEntity<?> handleCallback(
            @RequestParam String orderId,
            @RequestParam String resultCode)
    {
            return  paymentService.callBack(resultCode,orderId);
    }

    @GetMapping("/check-status-payment")
    public ResponseEntity<?> checkStatusPayment(
            @RequestParam int paymentId
    )
    {
        return paymentService.checkStatusPayment(paymentId);
    }

    @GetMapping("/listAll")
    public ResponseEntity<?> listAll(@RequestParam(name = "page") String page,
                                     @RequestParam(name = "limit") String limit) {
        return paymentService.getAllPayment(page, limit);
    }

    @GetMapping("/listAll-method")
    public ResponseEntity<?> listAllMethod(@RequestParam(name = "page") String page,
                                           @RequestParam(name = "limit") String limit,
                                           @RequestParam(name = "method") Payment_Method method)

    {
        return paymentService.getAllPaymentMethod(page, limit,method);
    }

    @GetMapping("/info/payOs/{paymentId}")
    public ResponseEntity<?> getInformationPayOS(@PathVariable int paymentId) throws Exception {
        return paymentService.getInformationPayOs(paymentId);
    }

    @GetMapping("/listAll-status")
    public ResponseEntity<?> listAllByStatus(@RequestParam(name = "page") String page,
                                             @RequestParam(name = "limit") String limit,
                                             @RequestParam(name = "status") Status_Payment status)

    {
        return paymentService.getAllPaymentStatus(page, limit, status);
    }

    @GetMapping("/view/{paymentId}")
    public ResponseEntity<?> viewPayment(@PathVariable int paymentId) {
        return paymentService.getOnePayment(paymentId);
    }

    @GetMapping("/check-time")
    public ResponseEntity<?> getListShipment()
    {
        return paymentService.checkTimePayment();
    }


}