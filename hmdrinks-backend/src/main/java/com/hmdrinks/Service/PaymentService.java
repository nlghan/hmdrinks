package com.hmdrinks.Service;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.*;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.CreatePaymentReq;
import com.hmdrinks.Request.CreatePaymentVNPayReq;
import com.hmdrinks.Request.InitPaymentRequest;
import com.hmdrinks.Response.CRUDPaymentResponse;
import com.hmdrinks.Response.CreatePaymentResponse;
import com.hmdrinks.Response.ListAllPaymentResponse;
import com.hmdrinks.SupportFunction.DistanceAndDuration;
import com.hmdrinks.SupportFunction.SupportFunction;
import jakarta.transaction.Transactional;
import org.apache.hadoop.shaded.com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    //payos
    private static final String clientId = "0e21da23-8871-45ef-8624-694417cf10eb";
    private static final String apiKey = "0ad492a1-cf68-446e-901d-d282171cbdd9";
    private static final String checksumKey = "465b9e56baff7a17688d4867b9ad117f9f0ccc95732f18971f9e94b72ffe9a2e";
    private static  final String webhookUrl = "https://cff4-171-232-181-39.ngrok-free.app/intermediary-page";

    //momo
    private final String accessKey = "F8BBA842ECF85";
    private final String secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private final String partnerCode = "MOMO";
    private final String redirectUrl = "https://cff4-171-232-181-39.ngrok-free.app/intermediary-page";
    private final String ipnUrl = "https://rightly-poetic-amoeba.ngrok-free.app/api/payment/callback";
    private final String requestType = "payWithMethod";
    private final boolean autoCapture = true;
    private final int orderExpireTime = 15;
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
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private SupportFunction supportFunction;

    private static Long generateRandomOrderCode() {
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000);
        return Long.valueOf(randomNumber);
    }



    public static LocalDateTime addDurationToCurrentTime(String duration, LocalDateTime currentTime) {
        int hours = 0;
        int minutes = 0;

        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now();

        if (currentTime.isBefore(now)) {
            return now.plusMinutes(20);
        }

        if (duration.contains("giờ")) {
            String[] parts = duration.split("giờ");
            hours = Integer.parseInt(parts[0].trim()); // Lấy số giờ
            if (parts.length > 1 && parts[1].contains("phút")) {
                minutes = Integer.parseInt(parts[1].replace("phút", "").trim()) + 25;
            }
        } else if (duration.contains("phút")) {
            minutes = Integer.parseInt(duration.replace("phút", "").trim()) + 25;
        }

        // Nếu không có giờ hoặc phút, thêm mặc định 25 phút
        if (hours == 0 && minutes == 0) {
            minutes += 25;
        }

        // Xử lý trường hợp số phút lớn hơn 60
        if (minutes >= 60) {
            hours += minutes / 60;
            minutes = minutes % 60;
        }

        return currentTime.plusHours(hours).plusMinutes(minutes);
    }


    @Transactional
    public boolean assignShipments(int orderId) {
        List<Shippment> pendingShipments = shipmentRepository.findByStatus(Status_Shipment.WAITING)
                .stream()
                .sorted(Comparator.comparing(Shippment::getDateCreated))
                .collect(Collectors.toList());

        Orders orders = orderRepository.findByOrderId(orderId);
        String placeId = supportFunction.getLocation(orders.getAddress());
        double[] destination = supportFunction.getCoordinates(placeId);
        double[] origin = {10.850575879000075, 106.77190192800003}; // Số 1-3 Võ Văn Ngân, Linh Chiểu, Thủ Đức, Tp HCM

        List<User> shippers = userRepository.findAllByRole(Role.SHIPPER);


        for (Shippment shipment : pendingShipments) {
            User selectedShipper = null;
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime now = currentTime;

            for (User shipper : shippers.stream()
                    .sorted(Comparator.comparingInt(shipper -> shipper.getShippments().size())) // Sắp xếp shipper theo số đơn
                    .collect(Collectors.toList())) {


                List<Shippment> recentShipments = shipper.getShippments().stream()
                        .filter(s -> s.getDateDelivered() != null &&
                                Duration.between(s.getDateDelivered(), now).toHours() < 1)
                        .collect(Collectors.toList());

                if (recentShipments.size() >= 5) {
                    continue;
                }

                double[] lastDestination = origin;
                if (!recentShipments.isEmpty()) {

                    Shippment lastShipment = recentShipments.get(recentShipments.size() - 1);
                    lastDestination = supportFunction.getCoordinates(
                            supportFunction.getLocation(lastShipment.getPayment().getOrder().getAddress()));

                    DistanceAndDuration distance = supportFunction.getShortestDistance(lastDestination, destination);
                    if (distance.getDistance() > 5) {
                        continue;
                    }

                    DistanceAndDuration lastToCurrent = supportFunction.getShortestDistance(lastDestination, destination);
                    String duration = lastToCurrent.getDuration();
                    currentTime = addDurationToCurrentTime(duration, lastShipment.getDateDelivered());

                } else {
                    DistanceAndDuration originToDestination = supportFunction.getShortestDistance(origin, destination);
                    if (originToDestination.getDistance() > 20) {
                        continue;
                    }

                    String duration = originToDestination.getDuration();
                    currentTime = addDurationToCurrentTime(duration, currentTime);
                }

                selectedShipper = shipper;
                break;
            }

            if (selectedShipper == null) {
                currentTime = null;
                return false;
            }

            shipment.setUser(selectedShipper);
            shipment.setStatus(Status_Shipment.SHIPPING);
            shipment.setDateDelivered(currentTime);
            shipmentRepository.save(shipment);
            selectedShipper.getShippments().add(shipment);
        }
        return true;
    }
    @Transactional
    public ResponseEntity<?> createPaymentMomo(int orderId1) {
        try {
            Payment payment1 = paymentRepository.findByOrderOrderIdAndIsDeletedFalse(orderId1);
            if (payment1 != null) {
                if (payment1.getPaymentMethod() == Payment_Method.CASH && payment1.getStatus() == Status_Payment.PENDING) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment create with type cash");
                }
                if (payment1.getStatus() == Status_Payment.COMPLETED || payment1.getStatus() == Status_Payment.PENDING) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already exists");
                }
            }
            Orders orders = orderRepository.findByOrderIdAndStatusAndIsDeletedFalse(orderId1, Status_Order.CONFIRMED);
            if (orders == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not confirmed");
            }
            Orders orders1 = orderRepository.findByOrderId(orderId1);
            if (orders1.getStatus() == Status_Order.CANCELLED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is cancelled");
            }
            String orderId = partnerCode + "-" + UUID.randomUUID();
            String requestId = partnerCode + "-" + UUID.randomUUID();
            Orders order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId1);
            User user = userRepository.findByUserIdAndIsDeletedFalse(order.getUser().getUserId());
            if(user == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
            }
            Double totalAmount = order.getTotalPrice() - order.getDiscountPrice() + order.getDeliveryFee();
            if(totalAmount <= 0.0)
            {
                    Payment payment = new Payment();
                    payment.setPaymentMethod(Payment_Method.CREDIT);
                    payment.setStatus(Status_Payment.COMPLETED);
                    payment.setOrder(order);
                    payment.setAmount(0.0);
                    payment.setDateCreated(LocalDateTime.now());
                    payment.setOrderIdPayment(orderId);
                    payment.setIsDeleted(false);
                    payment.setIsRefund(false);
                    paymentRepository.save(payment);

                Shippment shippment = new Shippment();
                shippment.setPayment(payment);
                shippment.setIsDeleted(false);
                shippment.setDateCreated(LocalDateTime.now());
                shippment.setDateDelivered(LocalDateTime.now().plusMinutes(25));
                shippment.setStatus(Status_Shipment.WAITING);
                shipmentRepository.save(shippment);

                Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
                List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

                for (CartItem cartItem : cartItems) {
                    ProductVariants productVariants = cartItem.getProductVariants();
                    if (productVariants.getStock() >= cartItem.getQuantity()) {
                        productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                        productVariantsRepository.save(productVariants);
                        if(productVariants.getStock() == 0)
                        {
                            List<CartItem> cartItemList = cartItemRepository.findByProductVariants_VarId(productVariants.getVarId());
                            for (CartItem cartItemList1 : cartItemList) {
                                Cart cart1 = cartItemList1.getCart();
                                if(cart1.getStatus() == Status_Cart.NEW)
                                {
                                    cartItemList1.setQuantity(0);
                                    cartItemList1.setNote("Hiện đang hết hàng");
                                    cartItemList1.setTotalPrice(0.0);
                                    cartItemRepository.save(cartItemList1);
                                }
                                List<CartItem> cartItemList2 = cartItemRepository.findByCart_CartId(cart1.getCartId());
                                double total = 0.0;
                                int total_quantity = 0;
                                for (CartItem cartItemList3 : cartItemList2) {
                                    total +=  cartItemList3.getTotalPrice();
                                    total_quantity += cartItemList3.getQuantity();
                                }
                                cart1.setTotalPrice(total);
                                cart1.setTotalProduct(total_quantity);
                                cartRepository.save(cart1);
                            }}
                    }
                }
                boolean status_assign = assignShipments(orderId1);
                String note = "";
                if(!status_assign)
                {
                    shippment.setDateDelivered(LocalDateTime.now().plusMinutes(30));
                    shipmentRepository.save(shippment);
                    note = "Hiện không thể giao hàng";

                }

                return new ResponseEntity<>(new CreatePaymentResponse(
                        payment.getPaymentId(),
                        payment.getAmount(),
                        payment.getDateCreated(),
                        payment.getDateDeleted(),
                        payment.getDateRefunded(),
                        payment.getIsDeleted(),
                        payment.getPaymentMethod(),
                        payment.getStatus(),
                        payment.getOrder().getOrderId(),
                        "",
                        note
                ), HttpStatus.OK);

            }
            Long totalAmountLong = totalAmount.longValue();
            String amount = totalAmountLong.toString();
            if (order.getDiscountPrice() > 0) {
                orderInfo = "Giảm giá: " + order.getDiscountPrice() + " VND";
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
                item.put("name", cartItem.getProductVariants().getProduct().getProName() + "- Size " + cartItem.getProductVariants().getSize());
                item.put("unit", cartItem.getProductVariants().getSize());
                item.put("quantity", cartItem.getQuantity());
                item.put("price", totalPrice);
                item.put("category", "beverage");
                item.put("manufacturer", "HMDrinks");
                items.add(item);
            }
            Map<String, Object> itemFee = new HashMap<>();
            itemFee.put("name","Phí giao hàng");
            itemFee.put("price",Math.round(order.getDeliveryFee()));
            itemFee.put("quantity", -1);
            itemFee.put("imageUrl","https://cdn.vectorstock.com/i/1000x1000/52/44/delivery-vector-30925244.webp");
            items.add(itemFee);

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
                payment.setIsRefund(false);
                payment.setLink(shortLink);
                paymentRepository.save(payment);
            }
            return new ResponseEntity<>(new CreatePaymentResponse(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    payment.getDateCreated(),
                    payment.getDateDeleted(),
                    payment.getDateRefunded(),
                    payment.getIsDeleted(),
                    payment.getPaymentMethod(),
                    payment.getStatus(),
                    payment.getOrder().getOrderId(),
                    shortLink,
                    ""
            ), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = Map.of("statusCode", 500, "message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> createPaymentATM(int orderId1) {
        try {
            Payment payment1 = paymentRepository.findByOrderOrderIdAndIsDeletedFalse(orderId1);
            if (payment1 != null) {
                if (payment1.getPaymentMethod() == Payment_Method.CASH && payment1.getStatus() == Status_Payment.PENDING) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment create with type cash");
                }
                if (payment1.getStatus() == Status_Payment.COMPLETED || payment1.getStatus() == Status_Payment.PENDING) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already exists");
                }
            }
            Orders orders = orderRepository.findByOrderIdAndStatusAndIsDeletedFalse(orderId1, Status_Order.CONFIRMED);
            if (orders == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not confirmed");
            }
            Orders orders1 = orderRepository.findByOrderId(orderId1);
            if (orders1.getStatus() == Status_Order.CANCELLED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is cancelled");
            }


            Orders order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId1);
            User user = userRepository.findByUserIdAndIsDeletedFalse(order.getUser().getUserId());
            if(user == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
            }
            Double totalAmount = order.getTotalPrice() - order.getDiscountPrice() + order.getDeliveryFee();
            if(totalAmount <= 0.0)
            {
                Payment payment = new Payment();
                payment.setPaymentMethod(Payment_Method.CREDIT);
                payment.setStatus(Status_Payment.COMPLETED);
                payment.setOrder(order);
                payment.setAmount(0.0);
                payment.setDateCreated(LocalDateTime.now());
                payment.setOrderIdPayment("None");
                payment.setIsDeleted(false);
                payment.setIsRefund(false);
                paymentRepository.save(payment);

                Shippment shippment = new Shippment();
                shippment.setPayment(payment);
                shippment.setIsDeleted(false);
                shippment.setDateCreated(LocalDateTime.now());
                shippment.setDateDelivered(LocalDateTime.now());
                shippment.setStatus(Status_Shipment.WAITING);
                shipmentRepository.save(shippment);

                Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
                List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

                for (CartItem cartItem : cartItems) {
                    ProductVariants productVariants = cartItem.getProductVariants();
                    if (productVariants.getStock() >= cartItem.getQuantity()) {
                        productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                        productVariantsRepository.save(productVariants);
                        if(productVariants.getStock() == 0)
                        {
                            List<CartItem> cartItemList = cartItemRepository.findByProductVariants_VarId(productVariants.getVarId());
                            for (CartItem cartItemList1 : cartItemList) {
                                Cart cart1 = cartItemList1.getCart();
                                if(cart1.getStatus() == Status_Cart.NEW)
                                {
                                    cartItemList1.setQuantity(0);
                                    cartItemList1.setNote("Hiện đang hết hàng");
                                    cartItemList1.setTotalPrice(0.0);
                                    cartItemRepository.save(cartItemList1);
                                }
                                List<CartItem> cartItemList2 = cartItemRepository.findByCart_CartId(cart1.getCartId());
                                double total = 0.0;
                                int total_quantity = 0;
                                for (CartItem cartItemList3 : cartItemList2) {
                                    total +=  cartItemList3.getTotalPrice();
                                    total_quantity += cartItemList3.getQuantity();
                                }
                                cart1.setTotalPrice(total);
                                cart1.setTotalProduct(total_quantity);
                                cartRepository.save(cart1);
                            }}
                    }
                }
                boolean status_assign = assignShipments(orderId1);
                String note = "";
                if(!status_assign)
                {
                    shippment.setDateDelivered(LocalDateTime.now().plusMinutes(30));
                    shipmentRepository.save(shippment);
                    note = "Hiện không thể giao hàng";
                }
                return new ResponseEntity<>(new CreatePaymentResponse(
                        payment.getPaymentId(),
                        payment.getAmount(),
                        payment.getDateCreated(),
                        payment.getDateDeleted(),
                        payment.getDateRefunded(),
                        payment.getIsDeleted(),
                        payment.getPaymentMethod(),
                        payment.getStatus(),
                        payment.getOrder().getOrderId(),
                        "",
                        note
                ), HttpStatus.OK);

            }
            int totalAmountLong = (int) totalAmount.longValue();
            OrderItem orderItem = order.getOrderItem();
            List<CartItem> cartItems = cartItemRepository.findByCart_CartId(orderItem.getCart().getCartId());
            List<ItemData> items = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                ProductVariants productVariants = cartItem.getProductVariants();
                ItemData itemData = ItemData.builder()
                        .name(productVariants.getProduct().getProName() + "-" + productVariants.getSize())
                        .quantity(cartItem.getQuantity())
                        .price((int) productVariants.getPrice())
                        .build();
                items.add(itemData);
            }
            int deliveryFee = (int) order.getDeliveryFee();
            PayOS payOS = new PayOS(clientId, apiKey, checksumKey);
            Long orderCode = generateRandomOrderCode();
            ItemData itemData = ItemData.builder()
                    .name("Phí giao hàng")
                    .quantity(1)
                    .price(deliveryFee)
                    .build();
            items.add(itemData);

            int discount = (int) order.getDiscountPrice();
            ItemData itemData1 = ItemData.builder()
                    .name("Giảm giá")
                    .quantity(1)
                    .price(discount)
                    .build();
            items.add(itemData1);

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(totalAmountLong)
                    .description("Thanh toán đơn hàng")
                    .returnUrl(webhookUrl)
                    .cancelUrl(webhookUrl)
                    .buyerAddress(order.getAddress())
                    .buyerEmail(user.getEmail())
                    .buyerName(user.getPhoneNumber())
                    .buyerName(user.getFullName())
                    .expiredAt((long) (System.currentTimeMillis() / 1000 + 15 * 60))
                    .items(items).build();

            CheckoutResponseData result = payOS.createPaymentLink(paymentData);
            String link = result.getCheckoutUrl();
            Payment payment = new Payment();
            if (result.getStatus().equals("PENDING")) {
                payment.setPaymentMethod(Payment_Method.CREDIT);
                payment.setStatus(Status_Payment.PENDING);
                payment.setOrder(order);
                payment.setAmount(totalAmount);
                payment.setDateCreated(LocalDateTime.now());
                payment.setOrderIdPayment("PayOS" + orderCode);
                payment.setIsDeleted(false);
                payment.setIsRefund(false);
                payment.setLink(link);
                paymentRepository.save(payment);
            }
            return new ResponseEntity<>(new CreatePaymentResponse(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    payment.getDateCreated(),
                    payment.getDateDeleted(),
                    payment.getDateRefunded(),
                    payment.getIsDeleted(),
                    payment.getPaymentMethod(),
                    payment.getStatus(),
                    payment.getOrder().getOrderId(),
                    link,
                    ""
            ), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = Map.of("statusCode", 500, "message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static String generateUniqueNumericString(int length) {
        String digits = "123456789";
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        while (result.length() < length) {
            char c = digits.charAt(random.nextInt(digits.length()));
            if (result.indexOf(String.valueOf(c)) == -1) {
                result.append(c);
            }
        }
        return result.toString();
    }
    @Transactional
    public ResponseEntity<?> createVNPay(CreatePaymentVNPayReq req)
    {
        int orderId1 = req.getOrderId();
        Payment payment1 = paymentRepository.findByOrderOrderIdAndIsDeletedFalse(orderId1);

        if (payment1 != null) {
            if (payment1.getPaymentMethod() == Payment_Method.CASH && payment1.getStatus() == Status_Payment.PENDING) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment cash already create");
            }
            if (payment1.getStatus() == Status_Payment.COMPLETED || payment1.getStatus() == Status_Payment.PENDING) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already exists");
            }
        }
        Orders orders = orderRepository.findByOrderIdAndStatusAndIsDeletedFalse(orderId1, Status_Order.CONFIRMED);
        if (orders == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not confirmed");
        }
        Orders orders1 = orderRepository.findByOrderId(orderId1);
        if (orders1.getStatus() == Status_Order.CANCELLED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is cancelled");
        }
        Orders order = orderRepository.findByOrderId(orderId1);
        User user = userRepository.findByUserId(order.getUser().getUserId());
        Double totalAmount = order.getTotalPrice() - order.getDiscountPrice() + order.getDeliveryFee();
        if(totalAmount <= 0.0)
        {
            Payment payment = new Payment();
            payment.setPaymentMethod(Payment_Method.CREDIT);
            payment.setStatus(Status_Payment.COMPLETED);
            payment.setOrder(order);
            payment.setAmount(0.0);
            payment.setDateCreated(LocalDateTime.now());
            payment.setOrderIdPayment("None");
            payment.setIsDeleted(false);
            payment.setIsRefund(false);
            paymentRepository.save(payment);

            Shippment shippment = new Shippment();
            shippment.setPayment(payment);
            shippment.setIsDeleted(false);
            shippment.setDateCreated(LocalDateTime.now());
            shippment.setDateDelivered(LocalDateTime.now());
            shippment.setStatus(Status_Shipment.WAITING);
            shipmentRepository.save(shippment);

            Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
            List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

            for (CartItem cartItem : cartItems) {
                ProductVariants productVariants = cartItem.getProductVariants();
                if (productVariants.getStock() >= cartItem.getQuantity()) {
                    productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                    productVariantsRepository.save(productVariants);
                    if(productVariants.getStock() == 0)
                    {
                        List<CartItem> cartItemList = cartItemRepository.findByProductVariants_VarId(productVariants.getVarId());
                        for (CartItem cartItemList1 : cartItemList) {
                            Cart cart1 = cartItemList1.getCart();
                            if(cart1.getStatus() == Status_Cart.NEW)
                            {
                                cartItemList1.setQuantity(0);
                                cartItemList1.setNote("Hiện đang hết hàng");
                                cartItemList1.setTotalPrice(0.0);
                                cartItemRepository.save(cartItemList1);
                            }
                            List<CartItem> cartItemList2 = cartItemRepository.findByCart_CartId(cart1.getCartId());
                            double total = 0.0;
                            int total_quantity = 0;
                            for (CartItem cartItemList3 : cartItemList2) {
                                total +=  cartItemList3.getTotalPrice();
                                total_quantity += cartItemList3.getQuantity();
                            }
                            cart1.setTotalPrice(total);
                            cart1.setTotalProduct(total_quantity);
                            cartRepository.save(cart1);
                        }}
                }
            }
            boolean status_assign = assignShipments(req.getOrderId());
            String note = "";
            if(!status_assign)
            {
                shippment.setDateDelivered(LocalDateTime.now().plusMinutes(30));
                shipmentRepository.save(shippment);
                note = "Hiện không thể giao hàng";
            }

            return new ResponseEntity<>(new CreatePaymentResponse(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    payment.getDateCreated(),
                    payment.getDateDeleted(),
                    payment.getDateRefunded(),
                    payment.getIsDeleted(),
                    payment.getPaymentMethod(),
                    payment.getStatus(),
                    payment.getOrder().getOrderId(),
                    "",
                    note
            ), HttpStatus.OK);

        }
        Long totalAmountLong = totalAmount.longValue();
        String orderId = partnerCode + "-" + UUID.randomUUID();
        Payment payment = new Payment();
        payment.setPaymentMethod(Payment_Method.CREDIT);
        payment.setStatus(Status_Payment.PENDING);
        payment.setOrder(order);
        payment.setAmount(totalAmount);
        payment.setDateCreated(LocalDateTime.now());
        payment.setOrderIdPayment(orderId);
        payment.setIsDeleted(false);
        payment.setIsRefund(false);
        paymentRepository.save(payment);
        String order_id = generateUniqueNumericString(5);
        var initPaymentRequest = InitPaymentRequest.builder()
                .userId(Long.valueOf(String.valueOf(user.getUserId())))
                .amount(totalAmountLong)
                .txnRef(order_id)
                .requestId(orderId)
                .ipAddress(req.getIpAddress())
                .build();
        payment.setOrderIdPayment(order_id);
        paymentRepository.save(payment);
        var initPaymentResponse = vnPayService.init(initPaymentRequest);
        payment.setLink(initPaymentResponse.getVnpUrl());
        paymentRepository.save(payment);
        return new ResponseEntity<>(new CreatePaymentResponse(
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getDateCreated(),
                payment.getDateDeleted(),
                payment.getDateRefunded(),
                payment.getIsDeleted(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getOrder().getOrderId(),
                initPaymentResponse.getVnpUrl(),
                ""
        ), HttpStatus.OK);
    }
    @Autowired
    private ZaloPayService zaloPayService;

    @Transactional
    public ResponseEntity<?> createZaloPay(CreatePaymentReq req) throws Exception {
        int orderId1 = req.getOrderId();
        Payment payment1 = paymentRepository.findByOrderOrderIdAndIsDeletedFalse(orderId1);
        if (payment1 != null) {
            if (payment1.getPaymentMethod() == Payment_Method.CASH && payment1.getStatus() == Status_Payment.PENDING) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment create with type cash");
            }
            if (payment1.getStatus() == Status_Payment.COMPLETED || payment1.getStatus() == Status_Payment.PENDING) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already exists");
            }
        }
        Orders orders = orderRepository.findByOrderIdAndStatusAndIsDeletedFalse(orderId1, Status_Order.CONFIRMED);
        if (orders == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not confirmed");
        }
        Orders orders1 = orderRepository.findByOrderId(orderId1);
        if (orders1.getStatus() == Status_Order.WAITING || orders1.getStatus() == Status_Order.CANCELLED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order cancelled");
        }
        Orders order = orderRepository.findByOrderId(orderId1);
        Double totalAmount = order.getTotalPrice() - order.getDiscountPrice() + order.getDeliveryFee();
        if(totalAmount <= 0.0)
        {
            Payment payment = new Payment();
            payment.setPaymentMethod(Payment_Method.CREDIT);
            payment.setStatus(Status_Payment.COMPLETED);
            payment.setOrder(order);
            payment.setAmount(0.0);
            payment.setDateCreated(LocalDateTime.now());
            payment.setOrderIdPayment("None");
            payment.setIsDeleted(false);
            payment.setIsRefund(false);
            paymentRepository.save(payment);

            Shippment shippment = new Shippment();
            shippment.setPayment(payment);
            shippment.setIsDeleted(false);
            shippment.setDateCreated(LocalDateTime.now());
            shippment.setDateDelivered(LocalDateTime.now());
            shippment.setStatus(Status_Shipment.WAITING);
            shipmentRepository.save(shippment);

            Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
            List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

            for (CartItem cartItem : cartItems) {
                ProductVariants productVariants = cartItem.getProductVariants();
                if (productVariants.getStock() >= cartItem.getQuantity()) {
                    productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                    productVariantsRepository.save(productVariants);
                    if(productVariants.getStock() == 0)
                    {
                        List<CartItem> cartItemList = cartItemRepository.findByProductVariants_VarId(productVariants.getVarId());
                        for (CartItem cartItemList1 : cartItemList) {
                            Cart cart1 = cartItemList1.getCart();
                            if(cart1.getStatus() == Status_Cart.NEW)
                            {
                                cartItemList1.setQuantity(0);
                                cartItemList1.setNote("Hiện đang hết hàng");
                                cartItemList1.setTotalPrice(0.0);
                                cartItemRepository.save(cartItemList1);
                            }
                            List<CartItem> cartItemList2 = cartItemRepository.findByCart_CartId(cart1.getCartId());
                            double total = 0.0;
                            int total_quantity = 0;
                            for (CartItem cartItemList3 : cartItemList2) {
                                total +=  cartItemList3.getTotalPrice();
                                total_quantity += cartItemList3.getQuantity();
                            }
                            cart1.setTotalPrice(total);
                            cart1.setTotalProduct(total_quantity);
                            cartRepository.save(cart1);
                        }}
                }
            }
            boolean status_assign = assignShipments(orderId1);
            String note = "";
            if(!status_assign)
            {
                shippment.setDateDelivered(LocalDateTime.now().plusMinutes(30));
                shipmentRepository.save(shippment);
                note = "Hiện không thể giao hàng";
            }

            return new ResponseEntity<>(new CreatePaymentResponse(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    payment.getDateCreated(),
                    payment.getDateDeleted(),
                    payment.getDateRefunded(),
                    payment.getIsDeleted(),
                    payment.getPaymentMethod(),
                    payment.getStatus(),
                    payment.getOrder().getOrderId(),
                    "",
                    note
            ), HttpStatus.OK);

        }
        Long totalAmountLong = totalAmount.longValue();
        String orderId = partnerCode + "-" + UUID.randomUUID();
        Payment payment = new Payment();
        payment.setPaymentMethod(Payment_Method.CREDIT);
        payment.setStatus(Status_Payment.PENDING);
        payment.setOrder(order);
        payment.setAmount(totalAmount);
        payment.setDateCreated(LocalDateTime.now());
        payment.setOrderIdPayment(orderId);
        payment.setIsDeleted(false);
        payment.setIsRefund(false);

        paymentRepository.save(payment);
        Map<String, Object> response = zaloPayService.createPayment(totalAmountLong);
        String orderUrl = (String) response.get("order_url");
        String appTransId = (String) response.get("app_trans_id");
        payment.setLink(orderUrl);
        payment.setOrderIdPayment(appTransId);
        paymentRepository.save(payment);
        return new ResponseEntity<>(new CreatePaymentResponse(
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getDateCreated(),
                payment.getDateDeleted(),
                payment.getDateRefunded(),
                payment.getIsDeleted(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getOrder().getOrderId(),
                orderUrl,
                ""
        ), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> callBack(String resultCode, String orderId) {
        Payment payment = paymentRepository.findByOrderIdPayment(orderId);
        if (payment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Not found payment"));
        }
        if (payment.getIsDeleted()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Payment is deleted"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("paymentId", payment.getPaymentId());

        if ("0".equals(resultCode)) {
            payment.setStatus(Status_Payment.COMPLETED);
            paymentRepository.save(payment);

            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
            List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

            for (CartItem cartItem : cartItems) {
                ProductVariants productVariants = cartItem.getProductVariants();
                if (productVariants.getStock() >= cartItem.getQuantity()) {
                    productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                    productVariantsRepository.save(productVariants);
                    if(productVariants.getStock() == 0)
                    {
                        List<CartItem> cartItemList = cartItemRepository.findByProductVariants_VarId(productVariants.getVarId());
                        for (CartItem cartItemList1 : cartItemList) {
                            Cart cart1 = cartItemList1.getCart();
                            if(cart1.getStatus() == Status_Cart.NEW)
                            {
                                cartItemList1.setQuantity(0);
                                cartItemList1.setNote("Hiện đang hết hàng");
                                cartItemList1.setTotalPrice(0.0);
                                cartItemRepository.save(cartItemList1);
                            }
                            List<CartItem> cartItemList2 = cartItemRepository.findByCart_CartId(cart1.getCartId());
                            double total = 0.0;
                            int total_quantity = 0;
                            for (CartItem cartItemList3 : cartItemList2) {
                                total +=  cartItemList3.getTotalPrice();
                                total_quantity += cartItemList3.getQuantity();
                            }
                            cart1.setTotalPrice(total);
                            cart1.setTotalProduct(total_quantity);
                            cartRepository.save(cart1);
                        }

                    }
                } else {
                    response.put("status", HttpStatus.BAD_REQUEST.value());
                    response.put("message", "Insufficient quantity");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
            Shippment shippment = new Shippment();
            shippment.setPayment(payment);
            shippment.setIsDeleted(false);
            shippment.setDateCreated(LocalDateTime.now());
            shippment.setDateDelivered(LocalDateTime.now());
            shippment.setStatus(Status_Shipment.WAITING);
            shipmentRepository.save(shippment);
            boolean status = assignShipments(shippment.getPayment().getOrder().getOrderId());
            if(!status)
            {
                shippment.setDateDelivered(LocalDateTime.now().plusMinutes(30));
                shipmentRepository.save(shippment);
            }

            response.put("status", HttpStatus.OK.value());
            response.put("message", "Payment completed successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            payment.setStatus(Status_Payment.FAILED);
            paymentRepository.save(payment);
            Orders orders = orderRepository.findByOrderId(payment.getOrder().getOrderId());
            orders.setStatus(Status_Order.CANCELLED);
            orders.setDateCanceled(LocalDateTime.now());
            orderRepository.save(orders);
            Voucher voucher = orders.getVoucher();
            if(voucher != null) {
                UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(orders.getUser().getUserId(), voucher.getVoucherId());
                userVoucher.setStatus(Status_UserVoucher.INACTIVE);
                userVoucherRepository.save(userVoucher);
            }

//            OrderItem orderItem1 = orders.getOrderItem();
//            if (orderItem1 != null) {
//                orderItemRepository.delete(orderItem1);
//                Cart cart = cartRepository.findByCartId(orderItem1.getCart().getCartId());
//                cart.setStatus(Status_Cart.NEW);
//                cartRepository.save(cart);
//            }

            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Payment failed");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ResponseEntity<?> getInformationPayOs(int paymentId) throws Exception {
        PayOS payOS = new PayOS(clientId, apiKey, checksumKey);
        Payment payment = paymentRepository.findByPaymentId(paymentId);
        if(payment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found payment");
        }
        String orderCode = payment.getOrderIdPayment();
        String extractedCode = orderCode.replace("PayOS", "");
        PaymentLinkData paymentLinkData = payOS.getPaymentLinkInformation(Long.valueOf(extractedCode));
        String status = paymentLinkData.getStatus();
        switch (status) {
            case "EXPIRED", "CANCELLED" -> {
                payment.setStatus(Status_Payment.FAILED);
                paymentRepository.save(payment);
            }
            case "PAID" -> {
                payment.setStatus(Status_Payment.COMPLETED);
                paymentRepository.save(payment);
            }
            default -> {
                payment.setStatus(Status_Payment.PENDING);
                paymentRepository.save(payment);
            }
        }
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
                payment.getIsRefund(),
                payment.getLink()
        );
        return ResponseEntity.status(HttpStatus.OK).body(crudPaymentResponse);
    }


    public ResponseEntity<?> checkStatusPayment(int paymentId) {
        Payment payment = paymentRepository.findByPaymentIdAndIsDeletedFalse(paymentId);
        if (payment == null) {
            return new ResponseEntity<>("Not found payment", HttpStatus.NOT_FOUND);
        }
        if(payment.getIsDeleted())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment is deleted");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDPaymentResponse(
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getDateCreated(),
                payment.getDateDeleted(),
                payment.getDateRefunded(),
                payment.getIsDeleted(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getOrder().getOrderId(),
                payment.getIsRefund(),
                payment.getLink()
        ));
    }

    @Transactional
    public ResponseEntity<?> createPaymentCash(int orderId) {
        Payment payment = paymentRepository.findByOrderOrderId(orderId);
        if (payment != null) {
            if(payment.getPaymentMethod() == Payment_Method.CASH)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment already exists");
            }
            if (payment.getPaymentMethod() == Payment_Method.CREDIT && payment.getStatus() == Status_Payment.PENDING) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment create with type credit");
            }
            if (payment.getStatus() == Status_Payment.COMPLETED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment already completed");
            }
        }
        Orders orders = orderRepository.findByOrderIdAndStatus(orderId, Status_Order.CONFIRMED);
        if (orders == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not confirmed");
        }
        Orders orders1 = orderRepository.findByOrderId(orderId);
        if (orders1.getStatus() == Status_Order.CANCELLED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is cancelled");
        }
        Orders order = orderRepository.findByOrderId(orderId);

        User user = userRepository.findByUserId(order.getUser().getUserId());
        if(user.getIsDeleted())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is deleted");
        }
        Double totalAmount = order.getTotalPrice() - order.getDiscountPrice()+ order.getDeliveryFee();
        if(totalAmount <= 0.0)
        {
            Payment payments = new Payment();
            payments.setPaymentMethod(Payment_Method.CASH);
            payments.setStatus(Status_Payment.COMPLETED);
            payments.setOrder(order);
            payments.setAmount(0.0);
            payments.setDateCreated(LocalDateTime.now());
            payments.setOrderIdPayment("None");
            payments.setIsDeleted(false);
            payments.setIsRefund(false);
            paymentRepository.save(payments);

            Shippment shippment = new Shippment();
            shippment.setPayment(payments);
            shippment.setIsDeleted(false);
            shippment.setDateCreated(LocalDateTime.now());
            shippment.setDateDelivered(LocalDateTime.now());
            shippment.setStatus(Status_Shipment.WAITING);
            shipmentRepository.save(shippment);

            Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
            List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

            for (CartItem cartItem : cartItems) {
                ProductVariants productVariants = cartItem.getProductVariants();
                if (productVariants.getStock() >= cartItem.getQuantity()) {
                    productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                    productVariantsRepository.save(productVariants);
                    if(productVariants.getStock() == 0)
                    {
                        List<CartItem> cartItemList = cartItemRepository.findByProductVariants_VarId(productVariants.getVarId());
                        for (CartItem cartItemList1 : cartItemList) {
                            Cart cart1 = cartItemList1.getCart();
                            if(cart1.getStatus() == Status_Cart.NEW)
                            {
                                cartItemList1.setQuantity(0);
                                cartItemList1.setNote("Hiện đang hết hàng");
                                cartItemList1.setTotalPrice(0.0);
                                cartItemRepository.save(cartItemList1);
                            }
                            List<CartItem> cartItemList2 = cartItemRepository.findByCart_CartId(cart1.getCartId());
                            double total = 0.0;
                            int total_quantity = 0;
                            for (CartItem cartItemList3 : cartItemList2) {
                                total +=  cartItemList3.getTotalPrice();
                                total_quantity += cartItemList3.getQuantity();
                            }
                            cart1.setTotalPrice(total);
                            cart1.setTotalProduct(total_quantity);
                            cartRepository.save(cart1);
                        }}
                }
            }
            boolean status_assign = assignShipments(orderId);
            String note = "";
            if(!status_assign)
            {
                shippment.setDateDelivered(LocalDateTime.now().plusMinutes(30));
                shipmentRepository.save(shippment);
                note = "Hiện không thể giao hàng";
            }

            return new ResponseEntity<>(new CreatePaymentResponse(
                    payments.getPaymentId(),
                    payments.getAmount(),
                    payments.getDateCreated(),
                    payments.getDateDeleted(),
                    payment.getDateRefunded(),
                    payments.getIsDeleted(),
                    payments.getPaymentMethod(),
                    payments.getStatus(),
                    payments.getOrder().getOrderId(),
                    "",
                    note
            ), HttpStatus.OK);

        }
        Payment payment1 = new Payment();
        payment1.setAmount(totalAmount);
        payment1.setPaymentMethod(Payment_Method.CASH);
        payment1.setStatus(Status_Payment.PENDING);
        payment1.setDateCreated(LocalDateTime.now());
        payment1.setLink("");
        payment1.setIsDeleted(false);
        payment1.setOrder(order);
        payment1.setIsRefund(false);
        paymentRepository.save(payment1);

        Shippment shippment = new Shippment();
        shippment.setPayment(payment1);
        shippment.setIsDeleted(false);
        shippment.setDateCreated(LocalDateTime.now());
        shippment.setDateDelivered(LocalDateTime.now());
        shippment.setStatus(Status_Shipment.WAITING);

        shipmentRepository.save(shippment);
        Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

        for (CartItem cartItem : cartItems) {
            ProductVariants productVariants = cartItem.getProductVariants();
            if (productVariants.getStock() >= cartItem.getQuantity()) {
                productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                productVariantsRepository.save(productVariants);
                if(productVariants.getStock() == 0)
                {
                    List<CartItem> cartItemList = cartItemRepository.findByProductVariants_VarId(productVariants.getVarId());
                    for (CartItem cartItemList1 : cartItemList) {
                        Cart cart1 = cartItemList1.getCart();
                        if(cart1.getStatus() == Status_Cart.NEW)
                        {
                            cartItemList1.setQuantity(0);
                            cartItemList1.setNote("Hiện đang hết hàng");
                            cartItemList1.setTotalPrice(0.0);
                            cartItemRepository.save(cartItemList1);
                        }
                        List<CartItem> cartItemList2 = cartItemRepository.findByCart_CartId(cart1.getCartId());
                        double total = 0.0;
                        int total_quantity = 0;
                        for (CartItem cartItemList3 : cartItemList2) {
                            total +=  cartItemList3.getTotalPrice();
                            total_quantity += cartItemList3.getQuantity();
                        }
                        cart1.setTotalPrice(total);
                        cart1.setTotalProduct(total_quantity);
                        cartRepository.save(cart1);
                    }}
            }
        }
        boolean status = assignShipments(orderId);
        if(!status)
        {
            shippment.setDateDelivered(LocalDateTime.now().plusMinutes(30));
            shipmentRepository.save(shippment);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDPaymentResponse(
                payment1.getPaymentId(),
                payment1.getAmount(),
                payment1.getDateCreated(),
                payment1.getDateDeleted(),
                payment1.getDateRefunded(),
                payment1.getIsDeleted(),
                payment1.getPaymentMethod(),
                payment1.getStatus(),
                payment1.getOrder().getOrderId(),
                payment1.getIsRefund(),
                payment1.getLink()

        ));
    }

    public ResponseEntity<?> getAllPayment(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Payment> payments = paymentRepository.findAllByIsDeletedFalse(pageable);
        List<Payment> payments1 = paymentRepository.findAllByIsDeletedFalse();
        List<CRUDPaymentResponse> responses = new ArrayList<>();
        for (Payment payment : payments) {
            responses.add(
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
                            payment.getIsRefund(),
                            payment.getLink()
                    )
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllPaymentResponse(
                page,
                payments.getTotalPages(),
                limit,
                payments1.size(),
                responses
        ));
    }

    public ResponseEntity<?> getAllPaymentStatus(String pageFromParam, String limitFromParam, Status_Payment statusPayment) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Payment> payments = paymentRepository.findAllByStatusAndIsDeletedFalse(statusPayment, pageable);
        List<Payment> payments1 = paymentRepository.findAllByStatusAndIsDeletedFalse(statusPayment);
        List<CRUDPaymentResponse> responses = new ArrayList<>();
        for (Payment payment : payments) {
            responses.add(
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
                            payment.getIsRefund(),
                            payment.getLink()
                    )
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllPaymentResponse(
                page,
                payments.getTotalPages(),
                limit,
                payments1.size(),
                responses
        ));
    }

    public ResponseEntity<?> getAllPaymentMethod(String pageFromParam, String limitFromParam, Payment_Method paymentMethod) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Payment> payments = paymentRepository.findAllByPaymentMethodAndIsDeletedFalse(paymentMethod, pageable);
        List<Payment> payments1 = paymentRepository.findAllByPaymentMethodAndIsDeletedFalse(paymentMethod);
        List<CRUDPaymentResponse> responses = new ArrayList<>();
        for (Payment payment : payments) {
            responses.add(
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
                            payment.getIsRefund(),
                            payment.getLink()
                    )
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllPaymentResponse(
                page,
                payments.getTotalPages(),
                limit,
                payments1.size(),
                responses
        ));
    }

    public ResponseEntity<?> getOnePayment(int paymentId){
        Payment payment = paymentRepository.findByPaymentIdAndIsDeletedFalse(paymentId);
        if(payment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDPaymentResponse(
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getDateCreated(),
                payment.getDateDeleted(),
                payment.getDateRefunded(),
                payment.getIsDeleted(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getOrder().getOrderId(),
                payment.getIsRefund(),
                payment.getLink()
        ));
    }

    @Transactional
    public ResponseEntity<?> handleCallBackPayOS(int orderCode) throws Exception {
        PayOS payOS = new PayOS(clientId, apiKey, checksumKey);
        List<Payment> payments = paymentRepository.findAll();
        String orderCode1="";
        for (Payment payment : payments) {
            String equal = "PayOS" + orderCode;
            System.out.println("PayOS" + orderCode);
            if(equal.equals(payment.getOrderIdPayment()))
            {
                orderCode1 = payment.getOrderIdPayment();
            }
        }

        if(orderCode1.equals(""))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found orderCode");
        }
        Payment payment = paymentRepository.findByOrderIdPayment(orderCode1);
        if(payment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found payment");
        }
        String extractedCode = orderCode1.replace("PayOS", "");
        PaymentLinkData paymentLinkData = payOS.getPaymentLinkInformation(Long.valueOf(extractedCode));
        String status = paymentLinkData.getStatus();
        switch (status) {
            case "EXPIRED", "CANCELLED" -> {
                payment.setStatus(Status_Payment.FAILED);
                paymentRepository.save(payment);
                Orders order = payment.getOrder();
                order.setDateCanceled(LocalDateTime.now());
                order.setStatus(Status_Order.CANCELLED);
                Voucher voucher = order.getVoucher();
                if(voucher != null) {
                    UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(order.getUser().getUserId(), voucher.getVoucherId());
                    userVoucher.setStatus(Status_UserVoucher.INACTIVE);
                    userVoucherRepository.save(userVoucher);
                }
            }
            case "PAID" -> {
                payment.setStatus(Status_Payment.COMPLETED);
                paymentRepository.save(payment);
                Shippment shippment = new Shippment();
                shippment.setPayment(payment);
                shippment.setIsDeleted(false);
                shippment.setDateCreated(LocalDateTime.now());
                shippment.setDateDelivered(LocalDateTime.now());
                shippment.setStatus(Status_Shipment.WAITING);
                shipmentRepository.save(shippment);

                Orders orders = payment.getOrder();
                Cart cart = cartRepository.findByCartId(orders.getOrderItem().getCart().getCartId());
                List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

                for (CartItem cartItem : cartItems) {
                    ProductVariants productVariants = cartItem.getProductVariants();
                    if (productVariants.getStock() >= cartItem.getQuantity()) {
                        productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                        productVariantsRepository.save(productVariants);
                        if(productVariants.getStock() == 0) {
                            List<CartItem> cartItemList = cartItemRepository.findByProductVariants_VarId(productVariants.getVarId());
                            for (CartItem cartItemList1 : cartItemList) {
                                Cart cart1 = cartItemList1.getCart();
                                if (cart1.getStatus() == Status_Cart.NEW) {
                                    cartItemList1.setQuantity(0);
                                    cartItemList1.setNote("Hiện đang hết hàng");
                                    cartItemList1.setTotalPrice(0.0);
                                    cartItemRepository.save(cartItemList1);
                                }
                                List<CartItem> cartItemList2 = cartItemRepository.findByCart_CartId(cart1.getCartId());
                                double total = 0.0;
                                int total_quantity = 0;
                                for (CartItem cartItemList3 : cartItemList2) {
                                    total += cartItemList3.getTotalPrice();
                                    total_quantity += cartItemList3.getQuantity();
                                }
                                cart1.setTotalPrice(total);
                                cart1.setTotalProduct(total_quantity);
                                cartRepository.save(cart1);
                            }
                        }
                    }

                }
                boolean status1 = assignShipments(shippment.getPayment().getOrder().getOrderId());
                if(!status1)
                {
                    shippment.setDateDelivered(LocalDateTime.now().plusMinutes(30));
                    shipmentRepository.save(shippment);
                }
            }
            default -> {
                payment.setStatus(Status_Payment.PENDING);
                paymentRepository.save(payment);
            }
        }
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
                payment.getIsRefund(),
                payment.getLink()
        );
        return ResponseEntity.status(HttpStatus.OK).body(crudPaymentResponse);
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

    @Transactional
    public  ResponseEntity<?> listAllPaymentRefund(String pageFromParam, String limitFromParam){
       int page = Integer.parseInt(pageFromParam);
       int limit = Integer.parseInt(limitFromParam);
       if (limit >= 100) limit = 100;
       Pageable pageable = PageRequest.of(page - 1, limit);
       Page<Payment> payments = paymentRepository.findAllByStatusAndIsDeletedFalse(Status_Payment.REFUND, pageable);
       List<Payment> payments1 = paymentRepository.findAllByStatusAndIsDeletedFalse(Status_Payment.REFUND);
       List<CRUDPaymentResponse> responses = new ArrayList<>();
       for (Payment payment : payments) {
           responses.add(
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
                           payment.getIsRefund(),
                           payment.getLink()
                   )
           );
       }
       return ResponseEntity.status(HttpStatus.OK).body(new ListAllPaymentResponse(
               page,
               payments.getTotalPages(),
               limit,
               payments1.size(),
               responses
       ));
   }

    @Transactional
    public ResponseEntity<?> activateRefund(int paymentId)
    {
       Payment payment = paymentRepository.findByPaymentIdAndIsDeletedFalse(paymentId);
       if(payment == null)
       {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found payment");
       }
       Shippment shippment = payment.getShipment();

       if(shippment.getStatus() == Status_Shipment.WAITING ||shippment.getStatus() == Status_Shipment.SHIPPING)
       {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not refund with shipment");
       }
       if(payment.getPaymentMethod() == Payment_Method.CASH)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not refund with payment method cash");
        }
       if(payment.getIsRefund())
       {
           return ResponseEntity.status(HttpStatus.CONFLICT).body("Refund exists");
       }
       payment.setIsRefund(true);
       payment.setDateRefunded(LocalDateTime.now());
       paymentRepository.save(payment);
       return ResponseEntity.status(HttpStatus.OK).body("Refund activated");
   }

    @Transactional
    public ResponseEntity<?> checkTimePayment(){
        List<Payment> paymentList = paymentRepository.findAllByPaymentMethodAndIsDeletedFalse(Payment_Method.CREDIT);

        for(Payment payment : paymentList)
        {
            Orders orders1 = payment.getOrder();
            if(orders1 != null)
            {
                if(orders1.getStatus() == Status_Order.WAITING || (orders1.getStatus() == Status_Order.CONFIRMED && orders1.getPayment() == null))
                {
                    if(orders1.getDateDeleted().plusMinutes(20).isBefore(LocalDateTime.now()))
                    {
                              orders1.setDateCanceled(LocalDateTime.now());
                              orders1.setStatus(Status_Order.CONFIRMED);
                    }
                }

            }
            if(payment.getStatus() != Status_Payment.PENDING)
            {
                continue;
            }
            if (payment.getDateCreated().plusMinutes(30).isBefore(LocalDateTime.now())) {
                payment.setStatus(Status_Payment.FAILED);
                paymentRepository.save(payment);
                Orders orders = payment.getOrder();
                orders.setDateCanceled(LocalDateTime.now());
                orders.setStatus(Status_Order.CANCELLED);
                orderRepository.save(orders);
                Voucher voucher = orders.getVoucher();
                if(voucher != null) {
                    UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(orders.getUser().getUserId(), voucher.getVoucherId());
                    userVoucher.setStatus(Status_UserVoucher.INACTIVE);
                    userVoucherRepository.save(userVoucher);
                }
            }
        }
        return ResponseEntity.ok().build();
    }

}
