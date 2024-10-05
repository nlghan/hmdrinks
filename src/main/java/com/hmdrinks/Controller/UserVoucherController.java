package com.hmdrinks.Controller;

import com.hmdrinks.Request.CreateVoucherReq;
import com.hmdrinks.Request.GetVoucherReq;
import com.hmdrinks.Response.CRUDVoucherResponse;
import com.hmdrinks.Response.GetVoucherResponse;
import com.hmdrinks.Response.ListAllVoucherUserIdResponse;
import com.hmdrinks.Service.UserVoucherService;
import com.hmdrinks.Service.VoucherService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private VoucherService voucherService;

    @PostMapping(value = "/get-voucher")
    public ResponseEntity<GetVoucherResponse> createVoucher(@RequestBody GetVoucherReq req,HttpServletRequest httpRequest){
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return ResponseEntity.ok(userVoucherService.getVoucher(req));
    }

    @GetMapping(value ="/view-all/{id}")
    public ResponseEntity<ListAllVoucherUserIdResponse> getAllVoucher(
            @PathVariable Integer id, HttpServletRequest httpRequest
    ){
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(id));
        return ResponseEntity.ok(userVoucherService.listAllVoucherUserId(id));
    }
}
