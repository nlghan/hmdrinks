package com.hmdrinks.Service;
import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.*;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.CreateOrdersReq;
import com.hmdrinks.Response.*;
import com.hmdrinks.SupportFunction.DistanceAndDuration;
import com.hmdrinks.SupportFunction.SupportFunction;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    @Autowired
    private  ShipmentRepository shipmentRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductVariantsRepository productVariantsRepository;

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
        } else if (distance >= 15 && distance <= 20) {
            return 25000.0;
        } else {
            return 0.0;
        }
    }

    public boolean isVoucherValid(Voucher voucher) {
        LocalDateTime startDate = voucher.getStartDate();
        LocalDateTime endDate = voucher.getEndDate();
        LocalDateTime now = LocalDateTime.now();
        return (now.isEqual(startDate) || now.isAfter(startDate)) && (now.isEqual(endDate) || now.isBefore(endDate));
    }

    public ResponseEntity<?> addOrder(CreateOrdersReq req) {
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
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
            if (voucher == null || voucher.getStatus() == Status_Voucher.EXPIRED || voucher.getIsDeleted()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher is deleted");
            }
            boolean checkVoucher = isVoucherValid(voucher);
            if (!checkVoucher) {
                voucher.setStatus(Status_Voucher.EXPIRED);
                voucherRepository.save(voucher);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher expired");
            }

        }

        OrderItem existingOrderItem = orderItemRepository.findByUserUserIdAndCartCartId(req.getUserId(), req.getCartId());
        if (existingOrderItem != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cart already exists");
        }

        Orders order = new Orders();
        order.setOrderDate(LocalDateTime.now());
        String address = user.getStreet() +  ", " + user.getWard() + ", " + user.getDistrict() + ", " + user.getCity();
        order.setAddress(address);

        String place_id = supportFunction.getLocation(address);
        double[] destinations= supportFunction.getCoordinates(place_id);
        double[] origins = {10.850575879000075,106.77190192800003};
        // Số 1-3 Võ Văn Ngân, Thủ Đức, Tp HCM
        DistanceAndDuration distanceAndDuration = supportFunction.getShortestDistance(origins, destinations);
        double distance = distanceAndDuration.getDistance();
        if(distance > 20){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Distance exceeded, please update address");
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
        order.setDateCanceled(null);
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
                order.getDateCanceled(),
                order.getDiscountPrice(),
                order.getIsDeleted(),
                order.getNote(),
                order.getOrderDate(),
                order.getPhoneNumber(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getUser().getUserId(),
                voucher != null ? voucher.getVoucherId() : null
        ));
    }


    public ResponseEntity<?> confirmCancelOrder(int orderId) {
        Orders order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order");
        }
        Orders orders = orderRepository.findByOrderIdAndStatusAndIsDeletedFalse(orderId, Status_Order.WAITING);
        if (orders == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order status waiting");
        }
        orders.setDateCanceled(LocalDateTime.now());
        order.setStatus(Status_Order.CANCELLED);
        orderRepository.save(order);
        if(order.getVoucher() != null)
        {
            UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(
                    order.getUser().getUserId(), order.getVoucher().getVoucherId()
            );
            userVoucher.setStatus(Status_UserVoucher.INACTIVE);
            userVoucherRepository.save(userVoucher);
        }
//        OrderItem orderItem1 = order.getOrderItem();
//        if(orderItem1 != null)
//        {
//            orderItemRepository.delete(order.getOrderItem());
//            Cart cart = cartRepository.findByCartId(order.getOrderItem().getCart().getCartId());
//            cart.setStatus(Status_Cart.NEW);
//            cartRepository.save(cart);
//        }
        return ResponseEntity.status(HttpStatus.OK).body("Order has been canceled");
    }

    public ResponseEntity<?> confirmOrder(int orderId) {
        Orders order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId);
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
            if(order.getVoucher() != null)
            {
                UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(
                        order.getUser().getUserId(), order.getVoucher().getVoucherId()
                );
                userVoucher.setStatus(Status_UserVoucher.INACTIVE);
                userVoucherRepository.save(userVoucher);
            }

            OrderItem orderItem1 = order.getOrderItem();
            if(orderItem1 != null)
            {
                orderItemRepository.delete(order.getOrderItem());
                Cart cart = cartRepository.findByCartId(order.getOrderItem().getCart().getCartId());
                cart.setStatus(Status_Cart.NEW);
                cartRepository.save(cart);
            }

            return ResponseEntity.status(HttpStatus.OK).body("Order has been canceled due to timeout");
        }
        else
        {
            order.setStatus(Status_Order.CONFIRMED);
            orderRepository.save(order);
        }
        return ResponseEntity.status(HttpStatus.OK).body("Confirm success");
    }

    public ResponseEntity<?> getInformationPayment(int orderId) {
        Orders order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId);
        if(order == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order");
        }
        Payment payment = paymentRepository.findByOrderOrderIdAndIsDeletedFalse(orderId);
        if(payment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found payment");
        }
        Voucher voucher = null;
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
                voucher != null ? voucher.getVoucherId() :0,
                new CRUDPaymentResponse(
                        payment.getPaymentId(),
                        payment.getAmount(),
                        payment.getDateCreated(),
                        payment.getDateDeleted(),
                        payment.getDateRefunded(),
                        payment.getIsDeleted(),
                        payment.getPaymentMethod(),
                        payment.getStatus(),
                        payment.getOrder().getOrderId(),
                        payment.getIsRefund()
                )
        ));
    }

    public ResponseEntity<?> getAllOrderByUserId(String pageFromParam, String limitFromParam, int userId) {
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId);
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
        }
        Voucher voucher = null;
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Orders> orders = orderRepository.findAllByUserUserIdAndIsDeletedFalse(userId, pageable);
        List<CreateOrdersResponse> list = new ArrayList<>();
        int total =0 ;
        for (Orders order : orders) {
            list.add(new CreateOrdersResponse(
                    order.getOrderId(),
                    order.getAddress(),
                    order.getDeliveryFee(),
                    order.getDateCreated(),
                    order.getDateDeleted(),
                    order.getDateUpdated(),
                    order.getDeliveryDate(),
                    order.getDateCanceled(),
                    order.getDiscountPrice(),
                    order.getIsDeleted(),
                    order.getNote(),
                    order.getOrderDate(),
                    order.getPhoneNumber(),
                    order.getStatus(),
                    order.getTotalPrice(),
                    order.getUser().getUserId(),
                    voucher != null ? voucher.getVoucherId() : null
            ));
            total++;
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ListAllOrdersResponse(page,
                orders.getTotalPages(),
                limit,
                total,
                userId,
                list));
    }

    public ResponseEntity<?> getAllOrderByUserIdAndStatus(String pageFromParam, String limitFromParam, int userId,Status_Order status) {
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId);
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
        }
        Voucher voucher = null;
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Orders> orders = orderRepository.findAllByUserUserIdAndStatusAndIsDeletedFalse(userId,status,pageable);
        List<CreateOrdersResponse> list = new ArrayList<>();
        int total = 0;
        for (Orders order : orders) {
            list.add(new CreateOrdersResponse(
                    order.getOrderId(),
                    order.getAddress(),
                    order.getDeliveryFee(),
                    order.getDateCreated(),
                    order.getDateDeleted(),
                    order.getDateUpdated(),
                    order.getDeliveryDate(),
                    order.getDateCanceled(),
                    order.getDiscountPrice(),
                    order.getIsDeleted(),
                    order.getNote(),
                    order.getOrderDate(),
                    order.getPhoneNumber(),
                    order.getStatus(),
                    order.getTotalPrice(),
                    order.getUser().getUserId(),
                    voucher != null ? voucher.getVoucherId() : null
            ));
            total++;
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ListAllOrdersResponse(page,
                orders.getTotalPages(),
                limit,
                total,
                userId,
                list));
    }

    @Transactional
    public ResponseEntity<?> cancelOrder(int orderId, int userId) {
        Orders order = orderRepository.findByOrderIdAndUserUserIdAndIsDeletedFalse(orderId, userId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
        if (order.getStatus() == Status_Order.CANCELLED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Order already cancelled");
        }
        Payment payment = paymentRepository.findByOrderOrderId(order.getOrderId());
        if (payment != null) {

            if (payment.getStatus() == Status_Payment.PENDING) {
                payment.setStatus(Status_Payment.FAILED);
                paymentRepository.save(payment);
            }
            Shippment shipment = shipmentRepository.findByPaymentPaymentIdAndIsDeletedFalse(payment.getPaymentId());
            if (shipment != null) {
                if (shipment.getStatus() == Status_Shipment.SUCCESS || (shipment.getStatus() == Status_Shipment.SHIPPING && shipment.getUser() != null) ) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Order cannot be cancelled as shipment is in progress or completed");
                }
                if (shipment.getStatus() == Status_Shipment.WAITING) {

                    if (payment.getStatus() == Status_Payment.COMPLETED) {
                        payment.setStatus(Status_Payment.REFUND);
                        payment.setDateRefunded(LocalDateTime.now());
                        payment.setIsRefund(false);
                        paymentRepository.save(payment);
                    }
                    shipment.setStatus(Status_Shipment.CANCELLED);
                    shipment.setDateCancel(LocalDateTime.now());
                    shipmentRepository.save(shipment);
                }
            } else {
                System.out.println("Shipment not found or already deleted");
            }
        } else {
            System.out.println("Payment not found");
        }
        order.setStatus(Status_Order.CANCELLED);
        order.setDateCanceled(LocalDateTime.now());
        orderRepository.save(order);
        Voucher voucher = order.getVoucher();
        if (voucher != null) {
            UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(userId, voucher.getVoucherId());
            if (userVoucher != null) {
                userVoucher.setStatus(Status_UserVoucher.INACTIVE);
                userVoucherRepository.save(userVoucher);
                System.out.println("User voucher updated to INACTIVE");
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body("Order cancelled successfully");
    }

    public ResponseEntity<?> detailItemOrders(int orderId)
    {
        Orders orders = orderRepository.findByOrderId(orderId);
        if (orders == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        OrderItem orderItem = orders.getOrderItem();
        Cart cart = orderItem.getCart();
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        List<ItemOrderResponse> itemOrderResponses = new ArrayList<>();
        for(CartItem cartItem: cartItems)
        {
            ProductVariants productVariants = cartItem.getProductVariants();
            itemOrderResponses.add(new ItemOrderResponse(
                    cartItem.getCartItemId(),
                    cart.getCartId(),
                    productVariants.getProduct().getProId(),
                    productVariants.getProduct().getProName(),
                    productVariants.getSize(),
                    productVariants.getPrice(),
                    cartItem.getTotalPrice(),
                    cartItem.getQuantity()
            ));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListItemOrderResponse(orderId,itemOrderResponses.size(),itemOrderResponses));
    }

    @Transactional
    public ResponseEntity<?> listHistoryOrder(int userId){
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId);
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        List<Orders> orders = orderRepository.findAllByUserUserId(userId);
        List<HistoryOrderResponse> historyOrderResponses = new ArrayList<>();
        Voucher voucher = null;
        for(Orders order: orders)
        {
            if(order.getStatus() == Status_Order.CONFIRMED)
            {
                Payment payment = order.getPayment();
                if(payment != null)
                {
                    Shippment shipment = shipmentRepository.findByPaymentPaymentIdAndIsDeletedFalse(payment.getPaymentId());
                    if(shipment != null && shipment.getStatus() == Status_Shipment.SUCCESS)
                    {
                        CreateOrdersResponse createOrdersResponse = new CreateOrdersResponse(
                                order.getOrderId(),
                                order.getAddress(),
                                order.getDeliveryFee(),
                                order.getDateCreated(),
                                order.getDateDeleted(),
                                order.getDateUpdated(),
                                order.getDeliveryDate(),
                                order.getDateCanceled(),
                                order.getDiscountPrice(),
                                order.getIsDeleted(),
                                order.getNote(),
                                order.getOrderDate(),
                                order.getPhoneNumber(),
                                order.getStatus(),
                                order.getTotalPrice(),
                                order.getUser().getUserId(),
                                voucher != null ? voucher.getVoucherId() : null
                        );
                        User customer = order.getUser();
                        User shipper = shipment.getUser();

                        CRUDShipmentResponse crudShipmentResponse = new CRUDShipmentResponse(
                                shipment.getShipmentId(),
                                shipper.getFullName(),
                                shipment.getDateCreated(),
                                shipment.getDateDeleted(),
                                shipment.getDateDelivered(),
                                shipment.getDateShip(),
                                shipment.getDateCancel(),
                                shipment.getIsDeleted(),
                                shipment.getStatus(),
                                shipment.getPayment().getPaymentId(),
                                shipment.getUser().getUserId(),
                                customer.getFullName(),
                                customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                                customer.getPhoneNumber(),
                                customer.getEmail(),
                                order.getOrderId()
                        );
                        historyOrderResponses.add(new HistoryOrderResponse(createOrdersResponse,crudShipmentResponse));
                    }
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ListAllHistoryOrderResponse(userId,historyOrderResponses.size(),historyOrderResponses));
    }

    @Transactional
    public  ResponseEntity<?> fetchOrdersAwaitingPayment(int userId)
    {
        List<Orders> ordersWaiting = orderRepository.findAllByUserUserIdAndStatus(userId, Status_Order.WAITING);
        List<Orders> ordersConfirm = orderRepository.findAllByUserUserIdAndStatus(userId, Status_Order.CONFIRMED);
        List<CreateOrdersResponse> listOrderWaiting = new ArrayList<>();
        List<CreateOrdersResponse> listOrderConfirm = new ArrayList<>();
        for(Orders order: ordersWaiting)
        {
            Voucher voucher = null;
            CreateOrdersResponse createOrdersResponse = new CreateOrdersResponse(
                    order.getOrderId(),
                    order.getAddress(),
                    order.getDeliveryFee(),
                    order.getDateCreated(),
                    order.getDateDeleted(),
                    order.getDateUpdated(),
                    order.getDeliveryDate(),
                    order.getDateCanceled(),
                    order.getDiscountPrice(),
                    order.getIsDeleted(),
                    order.getNote(),
                    order.getOrderDate(),
                    order.getPhoneNumber(),
                    order.getStatus(),
                    order.getTotalPrice(),
                    order.getUser().getUserId(),
                    voucher != null ? voucher.getVoucherId() : null
            );
            listOrderWaiting.add(createOrdersResponse);
        }

        for(Orders order: ordersConfirm)
        {
                Payment payment = order.getPayment();
                if(payment != null) {
                    continue;
                }
                Voucher voucher = null;
                CreateOrdersResponse createOrdersResponse = new CreateOrdersResponse(
                        order.getOrderId(),
                        order.getAddress(),
                        order.getDeliveryFee(),
                        order.getDateCreated(),
                        order.getDateDeleted(),
                        order.getDateUpdated(),
                        order.getDeliveryDate(),
                        order.getDateCanceled(),
                        order.getDiscountPrice(),
                        order.getIsDeleted(),
                        order.getNote(),
                        order.getOrderDate(),
                        order.getPhoneNumber(),
                        order.getStatus(),
                        order.getTotalPrice(),
                        order.getUser().getUserId(),
                        voucher != null ? voucher.getVoucherId() : null
                );
                listOrderConfirm.add(createOrdersResponse);
            }
        ListOrderWaiting list1 = new ListOrderWaiting(ordersWaiting.size(),listOrderWaiting);
        ListAllOrderConfirmAndNotPayment list2 = new ListAllOrderConfirmAndNotPayment(ordersConfirm.size(),listOrderConfirm);
        return ResponseEntity.status(HttpStatus.OK).body(
                new fetchOrdersAwaitingPayment(
                        list1.getTotal() + list2.getTotal(),
                        list1,
                        list2
                )
        );
        }

        @Transactional
    public ResponseEntity<?> listOrderCancelnotPayment(int userId)
    {
        List<Orders> orders = orderRepository.findAllByUserUserIdAndStatus(userId, Status_Order.CANCELLED);
        List<CreateOrdersResponse> createOrdersResponses = new ArrayList<>();
        for(Orders order: orders)
        {
            Payment payment = order.getPayment();
            if(payment != null)
            {
                continue;
            }
            Voucher voucher = null;
            CreateOrdersResponse createOrdersResponse = new CreateOrdersResponse(
                    order.getOrderId(),
                    order.getAddress(),
                    order.getDeliveryFee(),
                    order.getDateCreated(),
                    order.getDateDeleted(),
                    order.getDateUpdated(),
                    order.getDeliveryDate(),
                    order.getDateCanceled(),
                    order.getDiscountPrice(),
                    order.getIsDeleted(),
                    order.getNote(),
                    order.getOrderDate(),
                    order.getPhoneNumber(),
                    order.getStatus(),
                    order.getTotalPrice(),
                    order.getUser().getUserId(),
                    voucher != null ? voucher.getVoucherId() : null
            );
            createOrdersResponses.add(createOrdersResponse);
        }
         return ResponseEntity.status(HttpStatus.OK).body(new ListAllOrderCancelAndNotPayment(createOrdersResponses.size(),createOrdersResponses));
    }

    @Transactional
    public ResponseEntity<?> listOrderCancelHavetPayment(int userId)
    {
        List<Orders> orders = orderRepository.findAllByUserUserIdAndStatus(userId, Status_Order.CANCELLED);
        List<CreateOrdersResponse> createOrdersResponses = new ArrayList<>();
        for(Orders order: orders)
        {
            Payment payment = order.getPayment();
            if (payment != null && "REFUND".equals(payment.getStatus()))
            {
                continue;
            }
            Voucher voucher = null;
            CreateOrdersResponse createOrdersResponse = new CreateOrdersResponse(
                    order.getOrderId(),
                    order.getAddress(),
                    order.getDeliveryFee(),
                    order.getDateCreated(),
                    order.getDateDeleted(),
                    order.getDateUpdated(),
                    order.getDeliveryDate(),
                    order.getDateCanceled(),
                    order.getDiscountPrice(),
                    order.getIsDeleted(),
                    order.getNote(),
                    order.getOrderDate(),
                    order.getPhoneNumber(),
                    order.getStatus(),
                    order.getTotalPrice(),
                    order.getUser().getUserId(),
                    voucher != null ? voucher.getVoucherId() : null
            );
            createOrdersResponses.add(createOrdersResponse);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllOrderCancelAndNotPayment(createOrdersResponses.size(),createOrdersResponses));
    }


    @Transactional
    public ResponseEntity<?> listOrderCancelAndPaymentRefund()
    {
        List<Orders> orders = orderRepository.findAllByStatus(Status_Order.CANCELLED);
        List<OrderCancelPaymentRefund> historyOrderResponses = new ArrayList<>();
        for(Orders order: orders)
        {
            Payment payment = order.getPayment();
            if(payment == null)
            {
                continue;
            }
            if(payment.getStatus() != Status_Payment.REFUND)
            {
                continue;
            }
            Voucher voucher = null;
            CreateOrdersResponse createOrdersResponse = new CreateOrdersResponse(
                    order.getOrderId(),
                    order.getAddress(),
                    order.getDeliveryFee(),
                    order.getDateCreated(),
                    order.getDateDeleted(),
                    order.getDateUpdated(),
                    order.getDeliveryDate(),
                    order.getDateCanceled(),
                    order.getDiscountPrice(),
                    order.getIsDeleted(),
                    order.getNote(),
                    order.getOrderDate(),
                    order.getPhoneNumber(),
                    order.getStatus(),
                    order.getTotalPrice(),
                    order.getUser().getUserId(),
                    voucher != null ? voucher.getVoucherId() : null
            );

            CRUDPaymentResponse crudPaymentResponse = new CRUDPaymentResponse(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    payment.getDateCreated(),
                    payment.getDateDeleted(),
                    payment.getDateRefunded(),
                    payment.getIsDeleted(),
                    payment.getPaymentMethod(),
                    payment.getStatus(),
                    payment.getOrder().getOrderId(),
                    payment.getIsRefund()
            );

            historyOrderResponses.add(new OrderCancelPaymentRefund(createOrdersResponse, crudPaymentResponse));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllOrderCancelAndPaymentRefund(historyOrderResponses.size(), historyOrderResponses));
    }

    @Transactional
    public ResponseEntity<?> listOrderCancelAndPaymentRefundUser(int userId)
    {
        List<Orders> orders = orderRepository.findAllByUserUserIdAndStatus(userId, Status_Order.CANCELLED);
        List<OrderCancelPaymentRefund> historyOrderResponses = new ArrayList<>();
        for(Orders order: orders)
        {
            Payment payment = order.getPayment();
            if(payment == null)
            {
                continue;
            }
            if(payment.getStatus() != Status_Payment.REFUND)
            {
                continue;
            }
            Voucher voucher = null;
            CreateOrdersResponse createOrdersResponse = new CreateOrdersResponse(
                    order.getOrderId(),
                    order.getAddress(),
                    order.getDeliveryFee(),
                    order.getDateCreated(),
                    order.getDateDeleted(),
                    order.getDateUpdated(),
                    order.getDeliveryDate(),
                    order.getDateCanceled(),
                    order.getDiscountPrice(),
                    order.getIsDeleted(),
                    order.getNote(),
                    order.getOrderDate(),
                    order.getPhoneNumber(),
                    order.getStatus(),
                    order.getTotalPrice(),
                    order.getUser().getUserId(),
                    voucher != null ? voucher.getVoucherId() : null
            );

            CRUDPaymentResponse crudPaymentResponse = new CRUDPaymentResponse(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    payment.getDateCreated(),
                    payment.getDateDeleted(),
                    payment.getDateRefunded(),
                    payment.getIsDeleted(),
                    payment.getPaymentMethod(),
                    payment.getStatus(),
                    payment.getOrder().getOrderId(),
                    payment.getIsRefund()
            );

            historyOrderResponses.add(new OrderCancelPaymentRefund(createOrdersResponse, crudPaymentResponse));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllOrderCancelAndPaymentRefund(historyOrderResponses.size(), historyOrderResponses));
    }


    @Transactional
    public ResponseEntity<?> listOrderConfirmed(int userId) {
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        List<Orders> orders = orderRepository.findAllByUserUserIdAndStatus(userId, Status_Order.CONFIRMED);
        List<HistoryOrderResponse> historyOrderResponses = new ArrayList<>();

        for (Orders order : orders) {
            Payment payment = order.getPayment();
            if (payment == null) {
                continue;
            }

            Shippment shipment = shipmentRepository.findByPaymentPaymentIdAndIsDeletedFalse(payment.getPaymentId());
            if (shipment == null || shipment.getStatus() != Status_Shipment.SHIPPING) {
                continue;
            }

            CreateOrdersResponse createOrdersResponse = new CreateOrdersResponse(
                    order.getOrderId(),
                    order.getAddress(),
                    order.getDeliveryFee(),
                    order.getDateCreated(),
                    order.getDateDeleted(),
                    order.getDateUpdated(),
                    order.getDeliveryDate(),
                    order.getDateCanceled(),
                    order.getDiscountPrice(),
                    order.getIsDeleted(),
                    order.getNote(),
                    order.getOrderDate(),
                    order.getPhoneNumber(),
                    order.getStatus(),
                    order.getTotalPrice(),
                    order.getUser().getUserId(),
                    null
            );

            User shipper = shipment.getUser();
            User customer = order.getUser();
            CRUDShipmentResponse crudShipmentResponse = new CRUDShipmentResponse(
                    shipment.getShipmentId(),
                    shipper != null ? shipper.getFullName() : null,
                    shipment.getDateCreated(),
                    shipment.getDateDeleted(),
                    shipment.getDateDelivered(),
                    shipment.getDateShip(),
                    shipment.getDateCancel(),
                    shipment.getIsDeleted(),
                    shipment.getStatus(),
                    shipment.getPayment().getPaymentId(),
                    shipper != null ? shipper.getUserId() : null,
                    customer.getFullName(),
                    customer.getStreet() + ", " + customer.getWard() + ", " + customer.getDistrict() + ", " + customer.getCity(),
                    customer.getPhoneNumber(),
                    customer.getEmail(),
                    order.getOrderId()
            );

            historyOrderResponses.add(new HistoryOrderResponse(createOrdersResponse, crudShipmentResponse));
        }

        return ResponseEntity.status(HttpStatus.OK).body(historyOrderResponses);
    }

    @Transactional
    public  ResponseEntity<?> CancelReason(CancelReasonReq req)
    {
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Orders orders = orderRepository.findByOrderIdAndUserUserIdAndIsDeletedFalse(req.getOrderId(), req.getUserId());
        if (orders == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
        if(orders.getIsCancelReason() != null && orders.getIsCancelReason())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is accept");
        }
        if(orders.getIsCancelReason() != null && !orders.getIsCancelReason())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is reject");
        }
        if(orders.getIsCancelReason() == null)
        {
            orders.setCancelReason(req.getCancelReason());
            orderRepository.save(orders);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Transactional
    public ResponseEntity<?> listAllCancelReasonAwait()
    {
        List<Orders> orders = orderRepository.findAllByIsDeletedFalse();
        List<CancelReasonResponse> listCancelReasonResponses = new ArrayList<>();
        for (Orders order : orders) {
            if(order.getCancelReason() == null)
            {
                continue;
            }
            if(order.getIsCancelReason() != null)
            {
                continue;
            }
            CancelReasonResponse cancelReasonResponse = new CancelReasonResponse(
                    order.getUser().getUserId(),
                    order.getOrderId(),
                    order.getCancelReason()

            );
            listCancelReasonResponses.add(cancelReasonResponse);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllCancelReasonResponse(listCancelReasonResponses.size(), listCancelReasonResponses));
    }

    @Transactional
    public ResponseEntity<?> acceptCancelReason(int orderId)
    {
        Orders orders = orderRepository.findByOrderIdAndIsDeletedFalse(orderId);
        if (orders == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
        if(orders.getIsCancelReason() != null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is accept or reject");
        }
        orders.setIsCancelReason(true);
        orders.setDateCanceled(LocalDateTime.now());
        orders.setStatus(Status_Order.CANCELLED);
        orderRepository.save(orders);
        Payment payment = orders.getPayment();

        Cart cart = orders.getOrderItem().getCart();
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

        for (CartItem cartItem : cartItems) {
            ProductVariants productVariants = cartItem.getProductVariants();
            productVariants.setStock(productVariants.getStock() + cartItem.getQuantity());
            productVariantsRepository.save(productVariants);
        }
        if(payment != null)
        {
            if(payment.getPaymentMethod() == Payment_Method.CREDIT && payment.getStatus() == Status_Payment.COMPLETED)
            {
                payment.setStatus(Status_Payment.REFUND);
                payment.setDateRefunded(LocalDateTime.now());
                payment.setIsRefund(false);
                paymentRepository.save(payment);
            }
            if (payment.getPaymentMethod() == Payment_Method.CASH) {
                payment.setStatus(Status_Payment.FAILED);
                paymentRepository.save(payment);
            }
        }
        assert payment != null;
        Shippment shipment = payment.getShipment();
        if(shipment != null)
        {
            shipment.setDateCancel(LocalDateTime.now());
            shipment.setStatus(Status_Shipment.CANCELLED);
            shipmentRepository.save(shipment);

        }
        return ResponseEntity.status(HttpStatus.OK).body("Success");
    }

    @Transactional
    public ResponseEntity<?> rejectCancelReason(int orderId) {
        Orders orders = orderRepository.findByOrderIdAndIsDeletedFalse(orderId);
        if (orders == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
        if(orders.getIsCancelReason() != null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is accept or reject");
        }
        if (orders.getCancelReason() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not allow");
        }
        orders.setIsCancelReason(false);
        orders.setDateCanceled(LocalDateTime.now());
        orders.setStatus(Status_Order.CONFIRMED);
        orderRepository.save(orders);
        return ResponseEntity.ok("Success");
    }
}