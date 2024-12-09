package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.CategoryController;
import com.hmdrinks.Controller.UserController;
import com.hmdrinks.Entity.Category;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Repository.CategoryRepository;
import com.hmdrinks.Repository.ProductRepository;
import com.hmdrinks.Repository.TokenRepository;
import com.hmdrinks.Request.CRUDCategoryRequest;
import com.hmdrinks.Request.ChangePasswordReq;
import com.hmdrinks.Request.CreateCategoryRequest;
import com.hmdrinks.Request.UserInfoUpdateReq;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.*;
import com.hmdrinks.SupportFunction.SupportFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import static java.nio.file.Paths.get;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebAppConfiguration
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {
    private static final String endPointPath="/api/cate";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private ProductRepository productRepository;
    @Autowired
    private ObjectMapper objectMapper;

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
    void createCategory_Success() throws Exception {
        CreateCategoryRequest req = new CreateCategoryRequest("New Category", "category_img.jpg");
        CRUDCategoryResponse response = new CRUDCategoryResponse(
                1,
                "New Category",
                "category_img.jpg",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(categoryService.crateCategory(any(CreateCategoryRequest.class)))
                .thenReturn((ResponseEntity) ResponseEntity.ok(response)); // Ensure correct mock behavior

        String requestBody = objectMapper.writeValueAsString(req);

        mockMvc.perform(post(endPointPath + "/create-category") // Check endPointPath value
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cateName").value(req.getCateName()))
                .andDo(print());
    }

    @Test
    void createCategory_Conflict() throws Exception {
        CreateCategoryRequest req = new CreateCategoryRequest("Existing Category", "category_img.jpg");
        when(categoryService.crateCategory(any(CreateCategoryRequest.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("cateName exists"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create-category")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("cateName exists"))
                .andDo(print());
    }

    @Test
    void getOneCategory_NotFound() throws Exception {
        int cateId = 1;
        when(categoryService.getOneCategory(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cate/view/{id}", 1)) // Sử dụng HTTP GET thay vì POST
                .andExpect(status().isNotFound())
                .andExpect(content().string("category not found"))
                .andDo(print());
    }

    @Test
    void getOneCategory_Success() throws Exception {
        int cateId = 1;
        CRUDCategoryResponse response = new CRUDCategoryResponse(
                1,
                "New Category",
                "category_img.jpg",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(categoryService.getOneCategory(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/cate/view/{id}", 1)) // Sử dụng HTTP GET thay vì POST
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cateName").value("New Category"))
                .andDo(print());
    }

    @Test
    void updateCategory_Success() throws Exception {
        CRUDCategoryRequest request = new CRUDCategoryRequest(
                1,
                "Yahana",
                ""
        );
        CRUDCategoryResponse response = new CRUDCategoryResponse(
                1,
                "New Category1",
                "category_img.jpg",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(categoryService.updateCategory(any(CRUDCategoryRequest.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(request);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cateName").value(response.getCateName()))
                .andDo(print());
    }

    @Test
    void updateCategory_NotFound() throws Exception {
        CRUDCategoryRequest request = new CRUDCategoryRequest(
                1,
                "Yahana",
                ""
        );
        when(categoryService.updateCategory(any(CRUDCategoryRequest.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not found"));
        String requestBody = objectMapper.writeValueAsString(request);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("category not found"))
                .andDo(print());
    }

    @Test
    void updateCategory_Conflict() throws Exception {
        CRUDCategoryRequest request = new CRUDCategoryRequest(
                1,
                "Yahana",
                ""
        );
        when(categoryService.updateCategory(any(CRUDCategoryRequest.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body("category already exists"));
        String requestBody = objectMapper.writeValueAsString(request);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("category already exists"))
                .andDo(print());
    }

    @Test
    void listCategory_Success() throws Exception {
        CRUDCategoryResponse response1 = new CRUDCategoryResponse(
                1,
                "New Category1",
                "category_img.jpg",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        CRUDCategoryResponse response2 = new CRUDCategoryResponse(
                2,
                "New Category2",
                "category_img.jpg",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        ListCategoryResponse listCategoryResponse = new ListCategoryResponse(
                1,
                2,
                1,
                2,
                Arrays.asList(response1, response2)
        );

        when(categoryService.listCategory(anyString(), anyString())).thenReturn(listCategoryResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/list-category?page=1&limit=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryResponseList.length()").value(2))
                .andDo(print());
    }

    @Test
    void getAllProductFromCategory_Success() throws Exception {
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        CRUDProductResponse response1 = new CRUDProductResponse(
                1,
                1,
                "Product1",
                productImageResponses,
                "",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>()

        );
        CRUDProductResponse response2 = new CRUDProductResponse(
                2,
                1,
                "Product2",
                productImageResponses,
                "",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>()

        );
        GetViewProductCategoryResponse listCategoryResponse = new GetViewProductCategoryResponse(
                1,
                2,
                1,
                2,
                Arrays.asList(response1, response2)
        );

        when(categoryService.getAllProductFromCategory(anyInt(),anyString(), anyString())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listCategoryResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/view/1/product?page=1&limit=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseList.length()").value(2))
                .andExpect(jsonPath("$.responseList[0].proId").value(1))
                .andExpect(jsonPath("$.responseList[0].proName").value("Product1"))
                .andDo(print());

    }

    @Test
    void getAllProductFromCategory_Notfound() throws Exception {
        when(categoryService.getAllProductFromCategory(anyInt(),anyString(), anyString())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not found"));

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/view/1/product?page=1&limit=1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("category not found"))
                .andDo(print());
    }

    @Test
    void TotalSearchCategory_Success() throws Exception {
        CRUDCategoryResponse response1 = new CRUDCategoryResponse(
                1,
                "New Category1",
                "category_img.jpg",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        CRUDCategoryResponse response2 = new CRUDCategoryResponse(
                2,
                "New Category2",
                "category_img.jpg",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        TotalSearchCategoryResponse listCategoryResponse = new TotalSearchCategoryResponse(
                1,
                2,
                1,
                2,
                Arrays.asList(response1, response2)
        );

        when(categoryService.totalSearchCategory(anyString(),anyInt(),anyInt())).thenReturn(listCategoryResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(endPointPath + "/search?keyword=a&page=1&limit=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryResponseList.length()").value(2)) // Kiểm tra độ dài danh sách categoryResponseList
                .andExpect(jsonPath("$.categoryResponseList[0].cateId").value(1)) // Kiểm tra cateId của phần tử đầu tiên
                .andExpect(jsonPath("$.categoryResponseList[1].cateName").value("New Category2")) // Kiểm tra proName của phần tử thứ hai
                .andDo(print());
    }
}