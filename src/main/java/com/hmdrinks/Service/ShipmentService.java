package com.hmdrinks.Service;

import cats.kernel.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.*;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.CRUDShipmentReq;
import com.hmdrinks.Response.*;
import org.apache.hadoop.shaded.com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShipmentService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserVoucherRepository userVoucherRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductVariantsRepository productVariantsRepository;
    @Autowired
    private ShipmentRepository shipmentRepository;

    public ResponseEntity<?> shipmentAllocation(CRUDShipmentReq req)
    {
       Shippment shippment = shipmentRepository.findByShipmentId(req.getShipmentId());
       if(shippment == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shipment Not Found");
            }
       User user = userRepository.findByUserId(req.getUserId());
       if(user == null)
       {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
       }
       if(user.getRole() != Role.SHIPPER)
       {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not shipper");
       }
       shippment.setDateDelivered(req.getDateDeliver());
       shippment.setDateShip(req.getDateShip());
       shippment.setUser(user);
       shipmentRepository.save(shippment);

       Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
       Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
       User customer = userRepository.findByUserId(orders.getUser().getUserId());
       return  ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
               shippment.getShipmentId(),
               shippment.getDateCreated(),
               shippment.getDateDeleted(),
               shippment.getDateDelivered(),
               shippment.getDateShip(),
               shippment.getIsDeleted(),
               shippment.getStatus(),
               shippment.getPayment().getPaymentId(),
               shippment.getUser().getUserId(),
               customer.getFullName(),
               customer.getStreet() + ", " + customer.getDistrict() + " ," + customer.getCity(),
               customer.getPhoneNumber(),
               customer.getEmail()
       ));
    }

    public ResponseEntity<?> activateShipment(int shipmentId,int userId)
    {
        Shippment shippment = shipmentRepository.findByUserUserIdAndShipmentId(userId,shipmentId);
        if(shippment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shipment Not Found");
        }
        if(shippment.getStatus() != Status_Shipment.WAITING)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Shipment is not waiting");
        }
        shippment.setStatus(Status_Shipment.SHIPPING);
        shipmentRepository.save(shippment);
        Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
        Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
        Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        for (CartItem cartItem : cartItems) {
            ProductVariants productVariants = cartItem.getProductVariants();
            if (productVariants.getStock() > cartItem.getQuantity()) {
                productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                productVariantsRepository.save(productVariants);
            }
            else{
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Stock Not Enough");
            }
        }
        User customer = userRepository.findByUserId(orders.getUser().getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shippment.getShipmentId(),
                shippment.getDateCreated(),
                shippment.getDateDeleted(),
                shippment.getDateDelivered(),
                shippment.getDateShip(),
                shippment.getIsDeleted(),
                shippment.getStatus(),
                shippment.getPayment().getPaymentId(),
                shippment.getUser().getUserId(),
                customer.getFullName(),
                customer.getStreet() + ", " + customer.getDistrict() + " ," + customer.getCity(),
                customer.getPhoneNumber(),
                customer.getEmail()
        ));
    }

    public ResponseEntity<?> successShipment(int shipmentId,int userId)
    {
        Shippment shippment = shipmentRepository.findByUserUserIdAndShipmentId(userId,shipmentId);
        if(shippment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shipment Not Found");
        }
        if(shippment.getStatus() != Status_Shipment.SHIPPING)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request");
        }
        shippment.setStatus(Status_Shipment.SUCCESS);
        shipmentRepository.save(shippment);

        Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
        Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
        User customer = userRepository.findByUserId(orders.getUser().getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shippment.getShipmentId(),
                shippment.getDateCreated(),
                shippment.getDateDeleted(),
                shippment.getDateDelivered(),
                shippment.getDateShip(),
                shippment.getIsDeleted(),
                shippment.getStatus(),
                shippment.getPayment().getPaymentId(),
                shippment.getUser().getUserId(),
                customer.getFullName(),
                customer.getStreet() + ", " + customer.getDistrict() + " ," + customer.getCity(),
                customer.getPhoneNumber(),
                customer.getEmail()
        ));
    }

    public ResponseEntity<?> getListShipmentStatusByShipper(String pageFromParam, String limitFromParam, int userId, Status_Shipment status)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Shippment> shippments = shipmentRepository.findAllByUserUserIdAndStatus(userId,status,pageable);
        List<CRUDShipmentResponse> responses = new ArrayList<>();
        for(Shippment shippment : shippments)
        {
            Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            User customer = userRepository.findByUserId(orders.getUser().getUserId());
            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shippment.getShipmentId(),
                    shippment.getDateCreated(),
                    shippment.getDateDeleted(),
                    shippment.getDateDelivered(),
                    shippment.getDateShip(),
                    shippment.getIsDeleted(),
                    shippment.getStatus(),
                    shippment.getPayment().getPaymentId(),
                    shippment.getUser().getUserId(),
                    customer.getFullName(),
                    customer.getStreet() + ", " + customer.getDistrict() + " ," + customer.getCity(),
                    customer.getPhoneNumber(),
                    customer.getEmail()
            );
            responses.add(response);
        }
        return  ResponseEntity.status(HttpStatus.OK).body(new ListAllScheduledShipmentsResponse(
                page,
                shippments.getTotalPages(),
                limit,
                responses
        ));
    }

    public ResponseEntity<?> getListAllShipment(String pageFromParam, String limitFromParam)
    {
        User user1 = null;
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Shippment> shippments = shipmentRepository.findAll(pageable);
        List<CRUDShipmentResponse> responses = new ArrayList<>();
        for(Shippment shippment : shippments)
        {
            Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            User customer = userRepository.findByUserId(orders.getUser().getUserId());
            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shippment.getShipmentId(),
                    shippment.getDateCreated(),
                    shippment.getDateDeleted(),
                    shippment.getDateDelivered(),
                    shippment.getDateShip(),
                    shippment.getIsDeleted(),
                    shippment.getStatus(),
                    shippment.getPayment().getPaymentId(),
                    user1 != null ? shippment.getUser().getUserId() : null,
                    customer.getFullName(),
                    customer.getStreet() + ", " + customer.getDistrict() + " ," + customer.getCity(),
                    customer.getPhoneNumber(),
                    customer.getEmail()
            );
            responses.add(response);
        }
        return  ResponseEntity.status(HttpStatus.OK).body(new ListAllScheduledShipmentsResponse(
                page,
                shippments.getTotalPages(),
                limit,
                responses
        ));

    }

    public ResponseEntity<?> getListAllShipmentByStatus(String pageFromParam, String limitFromParam,Status_Shipment status)
    {
        User user1 = null;
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Shippment> shippments = shipmentRepository.findAllByStatus(status,pageable);
        List<CRUDShipmentResponse> responses = new ArrayList<>();
        for(Shippment shippment : shippments)
        {
            Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            User customer = userRepository.findByUserId(orders.getUser().getUserId());
            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shippment.getShipmentId(),
                    shippment.getDateCreated(),
                    shippment.getDateDeleted(),
                    shippment.getDateDelivered(),
                    shippment.getDateShip(),
                    shippment.getIsDeleted(),
                    shippment.getStatus(),
                    shippment.getPayment().getPaymentId(),
                    user1 != null ? shippment.getUser().getUserId() : null,
                    customer.getFullName(),
                    customer.getStreet() + ", " + customer.getDistrict() + " ," + customer.getCity(),
                    customer.getPhoneNumber(),
                    customer.getEmail()
            );
            responses.add(response);
        }
        return  ResponseEntity.status(HttpStatus.OK).body(new ListAllScheduledShipmentsResponse(
                page,
                shippments.getTotalPages(),
                limit,
                responses
        ));
    }
}