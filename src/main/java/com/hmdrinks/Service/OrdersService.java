package com.hmdrinks.Service;
import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Enum.Status_Order;
import com.hmdrinks.Enum.Status_UserVoucher;
import com.hmdrinks.Enum.Status_Voucher;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.ConfirmOderReq;
import com.hmdrinks.Request.CreateOrdersReq;
import com.hmdrinks.Request.CreateVoucherReq;
import com.hmdrinks.Request.CrudVoucherReq;
import com.hmdrinks.Response.*;
import com.hmdrinks.SupportFunction.SupportFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrdersService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserVoucherRepository userVoucherRepository;
    @Autowired
    private  CartRepository cartRepository;
    @Autowired
    private  PaymentRepository paymentRepository;
    @Autowired
    private SupportFunction supportFunction;

    public boolean isNumeric(String voucherId) {
        if (voucherId == null) {
            return false;
        }
        try {
            Integer.parseInt(voucherId);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static double calculateFee(double distance) {
        if (distance < 1) {
            return 0.0;
        } else if (distance >= 1 && distance < 5) {
            return 10000.0;
        } else if (distance >= 5 && distance < 10) {
            return 15000.0;
        } else if (distance >= 10 && distance < 15) {
            return 20000.0;
        } else if (distance >= 15 && distance < 20) {
            return 25000.0;
        } else {
            return 0.0;
        }
    }

    public ResponseEntity<?> addOrder(CreateOrdersReq req) {
        User user = userRepository.findByUserId(req.getUserId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
        }

        Cart cart = cartRepository.findByCartId(req.getCartId());
        if (cart == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found cart");
        }

        if (cart.getStatus() != Status_Cart.NEW) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Not allowed to add order");
        }

        UserVoucher userVoucher = null;
        Voucher voucher = null;
        if (isNumeric(req.getVoucherId())) {
            userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(req.getUserId(), Integer.parseInt(req.getVoucherId()));
            if (userVoucher == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found userVoucher");
            }
            if (userVoucher.getStatus() == Status_UserVoucher.USED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher already in use");
            }
            voucher = voucherRepository.findByVoucherIdAndIsDeletedFalse(userVoucher.getVoucher().getVoucherId());
            if (voucher == null || voucher.getStatus() == Status_Voucher.EXPIRED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not allowed");
            }
        }

        OrderItem existingOrderItem = orderItemRepository.findByUserUserIdAndCartCartId(req.getUserId(), req.getCartId());
        if (existingOrderItem != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cart already exists");
        }

        Orders order = new Orders();
        order.setOrderDate(LocalDateTime.now());
        String address = user.getStreet() + ", " + user.getDistrict() + ", " + user.getCity();
        order.setAddress(address);

        String place_id = supportFunction.getLocation(address);
        double[] destinations= supportFunction.getCoordinates(place_id);
        double[] origins = {10.850575879000075,106.77190192800003};
        double distance =supportFunction.getShortestDistance(origins, destinations);
        if(distance > 20){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Distance exceeded");
        }
        double fee = calculateFee(distance);
        order.setDeliveryFee(fee);
        order.setPhoneNumber(user.getPhoneNumber());
        order.setStatus(Status_Order.WAITING);
        order.setUser(user);
        order.setDeliveryFee(fee);
        order.setIsDeleted(false);

        if (voucher != null) {
            order.setVoucher(voucher);
            order.setDiscountPrice(voucher.getDiscount());
        } else {
            order.setDiscountPrice(0.0);
        }

        orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setUser(user);
        orderItem.setCart(cart);
        orderItem.setDateCreated(LocalDateTime.now());
        orderItem.setIsDeleted(false);
        orderItem.setQuantity(cart.getTotalProduct());
        orderItem.setTotalPrice(cart.getTotalPrice());
        orderItemRepository.save(orderItem);

        order.setOrderItem(orderItem);
        order.setDateCreated(LocalDateTime.now());
        order.setDeliveryDate(LocalDateTime.now());
        order.setNote(req.getNote());
        order.setTotalPrice(orderItem.getTotalPrice());


        orderRepository.save(order);

        cart.setStatus(Status_Cart.COMPLETED);
        cartRepository.save(cart);

        if (userVoucher != null) {
            userVoucher.setStatus(Status_UserVoucher.USED);
            userVoucherRepository.save(userVoucher);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new CreateOrdersResponse(
                order.getOrderId(),
                order.getAddress(),
                order.getDeliveryFee(),
                order.getDateCreated(),
                order.getDateDeleted(),
                order.getDateUpdated(),
                order.getDeliveryDate(),
                order.getDiscountPrice(),
                order.getIsDeleted(),
                order.getNote(),
                order.getOrderDate(),
                order.getPhoneNumber(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getUser().getUserId(),
                voucher != null ? voucher.getVoucherId() : null // Chỉ lấy voucherId nếu voucher không phải null
        ));
    }


    public ResponseEntity<?> confirmCancelOrder(int orderId) {
        Orders order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order");
        }
        Orders orders = orderRepository.findByOrderIdAndStatus(orderId, Status_Order.WAITING);
        if (orders == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order");
        }
        order.setStatus(Status_Order.CANCELLED);
        orderRepository.save(order);
        UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(
                    order.getUser().getUserId(), order.getVoucher().getVoucherId()
            );
        userVoucher.setStatus(Status_UserVoucher.INACTIVE);
        userVoucherRepository.save(userVoucher);
        OrderItem orderItem1 = order.getOrderItem();
        if(orderItem1 != null)
        {
            orderItemRepository.delete(order.getOrderItem());
            Cart cart = cartRepository.findByCartId(order.getOrderItem().getCart().getCartId());
            cart.setStatus(Status_Cart.NEW);
            cartRepository.save(cart);
        }
        return ResponseEntity.status(HttpStatus.OK).body("Order has been canceled");
    }

    public ResponseEntity<?> confirmOrder(int orderId) {
        Orders order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order");
        }
        if (order.getStatus() != Status_Order.WAITING)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order already in use");
        }
        if (Duration.between(order.getOrderDate(), LocalDateTime.now()).toMinutes() > 5) {
            order.setStatus(Status_Order.CANCELLED);
            orderRepository.save(order);
            UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(
                    order.getUser().getUserId(), order.getVoucher().getVoucherId()
            );
            userVoucher.setStatus(Status_UserVoucher.INACTIVE);
            OrderItem orderItem1 = order.getOrderItem();
            if(orderItem1 != null)
            {
                orderItemRepository.delete(order.getOrderItem());
                Cart cart = cartRepository.findByCartId(order.getOrderItem().getCart().getCartId());
                cart.setStatus(Status_Cart.NEW);
                cartRepository.save(cart);
            }
            userVoucherRepository.save(userVoucher);
            return ResponseEntity.status(HttpStatus.OK).body("Order has been canceled due to timeout.");
        }
        else
        {
            order.setStatus(Status_Order.CONFIRMED);
            orderRepository.save(order);
        }
        return ResponseEntity.status(HttpStatus.OK).body("Confirm success");
    }

    public ResponseEntity<?> getInformationPayment(int orderId) {
        Orders order = orderRepository.findByOrderId(orderId);
        if(order == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order");
        }
        Payment payment = paymentRepository.findByOrderOrderId(orderId);
        if(payment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found payment");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new getInformationPaymentFromOrderIdResponse(
                order.getOrderId(),
                order.getAddress(),
                order.getDeliveryFee(),
                order.getDateCreated(),
                order.getDateDeleted(),
                order.getDateUpdated(),
                order.getDeliveryDate(),
                order.getDiscountPrice(),
                order.getIsDeleted(),
                order.getNote(),
                order.getOrderDate(),
                order.getPhoneNumber(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getUser().getUserId(),
                order.getVoucher().getVoucherId(),
                new CRUDPaymentResponse(
                        payment.getPaymentId(),
                        payment.getAmount(),
                        payment.getDateCreated(),
                        payment.getDateDeleted(),
                        payment.getIsDeleted(),
                        payment.getPaymentMethod(),
                        payment.getStatus(),
                        payment.getOrder().getOrderId()
                )
        ));
    }
}