package com.hmdrinks;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdrinks.Controller.UserController;
import com.hmdrinks.Repository.TokenRepository;
import com.hmdrinks.Request.ChangePasswordReq;
import com.hmdrinks.Request.UserInfoUpdateReq;
import com.hmdrinks.Response.ChangePasswordResponse;
import com.hmdrinks.Response.UpdateUserInfoResponse;
import com.hmdrinks.Service.JwtService;
import com.hmdrinks.Service.UserInfoService;
import com.hmdrinks.Service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebAppConfiguration
@WebMvcTest(UserController.class)
class UserControllerTest {
    private static final String endPointPath="/api/user";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;
    @MockBean
    private SupportFunction supportFunction;

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
    void updateUser_Successfully() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        String dateString = "15/11/2002";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = formatter.parse(dateString);
        UserInfoUpdateReq req = new UserInfoUpdateReq(
                1,
                "ohhhchank3@gmail.com",
                "Vo Nhu Y",
                "0777464215",
                "",
                "MALE",
                date,
                "99 đường số 7, Linh Trung, Thủ Đức, Hồ Chí Minh"
        );
        UpdateUserInfoResponse response = new UpdateUserInfoResponse(
                1,
                "ohhhchank3",
                "Vo Nhu Y",
                "",
                date,
                "99 đường số 7, Linh Trung, Thủ Đức, Hồ Chí Minh",
                "ohhhchank3@gmail.com",
                "0777464215",
                "MALE",
                "",
                false,
                date,
                date,
                date,
                "CUSTOMER"
        );
        when(userService.updateUserInfoResponse(any(UserInfoUpdateReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.ok(response));

        String requestBody = objectMapper.writeValueAsString(req);

        mockMvc.perform(put(endPointPath + "/info-update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("ohhhchank3"))
                .andDo(print());
    }

    @Test
    void updateUser_NotFound() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        String dateString = "15/11/2002";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = formatter.parse(dateString);
        UserInfoUpdateReq req = new UserInfoUpdateReq(
                1,
                "ohhhchank3@gmail.com",
                "Vo Nhu Y",
                "0777464215",
                "",
                "MALE",
                date,
                "99 đường số 7, Linh Trung, Thủ Đức, Hồ Chí Minh"
        );
        Map<String, String> response = new HashMap<>();
        response.put("message", "User not found");
        when(userService.updateUserInfoResponse(any(UserInfoUpdateReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/info-update")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"))
                .andDo(print());
    }

    @Test
    void updateUser_InvalidAddressFormat() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        String dateString = "15/11/2002";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = formatter.parse(dateString);
        UserInfoUpdateReq req = new UserInfoUpdateReq(
                1,
                "ohhhchank3@gmail.com",
                "Vo Nhu Y",
                "0777464215",
                "",
                "MALE",
                date,
                "99 đường số 7, Linh Trung, Thủ Đức"
        );

        when(userService.updateUserInfoResponse(any(UserInfoUpdateReq.class)))
                .thenReturn( (ResponseEntity)ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Invalid address format"));

        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/info-update")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string("Invalid address format"))
                .andDo(print());
    }


    @Test
    void updateUser_EmailExists() throws Exception {
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        String dateString = "15/11/2002";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = formatter.parse(dateString);

        UserInfoUpdateReq req = new UserInfoUpdateReq(
                1,
                "ohhhchank3@gmail.com",
                "Vo Nhu Y",
                "0777464215",
                "",
                "MALE",
                date,
                "99 đường số 7, Linh Trung, Thủ Đức, Hồ Chí Minh"
        );
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email already exists");
        when(userService.updateUserInfoResponse(any(UserInfoUpdateReq.class)))
                .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT).body(response));

        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath + "/info-update")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"))
                .andDo(print());
    }

    @Test
    void changePassword_Successfully() throws Exception{
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        ChangePasswordReq req=new ChangePasswordReq(1,"current","newpassword","newpassword");
        ChangePasswordResponse res=new ChangePasswordResponse("Password change successfully","newpassword");
        when(userService.changePasswordResponse(any(ChangePasswordReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.OK).body(res));
        String requestBody= objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath+"/password/change").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newPass").value("newpassword"))
                .andDo(print());
    }

    @Test
    void changePassword_NotFound() throws Exception{
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        ChangePasswordReq req=new ChangePasswordReq(1,"current","newpassword","newpassword");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Not found user");
        when(userService.changePasswordResponse(any(ChangePasswordReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
        String requestBody= objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath+"/password/change").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found user"))
                .andDo(print());
    }

    @Test
    void changePassword_CurrentPasswordIncorrect() throws Exception{
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        ChangePasswordReq req=new ChangePasswordReq(1,"current","newpassword","newpassword");
        Map<String, String> response = new HashMap<>();
        response.put("message", "The current password is incorrect");
        when(userService.changePasswordResponse(any(ChangePasswordReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
        String requestBody= objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath+"/password/change").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The current password is incorrect"))
                .andDo(print());
    }

    @Test
    void changePassword_UsingOldPassword() throws Exception{
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        ChangePasswordReq req=new ChangePasswordReq(1,"current","newpassword","newpassword");
        Map<String, String> response = new HashMap<>();
        response.put("message", "You're using an old password");
        when(userService.changePasswordResponse(any(ChangePasswordReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
        String requestBody= objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath+"/password/change").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You're using an old password"))
                .andDo(print());
    }

    @Test
    void changePassword_NoOverlap() throws Exception{
        ResponseEntity<?> mockAuthResponse = ResponseEntity.ok().build();
        when(supportFunction.checkUserAuthorization(any(HttpServletRequest.class), anyInt())).thenReturn((ResponseEntity) mockAuthResponse);
        ChangePasswordReq req=new ChangePasswordReq(1,"current","newpassword","newpassword");
        Map<String, String> response = new HashMap<>();
        response.put("message", "No overlap");
        when(userService.changePasswordResponse(any(ChangePasswordReq.class))).thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
        String requestBody= objectMapper.writeValueAsString(req);
        mockMvc.perform(put(endPointPath+"/password/change").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No overlap"))
                .andDo(print());
    }
}
