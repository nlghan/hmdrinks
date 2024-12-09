package com.hmdrinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.CartController;
import com.hmdrinks.Controller.PostController;
import com.hmdrinks.Enum.Size;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Enum.Status_Voucher;
import com.hmdrinks.Enum.Type_Post;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.CRUDPostReq;
import com.hmdrinks.Request.CreateNewCart;
import com.hmdrinks.Request.CreateNewPostReq;
import com.hmdrinks.Request.DeleteAllCartItemReq;
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
@WebMvcTest(PostController.class)
class PostControllerTest {
    private static final String endPointPath="/api/post";
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
    void createPost_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateNewPostReq req = new CreateNewPostReq("string","string","string","string", Type_Post.EVENT,3);
        CRUDPostResponse response = new CRUDPostResponse(
                1,
                Type_Post.EVENT,
                "string",
                "string",
                "string",
                "string",
                3,
                false,
                null,
                LocalDateTime.now()
        );

        when(postService.createPost(any(CreateNewPostReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(3))
                .andDo(print());
    }

    @Test
    void createPost_UserIdNotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateNewPostReq req = new CreateNewPostReq("string","string","string","string", Type_Post.EVENT,3);

        when(postService.createPost(any(CreateNewPostReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found user"))
                .andDo(print());
    }

    @Test
    void createPost_RoleNotAdmin() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CreateNewPostReq req = new CreateNewPostReq("string","string","string","string", Type_Post.EVENT,3);

        when(postService.createPost(any(CreateNewPostReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not allowed to create a post"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(post(endPointPath + "/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("You are not allowed to create a post"))
                .andDo(print());
    }

    @Test
    void getOnePost_NotFound() throws Exception {
        int postId = 1;
        when(postService.getPostById(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found post"));

        mockMvc.perform(get("/api/post/view/{id}",1))
                .andExpect(jsonPath("$.body").value("Not found post"))  // body chứa thông báo lỗi
                .andExpect(jsonPath("$.statusCode").value("NOT_FOUND"))  // mã trạng thái dạng chuỗi
                .andExpect(jsonPath("$.statusCodeValue").value(404))
                .andDo(print());
    }

    @Test
    void getOnePost_Success() throws Exception {
        int cateId = 1;
        CRUDPostResponse response = new CRUDPostResponse(
                1,
                Type_Post.EVENT,
                "string",
                "string",
                "string",
                "string",
                3,
                false,
                null,
                LocalDateTime.now()
        );
        when(postService.getPostById(anyInt()))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        mockMvc.perform(get("/api/post/view/{id}", 1)) // Sử dụng HTTP GET thay vì POST
                .andExpect(jsonPath("$.body.postId").value(1))
                .andExpect(jsonPath("$.body.typePost").value("EVENT"))// body chứa thông báo lỗi
                .andExpect(jsonPath("$.statusCode").value("OK"))  // mã trạng thái dạng chuỗi
                .andExpect(jsonPath("$.statusCodeValue").value(200))
                .andDo(print());
    }

    @Test
    void updatePost_Success() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CRUDPostReq req = new CRUDPostReq(1,"string","string","string","string", Type_Post.EVENT,3);
        CRUDPostResponse response = new CRUDPostResponse(
                1,
                Type_Post.EVENT,
                "string",
                "string",
                "string",
                "string",
                3,
                false,
                null,
                LocalDateTime.now()


        );
        when(postService.updatePost(any(CRUDPostReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(3))
                .andDo(print());
    }

    @Test
    void updatePost_NotFoundPost() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CRUDPostReq req = new CRUDPostReq(1,"string","string","string","string", Type_Post.EVENT,3);

        when(postService.updatePost(any(CRUDPostReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found post"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found post"))
                .andDo(print());
    }

    @Test
    void updatePost_NotFoundUser() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CRUDPostReq req = new CRUDPostReq(1,"string","string","string","string", Type_Post.EVENT,3);

        when(postService.updatePost(any(CRUDPostReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found user"))
                .andDo(print());
    }

    @Test
    void updatePost_RoleNotAllow() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        CRUDPostReq req = new CRUDPostReq(1,"string","string","string","string", Type_Post.EVENT,3);

        when(postService.updatePost(any(CRUDPostReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not allowed to update a post"));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("You are not allowed to update a post"))
                .andDo(print());
    }

    @Test
    void listPostByTye_Success() throws Exception {
        CRUDPostAndVoucherResponse response1 = new CRUDPostAndVoucherResponse(
                1,
                Type_Post.EVENT,
                "string",
                "string",
                "string",
                "string",
                3,
                false,
                null,
                LocalDateTime.now(),
                new CRUDVoucherResponse(
                        1,
                        "Voucher123",
                        50,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        10000.0,
                        Status_Voucher.ACTIVE,
                        1
                )
        );
        CRUDPostAndVoucherResponse response2 = new CRUDPostAndVoucherResponse(
                2,
                Type_Post.EVENT,
                "string123",
                "string",
                "string",
                "string",
                5,
                false,
                null,
                LocalDateTime.now(),
                new CRUDVoucherResponse(
                        2,
                        "Voucher123a",
                        500,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        10000.0,
                        Status_Voucher.ACTIVE,
                        2
                )
        );

        ListAllPostResponse listAllPostResponse = new ListAllPostResponse(
                1,
                2,
                1,
                2,
                Arrays.asList(response1, response2)
        );


        when(postService.getAllPostByType(anyString(), anyString(),any(Type_Post.class))).thenReturn(listAllPostResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/post/view/type/all")
                .param("page", "1")
                .param("limit", "10")
                .param("type", "EVENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listPosts.length()").value(2))
                .andExpect(jsonPath("$.listPosts[0].postId").value(1))
                .andDo(print());
    }

    @Test
    void listPost_Success() throws Exception {
        CRUDPostAndVoucherResponse response1 = new CRUDPostAndVoucherResponse(
                1,
                Type_Post.EVENT,
                "string",
                "string",
                "string",
                "string",
                3,
                false,
                null,
                LocalDateTime.now(),
                new CRUDVoucherResponse(
                        1,
                        "Voucher123",
                        50,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        10000.0,
                        Status_Voucher.ACTIVE,
                        1
                )
        );
        CRUDPostAndVoucherResponse response2 = new CRUDPostAndVoucherResponse(
                2,
                Type_Post.EVENT,
                "string123",
                "string",
                "string",
                "string",
                5,
                false,
                null,
                LocalDateTime.now(),
                new CRUDVoucherResponse(
                        2,
                        "Voucher123a",
                        500,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        10000.0,
                        Status_Voucher.ACTIVE,
                        2
                )
        );

        ListAllPostResponse listAllPostResponse = new ListAllPostResponse(
                1,
                2,
                1,
                2,
                Arrays.asList(response1, response2)
        );


        when(postService.getAllPost(anyString(), anyString())).thenReturn(listAllPostResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/post/view/all")
                        .param("page", "1")
                        .param("limit", "10"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listPosts.length()").value(2))
                .andExpect(jsonPath("$.listPosts[0].postId").value(1))
                .andDo(print());
    }

    @Test
    void listPostByUserId_Success() throws Exception {
        CRUDPostResponse response1 = new CRUDPostResponse(
                1,
                Type_Post.EVENT,
                "string",
                "string",
                "string",
                "string",
                3,
                false,
                null,
                LocalDateTime.now()
        );
        CRUDPostResponse response2 = new CRUDPostResponse(
                2,
                Type_Post.EVENT,
                "string123",
                "string",
                "string",
                "string",
                3,
                false,
                null,
                LocalDateTime.now()
        );

        ListAllPostByUserIdResponse listAllPostResponse = new ListAllPostByUserIdResponse(
                3,
                2,
                Arrays.asList(response1, response2)
        );

        when(postService.listAllPostByUserId(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(listAllPostResponse));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/post/view/author/{userId}",3)
                       )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listPosts.length()").value(2))
                .andExpect(jsonPath("$.listPosts[1].postId").value(2))
                .andDo(print());
    }

    @Test
    void listPostByUserId_NotFound() throws Exception {
        when(postService.listAllPostByUserId(anyInt())).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/post/view/author/{userId}",3)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found user"))
                .andDo(print());
    }

}