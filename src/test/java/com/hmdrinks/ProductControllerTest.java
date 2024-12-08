package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.CategoryController;
import com.hmdrinks.Controller.ProductController;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Enum.Size;
import com.hmdrinks.Repository.CategoryRepository;
import com.hmdrinks.Repository.ProductRepository;
import com.hmdrinks.Repository.TokenRepository;
import com.hmdrinks.Repository.UserRepository;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebAppConfiguration
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    private static final String endPointPath="/api/product";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private ProductRepository productRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Recommender recommender;

    @MockBean
    private ReviewService reviewService;
    @MockBean
    private UserRepository userRepository;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createProduct_Success() throws Exception {
        CreateProductReq createProductReq = new CreateProductReq(1,"Product1","","");
        List<ProductImageResponse> productImageResponse = new ArrayList<>();
        CRUDProductResponse response = new CRUDProductResponse(
                1,
                1,
                "Product1",
                productImageResponse,
                "",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>()

        );

        when(productService.crateProduct(any(CreateProductReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(createProductReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proName").value("Product1"))
                .andDo(print());
    }

    @Test
    void createProduct_CateIdNotFound() throws Exception {
        CreateProductReq createProductReq = new CreateProductReq(1,"Product1","","");
        when(productService.crateProduct(any(CreateProductReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("cateId not exists"));
        String requestBody = objectMapper.writeValueAsString(createProductReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("cateId not exists"))
                .andDo(print());
    }

    @Test
    void createProduct_ProductConflict() throws Exception {
        CreateProductReq createProductReq = new CreateProductReq(1,"Product1","","");
        when(productService.crateProduct(any(CreateProductReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("product already exists"));
        String requestBody = objectMapper.writeValueAsString(createProductReq);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("product already exists"))
                .andDo(print());
    }

    @Test
    void getOneProduct_NotFound() throws Exception {

        when(productService.getOneProduct(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists"));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/view/1")) // Sử dụng HTTP GET thay vì POST
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not exists"))
                .andDo(print());
    }

    @Test
    void getOneProduct_Success() throws Exception {
        List<ProductImageResponse> productImageResponse = new ArrayList<>();
        CRUDProductResponse response = new CRUDProductResponse(
                1,
                1,
                "Product1",
                productImageResponse,
                "",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>()
        );
        when(productService.getOneProduct(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/view/1")) // Sử dụng HTTP GET thay vì POST
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proName").value("Product1"))
                .andExpect(jsonPath("$.proId").value(1))
                .andDo(print());
    }

    @Test
    void updateProduct_CategoryNotExist() throws Exception {
        CRUDProductReq crudProductReq = new CRUDProductReq(
                1,
                1,
                "Product1",
                "",
                ""
        );
        when(productService.updateProduct(any(CRUDProductReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not exists"));
        String requestBody = objectMapper.writeValueAsString(crudProductReq);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("category not exists"))
                .andDo(print());
    }

    @Test
    void updateProduct_ProductConflict() throws Exception {
        CRUDProductReq crudProductReq = new CRUDProductReq(
                1,
                1,
                "Product1",
                "",
                ""
        );
        when(productService.updateProduct(any(CRUDProductReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("product already exists"));
        String requestBody = objectMapper.writeValueAsString(crudProductReq);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("product already exists"))
                .andDo(print());
    }

    @Test
    void updateProduct_ProductNotFound() throws Exception {
        CRUDProductReq crudProductReq = new CRUDProductReq(
                1,
                1,
                "Product1",
                "",
                ""
        );
        when(productService.updateProduct(any(CRUDProductReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists"));
        String requestBody = objectMapper.writeValueAsString(crudProductReq);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not exists"))
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
        List<ProductImageResponse> productImageResponse = new ArrayList<>();
        CRUDProductResponse response = new CRUDProductResponse(
                1,
                1,
                "Product1",
                productImageResponse,
                "",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>()

        );
        when(productService.updateProduct(any(CRUDProductReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(crudProductReq);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proName").value(response.getProName()))
                .andDo(print());
    }

    @Test
    void listProduct_Success() throws Exception {
        List<ProductImageResponse> productImageResponse = new ArrayList<>();
        CRUDProductResponse response1 = new CRUDProductResponse(
                1,
                1,
                "Product1",
                productImageResponse,
                "",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>()
        );
        CRUDProductResponse response2 = new CRUDProductResponse(
                1,
                1,
                "Product1",
                productImageResponse,
                "",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>()
        );
        ListProductResponse listProductResponse = new ListProductResponse(
                1,
                2,
                1,
                2,
                Arrays.asList(response1, response2)
        );
        when(productService.listProduct(anyString(), anyString())).thenReturn((ResponseEntity) ResponseEntity.ok().body(listProductResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/list-product?page=1&limit=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productResponses.length()").value(2))
                .andExpect(jsonPath("$.productResponses[0].proName").value("Product1"))
                .andDo(print());
    }

    @Test
    void getAllProductVariantFromProduct_Success() throws Exception {
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        CRUDProductVarResponse response1 = new CRUDProductVarResponse(
                1,
                1,
                Size.S,
                2000.0,
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
                2500.0,
                100,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        GetProductVariantFromProductIdResponse getProductVariantFromProductIdResponse = new GetProductVariantFromProductIdResponse(
                1,
                Arrays.asList(response1, response2)
        );

        when(productService.getAllProductVariantFromProduct(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(getProductVariantFromProductIdResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/variants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseList.length()").value(2))
                .andExpect(jsonPath("$.responseList[0].varId").value(1))
                .andExpect(jsonPath("$.responseList[0].size").value("S"))
                .andDo(print());
    }

    @Test
    void getAllProductVariantFromProduct_NotFound() throws Exception {
        when(productService.getAllProductVariantFromProduct(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists"));
        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/variants/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not exists"))
                .andDo(print());
    }

    @Test
    void TotalSearchProduct_Success() throws Exception {
        List<ProductImageResponse> productImageResponse = new ArrayList<>();
        CRUDProductResponse response1 = new CRUDProductResponse(
                1,
                1,
                "Product1",
                productImageResponse,
                "",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>()
        );
        CRUDProductResponse response2 = new CRUDProductResponse(
                1,
                1,
                "Product2",
                productImageResponse,
                "",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>()
        );
        TotalSearchProductResponse listCategoryResponse = new TotalSearchProductResponse(
                1,
                2,
                1,
                1,
                Arrays.asList(response1, response2)
        );

        when(productService.totalSearchProduct(anyString(),anyString(),anyString())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listCategoryResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/search?keyword=a&page=1&limit=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productResponseList.length()").value(2)) // Kiểm tra độ dài danh sách categoryResponseList
                .andExpect(jsonPath("$.productResponseList[0].proId").value(1))
                .andExpect(jsonPath("$.productResponseList[1].proName").value("Product2"))
                .andDo(print());
    }

//    @Test
//    void filterProduct_Success() throws Exception {
//        CRUDProductVarFilterResponse response1 = new CRUDProductVarFilterResponse(
//                4.5,
//                1,
//                1,
//                Size.S,
//                20000.0,
//                100,
//                false,
//                LocalDateTime.now(),
//                LocalDateTime.now(),
//                LocalDateTime.now()
//        );
//
//        CRUDProductVarFilterResponse response2 = new CRUDProductVarFilterResponse(
//                4.2,
//                2,
//                1,
//                Size.L,
//                25000.0,
//                100,
//                false,
//                LocalDateTime.now(),
//                LocalDateTime.now(),
//                LocalDateTime.now()
//        );
//        FilterProductBoxResponse filterProductBoxResponse = new FilterProductBoxResponse(
//                true,
//                2,
//                 Arrays.asList(response1, response2),
//                "OK",
//                false
//        );
//        FilterProductBox req = new FilterProductBox(
//                1,
//                Arrays.asList(1),
//                1
//        );
//
//        when(productService.filterProduct(any(FilterProductBox.class))).thenReturn((ResponseEntity) ResponseEntity.ok(filterProductBoxResponse));
//        String requestBody = objectMapper.writeValueAsString(req);
//
//        mockMvc.perform(post(endPointPath + "/filter-product") // Check endPointPath value
//                        .contentType(MediaType.APPLICATION_JSON_VALUE)
//                        .content(requestBody))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.total").value(2))
//                 .andExpect(jsonPath("$.productResponseList[0].varId").value(1))
//                 .andExpect(jsonPath("$.productResponseList[1].price").value(25000.0))
//                 .andDo(print());
//    }

    @Test
    void filterProduct_CategoryNotFound() throws Exception {
        FilterProductBox req = new FilterProductBox(
                1,
                Arrays.asList(1),
                1,
                "1",
                "1"
        );

        when(productService.filterProduct(any(FilterProductBox.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not exists"));
        String requestBody = objectMapper.writeValueAsString(req);

        mockMvc.perform(post(endPointPath + "/filter-product")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("category not exists"))
                .andDo(print());
    }

    @Test
    void filterProduct_ProductNotFound() throws Exception {
        FilterProductBox req = new FilterProductBox(
                1,
                Arrays.asList(1),
                1,
                "1",
                "1"
        );

        when(productService.filterProduct(any(FilterProductBox.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists"));
        String requestBody = objectMapper.writeValueAsString(req);

        mockMvc.perform(post(endPointPath + "/filter-product")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not exists"))
                .andDo(print());
    }

    @Test
    void filterProduct_BadReq() throws Exception {
        FilterProductBox req = new FilterProductBox(
                1,
                Arrays.asList(0),
                1,
                "1",
                "1"
        );

        when(productService.filterProduct(any(FilterProductBox.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("o must be greater than 0"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/filter-product")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("o must be greater than 0"))
                .andDo(print());
    }

    @Test
    void deleteAllImageFromProduct_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        DeleteAllProductImgReq req = new DeleteAllProductImgReq(1,1);
        ImgResponse imgResponse = new ImgResponse(
                ""
        );
        when(productService.deleteAllImageFromProduct(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(imgResponse));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(delete(endPointPath + "/image/deleteAll")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(""))
                .andDo(print());
    }

    @Test
    void deleteAllImageFromProduct_NotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        DeleteAllProductImgReq req = new DeleteAllProductImgReq(1,1);
        ImgResponse imgResponse = new ImgResponse(
                ""
        );
        when(productService.deleteAllImageFromProduct(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(delete(endPointPath + "/image/deleteAll")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not exists"))
                .andDo(print());
    }

    @Test
    void getAllProductImages_Success() throws Exception {
        ProductImageResponse productImageResponse = new ProductImageResponse(
                1,
                "https://example.com/image1.jpg"
        );
        ProductImageResponse productImageResponse2 = new ProductImageResponse(
                2,
                "https://example.com/image2.jpg"
        );


        ListProductImageResponse listProductImageResponse = new ListProductImageResponse(
                1,
                2,
                Arrays.asList(productImageResponse, productImageResponse2)
        );

        when(productService.getAllProductImages(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listProductImageResponse));
        mockMvc.perform(get(endPointPath + "/list-image/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productImageResponseList[0].id").value(1))
                .andExpect(jsonPath("$.productImageResponseList[0].linkImage").value("https://example.com/image1.jpg"))
                .andExpect(jsonPath("$.productImageResponseList[1].id").value(2))
                .andExpect(jsonPath("$.productImageResponseList[1].linkImage").value("https://example.com/image2.jpg"))
                .andDo(print());
    }


    @Test
    void getAllProductImages_ProductNotFound() throws Exception {
        when(productService.getAllProductImages(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists"));
        mockMvc.perform(get(endPointPath + "/list-image/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not exists"))
                .andDo(print());
    }

    @Test
    void deleteImageFromProduct_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);

        DeleteProductImgReq req = new DeleteProductImgReq(1, 1);

        Product mockProduct = new Product();
        mockProduct.setProId(1);
        mockProduct.setListProImg("1: https://example.com/image1.jpg, 2: https://example.com/image2.jpg");
        when(productRepository.findByProIdAndIsDeletedFalse(1)).thenReturn(mockProduct);

        List<ProductImageResponse> updatedImages = new ArrayList<>();
        updatedImages.add(new ProductImageResponse(1, "https://example.com/image2.jpg"));

        when(productService.deleteImageFromProduct(1, 1))
                .thenReturn((ResponseEntity )ResponseEntity.status(HttpStatus.OK)
                        .body(new ListProductImageResponse(1, 2,updatedImages)));

        String requestBody = objectMapper.writeValueAsString(req);

        // Thực hiện yêu cầu HTTP và kiểm tra kết quả
        mockMvc.perform(delete(endPointPath + "/image/deleteOne")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk()) // Kiểm tra trạng thái 200 OK
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productImageResponseList[0].id").value(1))
                .andExpect(jsonPath("$.productImageResponseList[0].linkImage").value("https://example.com/image2.jpg"))
                .andDo(print());
    }

    @Test
    void deleteImageFromProduct_NotFoundProduct() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        DeleteProductImgReq req = new DeleteProductImgReq(1, 1);
        when(productService.deleteImageFromProduct(1, 1))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(delete(endPointPath + "/image/deleteOne")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not exists"))
                .andDo(print());
    }

    @Test
    void deleteImageFromProduct_NoImage() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        DeleteProductImgReq req = new DeleteProductImgReq(1, 1);
        when(productService.deleteImageFromProduct(1, 1))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No images found for this product."));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(delete(endPointPath + "/image/deleteOne")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No images found for this product."))
                .andDo(print());
    }

}