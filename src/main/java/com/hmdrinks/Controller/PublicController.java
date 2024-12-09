package com.hmdrinks.Controller;

import com.hmdrinks.Request.ForgetPasswordReq;
import com.hmdrinks.Request.ForgetPasswordSendReq;
import com.hmdrinks.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    @Autowired
    private UserService userService;

    @PostMapping("/password/forget/send")
    public ResponseEntity<?> getPassword(@RequestBody ForgetPasswordReq forgetPasswordReq) {
        return ResponseEntity.ok(userService.sendEmail(forgetPasswordReq.getEmail()));
    }

    @PostMapping("/password/acceptOtp")
    public ResponseEntity<?> getSendPassword(@RequestBody ForgetPasswordSendReq Req) {
        return ResponseEntity.ok(userService.AcceptOTP(Req.getEmail(), Req.getOtp()));
    }
}