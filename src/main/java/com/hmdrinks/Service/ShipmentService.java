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
import org.springframework.data.domain.Sort;
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

    @Transactional
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
               shippment.getUser() != null ? shippment.getUser().getFullName() : null,
               shippment.getDateCreated(),
               shippment.getDateDeleted(),
               shippment.getDateDelivered(),
               shippment.getDateShip(),
               shippment.getDateCancel(),
               shippment.getIsDeleted(),
               shippment.getStatus(),
               shippment.getPayment().getPaymentId(),
               shippment.getUser() != null ? shippment.getUser().getUserId() : null,
               customer.getFullName(),
               orders.getAddress(),
               customer.getPhoneNumber(),
               customer.getEmail(),
               orders.getOrderId()
       ));
    }

    @Transactional
    public ResponseEntity<?> activateShipment(int shipmentId, int userId)
    {
        Shippment shippment = shipmentRepository.findByShipmentIdAndIsDeletedFalse(shipmentId);
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
        shippment.setUser(userRepository.findByUserIdAndIsDeletedFalse(userId));
        shippment.setDateDelivered(LocalDateTime.now().plusMinutes(25));
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
        User shipper = shippment.getUser();
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shippment.getShipmentId(),
                shippment.getUser() != null ? shippment.getUser().getFullName() : null,
                shippment.getDateCreated(),
                shippment.getDateDeleted(),
                shippment.getDateDelivered(),
                shippment.getDateShip(),
                shippment.getDateCancel(),
                shippment.getIsDeleted(),
                shippment.getStatus(),
                shippment.getPayment().getPaymentId(),
                shippment.getUser() != null ? shippment.getUser().getUserId() : null,
                customer.getFullName(),
                orders.getAddress(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                orders.getOrderId()
        ));
    }

    @Transactional
    public ResponseEntity<?> cancelShipment(int shipmentId)
    {
        Shippment shippment = shipmentRepository.findByShipmentIdAndIsDeletedFalse(shipmentId);
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
                    shippment.setDateCancel(LocalDateTime.now());
                    shippment.setStatus(Status_Shipment.CANCELLED);
                    shipmentRepository.save(shippment);
                    Payment payment = shippment.getPayment();
                    Orders orders = payment.getOrder();

                    if(payment.getPaymentMethod() == Payment_Method.CASH)
                    {
                        payment.setStatus(Status_Payment.FAILED);
                        paymentRepository.save(payment);
                        orders.setDateCanceled(LocalDateTime.now());
                        orders.setStatus(Status_Order.CANCELLED);
                        orderRepository.save(orders);
                    }
                    if(payment.getPaymentMethod() == Payment_Method.CREDIT)
                    {
                        payment.setStatus(Status_Payment.REFUND);
                        payment.setDateRefunded(LocalDateTime.now());
                        payment.setIsRefund(false);
                        if(payment.getAmount() == 0.0)
                        {
                            payment.setIsRefund(true);
                        }
                        paymentRepository.save(payment);
                        orders.setDateCanceled(LocalDateTime.now());
                        orders.setStatus(Status_Order.CANCELLED);
                        orderRepository.save(orders);
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
        shippment.setDateCancel(LocalDateTime.now());
        shipmentRepository.save(shippment);

        Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
        if(payment.getPaymentMethod() == Payment_Method.CREDIT && payment.getStatus() == Status_Payment.COMPLETED)
        {
            payment.setStatus(Status_Payment.REFUND);
            payment.setDateRefunded(LocalDateTime.now());
            payment.setIsRefund(false);
            if(payment.getAmount() == 0.0)
            {
                payment.setIsRefund(true);
            }
            paymentRepository.save(payment);
            Orders orders = payment.getOrder();
            orders.setDateCanceled(LocalDateTime.now());
            orders.setStatus(Status_Order.CANCELLED);
            orderRepository.save(orders);
        }
        else{
            payment.setStatus(Status_Payment.FAILED);
            paymentRepository.save(payment);
            Orders orders = payment.getOrder();
            orders.setDateCanceled(LocalDateTime.now());
            orders.setStatus(Status_Order.CANCELLED);
            orderRepository.save(orders);
        }
        Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
        User customer = userRepository.findByUserId(orders.getUser().getUserId());
        User shipper = shippment.getUser();
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shippment.getShipmentId(),
                shippment.getUser() != null ? shippment.getUser().getFullName() : null,
                shippment.getDateCreated(),
                shippment.getDateDeleted(),
                shippment.getDateDelivered(),
                shippment.getDateShip(),
                shippment.getDateCancel(),
                shippment.getIsDeleted(),
                shippment.getStatus(),
                shippment.getPayment().getPaymentId(),
                shippment.getUser() != null ? shippment.getUser().getUserId() : null,
                customer.getFullName(),
                orders.getAddress(),
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
        if(statusShipment == Status_Shipment.CANCELLED)
        {
            shippment.setDateCancel(LocalDateTime.now());
            shipmentRepository.save(shippment);
        }
        shipmentRepository.save(shippment);
        if(statusShipment == Status_Shipment.SUCCESS)
        {
            shippment.setDateShip(LocalDateTime.now());
            shipmentRepository.save(shippment);
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
                payment.setDateRefunded(LocalDateTime.now());
                payment.setIsRefund(false);
                if(payment.getAmount() == 0.0)
                {
                    payment.setIsRefund(true);
                }
                paymentRepository.save(payment);
            }
            if (payment.getPaymentMethod() == Payment_Method.CASH
            ) {
                payment.setStatus(Status_Payment.FAILED);
                paymentRepository.save(payment);
            }
            orders.setStatus(Status_Order.CANCELLED);
            orders.setDateCanceled(LocalDateTime.now());
            orderRepository.save(orders);
        }

        User shipper = shippment.getUser();
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shippment.getShipmentId(),
                shippment.getUser() != null ? shippment.getUser().getFullName() : null,
                shippment.getDateCreated(),
                shippment.getDateDeleted(),
                shippment.getDateDelivered(),
                shippment.getDateShip(),
                shippment.getDateCancel(),
                shippment.getIsDeleted(),
                shippment.getStatus(),
                shippment.getPayment().getPaymentId(),
                shippment.getUser() != null ? shippment.getUser().getUserId() : null,
                customer.getFullName(),
                orders.getAddress(),
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
        shippment.setDateShip(LocalDateTime.now());
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
        User shipper = shippment.getUser();
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shippment.getShipmentId(),
                shippment.getUser() != null ? shippment.getUser().getFullName() : null,
                shippment.getDateCreated(),
                shippment.getDateDeleted(),
                shippment.getDateDelivered(),
                shippment.getDateShip(),
                shippment.getDateCancel(),
                shippment.getIsDeleted(),
                shippment.getStatus(),
                shippment.getPayment().getPaymentId(),
                shippment.getUser() != null ? shippment.getUser().getUserId() : null,
                customer.getFullName(),
                orders.getAddress(),
                customer.getPhoneNumber(),
                customer.getEmail(), orders.getOrderId()
        ));
    }

    @Transactional
    public ResponseEntity<?> getListShipmentStatusByShipper(String pageFromParam, String limitFromParam, int userId, Status_Shipment status)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Sort sort = null;
        if(status == Status_Shipment.CANCELLED)
        {
            sort = Sort.by(Sort.Direction.DESC, "dateCancel");
        } else if (status == Status_Shipment.SUCCESS) {
            sort = Sort.by(Sort.Direction.DESC, "dateShip");
        } else if (status == Status_Shipment.SHIPPING) {
            sort = Sort.by(Sort.Direction.DESC, "dateCreated");
        }
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        if(sort == null)
        {
           pageable = PageRequest.of(page - 1, limit);
        }
        Page<Shippment> shippments = shipmentRepository.findAllByUserUserIdAndStatus(userId,status,pageable);
        List<Shippment> shippments1 = shipmentRepository.findAllByUserUserIdAndStatus(userId,status);
        List<CRUDShipmentResponse> responses = new ArrayList<>();
        for(Shippment shippment : shippments)
        {
            Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            User customer = userRepository.findByUserId(orders.getUser().getUserId());
            User shipper = shippment.getUser();
            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shippment.getShipmentId(),
                    shippment.getUser() != null ? shippment.getUser().getFullName() : null,
                    shippment.getDateCreated(),
                    shippment.getDateDeleted(),
                    shippment.getDateDelivered(),
                    shippment.getDateShip(),
                    shippment.getDateCancel(),
                    shippment.getIsDeleted(),
                    shippment.getStatus(),
                    shippment.getPayment().getPaymentId(),
                    shippment.getUser() != null ? shippment.getUser().getUserId() : null,
                    customer.getFullName(),
                    orders.getAddress(),
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
        Sort sort = Sort.by(Sort.Direction.DESC, "dateCreated");
        Pageable pageable = PageRequest.of(page - 1, limit,sort);
        Page<Shippment> shippments = shipmentRepository.findAllByUserUserId(userId,pageable);
        List<Shippment> shippments1 = shipmentRepository.findAllByUserUserId(userId);

        List<CRUDShipmentResponse> responses = new ArrayList<>();


        for(Shippment shippment : shippments)
        {
            Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            User customer = userRepository.findByUserId(orders.getUser().getUserId());
            User shipper = shippment.getUser();
            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shippment.getShipmentId(),
                    shippment.getUser() != null ? shippment.getUser().getFullName() : null,
                    shippment.getDateCreated(),
                    shippment.getDateDeleted(),
                    shippment.getDateDelivered(),
                    shippment.getDateShip(),
                    shippment.getDateCancel(),
                    shippment.getIsDeleted(),
                    shippment.getStatus(),
                    shippment.getPayment().getPaymentId(),
                    shippment.getUser() != null ? shippment.getUser().getUserId() : null,
                    customer.getFullName(),
                    orders.getAddress(),
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
        Sort sort = Sort.by(Sort.Direction.DESC, "dateCreated");
        Pageable pageable = PageRequest.of(page - 1, limit,sort);
        Page<Shippment> shippments = shipmentRepository.findAll(pageable);
        List<Shippment> shippments1 = shipmentRepository.findAll();

        List<CRUDShipmentResponse> responses = new ArrayList<>();

        for (Shippment shippment : shippments) {
            User shipper = shippment.getUser();
            Integer shipperId = (shipper != null) ? shipper.getUserId() : null;
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
            User shipper1 = shippment.getUser();
            String nameShipper = null;
            if(shipper1 != null)
            {
                nameShipper = shipper.getFullName();
            }
            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shippment.getShipmentId(),
                    nameShipper,
                    orders.getOrderDate(),
                    shippment.getDateDeleted(),
                    shippment.getDateDelivered(),
                    shippment.getDateShip(),
                    shippment.getDateCancel(),
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

    @Transactional
    public ResponseEntity<?> getListAllShipmentByStatus(String pageFromParam, String limitFromParam,Status_Shipment status)
    {
        User user1 = null;
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Sort sort = null;
        if(status == Status_Shipment.CANCELLED)
        {
            sort = Sort.by(Sort.Direction.DESC, "dateCancel");
        } else if (status == Status_Shipment.SUCCESS) {
            sort = Sort.by(Sort.Direction.DESC, "dateShip");
        } else if (status == Status_Shipment.SHIPPING || status == Status_Shipment.WAITING) {
            sort = Sort.by(Sort.Direction.DESC, "dateCreated");
        }
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        Page<Shippment> shippments = shipmentRepository.findAllByStatus(status,pageable);
        List<Shippment> shippments1 = shipmentRepository.findAllByStatus(status);
        List<CRUDShipmentResponse> responses = new ArrayList<>();
        int total = 0;
        for(Shippment shippment : shippments)
        {
            Payment payment = paymentRepository.findByPaymentId(shippment.getPayment().getPaymentId());
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            User customer = userRepository.findByUserId(orders.getUser().getUserId());
            User shipper = shippment.getUser();
            String nameShipper = null;
            if(shipper != null)
            {
                nameShipper = shipper.getFullName();
            }
            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shippment.getShipmentId(),
                    nameShipper,
                    orders.getOrderDate(),
                    shippment.getDateDeleted(),
                    shippment.getDateDelivered(),
                    shippment.getDateShip(),
                    shippment.getDateCancel(),
                    shippment.getIsDeleted(),
                    shippment.getStatus(),
                    shippment.getPayment().getPaymentId(),
                    shippment.getUser() != null ? shippment.getUser().getUserId() : null,
                    customer.getFullName(),
                    orders.getAddress(),
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

        boolean isDateShippedValid = dateShipped != null && dateShipped.isAfter(dateCreated);
        boolean isDateDeliveredValid = dateDelivered != null && dateDelivered.isAfter(dateCreated);

        return isDateShippedValid && isDateDeliveredValid;
    }


    @Transactional
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
        User shipper = shippment.getUser();
         return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                 shippment.getShipmentId(),
                 shippment.getUser() != null ? shippment.getUser().getFullName() : null,
                 shippment.getDateCreated(),
                 shippment.getDateDeleted(),
                 shippment.getDateDelivered(),
                 shippment.getDateShip(),
                 shippment.getDateCancel(),
                 shippment.getIsDeleted(),
                 shippment.getStatus(),
                 shippment.getPayment().getPaymentId(),
                 shippment.getUser() != null ? shippment.getUser().getUserId() : null,
                 customer.getFullName(),
                 orders.getAddress(),
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
        User customer = order.getUser();
        Shippment shipment = shipmentRepository.findByPaymentPaymentIdAndIsDeletedFalse(payment.getPaymentId());
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shipment != null ?shipment.getShipmentId() : null,
                shipment != null && shipment.getUser() != null ? shipment.getUser().getFullName() : null,

                shipment != null ? shipment.getDateCreated(): null,
                shipment != null ? shipment.getDateDeleted(): null,
                shipment != null ? shipment.getDateDelivered(): null,
                shipment != null ? shipment.getDateShip(): null,
                shipment != null ? shipment.getDateCancel(): null,
                shipment != null ? shipment.getIsDeleted(): null,
                shipment != null ? shipment.getStatus(): null,
                shipment != null ? shipment.getPayment().getPaymentId(): null,
                shipment != null && shipment.getUser() != null  ? shipment.getUser().getUserId() : null,
                customer.getFullName(),
                order.getAddress(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                order.getOrderId()
        ));
    }

    @Transactional
    public ResponseEntity<?> checkTimeDelivery(){
        List<Shippment> shippmentList = shipmentRepository.findAll();
        LocalDateTime now  = LocalDateTime.now();
        for(Shippment shippment : shippmentList)
        {
            if(shippment.getUser() == null && shippment.getStatus() == Status_Shipment.WAITING)
            {
                LocalDateTime time_create = shippment.getDateCreated().plusHours(1);
                if(now.isAfter(time_create))
                {
                    Payment payment = shippment.getPayment();
                    Orders orders = payment.getOrder();
                    if(payment.getPaymentMethod() == Payment_Method.CASH)
                    {
                        payment.setStatus(Status_Payment.FAILED);
                        paymentRepository.save(payment);
                        orders.setStatus(Status_Order.CANCELLED);
                        orders.setDateCanceled(LocalDateTime.now());
                        orderRepository.save(orders);
                    }
                    if(payment.getPaymentMethod() == Payment_Method.CREDIT && payment.getStatus() == Status_Payment.COMPLETED)
                    {
                        payment.setStatus(Status_Payment.REFUND);
                        payment.setDateRefunded(LocalDateTime.now());
                        payment.setIsRefund(false);
                        paymentRepository.save(payment);
                        orders.setStatus(Status_Order.CANCELLED);
                        orders.setDateCanceled(LocalDateTime.now());
                        orderRepository.save(orders);
                    }
                    shippment.setStatus(Status_Shipment.CANCELLED);
                    shippment.setDateCancel(LocalDateTime.now());
                    shipmentRepository.save(shippment);
                }
            }
            if(shippment.getStatus() == Status_Shipment.SHIPPING && shippment.getUser() != null)
            {
                if(now.isAfter(shippment.getDateDelivered())) {
                    Payment payment = shippment.getPayment();
                    Orders orders = payment.getOrder();
                    if(payment.getPaymentMethod() == Payment_Method.CASH)
                    {
                        payment.setStatus(Status_Payment.FAILED);
                        paymentRepository.save(payment);
                        orders.setStatus(Status_Order.CANCELLED);
                        orders.setDateCanceled(LocalDateTime.now());
                        orderRepository.save(orders);
                    }
                    if(payment.getPaymentMethod() == Payment_Method.CREDIT && payment.getStatus() == Status_Payment.COMPLETED)
                    {
                        payment.setStatus(Status_Payment.REFUND);
                        payment.setDateRefunded(LocalDateTime.now());
                        payment.setIsRefund(false);
                        paymentRepository.save(payment);
                        orders.setStatus(Status_Order.CANCELLED);
                        orders.setDateCanceled(LocalDateTime.now());
                        orderRepository.save(orders);
                    }
                    shippment.setStatus(Status_Shipment.CANCELLED);
                    shippment.setDateCancel(LocalDateTime.now());
                    shipmentRepository.save(shippment);
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> getOneShipment(int shipmentId){
        Shippment shipment = shipmentRepository.findByShipmentIdAndIsDeletedFalse(shipmentId);
        if(shipment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Payment payment = paymentRepository.findByPaymentId(shipment.getPayment().getPaymentId());
        Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
        User customer = orders.getUser();

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shipment.getShipmentId(),
                shipment.getUser() != null ? shipment.getUser().getFullName() : null,
                shipment.getDateCreated(),
                shipment.getDateDeleted(),
                shipment.getDateDelivered(),
                shipment.getDateShip(),
                shipment.getDateCancel(),
                shipment.getIsDeleted(),
                shipment.getStatus(),
                shipment.getPayment().getPaymentId(),
                shipment.getUser() != null ? shipment.getUser().getUserId() : null,
                customer.getFullName(),
                orders.getAddress(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                orders.getOrderId()
        ));
    }

    @Transactional
    public ResponseEntity<?> ActivateReceiving(int shipmentId, int userId)
    {
        Shippment shippment = shipmentRepository.findByShipmentIdAndIsDeletedFalse(shipmentId);
        if(shippment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shipment Not Found");
        }
        User user = userRepository.findByUserId(userId);
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
        }
        if(user.getRole() != Role.SHIPPER)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not shipper");
        }
        if (shippment.getUser() != null) {
            if (shippment.getUser().getUserId() != null && shippment.getUser().getUserId() != userId) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Shipment is receiving");
            }
        }
        if(shippment.getStatus() != Status_Shipment.WAITING)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Shipment is not waiting");
        }
        shippment.setUser(user);
        shippment.setDateDelivered(LocalDateTime.now().plusMinutes(25));
        shipmentRepository.save(shippment);
        Payment payment = shippment.getPayment();
        Orders orders = payment.getOrder();
        User customer = orders.getUser();
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDShipmentResponse(
                shippment.getShipmentId(),
                shippment.getUser() != null ? shippment.getUser().getFullName() : null,
                shippment.getDateCreated(),
                shippment.getDateDeleted(),
                shippment.getDateDelivered(),
                shippment.getDateShip(),
                shippment.getDateCancel(),
                shippment.getIsDeleted(),
                shippment.getStatus(),
                shippment.getPayment().getPaymentId(),
                shippment.getUser() != null ? shippment.getUser().getUserId() : null,
                customer.getFullName(),
                orders.getAddress(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                orders.getOrderId()
        ));
    }

    @Transactional
    public ResponseEntity<?> getListShipmentStatusWaitingByUserId( int userId)
    {


        User user = userRepository.findByUserIdAndIsDeletedFalse(userId);
        if(user == null)
        {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
        }
        List<Orders> ordersList = orderRepository.findAllByUserUserId(userId);
        int total = 0;
        List<CRUDShipmentResponse> responses = new ArrayList<>();
        for(Orders order : ordersList)
        {

            Payment payment = order.getPayment();
            if(order.getStatus() != Status_Order.CONFIRMED)
            {
                continue;
            }

            if(payment == null)
            {
                continue;
            }
            if(payment.getStatus() != Status_Payment.COMPLETED && payment.getPaymentMethod() == Payment_Method.CREDIT)
            {
                continue;
            }
            if(payment.getStatus() == Status_Payment.FAILED && payment.getPaymentMethod() == Payment_Method.CASH)
            {
                continue;
            }
            Shippment shippment = payment.getShipment();
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            User customer = userRepository.findByUserId(orders.getUser().getUserId());
            if(shippment.getStatus() != Status_Shipment.WAITING)
            {
                continue;
            }

            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shippment.getShipmentId(),
                    shippment.getUser() != null ? shippment.getUser().getFullName() : null,
                    shippment.getDateCreated(),
                    shippment.getDateDeleted(),
                    shippment.getDateDelivered(),
                    shippment.getDateShip(),
                    shippment.getDateCancel(),
                    shippment.getIsDeleted(),
                    shippment.getStatus(),
                    shippment.getPayment().getPaymentId(),
                    shippment.getUser() != null ? shippment.getUser().getUserId() : null,
                    customer.getFullName(),
                    orders.getAddress(),
                    customer.getPhoneNumber(),
                    customer.getEmail(),
                    orders.getOrderId()
            );
            System.out.println(response.toString());
            responses.add(response);
            total++;
        }

        return  ResponseEntity.status(HttpStatus.OK).body(new ListAllShipmentsWaitingByUserId(
                responses.size(),
                responses
        ));
    }
    @Transactional
    public TotalSearchShipmentResponse searchShipment(String keyword, String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);

        if (limit >= 100) limit = 100;

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Shippment> shipments = shipmentRepository.findByUserNameContaining(keyword, pageable);
        List<CRUDShipmentResponse> responses = new ArrayList<>();

        for (Shippment shipment : shipments) {
            User shipper = shipment.getUser();
            Integer shipperId = (shipper != null) ? shipper.getUserId() : null;
            String shipperName = (shipper != null) ? shipper.getFullName() : "Unknown Shipper";

            Payment payment = paymentRepository.findByPaymentId(shipment.getPayment().getPaymentId());
            if (payment == null || payment.getOrder() == null) {
                continue;
            }

            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            User customer = userRepository.findByUserId(orders.getUser().getUserId());

            String customerName = (customer != null) ? customer.getFullName() : "Unknown Customer";
            String customerAddress = (customer != null)
                    ? (customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity())
                    : "Unknown Address";
            String customerPhone = (customer != null) ? customer.getPhoneNumber() : "Unknown Phone";
            String customerEmail = (customer != null) ? customer.getEmail() : "Unknown Email";

            CRUDShipmentResponse response = new CRUDShipmentResponse(
                    shipment.getShipmentId(),
                    shipperName,
                    orders.getOrderDate(),
                    shipment.getDateDeleted(),
                    shipment.getDateDelivered(),
                    shipment.getDateShip(),
                    shipment.getDateCancel(),
                    shipment.getIsDeleted(),
                    shipment.getStatus(),
                    shipment.getPayment().getPaymentId(),
                    shipperId,
                    customerName,
                    customerAddress,
                    customerPhone,
                    customerEmail,
                    orders.getOrderId()
            );

            responses.add(response);
        }

        return new TotalSearchShipmentResponse(
                page,
                shipments.getTotalPages(),
                limit,
                responses
        );
    }



}
