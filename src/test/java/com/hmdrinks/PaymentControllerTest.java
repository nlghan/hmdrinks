package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.PaymentController;
import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Payment;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.CreatePaymentReq;
import com.hmdrinks.Request.CreatePaymentVNPayReq;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebAppConfiguration
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    private static final String endPointPath="/api/payment";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private ProductRepository productRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private VNPayIpnHandler vnPayIpnHandler;
    @MockBean
    private ZaloPayService zaloPayService;
    @MockBean
    private ReviewService reviewService;
    @MockBean
    private GenerateInvoiceService generateInvoiceService;
    @MockBean
    private  PostService postService;
    @MockBean
    private OrdersService ordersService;
    @MockBean
    private PaymentService paymentService;

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
    void createPaymentVnPay_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentVNPayReq req = new CreatePaymentVNPayReq(
                1,
                1,
                "127.0.0.1"
        );
        CreatePaymentResponse response = new CreatePaymentResponse(
                1,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                1,
                "",
                ""
        );

        when(paymentService.createVNPay(any(CreatePaymentVNPayReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/vnPay")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100000.0))
                .andExpect(jsonPath("$.orderId").value(1))
                .andDo(print());
    }

    @Test
    void createPaymentVnPay_CashAlreadyCreate() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentVNPayReq req = new CreatePaymentVNPayReq(
                1,
                1,
                "127.0.0.1"
        );
        when(paymentService.createVNPay(any(CreatePaymentVNPayReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment cash already create"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/vnPay")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment cash already create"))
                .andDo(print());
    }

    @Test
    void createPaymentVnPay_PaymentAlready() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentVNPayReq req = new CreatePaymentVNPayReq(
                1,
                1,
                "127.0.0.1"
        );
        when(paymentService.createVNPay(any(CreatePaymentVNPayReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/vnPay")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("Payment already exists"))
                .andDo(print());
    }

    @Test
    void createPaymentVnPay_OrderNotConfirm() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentVNPayReq req = new CreatePaymentVNPayReq(
                1,
                1,
                "127.0.0.1"
        );
        when(paymentService.createVNPay(any(CreatePaymentVNPayReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not confirmed"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/vnPay")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order not confirmed"))
                .andDo(print());
    }

    @Test
    void createPaymentVnPay_OrderCancel() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentVNPayReq req = new CreatePaymentVNPayReq(
                1,
                1,
                "127.0.0.1"
        );
        when(paymentService.createVNPay(any(CreatePaymentVNPayReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is cancelled"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/vnPay")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order is cancelled"))
                .andDo(print());
    }


    @Test
    void createPaymentMomo_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        CreatePaymentResponse response = new CreatePaymentResponse(
                1,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                1,
                "",
                ""
        );

        when(paymentService.createPaymentMomo(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/momo")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100000.0))
                .andExpect(jsonPath("$.orderId").value(1))
                .andDo(print());
    }

    @Test
    void createPaymentMomo_AlreadyTypeCash() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        when(paymentService.createPaymentMomo(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment create with type cash"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/momo")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment create with type cash"))
                .andDo(print());
    }

    @Test
    void createPaymentMomo_PaymentAlready() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        when(paymentService.createPaymentMomo(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/momo")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("Payment already exists"))
                .andDo(print());
    }

    @Test
    void createPaymentMomo_OrderNotConfirm() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        when(paymentService.createPaymentMomo(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not confirmed"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/momo")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order not confirmed"))
                .andDo(print());
    }

    @Test
    void createPaymentMomo_OrderCancel() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        when(paymentService.createPaymentMomo(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is cancelled"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/momo")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order is cancelled"))
                .andDo(print());
    }

    @Test
    void createPaymentMomo_NotFoundUser() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        when(paymentService.createPaymentMomo(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/momo")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found user"))
                .andDo(print());
    }

    @Test
    void createPaymentZaloPay_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        CreatePaymentResponse response = new CreatePaymentResponse(
                1,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                1,
                "",
                ""
        );

        when(paymentService.createZaloPay(any(CreatePaymentReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/zaloPay")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100000.0))
                .andExpect(jsonPath("$.orderId").value(1))
                .andDo(print());
    }

    @Test
    void createPaymentPayOS_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        CreatePaymentResponse response = new CreatePaymentResponse(
                1,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                1,
                "",
                ""
        );

        when(paymentService.createPaymentATM(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/payOs")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100000.0))
                .andExpect(jsonPath("$.orderId").value(1))
                .andDo(print());
    }


    @Test
    void createPaymentPayOS_PaymentCash() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1);
        when(paymentService.createPaymentATM(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment create with type cash"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/payOs")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment create with type cash"))
                .andDo(print());
    }

    @Test
    void createPaymentPayOS_PaymentAlready() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1);
        when(paymentService.createPaymentATM(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/payOs")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("Payment already exists"))
                .andDo(print());
    }

    @Test
    void createPaymentPayOS_OrderNotConfirm() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1);
        when(paymentService.createPaymentATM(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not confirmed"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/payOs")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order not confirmed"))
                .andDo(print());
    }

    @Test
    void createPaymentPayOS_OrderCancelled() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1);
        when(paymentService.createPaymentATM(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is cancelled"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/payOs")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order is cancelled"))
                .andDo(print());
    }

    @Test
    void createPaymentPayOS_UserNotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1);
        when(paymentService.createPaymentATM(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/payOs")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found user"))
                .andDo(print());
    }


    @Test
    void createPaymentZaloPay_PaymentCash() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );

        when(paymentService.createZaloPay(any(CreatePaymentReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment create with type cash"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/zaloPay")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment create with type cash"))
                .andDo(print());
    }

    @Test
    void createPaymentZaloPay_PaymentExist() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );

        when(paymentService.createZaloPay(any(CreatePaymentReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/zaloPay")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("Payment already exists"))
                .andDo(print());
    }

    @Test
    void createPaymentZaloPay_OrderNotConfirmed() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );

        when(paymentService.createZaloPay(any(CreatePaymentReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not confirmed"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/zaloPay")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order not confirmed"))
                .andDo(print());
    }

    @Test
    void createPaymentZaloPay_OrderCancel() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );

        when(paymentService.createZaloPay(any(CreatePaymentReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order cancelled"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/credit/zaloPay")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order cancelled"))
                .andDo(print());
    }

    @Test
    void createPaymentCash_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        CreatePaymentResponse response = new CreatePaymentResponse(
                1,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CASH,
                Status_Payment.PENDING,
                1,
                "",
                ""
        );

        when(paymentService.createPaymentCash(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/cash")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100000.0))
                .andExpect(jsonPath("$.orderId").value(1))
                .andDo(print());
    }

    @Test
    void createPaymentCash_OrderNotConfirm() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        when(paymentService.createPaymentCash(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order not confirmed"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/cash")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order not confirmed"))
                .andDo(print());
    }

    @Test
    void createPaymentCash_OrderCancelled() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        when(paymentService.createPaymentCash(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order is cancelled"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/cash")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Order is cancelled"))
                .andDo(print());
    }

    @Test
    void createPaymentCash_UserDeleted() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        when(paymentService.createPaymentCash(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is deleted"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/cash")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User is deleted"))
                .andDo(print());
    }

    @Test
    void createPaymentCash_PaymentCompleted() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        when(paymentService.createPaymentCash(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment already completed"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/cash")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment already completed"))
                .andDo(print());
    }

    @Test
    void createPaymentCash_PaymentCash() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreatePaymentReq req = new CreatePaymentReq(
                1,
                1
        );
        when(paymentService.createPaymentCash(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment create with type cash"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create/cash")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment create with type cash"))
                .andDo(print());
    }


    @Test
    void getAllPayment_Success() throws Exception {
        CRUDPaymentResponse response1 = new CRUDPaymentResponse(
                1,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                1,
                true,
                ""
        );

        CRUDPaymentResponse response2 = new CRUDPaymentResponse(
                2,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                2,
                true,
                ""
        );
        ListAllPaymentResponse listAllOrdersResponse = new ListAllPaymentResponse(
                1,
                2,
                1,
                2,
                Arrays.asList(response1,response2)
        );
        when(paymentService.getAllPayment(anyString(), anyString())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listAllOrdersResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath +"/listAll")
                        .param("page", "1")
                        .param("limit", "10"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listPayments.length()").value(2))
                .andExpect(jsonPath("$.listPayments[1].orderId").value(2))
                .andExpect(jsonPath("$.listPayments[0].amount").value(100000.0))
                .andDo(print());
    }

    @Test
    void getAllPaymentMethod_Success() throws Exception {
        CRUDPaymentResponse response1 = new CRUDPaymentResponse(
                1,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                1,
                true,
                ""
        );

        CRUDPaymentResponse response2 = new CRUDPaymentResponse(
                2,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                2,
                false,
                ""
        );
        ListAllPaymentResponse listAllOrdersResponse = new ListAllPaymentResponse(
                1,
                2,
                1,
                2,
                Arrays.asList(response1,response2)
        );
        when(paymentService.getAllPaymentMethod(anyString(), anyString(),any(Payment_Method.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listAllOrdersResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath +"/listAll-method")
                        .param("page", "1")
                        .param("limit", "10")
                        .param("method","CREDIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listPayments.length()").value(2))
                .andExpect(jsonPath("$.listPayments[1].orderId").value(2))
                .andExpect(jsonPath("$.listPayments[0].amount").value(100000.0))
                .andDo(print());
    }

    @Test
    void getAllPaymentStatus_Success() throws Exception {
        CRUDPaymentResponse response1 = new CRUDPaymentResponse(
                1,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                1,
                true,
                ""
        );

        CRUDPaymentResponse response2 = new CRUDPaymentResponse(
                2,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                2,
                false,
                ""
        );
        ListAllPaymentResponse listAllOrdersResponse = new ListAllPaymentResponse(
                1,
                2,
                1,
                2,
                Arrays.asList(response1,response2)
        );
        when(paymentService.getAllPaymentStatus(anyString(), anyString(),any(Status_Payment.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listAllOrdersResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath +"/listAll-status")
                        .param("page", "1")
                        .param("limit", "10")
                        .param("status","PENDING"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listPayments.length()").value(2))
                .andExpect(jsonPath("$.listPayments[1].orderId").value(2))
                .andExpect(jsonPath("$.listPayments[0].amount").value(100000.0))
                .andDo(print());
    }

    @Test
    void checkStatusPayment_Success() throws Exception {
        CRUDPaymentResponse response1 = new CRUDPaymentResponse(
                1,
                100000.0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                Payment_Method.CREDIT,
                Status_Payment.PENDING,
                1,
                true,
                ""
        );
        when(paymentService.checkStatusPayment(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response1));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath +"/check-status-payment")
                        .param("paymentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100000.0))
                .andExpect(jsonPath("$.statusPayment").value("PENDING"))
                .andDo(print());

    }

    @Test
    void checkStatusPayment_NotFound() throws Exception {
        when(paymentService.checkStatusPayment(anyInt())).thenReturn((ResponseEntity) new  ResponseEntity<>("Not found payment", HttpStatus.NOT_FOUND));
        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath +"/check-status-payment")
                        .param("paymentId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found payment"))
                .andDo(print());
    }

    @Test
    void checkStatusPayment_PaymentIsDeleted() throws Exception {
        when(paymentService.checkStatusPayment(anyInt())).thenReturn((ResponseEntity) new  ResponseEntity<>("Payment is deleted", HttpStatus.BAD_REQUEST));
        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath +"/check-status-payment")
                        .param("paymentId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Payment is deleted"))
                .andDo(print());
    }

    @Test
    void ZaloPayCallBack_Success() throws Exception {
        Map<String, Integer> response = new HashMap<>();
        response.put("status", 0);
        when(zaloPayService.handleCallBack(anyString())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath +"/zalo/callback")
                        .param("app_trans_id", "123_0004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(0))
                .andDo(print());
    }

    @Test
    void VnPayCallback_success() throws Exception {
        IpnResponse ipnResponse = new IpnResponse("00", "Successful","");
        Map<String, String> params = new HashMap<>();
        params.put("vnp_ResponseCode", "00");
        when(vnPayIpnHandler.process(params)).thenReturn(ipnResponse);
        mockMvc.perform(get(endPointPath + "/vnpay_ipn").param("vnp_ResponseCode", "00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Successful"));
    }

    @Test
    public void testHandleCallback_PaymentNotFound() throws Exception {
        when(paymentService.callBack(anyString(), anyString()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Not found payment")));

        mockMvc.perform(get(endPointPath+ "/momo/callback")
                        .param("orderId", "12345")
                        .param("resultCode", "0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Not found payment"));
    }

    @Test
    public void testHandleCallback_PaymentDeleted() throws Exception {
        when(paymentService.callBack(anyString(), anyString()))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", "error", "message", "Payment is deleted")));
        mockMvc.perform(get(endPointPath +"/momo/callback")
                        .param("orderId", "12345")
                        .param("resultCode", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Payment is deleted"));
    }

    @Test
    public void testHandleCallback_PaymentCompleted() throws Exception {
        when(paymentService.callBack(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(Map.of("status", HttpStatus.OK.value(), "message", "Payment completed successfully")));
        mockMvc.perform(get( endPointPath + "/momo/callback")
                        .param("orderId", "12345")
                        .param("resultCode", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Payment completed successfully"));
    }

    @Test
    public void testHandleCallback_PaymentFailed() throws Exception {
        // Arrange: Mock the service response for a failed payment
        when(paymentService.callBack(anyString(), anyString()))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", HttpStatus.BAD_REQUEST.value(), "message", "Payment failed")));

        mockMvc.perform(get(endPointPath + "/momo/callback")
                        .param("orderId", "12345")
                        .param("resultCode", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Payment failed"));
    }

}