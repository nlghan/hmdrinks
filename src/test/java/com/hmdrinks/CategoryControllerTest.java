package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.CategoryController;
import com.hmdrinks.Controller.UserController;
import com.hmdrinks.Entity.Category;
import com.hmdrinks.Repository.CategoryRepository;
import com.hmdrinks.Repository.TokenRepository;
import com.hmdrinks.Request.ChangePasswordReq;
import com.hmdrinks.Request.CreateCategoryRequest;
import com.hmdrinks.Request.UserInfoUpdateReq;
import com.hmdrinks.Response.CRUDCategoryResponse;
import com.hmdrinks.Response.ChangePasswordResponse;
import com.hmdrinks.Response.UpdateUserInfoResponse;
import com.hmdrinks.Service.*;
import com.hmdrinks.SupportFunction.SupportFunction;
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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

}
