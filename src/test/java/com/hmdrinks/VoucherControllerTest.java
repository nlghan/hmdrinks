package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.PostController;
import com.hmdrinks.Controller.VoucherController;
import com.hmdrinks.Enum.Status_Voucher;
import com.hmdrinks.Enum.Type_Post;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.CRUDPostReq;
import com.hmdrinks.Request.CreateNewPostReq;
import com.hmdrinks.Request.CreateVoucherReq;
import com.hmdrinks.Request.CrudVoucherReq;
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
@WebMvcTest(VoucherController.class)
class VoucherControllerTest {
    private static final String endPointPath="/api/voucher";
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
    private  PostService postService;
    @MockBean
    private VoucherService voucherService;
    @MockBean
    private VoucherRepository voucherRepository;
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
    void createVoucher_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateVoucherReq voucherReq = new CreateVoucherReq(
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Voucher123",
                10000.0,
                50,
                Status_Voucher.ACTIVE,
                1
        );

        CRUDVoucherResponse voucherResponse = new CRUDVoucherResponse(
                1,
                "Voucher123",
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.createVoucher(any(CreateVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(voucherResponse));
        String requestBody = objectMapper.writeValueAsString(voucherReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(200))
                .andExpect(jsonPath("$.body.voucherId").value(1))
                .andExpect(jsonPath("$.body.number").value(50))
                .andDo(print());
    }

    @Test
    void createVoucher_PostNotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateVoucherReq voucherReq = new CreateVoucherReq(
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Voucher123",
                10000.0,
                50,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.createVoucher(any(CreateVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Post not found"));
        String requestBody = objectMapper.writeValueAsString(voucherReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(404))
                .andExpect(jsonPath("$.body").value("Post not found"))
                .andDo(print());
    }

    @Test
    void createVoucher_VoucherPostAlready() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateVoucherReq voucherReq = new CreateVoucherReq(
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Voucher123",
                10000.0,
                50,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.createVoucher(any(CreateVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Voucher Post already exists"));
        String requestBody = objectMapper.writeValueAsString(voucherReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(409))
                .andExpect(jsonPath("$.body").value("Voucher Post already exists"))
                .andDo(print());
    }

    @Test
    void createVoucher_StartDateError() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateVoucherReq voucherReq = new CreateVoucherReq(
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Voucher123",
                10000.0,
                50,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.createVoucher(any(CreateVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Start date must be greater than or equal to the current date"));
        String requestBody = objectMapper.writeValueAsString(voucherReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Start date must be greater than or equal to the current date"))
                .andDo(print());
    }

    @Test
    void createVoucher_EndDateError() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateVoucherReq voucherReq = new CreateVoucherReq(
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Voucher123",
                10000.0,
                50,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.createVoucher(any(CreateVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("End date must be greater than or equal to post creation date and current date"));
        String requestBody = objectMapper.writeValueAsString(voucherReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("End date must be greater than or equal to post creation date and current date"))
                .andDo(print());
    }

    @Test
    void createVoucher_StartDate_PostDateError() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateVoucherReq voucherReq = new CreateVoucherReq(
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Voucher123",
                10000.0,
                50,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.createVoucher(any(CreateVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Start date must be greater than or equal to post creation date"));
        String requestBody = objectMapper.writeValueAsString(voucherReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Start date must be greater than or equal to post creation date"))
                .andDo(print());
    }

    @Test
    void createVoucher_QuantityGreater0() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateVoucherReq voucherReq = new CreateVoucherReq(
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Voucher123",
                10000.0,
                50,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.createVoucher(any(CreateVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Number must be greater than 0"));
        String requestBody = objectMapper.writeValueAsString(voucherReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Number must be greater than 0"))
                .andDo(print());
    }


 ///////////////////// update
 @Test
 void updatVoucher_PostNotFound() throws Exception {
     ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
     when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
     CrudVoucherReq req = new CrudVoucherReq(
             1,
             "Voucher1234",
             50,
             LocalDateTime.now(),
             LocalDateTime.now(),
             10000.0,
             Status_Voucher.ACTIVE,
             1
     );

     when(voucherService.updateVoucher(any(CrudVoucherReq.class)))
             .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                     .body("Post not found"));
     String requestBody = objectMapper.writeValueAsString(req);
     mockMvc.perform(put(endPointPath + "/update")
                     .contentType(MediaType.APPLICATION_JSON_VALUE)
                     .content(requestBody))
             .andExpect(jsonPath("$.statusCodeValue").value(404))
             .andExpect(jsonPath("$.body").value("Post not found"))
             .andDo(print());
 }

    @Test
    void updatVoucher_VoucherNotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CrudVoucherReq req = new CrudVoucherReq(
                1,
                "Voucher1234",
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.updateVoucher(any(CrudVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Voucher not found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(404))
                .andExpect(jsonPath("$.body").value("Voucher not found"))
                .andDo(print());
    }

    @Test
    void updateVoucher_StartDateError() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CrudVoucherReq req = new CrudVoucherReq(
                1,
                "Voucher1234",
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.updateVoucher(any(CrudVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Start date must be greater than or equal to the current date"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Start date must be greater than or equal to the current date"))
                .andDo(print());
    }

    @Test
    void updateVoucher_EndDateError() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CrudVoucherReq req = new CrudVoucherReq(
                1,
                "Voucher1234",
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.updateVoucher(any(CrudVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("End date must be greater than or equal to post creation date and current date"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("End date must be greater than or equal to post creation date and current date"))
                .andDo(print());
    }

    @Test
    void updateVoucher_StartDate_PostDateError() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CrudVoucherReq req = new CrudVoucherReq(
                1,
                "Voucher1234",
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.updateVoucher(any(CrudVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Start date must be greater than or equal to post creation date"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Start date must be greater than or equal to post creation date"))
                .andDo(print());
    }

    @Test
    void updateVoucher_EndDate_PostDateError() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CrudVoucherReq req = new CrudVoucherReq(
                1,
                "Voucher1234",
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.updateVoucher(any(CrudVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("End date must be greater than or equal to post creation date"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("End date must be greater than or equal to post creation date"))
                .andDo(print());
    }

    @Test
    void updateVoucher_QuantityGreater0() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CrudVoucherReq req = new CrudVoucherReq(
                1,
                "Voucher1234",
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.updateVoucher(any(CrudVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Number must be greater than 0"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Number must be greater than 0"))
                .andDo(print());
    }

    @Test
    void updateVoucher_QuantityError() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CrudVoucherReq req = new CrudVoucherReq(
                1,
                "Voucher1234",
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                1
        );

        when(voucherService.updateVoucher(any(CrudVoucherReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Number must be greater"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(jsonPath("$.statusCodeValue").value(400))
                .andExpect(jsonPath("$.body").value("Number must be greater"))
                .andDo(print());
    }






    @Test
    void getOneVoucher_NotFound() throws Exception {
        when(voucherService.getVoucherById(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher not found"));

        mockMvc.perform(get("/api/voucher/view/{id}",1))
                .andExpect(jsonPath("$.body").value("Voucher not found"))  // body chứa thông báo lỗi
                .andExpect(jsonPath("$.statusCode").value("NOT_FOUND"))  // mã trạng thái dạng chuỗi
                .andExpect(jsonPath("$.statusCodeValue").value(404))
                .andDo(print());
    }

    @Test
    void getOneVoucher_Success() throws Exception {
        when(voucherService.getVoucherById(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher not found"));
        CRUDVoucherResponse voucherResponse = new CRUDVoucherResponse(
                1,
                "Voucher123",
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                1
        );
        when(voucherService.getVoucherById(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(voucherResponse));
        mockMvc.perform(get("/api/voucher/view/{id}",1))
                .andExpect(jsonPath("$.body.number").value(50))
                .andExpect(jsonPath("$.body.postId").value(1))
                .andExpect(jsonPath("$.statusCodeValue").value(200))
                .andDo(print());
    }

    @Test
    void listAllVoucher_Success() throws Exception {
        CRUDVoucherResponse voucherResponse1 = new CRUDVoucherResponse(
                1,
                "Voucher123",
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                1
        );

        CRUDVoucherResponse voucherResponse2 = new CRUDVoucherResponse(
                2,
                "Voucher123a",
                500,
                LocalDateTime.now(),
                LocalDateTime.now(),
                10000.0,
                Status_Voucher.ACTIVE,
                2
        );

        ListAllVoucherResponse listAllPostResponse = new ListAllVoucherResponse(
                2,
                Arrays.asList(voucherResponse1, voucherResponse2)
        );


        when(voucherService.listAllVoucher()).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listAllPostResponse));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/voucher/view/all"))
                .andExpect(jsonPath("$.body.voucherResponseList[0].number").value(50))
                .andExpect(jsonPath("$.body.voucherResponseList[1].postId").value(2))
                .andExpect(jsonPath("$.statusCodeValue").value(200))
                .andExpect(jsonPath("$.body.voucherResponseList.length()").value(2))
                .andDo(print());
    }

}