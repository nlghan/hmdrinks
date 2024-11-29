package com.hmdrinks.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.Status_Payment;
import com.hmdrinks.Enum.Status_Shipment;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Service.crypto.HMACUtil;
import com.hmdrinks.SupportFunction.DistanceAndDuration;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.apache.hadoop.shaded.com.squareup.okhttp.OkHttpClient;
import org.apache.hadoop.shaded.com.squareup.okhttp.Request;
import org.apache.hadoop.shaded.com.squareup.okhttp.Response;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public ZaloPayService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public static LocalDateTime addDurationToCurrentTime(String duration, LocalDateTime currentTime) {
        int hours = 0;
        int minutes = 0;

        // Phân tích chuỗi "3 giờ 4 phút"
        if (duration.contains("giờ")) {
            String[] parts = duration.split("giờ");
            hours = Integer.parseInt(parts[0].trim()); // Lấy số giờ
            if (parts.length > 1 && parts[1].contains("phút")) {
                minutes = Integer.parseInt(parts[1].replace("phút", "").trim()) + 10; // Lấy số phút
            }
        } else if (duration.contains("phút")) {
            minutes = Integer.parseInt(duration.replace("phút", "").trim());
        }

        return currentTime.plusHours(hours).plusMinutes(minutes);
    }

    @Transactional
    public  void assignShipments(int orderId) {
        List<Shippment> pendingShipments = shipmentRepository.findByStatus(Status_Shipment.WAITING)
                .stream()
                .sorted(Comparator.comparing(Shippment::getDateCreated))
                .collect(Collectors.toList());
        Orders orders = orderRepository.findByOrderId(orderId);
        String place_id = supportFunction.getLocation(orders.getAddress());
        double[] destinations = supportFunction.getCoordinates(place_id);
        double[] origins = {10.850575879000075, 106.77190192800003};  // Số 1-3 Võ Văn Ngân,Linh Chiểu, Thủ Đức, Tp HCM
        DistanceAndDuration distanceAndDuration = supportFunction.getShortestDistance(origins, destinations);
        String minute = distanceAndDuration.getDuration();
        LocalDateTime currentTime = LocalDateTime.now(); // Thời gian hiện tại
        LocalDateTime updatedTime = addDurationToCurrentTime(minute, currentTime);

        List<User> shippers = userRepository.findAllByRole(Role.SHIPPER);
        for (Shippment shipment : pendingShipments) {

            User selectedShipper = shippers.stream()
                    .min(Comparator.comparingInt(shipper -> shipper.getShippments().size()))
                    .orElse(null);
            if (selectedShipper != null) {
                shipment.setUser(selectedShipper);
                shipment.setStatus(Status_Shipment.SHIPPING);
                shipment.setDateDelivered(updatedTime);
                shipmentRepository.save(shipment);

            }
        }
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
        order.put("callback_url", "https://e017-14-186-74-32.ngrok-free.app/payment-online-status/");
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
        Map<String, Integer> response = new HashMap<>();
        boolean isSuccess = checkStatusOrder(appTransId);
        if (isSuccess) {
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
                    if (productVariants.getStock() > cartItem.getQuantity()) {
                        productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                        productVariantsRepository.save(productVariants);
                    }

                }

                assignShipments(orders.getOrderId());

                response.put("status", 1);
            } else {
                payment.setStatus(Status_Payment.FAILED);
                paymentRepository.save(payment);
                response.put("status", 0);
            }

        }
        return ResponseEntity.ok().body(response);
    }

}