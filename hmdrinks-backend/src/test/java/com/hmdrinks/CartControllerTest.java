package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.CartController;
import com.hmdrinks.Controller.ProductVariantsController;
import com.hmdrinks.Enum.Size;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Repository.CategoryRepository;
import com.hmdrinks.Repository.PriceHistoryRepository;
import com.hmdrinks.Repository.ProductRepository;
import com.hmdrinks.Repository.TokenRepository;
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
import org.springframework.scheduling.config.ScheduledTask;
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
@WebMvcTest(CartController.class)
class CartControllerTest {
    private static final String endPointPath="/api/cart";
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createCart_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateNewCart req = new CreateNewCart(1);
        CreateNewCartResponse response = new CreateNewCartResponse(
                1,
                0.0,
                0,
                1,
                Status_Cart.NEW
        );


        when(cartService.createCart(any(CreateNewCart.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andDo(print());
    }

    @Test
    void createCart_UserIdNotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateNewCart req = new CreateNewCart(1);

        when(cartService.createCart(any(CreateNewCart.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserId not found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("UserId not found"))
                .andDo(print());
    }

    @Test
    void createCart_Conflict() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateNewCart req = new CreateNewCart(1);

        when(cartService.createCart(any(CreateNewCart.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("Cart already exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("Cart already exists"))
                .andDo(print());
    }

    @Test
    void getAllCartFromUser_Success() throws Exception {
        CreateNewCartResponse response1 = new CreateNewCartResponse(
                1,
                0.0,
                0,
                1,
                Status_Cart.NEW
        );
        CreateNewCartResponse response2 = new CreateNewCartResponse(
                2,
                200.0,
                1,
                1,
                Status_Cart.NEW
        );
        ListAllCartUserResponse listAllCartUserResponse = new ListAllCartUserResponse(
                1,
                 2,
                 Arrays.asList(response1,response2)
        );
        when(cartService.getAllCartFromUser(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listAllCartUserResponse));
        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/list-cart/1"))
                .andExpect(status().isOk())
                 .andExpect(jsonPath("$.userId").value(1))
                 .andExpect(jsonPath("$.listCart[0].cartId").value(1))
                 .andExpect(jsonPath("$.listCart[1].price").value(200.0))
                .andDo(print());
    }

    @Test
    void getAllCartFromUser_NotFound() throws Exception {

        when(cartService.getAllCartFromUser(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserId not found"));
        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/list-cart/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("UserId not found"))
                .andDo(print());
    }

    @Test
    void  getAllItemCart_Success() throws Exception {
        CRUDCartItemResponse response1 = new CRUDCartItemResponse(
                1,
                1,
                "",
                1,
                Size.S,
                50000.0,
                4
        );

        CRUDCartItemResponse response2 = new CRUDCartItemResponse(
                2,
                1,
                "",
                1,
                Size.L,
                80000.0,
                5
        );
        ListItemCartResponse listItemCartResponse = new ListItemCartResponse(
                1,
                2,
                Arrays.asList(response1,response2)
        );
        when(cartService.getAllItemCart(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listItemCartResponse));
        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/list-cartItem/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.listCartItemResponses[0].size").value("S"))
                .andExpect(jsonPath("$.listCartItemResponses[0].quantity").value(4))
                .andExpect(jsonPath("$.listCartItemResponses[1].totalPrice").value(80000.0))
                .andDo(print());
    }

    @Test
    void  getAllItemCart_NotFound() throws Exception {
        when(cartService.getAllItemCart(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found cart"));
        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/list-cartItem/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found cart"))
                .andDo(print());
    }

    @Test
    void deleteAllItem_Success() throws Exception {
        DeleteAllCartItemReq req = new DeleteAllCartItemReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        DeleteCartItemResponse response = new DeleteCartItemResponse(
                "Delete all item success"
        );
        when(cartItemService.deleteAllCartItem(any(DeleteAllCartItemReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(delete(endPointPath + "/delete-allItem/1")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete all item success"))
                .andDo(print());
    }

    @Test
    void deleteAllItem_NotFound() throws Exception {
        DeleteAllCartItemReq req = new DeleteAllCartItemReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(cartItemService.deleteAllCartItem(any(DeleteAllCartItemReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(delete(endPointPath + "/delete-allItem/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cart Not Found"))
                .andDo(print());
    }

}