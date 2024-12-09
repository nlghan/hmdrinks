package com.hmdrinks.Controller;

import com.hmdrinks.Request.GetVoucherReq;
import com.hmdrinks.Response.GetVoucherResponse;
import com.hmdrinks.Response.ListAllVoucherUserIdResponse;
import com.hmdrinks.Service.UserVoucherService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/user-voucher")
public class UserVoucherController {
    @Autowired
    private UserVoucherService userVoucherService;
    @Autowired
    private SupportFunction supportFunction;

    @PostMapping(value = "/get-voucher")
    public ResponseEntity<?> createVoucher(@RequestBody GetVoucherReq req,HttpServletRequest httpRequest){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return userVoucherService.getVoucher(req);
    }

    @GetMapping(value ="/view-all/{id}")
    public ResponseEntity<?> getAllVoucher(
            @PathVariable Integer id, HttpServletRequest httpRequest
    ){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, id);

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return userVoucherService.listAllVoucherUserId(id);
    }
}