package com.hmdrinks.Controller;

import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.UserService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private SupportFunction supportFunction;

    @GetMapping(value ="/info/{id}")
    public ResponseEntity<?> getDetailUserInfoResponseResponseEntity(
            @PathVariable Integer id,HttpServletRequest httpRequest
            ){
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(id));
        return userService.getDetailUserInfoResponse(id);
    }

    @PutMapping(value = "/info-update")
    public ResponseEntity<?> updateUserInfoResponseResponseEntity(
            @RequestBody UserInfoUpdateReq req, HttpServletRequest httpRequest
            ){
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return userService.updateUserInfoResponse(req);
    }

    @PutMapping("/password/change")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordReq req,HttpServletRequest httpRequest) {
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return userService.changePasswordResponse(req);
    }
}