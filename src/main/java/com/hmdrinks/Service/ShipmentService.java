package com.hmdrinks.Service;

import cats.kernel.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.*;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.CRUDShipmentReq;
import com.hmdrinks.Request.UpdateTimeShipmentReq;
import com.hmdrinks.Response.*;
import jakarta.transaction.Transactional;
import org.apache.hadoop.shaded.com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scala.Int;

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
       Shippment shippment = shipmentRepository.findByShipmentIdAndIsDeletedFalse(req.getShipmentId());
       if(shippment == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shipment Not Found");
            }
       User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
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
       if(payment.getIsDeleted())
       {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment is deleted");
       }
       Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
       if(orders.getIsDeleted()){
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order is deleted");
       }

       User customer = userRepository.findByUserIdAndIsDeletedFalse(orders.getUser().getUserId());
       if(customer == null)
       {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer Not Found");
       }
       User shipper = shippment.getUser();
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
               customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
               customer.getPhoneNumber(),
               customer.getEmail(),
               orders.getOrderId()
       ));
    }

    public ResponseEntity<?> activateShipment(int shipmentId,int userId)
    {
        Shippment shippment = shipmentRepository.findByUserUserIdAndShipmentId(userId,shipmentId);
        if(shippment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shipment Not Found");
        }
        if(shippment.getIsDeleted())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shipment is deleted");
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
        User customer = userRepository.findByUserIdAndIsDeletedFalse(orders.getUser().getUserId());
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
                customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                orders.getOrderId()
        ));
    }

    public ResponseEntity<?> cancelShipment(int shipmentId,int userId)
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

        if(LocalDateTime.now().isAfter(shippment.getDateDelivered()))
        {
                    shippment.setStatus(Status_Shipment.CANCELLED);
                    shipmentRepository.save(shippment);
                    Payment payment = shippment.getPayment();
                    Orders orders = payment.getOrder();

                    if(payment.getPaymentMethod() == Payment_Method.CASH)
                    {
                        payment.setStatus(Status_Payment.FAILED);
                        paymentRepository.save(payment);
                        orders.setStatus(Status_Order.CANCELLED);
                        orderRepository.save(orders);
                    }
                    if(payment.getPaymentMethod() == Payment_Method.CREDIT)
                    {
                        payment.setStatus(Status_Payment.REFUND);
                        payment.setIsRefund(false);
                        paymentRepository.save(payment);
                        paymentRepository.save(payment);
                        orders.setStatus(Status_Order.CANCELLED);
                    }
                    Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
                    List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

                    for (CartItem cartItem : cartItems) {
                      ProductVariants productVariants = cartItem.getProductVariants();
                      productVariants.setStock(productVariants.getStock() + cartItem.getQuantity());
                      productVariantsRepository.save(productVariants);

            }

               return ResponseEntity.ok().build();
        }

        shippment.setStatus(Status_Shipment.CANCELLED);
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
                customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                orders.getOrderId()
        ));
    }

    @Transactional
    public ResponseEntity<?> activate_Admin(int shipmentId, Status_Shipment statusShipment)
    {
        Shippment shippment = shipmentRepository.findByShipmentIdAndIsDeletedFalse(shipmentId);
        if(shippment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shipment Not Found");
        }
        shippment.setStatus(statusShipment);
        shipmentRepository.save(shippment);
        if(statusShipment == Status_Shipment.SUCCESS)
        {
            Payment payment = shippment.getPayment();
            if(payment.getPaymentMethod() == Payment_Method.CASH)
            {
                payment.setStatus(Status_Payment.COMPLETED);
                paymentRepository.save(payment);
            }
        }

        Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
        Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
        User customer = userRepository.findByUserId(orders.getUser().getUserId());

        if(statusShipment == Status_Shipment.CANCELLED) {
            Cart cart = orders.getOrderItem().getCart();
            List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

            for (CartItem cartItem : cartItems) {
                ProductVariants productVariants = cartItem.getProductVariants();
                productVariants.setStock(productVariants.getStock() + cartItem.getQuantity());
                productVariantsRepository.save(productVariants);
            }
            if(payment.getPaymentMethod() == Payment_Method.CREDIT && payment.getStatus() == Status_Payment.COMPLETED)
            {
                payment.setStatus(Status_Payment.REFUND);
                payment.setIsRefund(false);
                paymentRepository.save(payment);
            }
            if (payment.getPaymentMethod() == Payment_Method.CASH
            ) {
                payment.setStatus(Status_Payment.FAILED);
                paymentRepository.save(payment);
            }
            orders.setStatus(Status_Order.CANCELLED);
            orderRepository.save(orders);
        }

        User shipper = shippment.getUser();
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
                customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                customer.getPhoneNumber(),
                customer.getEmail(), orders.getOrderId()
        ));
    }


    @Transactional
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

        Payment payment1 = shippment.getPayment();
        if(payment1.getPaymentMethod() == Payment_Method.CASH)
        {
            payment1.setStatus(Status_Payment.COMPLETED);
            paymentRepository.save(payment1);
        }

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
                customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                customer.getPhoneNumber(),
                customer.getEmail(), orders.getOrderId()
        ));
    }

    public ResponseEntity<?> getListShipmentStatusByShipper(String pageFromParam, String limitFromParam, int userId, Status_Shipment status)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Shippment> shippments = shipmentRepository.findAllByUserUserIdAndStatus(userId,status,pageable);
        List<Shippment> shippments1 = shipmentRepository.findAllByUserUserIdAndStatus(userId,status);

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
                    customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                    customer.getPhoneNumber(),
                    customer.getEmail(),
                    orders.getOrderId()
            );
            responses.add(response);
        }
        return  ResponseEntity.status(HttpStatus.OK).body(new ListAllScheduledShipmentsResponse(
                page,
                shippments.getTotalPages(),
                limit,
                shippments1.size(),
                responses
        ));
    }

    @Transactional
    public ResponseEntity<?> getListAllShipmentByShipper(String pageFromParam, String limitFromParam, int userId)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Shippment> shippments = shipmentRepository.findAllByUserUserId(userId,pageable);
        List<Shippment> shippments1 = shipmentRepository.findAllByUserUserId(userId);

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
                    customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                    customer.getPhoneNumber(),
                    customer.getEmail(),
                    orders.getOrderId()
            );
            responses.add(response);
        }
        return  ResponseEntity.status(HttpStatus.OK).body(new ListAllScheduledShipmentsResponse(
                page,
                shippments.getTotalPages(),
                limit,
                shippments1.size(),
                responses
        ));
    }

    @Transactional
    public ResponseEntity<?> getListAllShipment(String pageFromParam, String limitFromParam)
    {
        User user1 = null;
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);

        if (limit >= 100) limit = 100;

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Shippment> shippments = shipmentRepository.findAll(pageable);
        List<Shippment> shippments1 = shipmentRepository.findAll();

        List<CRUDShipmentResponse> responses = new ArrayList<>();

        for (Shippment shippment : shippments) {
            // Retrieve shipper (user associated with shipment)
            User shipper = shippment.getUser();
            Integer shipperId = (shipper != null) ? shipper.getUserId() : null;

            // Retrieve payment and ensure it's valid
            Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());

            // Ensure the payment and associated order exist
            if (payment == null || payment.getOrder() == null) {
                continue;  // Skip if there's no associated payment or order
            }

            // Retrieve order and associated customer (user)
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            User customer = userRepository.findByUserId(orders.getUser().getUserId());

            // Set default values for customer if null
            String customerFullName = (customer != null) ? customer.getFullName() : "Unknown Customer";
            String customerAddress = (customer != null)
                    ? (customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity())
                    : "Unknown Address";
            String customerPhone = (customer != null) ? customer.getPhoneNumber() : "Unknown Phone";
            String customerEmail = (customer != null) ? customer.getEmail() : "Unknown Email";

            // Create response object
            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shippment.getShipmentId(),
                    shipper1.getFullName(),
                    orders.getOrderDate(),
                    shippment.getDateDeleted(),
                    shippment.getDateDelivered(),
                    shippment.getDateShip(),
                    shippment.getIsDeleted(),
                    shippment.getStatus(),
                    shippment.getPayment().getPaymentId(),
                    shipperId,  // Use shipperId, it can be null
                    customerFullName,
                    customerAddress,
                    customerPhone,
                    customerEmail,
                    orders.getOrderId()
            );


            responses.add(response);

        }

        // Return response with shipment data
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllScheduledShipmentsResponse(
                page,
                shippments.getTotalPages(),
                limit,
                shippments1.size(),
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
        List<Shippment> shippments1 = shipmentRepository.findAllByStatus(status);

        List<CRUDShipmentResponse> responses = new ArrayList<>();
        int total = 0;
        for(Shippment shippment : shippments)
        {
            Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            User customer = userRepository.findByUserId(orders.getUser().getUserId());
            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shippment.getShipmentId(),
                    shipper.getFullName(),
                    orders.getOrderDate(),
                    shippment.getDateDeleted(),
                    shippment.getDateDelivered(),
                    shippment.getDateShip(),
                    shippment.getIsDeleted(),
                    shippment.getStatus(),
                    shippment.getPayment().getPaymentId(),
                    user1 != null ? shippment.getUser().getUserId() : null,
                    customer.getFullName(),
                    customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                    customer.getPhoneNumber(),
                    customer.getEmail(),
                    orders.getOrderId()
            );
            responses.add(response);
            total++;
        }
        return  ResponseEntity.status(HttpStatus.OK).body(new ListAllScheduledShipmentsResponse(
                page,
                shippments.getTotalPages(),
                limit,
                shippments1.size(),
                responses
        ));
    }
    public boolean isShipmentDatesValid(Shippment shipment, UpdateTimeShipmentReq updateRequest) {
        LocalDateTime dateCreated = shipment.getDateCreated();
        LocalDateTime dateShipped = updateRequest.getDateShipped();
        LocalDateTime dateDelivered = updateRequest.getDateDelivered();

        // Kiểm tra nếu dateShipped và dateDelivered đều sau dateCreated
        boolean isDateShippedValid = dateShipped != null && dateShipped.isAfter(dateCreated);
        boolean isDateDeliveredValid = dateDelivered != null && dateDelivered.isAfter(dateCreated);

        return isDateShippedValid && isDateDeliveredValid;
    }


    public ResponseEntity<?> updateTimeShipment(UpdateTimeShipmentReq req)
    {
         User user1 = null;
         Shippment shippment = shipmentRepository.findByShipmentIdAndIsDeletedFalse(req.getShipmentId());
         if(shippment == null)
         {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shipment not found");
         }

         boolean check = isShipmentDatesValid(shippment,req);
         if(!check)
         {
             return ResponseEntity.status(HttpStatus.CONFLICT).body("Shipment date is not valid");
         }
         shippment.setDateDelivered(req.getDateDelivered());
         shippment.setDateShip(req.getDateShipped());
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
                 user1 != null ? shippment.getUser().getUserId() : null,
                 customer.getFullName(),
                 customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                 customer.getPhoneNumber(),
                 customer.getEmail(),
                 orders.getOrderId()
         ));
    }

    @Transactional
    public ResponseEntity<?> getInfoShipmentByOrderId(int orderId)
    {
        Orders order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId);
        if(order == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
        Payment payment = paymentRepository.findByPaymentId(order.getPayment().getPaymentId());
        if(payment != null)
        {

        }
        User customer = payment.getOrder().getUser();
        Shippment shipment = shipmentRepository.findByPaymentPaymentIdAndIsDeletedFalse(payment.getPaymentId());
        User shipper = shipment.getUser();
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shipment.getShipmentId(),
                shipment.getDateCreated(),
                shipment.getDateDeleted(),
                shipment.getDateDelivered(),
                shipment.getDateShip(),
                shipment.getIsDeleted(),
                shipment.getStatus(),
                shipment.getPayment().getPaymentId(),
                shipment.getUser().getUserId(),
                customer.getFullName(),
                customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                order.getOrderId()
        ));
    }

    @Transactional
    public ResponseEntity<?> getOneShipment(int shipmentId){
        Shippment shipment = shipmentRepository.findByShipmentIdAndIsDeletedFalse(shipmentId);
        if(shipment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }


        User shipper = shipment.getUser();
        Payment payment = paymentRepository.findByPaymentId(shipment.getPayment().getPaymentId());
        Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
        User customer = orders.getUser();

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shipment.getShipmentId(),
                shipment.getDateCreated(),
                shipment.getDateDeleted(),
                shipment.getDateDelivered(),
                shipment.getDateShip(),
                shipment.getIsDeleted(),
                shipment.getStatus(),
                shipment.getPayment().getPaymentId(),
                shipment.getUser().getUserId(),
                customer.getFullName(),
                customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                orders.getOrderId()
        ));
    }

}