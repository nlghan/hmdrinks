package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.OrdersController;
import com.hmdrinks.Controller.PostController;
import com.hmdrinks.Enum.*;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.*;
import com.hmdrinks.SupportFunction.SupportFunction;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebAppConfiguration
@WebMvcTest(OrdersController.class)
class OrderControllerTest {
    private static final String endPointPath="/api/orders";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private ProductRepository productRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;
    @MockBean
    private GenerateInvoiceService generateInvoiceService;
    @MockBean
    private  PostService postService;
    @MockBean
    private OrdersService ordersService;

    @MockBean
    private CartItemService cartItemService;

    @MockBean
    private UserService userService;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private  ProductService productService;
    @MockBean
    private ProductVarService productVarService;
    @MockBean
    private SupportFunction supportFunction;
    @MockBean
    private CategoryRepository categoryRepository;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserInfoService myUserDetailsService;
    @MockBean
    private TokenRepository tokenRepository;
    @MockBean
    private PriceHistoryRepository priceHistoryRepository;
    @MockBean
    private CartService cartService;
    @MockBean
    private ReviewRepository reviewRepository;
    @MockBean
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createOrder_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateOrdersReq req = new CreateOrdersReq(
                1,
                1,
                "1",
                "None"
        );

        CreateOrdersResponse createOrdersResponse = new CreateOrdersResponse(
                1,
                "99 đường số 7, Linh Trung, Thủ Đức, Hồ Chí Minh",
                10000.0,
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                5000.0,
                false,
                "",
                LocalDateTime.now(),
                "0769674810",
                Status_Order.WAITING,
                95000.0,
                1,
                1
        );

        when(ordersService.addOrder(any(CreateOrdersReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(createOrdersResponse));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(200))
                .andExpect(jsonPath("$.body.discountPrice").value(5000.0))
                .andExpect(jsonPath("$.body.userId").value(1))
                .andDo(print());
    }

    @Test
    void createOrder_NotFoundUser() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateOrdersReq req = new CreateOrdersReq(
                1,
                1,
                "1",
                "None"
        );
        when(ordersService.addOrder(any(CreateOrdersReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(404))
                .andExpect(jsonPath("$.body").value("Not found user"))
                .andDo(print());
    }

    @Test
    void createOrder_NotFoundCart() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateOrdersReq req = new CreateOrdersReq(
                1,
                1,
                "1",
                "None"
        );
        when(ordersService.addOrder(any(CreateOrdersReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found cart"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(404))
                .andExpect(jsonPath("$.body").value("Not found cart"))
                .andDo(print());
    }

    @Test
    void createOrder_NotAllowCart() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateOrdersReq req = new CreateOrdersReq(
                1,
                1,
                "1",
                "None"
        );
        when(ordersService.addOrder(any(CreateOrdersReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Not allowed to add order"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(406))
                .andExpect(jsonPath("$.body").value("Not allowed to add order"))
                .andDo(print());
    }

    @Test
    void createOrder_NotFoundUserVoucher() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateOrdersReq req = new CreateOrdersReq(
                1,
                1,
                "1",
                "None"
        );
        when(ordersService.addOrder(any(CreateOrdersReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found userVoucher"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(404))
                .andExpect(jsonPath("$.body").value("Not found userVoucher"))
                .andDo(print());
    }

    @Test
    void createOrder_VoucherAlreadyUse() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateOrdersReq req = new CreateOrdersReq(
                1,
                1,
                "1",
                "None"
        );
        when(ordersService.addOrder(any(CreateOrdersReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher already in use"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Voucher already in use"))
                .andDo(print());
    }

    @Test
    void createOrder_VoucherIsDelete() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateOrdersReq req = new CreateOrdersReq(
                1,
                1,
                "1",
                "None"
        );
        when(ordersService.addOrder(any(CreateOrdersReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher is deleted"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Voucher is deleted"))
                .andDo(print());
    }

    @Test
    void createOrder_VoucherExpire() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateOrdersReq req = new CreateOrdersReq(
                1,
                1,
                "1",
                "None"
        );
        when(ordersService.addOrder(any(CreateOrdersReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher expired"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Voucher expired"))
                .andDo(print());
    }

    @Test
    void createOrder_CartAlready() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateOrdersReq req = new CreateOrdersReq(
                1,
                1,
                "1",
                "None"
        );
        when(ordersService.addOrder(any(CreateOrdersReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("Cart already exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(409))
                .andExpect(jsonPath("$.body").value("Cart already exists"))
                .andDo(print());
    }

    @Test
    void createOrder_DistanceExceed() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateOrdersReq req = new CreateOrdersReq(
                1,
                1,
                "1",
                "None"
        );
        when(ordersService.addOrder(any(CreateOrdersReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Distance exceeded, please update address"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Distance exceeded, please update address"))
                .andDo(print());
    }

    @Test
    void confirmCancelOrder_Success() throws Exception {
        ConfirmCancelOrderReq req = new ConfirmCancelOrderReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(ordersService.confirmCancelOrder(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body("Order has been canceled"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/confirm-cancel")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(200))
                .andExpect(jsonPath("$.body").value("Order has been canceled"))
                .andDo(print());
    }

    @Test
    void confirmCancelOrder_NotFoundOrder() throws Exception {
        ConfirmCancelOrderReq req = new ConfirmCancelOrderReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(ordersService.confirmCancelOrder(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/confirm-cancel")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(404))
                .andExpect(jsonPath("$.body").value("Not found order"))
                .andDo(print());
    }

    @Test
    void confirmCancelOrder_NotFoundOrderWaiting() throws Exception {
        ConfirmCancelOrderReq req = new ConfirmCancelOrderReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(ordersService.confirmCancelOrder(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order status waiting"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/confirm-cancel")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(404))
                .andExpect(jsonPath("$.body").value("Not found order status waiting"))
                .andDo(print());
    }

    @Test
    void confirmOrder_Success() throws Exception {
        ConfirmCancelOrderReq req = new ConfirmCancelOrderReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(ordersService.confirmOrder(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body("Confirm success"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(200))
                .andExpect(jsonPath("$.body").value("Confirm success"))
                .andDo(print());
    }

    @Test
    void confirmOrder_NotFound() throws Exception {
        ConfirmCancelOrderReq req = new ConfirmCancelOrderReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(ordersService.confirmOrder(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(404))
                .andExpect(jsonPath("$.body").value("Not found order"))
                .andDo(print());
    }

    @Test
    void confirmOrder_OrderIsConfirmed() throws Exception {
        ConfirmCancelOrderReq req = new ConfirmCancelOrderReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(ordersService.confirmOrder(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order already in use"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Order already in use"))
                .andDo(print());
    }

    @Test
    void confirmOrder_OrderTimeOut() throws Exception {
        ConfirmCancelOrderReq req = new ConfirmCancelOrderReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(ordersService.confirmOrder(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body("Order has been canceled due to timeout"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(200))
                .andExpect(jsonPath("$.body").value("Order has been canceled due to timeout"))
                .andDo(print());
    }

    @Test
    void getInfoPayment_Success() throws Exception {
        getInformationPaymentFromOrderIdResponse infoPaymentFromOrderIdResponse = new getInformationPaymentFromOrderIdResponse(
                1,
                "99 đường số 7, Linh Trung, Thủ Đức, Hồ Chí Minh",
                10000.0,
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now(),
                5000.0,
                false,
                "",
                LocalDateTime.now(),
                "0769674810",
                Status_Order.WAITING,
                95000.0,
                1,
                1,
                new CRUDPaymentResponse(
                        1,
                        100000.0,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        null,
                        false,
                        Payment_Method.CREDIT,
                        Status_Payment.PENDING,
                        1,
                        false,
                        ""
                )
        );
        int orderId = 1;
        when(ordersService.getInformationPayment(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(infoPaymentFromOrderIdResponse));
        mockMvc.perform(get(endPointPath + "/info-payment")
                        .param("orderId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.discountPrice").value(5000.0))
                .andExpect(jsonPath("$.infoPaymentResponse.paymentId").value(1))
                .andExpect(jsonPath("$.infoPaymentResponse.paymentMethod").value("CREDIT"))
                .andDo(print());
    }

    @Test
    void getInfoPayment_NotFoundOrder() throws Exception {
        when(ordersService.getInformationPayment(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found order"));
        mockMvc.perform(get(endPointPath + "/info-payment")
                        .param("orderId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found order"))
                .andDo(print());
    }

    @Test
    void getInfoPayment_NotFoundPayment() throws Exception {
        when(ordersService.getInformationPayment(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found payment"));
        mockMvc.perform(get(endPointPath + "/info-payment")
                        .param("orderId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found payment"))
                .andDo(print());
    }

    @Test
    void getAllOrderByUserId_Success() throws Exception {
        CreateOrdersResponse response1 = new CreateOrdersResponse(
                1,
                "99 đường số 7, Linh Trung, Thủ Đức, Hồ Chí Minh",
                10000.0,
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                5000.0,
                false,
                "",
                LocalDateTime.now(),
                "0769674810",
                Status_Order.WAITING,
                95000.0,
                1,
                1
        );

        CreateOrdersResponse response2 = new CreateOrdersResponse(
                2,
                "99 đường số 7, Linh Trung, Thủ Đức, Hồ Chí Minh",
                18000.0,
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                5000.0,
                false,
                "",
                LocalDateTime.now(),
                "0769674810",
                Status_Order.WAITING,
                95000.0,
                1,
                3
        );
        ListAllOrdersResponse listAllOrdersResponse = new ListAllOrdersResponse(
                1,
                2,
                1,
                2,
                1,
                Arrays.asList(response1,response2)
        );
        when(ordersService.getAllOrderByUserId(anyString(), anyString(),anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listAllOrdersResponse));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/view/{userId}",1)
                        .param("page", "1")
                        .param("limit", "10"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listOrders.length()").value(2))
                .andExpect(jsonPath("$.listOrders[1].orderId").value(2))
                .andExpect(jsonPath("$.userId").value(1))
                .andDo(print());
    }

    @Test
    void getAllOrderByUserId_NotFound() throws Exception {

        when(ordersService.getAllOrderByUserId(anyString(), anyString(),anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/view/{userId}",1)
                        .param("page", "1")
                        .param("limit", "10"))

                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found user"))
                .andDo(print());
    }

    @Test
    void getAllOrderByUserIdAndStatus_Success() throws Exception {
        CreateOrdersResponse response1 = new CreateOrdersResponse(
                1,
                "99 đường số 7, Linh Trung, Thủ Đức, Hồ Chí Minh",
                10000.0,
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                5000.0,
                false,
                "",
                LocalDateTime.now(),
                "0769674810",
                Status_Order.WAITING,
                95000.0,
                1,
                1
        );

        CreateOrdersResponse response2 = new CreateOrdersResponse(
                2,
                "99 đường số 7, Linh Trung, Thủ Đức, Hồ Chí Minh",
                18000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now(),
                5000.0,
                false,
                "",
                LocalDateTime.now(),
                "0769674810",
                Status_Order.WAITING,
                95000.0,
                1,
                3
        );
        ListAllOrdersResponse listAllOrdersResponse = new ListAllOrdersResponse(
                1,
                2,
                1,
                2,
                1,
                Arrays.asList(response1,response2)
        );
        when(ordersService.getAllOrderByUserIdAndStatus(anyString(), anyString(),anyInt(),any(Status_Order.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listAllOrdersResponse));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/view/{userId}/status",1)
                        .param("page", "1")
                        .param("limit", "10")
                        .param("status", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listOrders.length()").value(2))
                .andExpect(jsonPath("$.listOrders[1].orderId").value(2))
                .andExpect(jsonPath("$.userId").value(1))
                .andDo(print());
    }

    @Test
    void getAllOrderByUserIdAndStatus_NotFound() throws Exception {

        when(ordersService.getAllOrderByUserIdAndStatus(anyString(), anyString(),anyInt(),any(Status_Order.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/view/{userId}/status",1)
                        .param("page", "1")
                        .param("limit", "10")
                        .param("status", "WAITING"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found user"))
                .andDo(print());
    }

    @Test
    void cancelOrder_UnauthorizedUser() throws Exception {
        CreatePaymentReq req = new CreatePaymentReq();
        req.setUserId(1);
        req.setOrderId(100);
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized"));

        mockMvc.perform(MockMvcRequestBuilders.put(endPointPath + "/cancel-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"orderId\": 100}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    void cancelOrder_OrderNotFound() throws Exception {
        CreatePaymentReq req = new CreatePaymentReq();
        req.setUserId(1);
        req.setOrderId(100);

        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        when(ordersService.cancelOrder(anyInt(), anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found"));

        mockMvc.perform(MockMvcRequestBuilders.put(endPointPath + "/cancel-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"orderId\": 100}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));
    }

    @Test
    void cancelOrder_SuccessfulCancellation() throws Exception {
        CreatePaymentReq req = new CreatePaymentReq();
        req.setUserId(1);
        req.setOrderId(100);

        // Mock authorized user response
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        // Mock successful cancellation response
        when(ordersService.cancelOrder(anyInt(), anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body("Order cancelled successfully"));

        mockMvc.perform(MockMvcRequestBuilders.put(endPointPath+ "/cancel-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"orderId\": 100}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order cancelled successfully"));
    }

    @Test
    void cancelOrder_OrderAlreadyCancelled() throws Exception {
        CreatePaymentReq req = new CreatePaymentReq();
        req.setUserId(1);
        req.setOrderId(100);

        // Mock authorized user response
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        // Mock order already cancelled response
        when(ordersService.cancelOrder(anyInt(), anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("Order already cancelled"));

        mockMvc.perform(MockMvcRequestBuilders.put(endPointPath + "/cancel-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"orderId\": 100}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Order already cancelled"));
    }

    @Test
    void cancelOrder_ShipmentInProgressOrCompleted() throws Exception {
        CreatePaymentReq req = new CreatePaymentReq();
        req.setUserId(1);
        req.setOrderId(100);

        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        when(ordersService.cancelOrder(anyInt(), anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("Order cannot be cancelled as shipment is in progress or completed"));

        mockMvc.perform(MockMvcRequestBuilders.put(endPointPath + "/cancel-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"orderId\": 100}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Order cannot be cancelled as shipment is in progress or completed"));
    }

    @Test
    void detailItemOrder_Success() throws Exception {
        ItemOrderResponse response1 = new ItemOrderResponse(
                1,
                1,
                1,
                "SP1",
                Size.S,
                20000.0,
                100000.0,
                5
        );

        ItemOrderResponse response2 = new ItemOrderResponse(
                2,
                1,
                1,
                "SP1",
                Size.L,
                25000.0,
                100000.0,
                4
        );

        ListItemOrderResponse listItemOrderResponse = new ListItemOrderResponse(
                1,
                2,
                Arrays.asList(response1,response2)
        );

        when(ordersService.detailItemOrders(anyInt())).thenReturn((ResponseEntity) ResponseEntity.ok(listItemOrderResponse));
        mockMvc.perform(get(endPointPath + "/detail-item/{orderId}",1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listItemOrders.length()").value(2))
                .andExpect(jsonPath("$.listItemOrders[0].cartItemId").value(1))
                .andExpect(jsonPath("$.listItemOrders[1].size").value("L"))
                .andDo(print());
    }

    @Test
    void detailItemOrder_NotFound() throws Exception {
        when(ordersService.detailItemOrders(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found"));
        mockMvc.perform(get(endPointPath + "/detail-item/{orderId}",1))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"))
                .andDo(print());
    }

}