package com.hmdrinks.Service;

import cats.kernel.Order;
import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.*;
import com.hmdrinks.Repository.*;
import com.hmdrinks.SupportFunction.DistanceAndDuration;
import com.hmdrinks.SupportFunction.SupportFunction;
import jakarta.transaction.Transactional;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ZaloPayService {
    // APP INFO
    private static final String APP_ID = "2553";
    private static final String KEY1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
    private static final String KEY2 = "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz";
    private static final String ENDPOINT_CREATE = "https://sb-openapi.zalopay.vn/v2/create";
    private static final String ENDPOINT_CHECK_STATUS = "https://sb-openapi.zalopay.vn/v2/query";
    private final PaymentRepository paymentRepository;
    @Autowired
    private ShipmentRepository shipmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductVariantsRepository productVariantsRepository;
    @Autowired
    private SupportFunction supportFunction;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserVoucherRepository userVoucherRepository;

    public ZaloPayService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
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
                return false;
            }

            shipment.setUser(selectedShipper);
            shipment.setStatus(Status_Shipment.SHIPPING);
            shipment.setDateDelivered(currentTime);
            shipmentRepository.save(shipment);

            selectedShipper.getShippments().add(shipment);
        }
        return  true;
    }

    public Map<String, Object> createPayment(Long total) throws Exception {

        String transId = String.valueOf(new Random().nextInt(1000000));
        Map<String, Object> order = new HashMap<>();
        order.put("app_id", APP_ID);
        order.put("app_trans_id", getCurrentTimeString("yyMMdd") + "_" + transId);
        order.put("app_user", "user123");
        order.put("app_time", System.currentTimeMillis());
        order.put("amount", total);
        order.put("description", "HMDrinks - Payment for the order #" + transId);
        order.put("bank_code", "");
        order.put("callback_url", "https://cff4-171-232-181-39.ngrok-free.app/payment-online-status/");
        order.put("embed_data", "{\"redirecturl\":\"http://localhost:5173/payment-online-status\"}");
        order.put("item", "[]");

        String data = APP_ID + "|" + order.get("app_trans_id") + "|" + order.get("app_user") + "|" +
                order.get("amount") + "|" + order.get("app_time") + "|" + order.get("embed_data") + "|" + order.get("item");

        // Tạo chữ ký `mac`
        String mac = generateHmacSHA256(data, KEY1);
        order.put("mac", mac);
        Map<String, Object> result = new HashMap<>();

        try {

            JSONObject response = sendRequest(ENDPOINT_CREATE, order);

            String payUrl = response.optString("order_url");
            result.put("order_url", payUrl);
            result.put("app_trans_id", (String) order.get("app_trans_id"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String getCurrentTimeString(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    private static String generateHmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    private static JSONObject sendRequest(String endpoint, Map<String, Object> order) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(endpoint);
        List<NameValuePair> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : order.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
        }

        post.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return new JSONObject(result.toString());
    }

    public static String hmacSHA256(String key, String data) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKey);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static boolean checkStatusOrder(String appTransId) {
        try {
            // Tạo dữ liệu chữ ký MAC
            String data = APP_ID + "|" + appTransId + "|" + KEY1;
            String mac = hmacSHA256(KEY1, data);
            String postData = "app_id=" + APP_ID + "&app_trans_id=" + appTransId + "&mac=" + mac;

            // Thiết lập kết nối HTTP
            URL url = new URL(ENDPOINT_CHECK_STATUS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);


            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                return response.contains("\"return_code\":1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Transactional
    public ResponseEntity<?> handleCallBack(String appTransId) {
        Payment payment = paymentRepository.findByOrderIdPayment(appTransId);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> response = new HashMap<>();
        boolean isSuccess = checkStatusOrder(appTransId);
        System.out.println(isSuccess);
        if (isSuccess != false) {
            if (payment.getStatus() != Status_Payment.FAILED || payment.getStatus() != Status_Payment.COMPLETED) {
                payment.setStatus(Status_Payment.COMPLETED);
                paymentRepository.save(payment);
                Shippment shippment = new Shippment();
                shippment.setPayment(payment);
                shippment.setIsDeleted(false);
                shippment.setDateCreated(LocalDateTime.now());
                shippment.setDateDelivered(LocalDateTime.now());
                shippment.setStatus(Status_Shipment.WAITING);
                shipmentRepository.save(shippment);

                // - sp
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
                boolean status_assign = assignShipments(orders.getOrderId());
                String note = "";
                if(!status_assign)
                {
                    shippment.setDateDelivered(LocalDateTime.now().plusMinutes(30));
                    shipmentRepository.save(shippment);
                    note = "Hiện không thể giao hàng";
                }

                response.put("status", 1);
                response.put("note",note);
            } else {
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
                response.put("status", 0);
            }

        }
        else
        {
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
            response.put("status", 0);
        }
        return ResponseEntity.ok().body(response);
    }

}
