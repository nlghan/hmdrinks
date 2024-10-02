package com.hmdrinks.Controller;

import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.AuthenticationService;
import com.hmdrinks.Service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;



@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private UserService userService;






    @GetMapping(value ="/info/{id}")
    public ResponseEntity<GetDetailUserInfoResponse> getDetailUserInfoResponseResponseEntity(
            @PathVariable Integer id
            ){
        return ResponseEntity.ok(userService.getDetailUserInfoResponse(id));
    }

    @PutMapping(value = "/info-update")
    public ResponseEntity<UpdateUserInfoResponse> updateUserInfoResponseResponseEntity(
            @RequestBody UserInfoUpdateReq req
            ){
        return ResponseEntity.ok(userService.updateUserInfoResponse(req));
    }

    @PutMapping("/password/change")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordReq req) {
        return ResponseEntity.ok(userService.changePasswordResponse(req));
    }

}
