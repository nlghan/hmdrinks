package com.hmdrinks.Controller;

import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.VoucherService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/voucher")
public class VoucherController {
    @Autowired
    private VoucherService voucherService;

    @PostMapping(value = "/create")
    public ResponseEntity<?> createVoucher(@RequestBody CreateVoucherReq req){
        return ResponseEntity.ok(voucherService.createVoucher(req));
    }

    @GetMapping(value ="/view/{id}")
    public ResponseEntity<?> getOneVoucher(
            @PathVariable Integer id,HttpServletRequest httpRequest
    ){
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @PostMapping(value ="/user/key")
    public ResponseEntity<?> getOneVoucher1(
            @RequestBody GetVoucherKeyReq req, HttpServletRequest httpRequest
    ){
        return voucherService.getVoucherByKey(req.getKeyVoucher(), req.getUserId());
    }

    @GetMapping(value = "/view/all")
    public ResponseEntity<?> getAllVouchers(){
        return  ResponseEntity.ok(voucherService.listAllVoucher());
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> updatePost(
            @RequestBody CrudVoucherReq req, HttpServletRequest httpRequest
    )
    {
        return ResponseEntity.ok(voucherService.updateVoucher(req));
    }

    @PutMapping(value = "/enable")
    public ResponseEntity<?> enableVoucher(@RequestBody IdReq req, HttpServletRequest httpRequest) {
        return  voucherService.enableVoucher(req.getId());
    }

    @PutMapping(value = "/disable")
    public ResponseEntity<?> disableVoucher(@RequestBody IdReq req, HttpServletRequest httpRequest) {
        return  voucherService.disableVoucher(req.getId());
    }
}