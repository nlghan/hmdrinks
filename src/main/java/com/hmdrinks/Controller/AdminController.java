package com.hmdrinks.Controller;

import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.AdminService;
import com.hmdrinks.Service.ReviewService;
import com.hmdrinks.Service.UserService;
import com.hmdrinks.Service.UserVoucherService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private UserVoucherService userVoucherService;

    @GetMapping("/list-image/{proId}")
    public ResponseEntity<?> getListImage(@PathVariable Integer proId){
        return ResponseEntity.ok(adminService.getAllProductImages(proId));
    }
    @GetMapping(value = "/listUser")
    public ResponseEntity<ListAllUserResponse> listAllUser(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    ) {
        return ResponseEntity.ok(userService.getListAllUser(page, limit));
    }

    @PostMapping(value = "/create-account")
    public ResponseEntity<CRUDAccountUserResponse> createAccount(@RequestBody CreateAccountUserReq req){
        return ResponseEntity.ok(adminService.createAccountUser(req));
    }

    @GetMapping(value = "/search-user")
    public ResponseEntity<?> searchByUser(@RequestParam(name = "keyword") String keyword, @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit) {
        return ResponseEntity.ok(userService.totalSearchUser(keyword, page, limit));
    }
    @PutMapping(value = "/update-account") // Changed to PutMapping
    public ResponseEntity<CRUDAccountUserResponse> updateAccount(@Valid @RequestBody UpdateAccountUserReq req) {
        return ResponseEntity.ok(adminService.updateAccountUser(req));
    }

    @DeleteMapping(value = "/product/review/deleteOne")
    public ResponseEntity<?> deleteReview(@RequestBody IdReq req, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(adminService.deleteOneReview(req.getId()));
    }

    @DeleteMapping(value = "/product/review/deleteAll")
    public ResponseEntity<?> deleteAllReview(@RequestBody IdReq req, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(adminService.deleteALlReviewProduct(req.getId()));
    }

    @PostMapping("/filter-product")
    public ResponseEntity<FilterProductBoxResponse> filterProduct(
            @RequestBody FilterProductBox req
    ) {

        return ResponseEntity.ok(adminService.filterProduct(req));
    }

    @GetMapping(value = "/list-product")
    public ResponseEntity<ListProductResponse> listAllProduct(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    ) {
        return ResponseEntity.ok(adminService.listProduct(page, limit));
    }

    @GetMapping(value = "/search-product")
    public ResponseEntity<?> searchByCategoryName(@RequestParam(name = "keyword") String keyword, @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit) {
        return ResponseEntity.ok(adminService.totalSearchProduct(keyword,page,limit));
    }

    @GetMapping( value = "/product/variants/{id}")
    public ResponseEntity<GetProductVariantFromProductIdResponse> viewProduct(@PathVariable Integer id){
        return ResponseEntity.ok(adminService.getAllProductVariantFromProduct(id));
    }

    @GetMapping(value ="/list-voucher/{userId}")
    public ResponseEntity<ListAllVoucherUserIdResponse> getAllVoucher(
            @PathVariable Integer userId
    ){
        return ResponseEntity.ok(userVoucherService.listAllVoucherUserId(userId));
    }

    @GetMapping("/product/view/{id}")
    public ResponseEntity<CRUDProductResponse> update( @PathVariable Integer id){
        return ResponseEntity.ok(adminService.getOneProduct(id));
    }

}