package com.hmdrinks.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.*;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Response.CRUDPaymentResponse;
import com.hmdrinks.Response.CreatePaymentResponse;
import com.hmdrinks.Response.ListAllPaymentResponse;
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
public class PaymentService {

    private final String accessKey = "F8BBA842ECF85";
    private final String secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private final String partnerCode = "MOMO";
    private final String redirectUrl = "https://a7f2-113-22-7-90.ngrok-free.app/api/payment/callback";
    private final String ipnUrl = "https://a7f2-113-22-7-90.ngrok-free.app/api/payment/callback";
    private final String requestType = "payWithMethod";
    private final boolean autoCapture = true;
    private final int orderExpireTime = 30;
    private final String lang = "vi";
    private String orderInfo = "Payment Order";
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

    public ResponseEntity<?> createPayment(int orderId1) {
        try {
            Payment payment1 = paymentRepository.findByOrderOrderId(orderId1);
            if (payment1 != null) {
                if (payment1.getPaymentMethod() == Payment_Method.CASH && payment1.getStatus() == Status_Payment.PENDING) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request");
                }
                if (payment1.getStatus() == Status_Payment.COMPLETED || payment1.getStatus() == Status_Payment.PENDING) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already exists");
                }
            }
            Orders orders = orderRepository.findByOrderIdAndStatus(orderId1, Status_Order.CONFIRMED);
            if (orders == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order NOT CONFIRMED");
            }
            Orders orders1 = orderRepository.findByOrderId(orderId1);
            if (orders1.getStatus() == Status_Order.WAITING || orders1.getStatus() == Status_Order.CANCELLED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request");
            }
            String orderId = partnerCode + "-" + UUID.randomUUID();
            String requestId = partnerCode + "-" + UUID.randomUUID();
            Orders order = orderRepository.findByOrderId(orderId1);
            User user = userRepository.findByUserId(order.getUser().getUserId());
            Double totalAmount = order.getTotalPrice() - order.getDiscountPrice();
            Long totalAmountLong = totalAmount.longValue();
            String amount = totalAmountLong.toString();
            if (order.getDiscountPrice() > 0) {
                orderInfo = "Giam gia: " + order.getDiscountPrice() + " VND";
            }
            String rawSignature = String.format(
                    "accessKey=%s&amount=%s&extraData=&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                    accessKey, amount, ipnUrl, orderId, orderInfo, partnerCode, redirectUrl, requestId, requestType
            );

            String signature = hmacSHA256(secretKey, rawSignature);
            JSONObject userInfo = new JSONObject();
            userInfo.put("phoneNumber", user.getPhoneNumber());
            userInfo.put("email", user.getEmail());
            userInfo.put("name", user.getFullName());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("partnerName", "TEST");
            requestBody.put("storeId", "MomoTestStore");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("lang", lang);
            requestBody.put("requestType", requestType);
            requestBody.put("autoCapture", autoCapture);
            requestBody.put("extraData", "");
            requestBody.put("userInfo", userInfo);
            requestBody.put("signature", signature);
            requestBody.put("orderExpireTime", orderExpireTime);
            List<CartItem> cartItems = cartItemRepository.findByCart_CartId(order.getOrderItem().getCart().getCartId());
            List<Map<String, Object>> items = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                Map<String, Object> item = new HashMap<>();
                String listProImg = cartItem.getProductVariants().getProduct().getListProImg();
                String imageUrl = "";
                if (listProImg != null && !listProImg.isEmpty()) {
                    String[] images = listProImg.split(", ");
                    imageUrl = images.length > 0 ? images[0] : "";
                }
                String firstImage = imageUrl.split(",")[0];
                int colonIndex = firstImage.indexOf(":");
                String imageUrl1 = (colonIndex != -1) ? firstImage.substring(colonIndex + 1).trim() : "";
                Long totalPrice = Math.round(cartItem.getTotalPrice());
                item.put("imageUrl", imageUrl1);
                item.put("name", cartItem.getProductVariants().getProduct().getProName());
                item.put("unit", cartItem.getProductVariants().getSize());
                item.put("quantity", cartItem.getQuantity());
                item.put("price", totalPrice);
                item.put("category", "beverage");
                item.put("manufacturer", "HMDrinks");
                items.add(item);
            }
            requestBody.put("items", items);

            URL url = new URL("https://test-payment.momo.vn/v2/gateway/api/create");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            conn.getOutputStream().write(mapper.writeValueAsBytes(requestBody));

            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            Map<String, Object> responseBody = mapper.readValue(response.toString(), HashMap.class);
            int statusCode = (Integer) responseBody.get("resultCode");
            String shortLink = (String) responseBody.get("shortLink");
            Payment payment = new Payment();
            if (statusCode == 0) {
                payment.setPaymentMethod(Payment_Method.CREDIT);
                payment.setStatus(Status_Payment.PENDING);
                payment.setOrder(order);
                payment.setAmount(totalAmount);
                payment.setDateCreated(LocalDateTime.now());
                payment.setOrderIdPayment(orderId);
                payment.setIsDeleted(false);
                paymentRepository.save(payment);
            }
            return new ResponseEntity<>(new CreatePaymentResponse(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    payment.getDateCreated(),
                    payment.getDateDeleted(),
                    payment.getIsDeleted(),
                    payment.getPaymentMethod(),
                    payment.getStatus(),
                    payment.getOrder().getOrderId(),
                    shortLink
            ), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = Map.of("statusCode", 500, "message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> callBack(String resultCode, String orderId) {
        Payment payment = paymentRepository.findByOrderIdPayment(orderId);
        if (payment == null) {
            return new ResponseEntity<>("Not found payment", HttpStatus.NOT_FOUND);
        }
        if ("0".equals(resultCode)) {
            payment.setStatus(Status_Payment.COMPLETED);
            paymentRepository.save(payment);
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
            List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
            for (CartItem cartItem : cartItems) {
                ProductVariants productVariants = cartItem.getProductVariants();
                if (productVariants.getStock() > cartItem.getQuantity()) {
                    productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                    productVariantsRepository.save(productVariants);
                } else {
                    String redirectUrl = "https://www.facebook.com"; // Địa chỉ trang bạn muốn redirect
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Location", redirectUrl + "?status=" + 404 + "&paymentId=" + payment.getPaymentId() + "&text=" + "insufficient quantity");
                    return new ResponseEntity<>("Redirecting...", headers, HttpStatus.FOUND);
                }
            }

            Shippment shippment = new Shippment();
            shippment.setPayment(payment);
            shippment.setIsDeleted(false);
            shippment.setDateCreated(LocalDateTime.now());
            shippment.setDateDelivered(LocalDateTime.now());
            shippment.setStatus(Status_Shipment.WAITING);
            shipmentRepository.save(shippment);

            String redirectUrl = "https://www.facebook.com"; // Địa chỉ trang bạn muốn redirect
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", redirectUrl + "?status=" + 200 + "&paymentId=" + payment.getPaymentId());
            return new ResponseEntity<>("Redirecting...", headers, HttpStatus.FOUND);
        } else {
            payment.setStatus(Status_Payment.FAILED);
            paymentRepository.save(payment);
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            orders.setStatus(Status_Order.CANCELLED);
            orderRepository.save(orders);
            UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(
                    orders.getUser().getUserId(), orders.getVoucher().getVoucherId()
            );
            userVoucher.setStatus(Status_UserVoucher.INACTIVE);
            userVoucherRepository.save(userVoucher);
            OrderItem orderItem1 = orders.getOrderItem();
            if (orderItem1 != null) {
                orderItemRepository.delete(orders.getOrderItem());
                Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
                cart.setStatus(Status_Cart.NEW);
                cartRepository.save(cart);
            }
            String redirectUrl = "https://www.facebook.com";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", redirectUrl + "?status=" + 400 + "&paymentId=" + payment.getPaymentId());
            return new ResponseEntity<>("Redirecting...", headers, HttpStatus.FOUND);
        }
    }

    public ResponseEntity<?> checkStatusPayment(int paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId);
        if (payment == null) {
            return new ResponseEntity<>("Not found payment", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDPaymentResponse(
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getDateCreated(),
                payment.getDateDeleted(),
                payment.getIsDeleted(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getOrder().getOrderId()
        ));
    }

    public ResponseEntity<?> createPaymentCash(int orderId) {
        Payment payment = paymentRepository.findByOrderOrderId(orderId);
        if (payment != null) {
            if (payment.getPaymentMethod() == Payment_Method.CREDIT && payment.getStatus() == Status_Payment.PENDING) {
                return ResponseEntity.status(HttpStatus.OK).body("Bad request");
            }
            if (payment.getStatus() == Status_Payment.COMPLETED) {
                return ResponseEntity.status(HttpStatus.OK).body("Payment already completed");
            }
        }
        Orders orders = orderRepository.findByOrderIdAndStatus(orderId, Status_Order.CONFIRMED);
        if (orders == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order NOT CONFIRMED");
        }
        Orders orders1 = orderRepository.findByOrderId(orderId);
        if (orders1.getStatus() == Status_Order.WAITING || orders1.getStatus() == Status_Order.CANCELLED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request");
        }
        Orders order = orderRepository.findByOrderId(orderId);
        User user = userRepository.findByUserId(order.getUser().getUserId());
        Double totalAmount = order.getTotalPrice() - order.getDiscountPrice();
        Payment payment1 = new Payment();
        payment1.setAmount(totalAmount);
        payment1.setPaymentMethod(Payment_Method.CASH);
        payment1.setStatus(Status_Payment.PENDING);
        payment1.setDateCreated(LocalDateTime.now());
        payment1.setIsDeleted(false);
        payment1.setOrder(order);
        paymentRepository.save(payment1);

        Shippment shippment = new Shippment();
        shippment.setPayment(payment1);
        shippment.setIsDeleted(false);
        shippment.setDateCreated(LocalDateTime.now());
        shippment.setDateDelivered(LocalDateTime.now());
        shippment.setStatus(Status_Shipment.WAITING);
        shipmentRepository.save(shippment);

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDPaymentResponse(
                payment1.getPaymentId(),
                payment1.getAmount(),
                payment1.getDateCreated(),
                payment1.getDateDeleted(),
                payment1.getIsDeleted(),
                payment1.getPaymentMethod(),
                payment1.getStatus(),
                payment1.getOrder().getOrderId()
        ));
    }

    public ResponseEntity<?> getAllPayment(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Payment> payments = paymentRepository.findAll(pageable);
        List<CRUDPaymentResponse> responses = new ArrayList<>();
        for (Payment payment : payments) {
            responses.add(
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
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllPaymentResponse(
                page,
                payments.getTotalPages(),
                limit,
                responses
        ));
    }

    public ResponseEntity<?> getAllPaymentStatus(String pageFromParam, String limitFromParam, Status_Payment statusPayment) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Payment> payments = paymentRepository.findAllByStatus(statusPayment, pageable);
        List<CRUDPaymentResponse> responses = new ArrayList<>();
        for (Payment payment : payments) {
            responses.add(
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
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllPaymentResponse(
                page,
                payments.getTotalPages(),
                limit,
                responses
        ));
    }

    public ResponseEntity<?> getAllPaymentMethod(String pageFromParam, String limitFromParam, Payment_Method paymentMethod) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Payment> payments = paymentRepository.findAllByPaymentMethod(paymentMethod, pageable);
        List<CRUDPaymentResponse> responses = new ArrayList<>();
        for (Payment payment : payments) {
            responses.add(
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
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllPaymentResponse(
                page,
                payments.getTotalPages(),
                limit,
                responses
        ));
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hmacData) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}