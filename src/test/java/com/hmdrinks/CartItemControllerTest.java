package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.CartController;
import com.hmdrinks.Controller.CartItemController;
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
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebAppConfiguration
@WebMvcTest(CartItemController.class)
class CartItemControllerTest {
    private static final String endPointPath="/api/cart-item";
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
    void insertCartItem_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        InsertItemToCart req = new InsertItemToCart(
                1,
                1,
                1,
                Size.S,
                5
        );
        CRUDCartItemResponse response = new CRUDCartItemResponse(
                1,
                1,
                "",
                1,
                Size.S,
                20000.0,
                5
        );

        when(cartItemService.insertCartItem(any(InsertItemToCart.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/insert")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proId").value(1))
                .andExpect(jsonPath("$.totalPrice").value(20000.0))
                .andDo(print());
    }

    @Test
    void insertCart_UserIdNotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        InsertItemToCart req = new InsertItemToCart(
                1,
                1,
                1,
                Size.S,
                5
        );

        when(cartItemService.insertCartItem(any(InsertItemToCart.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/insert")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User Not Found"))
                .andDo(print());
    }

    @Test
    void insertCart_ProductNotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        InsertItemToCart req = new InsertItemToCart(
                1,
                1,
                1,
                Size.S,
                5
        );

        when(cartItemService.insertCartItem(any(InsertItemToCart.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/insert")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product Not Found"))
                .andDo(print());
    }

    @Test
    void insertCart_ProductVarNotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        InsertItemToCart req = new InsertItemToCart(
                1,
                1,
                1,
                Size.S,
                5
        );

        when(cartItemService.insertCartItem(any(InsertItemToCart.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("production size not exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/insert")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("production size not exists"))
                .andDo(print());
    }

    @Test
    void insertCart_CartNotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        InsertItemToCart req = new InsertItemToCart(
                1,
                1,
                1,
                Size.S,
                5
        );

        when(cartItemService.insertCartItem(any(InsertItemToCart.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/insert")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cart Not Found"))
                .andDo(print());
    }

    @Test
    void insertCart_QuantityLessThan0() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        InsertItemToCart req = new InsertItemToCart(
                1,
                1,
                1,
                Size.S,
                0
        );

        when(cartItemService.insertCartItem(any(InsertItemToCart.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is less than 0"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/insert")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quantity is less than 0"))
                .andDo(print());
    }

    @Test
    void insertCart_QuantityGreaterThanStock() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        InsertItemToCart req = new InsertItemToCart(
                1,
                1,
                1,
                Size.S,
                0
        );

        when(cartItemService.insertCartItem(any(InsertItemToCart.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is greater than stock"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/insert")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quantity is greater than stock"))
                .andDo(print());
    }


    @Test
    void deleteOneItem_Success() throws Exception {
        DeleteOneCartItemReq req = new DeleteOneCartItemReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        DeleteCartItemResponse response = new DeleteCartItemResponse(
                "Delete item success"
        );
        when(cartItemService.deleteOneItem(any(DeleteOneCartItemReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(delete(endPointPath + "/delete/1")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete item success"))
                .andDo(print());
    }

    @Test
    void deleteOneItem_NotFound() throws Exception {
        DeleteOneCartItemReq req = new DeleteOneCartItemReq(1,1);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(cartItemService.deleteOneItem(any(DeleteOneCartItemReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("CartItem Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(delete(endPointPath + "/delete/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("CartItem Not Found"))
                .andDo(print());
    }

    @Test
    void increaseCartItemQuantity_Success() throws Exception {
        IncreaseDecreaseItemQuantityReq req = new IncreaseDecreaseItemQuantityReq(1,1,5);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        IncreaseDecreaseItemQuantityResponse response = new IncreaseDecreaseItemQuantityResponse(
                6,25000
        );
        when(cartItemService.increaseCartItemQuantity(any(IncreaseDecreaseItemQuantityReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/increase")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(6))
                .andDo(print());
    }

    @Test
    void increaseCartItemQuantity_CartItemNotFound() throws Exception {
        IncreaseDecreaseItemQuantityReq req = new IncreaseDecreaseItemQuantityReq(1,1,5);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(cartItemService.increaseCartItemQuantity(any(IncreaseDecreaseItemQuantityReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("CartItem Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/increase")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("CartItem Not Found"))
                .andDo(print());
    }

    @Test
    void increaseCartItemQuantity_QuantityLessThan0() throws Exception {
        IncreaseDecreaseItemQuantityReq req = new IncreaseDecreaseItemQuantityReq(1,1,0);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(cartItemService.increaseCartItemQuantity(any(IncreaseDecreaseItemQuantityReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is less than 0"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/increase")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quantity is less than 0"))
                .andDo(print());
    }

    @Test
    void increaseCartItemQuantity_QuantityGreaterThanStock() throws Exception {
        IncreaseDecreaseItemQuantityReq req = new IncreaseDecreaseItemQuantityReq(1,1,0);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(cartItemService.increaseCartItemQuantity(any(IncreaseDecreaseItemQuantityReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is greater than stock"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/increase")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quantity is greater than stock"))
                .andDo(print());
    }

    @Test
    void increaseCartItemQuantity_CartNotFound() throws Exception {
        IncreaseDecreaseItemQuantityReq req = new IncreaseDecreaseItemQuantityReq(1,1,0);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(cartItemService.increaseCartItemQuantity(any(IncreaseDecreaseItemQuantityReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/increase")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cart Not Found"))
                .andDo(print());
    }

    ////
    @Test
    void decreaseCartItemQuantity_CartItemNotFound() throws Exception {
        IncreaseDecreaseItemQuantityReq req = new IncreaseDecreaseItemQuantityReq(1,1,5);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(cartItemService.decreaseCartItemQuantity(any(IncreaseDecreaseItemQuantityReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("CartItem Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/decrease")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("CartItem Not Found"))
                .andDo(print());
    }

    @Test
    void decreaseCartItemQuantity_QuantityLessThan0() throws Exception {
        IncreaseDecreaseItemQuantityReq req = new IncreaseDecreaseItemQuantityReq(1,1,0);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(cartItemService.decreaseCartItemQuantity(any(IncreaseDecreaseItemQuantityReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is less than 0"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/decrease")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quantity is less than 0"))
                .andDo(print());
    }

    @Test
    void decreaseCartItemQuantity_QuantityGreaterThanStock() throws Exception {
        IncreaseDecreaseItemQuantityReq req = new IncreaseDecreaseItemQuantityReq(1,1,0);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(cartItemService.decreaseCartItemQuantity(any(IncreaseDecreaseItemQuantityReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is greater than stock"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/decrease")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quantity is greater than stock"))
                .andDo(print());
    }

    @Test
    void decreaseCartItemQuantity_CartNotFound() throws Exception {
        IncreaseDecreaseItemQuantityReq req = new IncreaseDecreaseItemQuantityReq(1,1,0);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        when(cartItemService.decreaseCartItemQuantity(any(IncreaseDecreaseItemQuantityReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/decrease")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cart Not Found"))
                .andDo(print());
    }

    @Test
    void decreaseCartItemQuantity_Success() throws Exception {
        IncreaseDecreaseItemQuantityReq req = new IncreaseDecreaseItemQuantityReq(1,1,5);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        IncreaseDecreaseItemQuantityResponse response = new IncreaseDecreaseItemQuantityResponse(
                4, 25000
        );
        when(cartItemService.decreaseCartItemQuantity(any(IncreaseDecreaseItemQuantityReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/decrease")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4))
                .andDo(print());
    }

}