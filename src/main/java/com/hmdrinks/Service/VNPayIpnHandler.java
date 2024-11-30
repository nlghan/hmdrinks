package com.hmdrinks.Service;


import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.Status_Payment;
import com.hmdrinks.Enum.Status_Shipment;
import com.hmdrinks.Exception.BusinessException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Response.IpnResponse;
import com.hmdrinks.SupportFunction.DistanceAndDuration;
import com.hmdrinks.SupportFunction.SupportFunction;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private UserRepository userRepository;
    @Autowired
    private SupportFunction supportFunction;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductVariantsRepository productVariantsRepository;

    public static class VnpIpnResponseConst {
        public static final IpnResponse SUCCESS = new IpnResponse("00", "Successful");
        public static final IpnResponse SIGNATURE_FAILED = new IpnResponse("97", "Signature failed");
        public static final IpnResponse ORDER_NOT_FOUND = new IpnResponse("01", "Order not found");
        public static final IpnResponse UNKNOWN_ERROR = new IpnResponse("99", "Unknown error");
    }
    public static LocalDateTime addDurationToCurrentTime(String duration, LocalDateTime currentTime) {
        int hours = 0;
        int minutes = 0;

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
    public void assignShipments(int orderId) {
        List<Shippment> pendingShipments = shipmentRepository.findByStatus(Status_Shipment.WAITING)
                .stream()
                .sorted(Comparator.comparing(Shippment::getDateCreated))
                .collect(Collectors.toList());

        Orders orders = orderRepository.findByOrderId(orderId);
        String placeId = supportFunction.getLocation(orders.getAddress());
        double[] destination = supportFunction.getCoordinates(placeId);
        double[] origin = {10.850575879000075, 106.77190192800003}; // Số 1-3 Võ Văn Ngân, Linh Chiểu, Thủ Đức, Tp HCM

        List<User> shippers = userRepository.findAllByRole(Role.SHIPPER);

        // Debugging: Kiểm tra xem có bao nhiêu shipper
        System.out.println("Số lượng shipper: " + shippers.size());
        for (User shipper : shippers) {
            System.out.println("ShipperId: " + shipper.getUserId());
        }

        // Duyệt qua các đơn hàng chờ để phân phối
        for (Shippment shipment : pendingShipments) {
            User selectedShipper = null;
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime now = currentTime;

            // Duyệt qua các shipper và tìm shipper có ít đơn nhất thỏa mãn điều kiện
            for (User shipper : shippers.stream()
                    .sorted(Comparator.comparingInt(shipper -> shipper.getShippments().size())) // Sắp xếp shipper theo số đơn
                    .collect(Collectors.toList())) {

                System.out.println("ShipperId dang check:" + shipper.getUserId()); // Debugging: In shipper đang kiểm tra

                // Lọc các đơn đã giao trong vòng 1 giờ gần nhất của shipper
                List<Shippment> recentShipments = shipper.getShippments().stream()
                        .filter(s -> s.getDateDelivered() != null &&
                                Duration.between(s.getDateDelivered(), now).toHours() < 1)
                        .collect(Collectors.toList());

                // Kiểm tra số lượng đơn hàng đã giao của shipper trong vòng 1 giờ
                if (recentShipments.size() >= 5) {
                    continue; // Nếu shipper đã nhận quá 5 đơn trong 1 giờ, bỏ qua shipper này
                }

                double[] lastDestination = origin; // Nếu chưa có đơn, lấy điểm gốc làm điểm xuất phát
                if (!recentShipments.isEmpty()) {
                    // Lấy đơn cuối cùng trong danh sách recentShipments của shipper
                    Shippment lastShipment = recentShipments.get(recentShipments.size() - 1);
                    lastDestination = supportFunction.getCoordinates(
                            supportFunction.getLocation(lastShipment.getPayment().getOrder().getAddress()));

                    // Kiểm tra khoảng cách từ đơn cuối cùng đến điểm giao mới
                    DistanceAndDuration distance = supportFunction.getShortestDistance(lastDestination, destination);
                    if (distance.getDistance() > 5) {
                        continue; // Nếu khoảng cách quá xa, bỏ qua shipper này
                    }

                    // Tính thời gian giao mới dựa trên đơn cuối cùng
                    DistanceAndDuration lastToCurrent = supportFunction.getShortestDistance(lastDestination, destination);
                    String duration = lastToCurrent.getDuration();
                    currentTime = addDurationToCurrentTime(duration, lastShipment.getDateDelivered());
                } else {
                    // Nếu không có đơn trước đó, kiểm tra khoảng cách từ origin đến destination
                    DistanceAndDuration originToDestination = supportFunction.getShortestDistance(origin, destination);
                    if (originToDestination.getDistance() > 25) {
                        continue;
                    }

                    String duration = originToDestination.getDuration();
                    currentTime = addDurationToCurrentTime(duration, currentTime);
                }

                // Nếu shipper này thỏa mãn tất cả điều kiện, gán đơn
                selectedShipper = shipper;
                break; // Dừng vòng lặp khi đã tìm thấy shipper phù hợp
            }

            // Nếu không tìm thấy shipper phù hợp
            if (selectedShipper == null) {
                System.out.println("Không tìm thấy shipper phù hợp");
                return;
            }

            // Gán đơn cho shipper
            shipment.setUser(selectedShipper);
            shipment.setStatus(Status_Shipment.SHIPPING);
            shipment.setDateDelivered(currentTime);
            shipmentRepository.save(shipment);

            // Cập nhật lại danh sách đơn hàng của shipper
            selectedShipper.getShippments().add(shipment);
        }
    }

    @Transactional
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

                Orders orders = payment.getOrder();
                Cart cart = orders.getOrderItem().getCart();
                List<CartItem> cartItems = cart.getCartItems();

                for (CartItem cartItem : cartItems) {
                    ProductVariants productVariants = cartItem.getProductVariants();
                    if (productVariants.getStock() >= cartItem.getQuantity()) {
                        productVariants.setStock(productVariants.getStock() - cartItem.getQuantity());
                        productVariantsRepository.save(productVariants);
                    }
                }
                assignShipments(orders.getOrderId());
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