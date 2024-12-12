package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.ProductController;
import com.hmdrinks.Controller.ProductVariantsController;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Enum.Size;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebAppConfiguration
@WebMvcTest(ProductVariantsController.class)
class ProductVarControllerTest {
    private static final String endPointPath="/api/productVar";
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createProductVar_Success() throws Exception {
        CreateProductVarReq createProductReq = new CreateProductVarReq(1,Size.S,25000.0,100);
        CRUDProductVarResponse response = new CRUDProductVarResponse(
                1,
                1,
                Size.S,
                25000.0,
                100,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(productVarService.crateProductVariants(any(CreateProductVarReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(createProductReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(25000.0))
                .andDo(print());
    }

    @Test
    void createProductVar_ProIdNotFound() throws Exception {
        CreateProductVarReq createProductReq = new CreateProductVarReq(1,Size.S,25000.0,100);

        when(productVarService.crateProductVariants(any(CreateProductVarReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found"));
        String requestBody = objectMapper.writeValueAsString(createProductReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product Not Found"))
                .andDo(print());
    }

    @Test
    void createProduct_ProductVarConflict() throws Exception {
        CreateProductVarReq createProductReq = new CreateProductVarReq(1,Size.S,25000.0,100);

        when(productVarService.crateProductVariants(any(CreateProductVarReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("Product Variant Size Already Exists"));
        String requestBody = objectMapper.writeValueAsString(createProductReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("Product Variant Size Already Exists"))
                .andDo(print());
    }


    @Test
    void getOneProductVar_NotFound() throws Exception {

        when(productVarService.getOneVarProduct(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found"));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/view/1")) // Sử dụng HTTP GET thay vì POST
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product Not Found"))
                .andDo(print());
    }

    @Test
    void getOneProductVar_Success() throws Exception {
        CRUDProductVarResponse response = new CRUDProductVarResponse(
                1,
                1,
                Size.S,
                25000.0,
                100,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(productVarService.getOneVarProduct(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/view/1")) // Sử dụng HTTP GET thay vì POST
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(25000.0))
                .andExpect(jsonPath("$.size").value("S"))
                .andDo(print());
    }

    @Test
    void updateProductVar_ProductNotExist() throws Exception {
        CRUDProductVarReq req = new CRUDProductVarReq(
                1,
                1,
                Size.S,
                25000.0,
                100
        );

        when(productVarService.updateProduct(any(CRUDProductVarReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product Not Found"))
                .andDo(print());
    }

    @Test
    void updateProductVar_ProductVarConflict() throws Exception {
        CRUDProductVarReq req = new CRUDProductVarReq(
                1,
                1,
                Size.S,
                25000.0,
                100
        );

        when(productVarService.updateProduct(any(CRUDProductVarReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("Product Variant Size Already Exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("Product Variant Size Already Exists"))
                .andDo(print());
    }

    @Test
    void updateProductVar_ProductVarNotFound() throws Exception {
        CRUDProductVarReq req = new CRUDProductVarReq(
                1,
                1,
                Size.S,
                25000.0,
                100
        );

        when(productVarService.updateProduct(any(CRUDProductVarReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Variant Not Found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product Variant Not Found"))
                .andDo(print());
    }

    @Test
    void updateProduct_Success() throws Exception {
        CRUDProductReq crudProductReq = new CRUDProductReq(
                1,
                1,
                "Product1",
                "",
                ""
        );
        CRUDProductVarResponse response = new CRUDProductVarResponse(
                1,
                1,
                Size.S,
                25000.0,
                100,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(productVarService.updateProduct(any(CRUDProductVarReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(crudProductReq);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(25000.0))
                .andExpect(jsonPath("$.size").value("S"))
                .andDo(print());
    }

    @Test
    void listProduct_Success() throws Exception {
        CRUDProductVarResponse response1 = new CRUDProductVarResponse(
                1,
                1,
                Size.S,
                25000.0,
                100,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        CRUDProductVarResponse response2 = new CRUDProductVarResponse(
                2,
                1,
                Size.L,
                35000.0,
                150,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        ListProductVarResponse listProductResponse = new ListProductVarResponse(
                1,
                2,
                1,
                2,
                Arrays.asList(response1, response2)
        );
        when(productVarService.listProduct(anyString(), anyString())).thenReturn(listProductResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/list-product?page=1&limit=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productVarResponses.length()").value(2))
                .andExpect(jsonPath("$.productVarResponses[0].price").value(25000.0))
                .andExpect(jsonPath("$.productVarResponses[1].size").value("L"))
                .andDo(print());
    }
}