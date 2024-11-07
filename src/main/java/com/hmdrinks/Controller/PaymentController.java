package com.hmdrinks.Controller;

import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Payment;
import com.hmdrinks.Request.CreatePaymentReq;
import com.hmdrinks.Request.CreatePaymentVNPayReq;
import com.hmdrinks.Response.IpnResponse;
import com.hmdrinks.Service.PaymentService;
import com.hmdrinks.Service.VNPayIpnHandler;
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

    @GetMapping("/vnpay_ipn")
    IpnResponse processIpn(@RequestParam Map<String, String> params) {
        return vnPayIpnHandler.process(params);
    }

    @PostMapping("/create/credit")
    public ResponseEntity<?> createPaymentVNPay(@RequestBody CreatePaymentReq req, HttpServletRequest httpRequest) {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }

        return paymentService.createPayment(req.getOrderId());
    }

    @PostMapping("/create/cash")
    public ResponseEntity<?> createPaymentCash(@RequestBody CreatePaymentReq req, HttpServletRequest httpRequest) {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());
        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return paymentService.createPaymentCash(req.getOrderId());
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(
            @RequestParam String partnerCode,
            @RequestParam String orderId,
            @RequestParam String requestId,
            @RequestParam String amount,
            @RequestParam String orderInfo,
            @RequestParam String orderType,
            @RequestParam String transId,
            @RequestParam String resultCode,
            @RequestParam String message,
            @RequestParam String payType,
            @RequestParam String responseTime,
            @RequestParam String extraData,
            @RequestParam String signature) {
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

    @GetMapping("/listAll-status")
    public ResponseEntity<?> listAllByStatus(@RequestParam(name = "page") String page,
                                             @RequestParam(name = "limit") String limit,
                                             @RequestParam(name = "status") Status_Payment status)

    {
        return paymentService.getAllPaymentStatus(page, limit, status);
    }
}