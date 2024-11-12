package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.PostController;
import com.hmdrinks.Controller.ReviewController;
import com.hmdrinks.Enum.Type_Post;
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
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {
    private static final String endPointPath="/api/review";
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
    void createReview_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateNewReview req = new CreateNewReview(1,1,"String",4);
        CRUDReviewResponse reviewResponse = new CRUDReviewResponse(
                1,
                1,
                1,
                "Vo Nhu Y",
                "",
                4,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(reviewService.createReview(any(CreateNewReview.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(reviewResponse));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.fullName").value("Vo Nhu Y"))
                .andDo(print());
    }

    @Test
    void createReview_UserIdNotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateNewReview req = new CreateNewReview(1,1,"String",4);

        when(reviewService.createReview(any(CreateNewReview.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User is deleted"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User is deleted"))
                .andDo(print());
    }

    @Test
    void createReview_NotfoundProduct() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateNewReview req = new CreateNewReview(1,1,"String",4);

        when(reviewService.createReview(any(CreateNewReview.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not fount product"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not fount product"))
                .andDo(print());
    }

    @Test
    void updateReview_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CRUDReviewReq req = new CRUDReviewReq(1,1,1,"String",3);
        CRUDReviewResponse reviewResponse = new CRUDReviewResponse(
                1,
                1,
                1,
                "Vo Nhu Y",
                "",
                3,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(reviewService.updateReview(any(CRUDReviewReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(reviewResponse));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.ratingStart").value(3))
                .andDo(print());
    }

    @Test
    void updateReview_NotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CRUDReviewReq req = new CRUDReviewReq(1,1,1,"String",3);
        when(reviewService.updateReview(any(CRUDReviewReq.class)))
                .thenReturn((ResponseEntity)  ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Review not found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Review not found"))
                .andDo(print());
    }

    @Test
    void deleteOneReview_Success() throws Exception {
        DeleteReviewReq req = new DeleteReviewReq(1,2);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);

        when(reviewService.deleteOneReview(any(DeleteReviewReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body("Review deleted successfully"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(delete(endPointPath + "/delete")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Review deleted successfully"))
                .andDo(print());
    }

    @Test
    void deleteOneReview_NotFound() throws Exception {
        DeleteReviewReq req = new DeleteReviewReq(1,2);
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);

        when(reviewService.deleteOneReview(any(DeleteReviewReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Review not found"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(delete(endPointPath + "/delete")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Review not found"))
                .andDo(print());
    }

}