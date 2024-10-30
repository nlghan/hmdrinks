package com.hmdrinks.Controller;

import com.hmdrinks.Request.LoginBasicReq;
import com.hmdrinks.Request.UserCreateReq;
import com.hmdrinks.Response.AuthenticationResponse;
import com.hmdrinks.Service.AuthenticationService;
import com.hmdrinks.Service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth") // Keep it only for the secured endpoint
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/oauth2")
    public String secured()
    {
        return "hello";
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody LoginBasicReq request
    ) {
        return authenticationService.authenticate(request);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody UserCreateReq req
    ){
        return authenticationService.register(req);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return authenticationService.refreshToken(request, response);
    }
}
