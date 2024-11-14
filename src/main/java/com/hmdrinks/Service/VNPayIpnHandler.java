package com.hmdrinks.Service;


import com.hmdrinks.Entity.Orders;
import com.hmdrinks.Entity.Payment;
import com.hmdrinks.Entity.Shippment;
import com.hmdrinks.Enum.Status_Payment;
import com.hmdrinks.Enum.Status_Shipment;
import com.hmdrinks.Exception.BusinessException;
import com.hmdrinks.Repository.OrderRepository;
import com.hmdrinks.Repository.PaymentRepository;
import com.hmdrinks.Repository.ShipmentRepository;
import com.hmdrinks.Response.IpnResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class VNPayIpnHandler {
    private final VNPayService vnPayService;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ShipmentRepository shipmentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentService paymentService;

    public static class VnpIpnResponseConst {
        public static final IpnResponse SUCCESS = new IpnResponse("00", "Successful");
        public static final IpnResponse SIGNATURE_FAILED = new IpnResponse("97", "Signature failed");
        public static final IpnResponse ORDER_NOT_FOUND = new IpnResponse("01", "Order not found");
        public static final IpnResponse UNKNOWN_ERROR = new IpnResponse("99", "Unknown error");
    }
    public IpnResponse process(Map<String, String> params) {
        if (!vnPayService.verifyIpn(params)) {
            return VnpIpnResponseConst.SIGNATURE_FAILED;
        }
        IpnResponse response;
        var txnRef = params.get(VNPayParams.TXN_REF);
        var code = params.get(VNPayParams.RESPONSE_CODE);
        try {
            Payment payment = paymentRepository.findByOrderIdPayment(txnRef);
            if(payment != null && code.equals("00")) {
                      payment.setStatus(Status_Payment.COMPLETED);
                     paymentRepository.save(payment);
                    Shippment shippment = new Shippment();
                    shippment.setPayment(payment);
                    shippment.setIsDeleted(false);
                    shippment.setDateCreated(LocalDateTime.now());
                    shippment.setDateDelivered(LocalDateTime.now());
                    shippment.setStatus(Status_Shipment.WAITING);
                    shipmentRepository.save(shippment);
                    paymentService.assignShipments();

            }
            response = VnpIpnResponseConst.SUCCESS;
        }
        catch (BusinessException e) {
            switch (e.getResponseCode()) {
                case BOOKING_NOT_FOUND -> response = VnpIpnResponseConst.ORDER_NOT_FOUND;
                default -> response = VnpIpnResponseConst.UNKNOWN_ERROR;
            }
            try {
                int orderId = Integer.parseInt(txnRef);
                Orders order = orderRepository.findByOrderId(orderId);
                if (order != null) {
                    Payment payment = paymentRepository.findByOrderOrderId(orderId);
                    if (payment != null) {
                        payment.setStatus(Status_Payment.FAILED);
                        paymentRepository.save(payment);
                    }
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid order ID format: " + txnRef);
            }
        }
        catch (Exception e) {
            response = VnpIpnResponseConst.UNKNOWN_ERROR;
            try {
                int orderId = Integer.parseInt(txnRef);
                Orders order = orderRepository.findByOrderId(orderId);
                if (order != null) {
                    Payment payment = paymentRepository.findByOrderOrderId(orderId);
                    if (payment != null) {
                        payment.setStatus(Status_Payment.FAILED);
                        paymentRepository.save(payment);
                    }
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid order ID format: " + txnRef);
            }
        }
        return response;
    }
}