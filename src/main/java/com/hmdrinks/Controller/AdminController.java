package com.hmdrinks.Controller;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.Type_Post;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private  ShipmentService shipmentService;
    @Autowired
    private UserVoucherService userVoucherService;
    @Autowired
    private  PaymentService paymentService;

    @GetMapping("/list-image/{proId}")
    public ResponseEntity<?> getListImage(@PathVariable Integer proId){
        return ResponseEntity.ok(adminService.getAllProductImages(proId));
    }
    @GetMapping(value = "/listUser")
    public ResponseEntity<?> listAllUser(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    ) {
        return userService.getListAllUser(page, limit);
    }

    @GetMapping(value = "/listUser-role")
    public ResponseEntity<?> listAllUserByRole(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit,
            @RequestParam(name = "role") Role role
    ) {
        return userService.getListAllUserByRole(page, limit, role);
    }


    @PostMapping(value = "/create-account")
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountUserReq req){
        return ResponseEntity.ok(adminService.createAccountUser(req));
    }

    @GetMapping(value = "/search-user")
    public ResponseEntity<?> searchByUser(@RequestParam(name = "keyword") String keyword, @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit) {
        return ResponseEntity.ok(userService.totalSearchUser(keyword, page, limit));
    }
    @PutMapping(value = "/update-account") // Changed to PutMapping
    public ResponseEntity<?> updateAccount(@Valid @RequestBody UpdateAccountUserReq req) {
        return adminService.updateAccountUser(req);
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
    public ResponseEntity<?> getAllVoucher(
            @PathVariable Integer userId
    ){
        return  userVoucherService.listAllVoucherUserId(userId);
    }

    @GetMapping("/product/view/{id}")
    public ResponseEntity<CRUDProductResponse> update( @PathVariable Integer id){
        return ResponseEntity.ok(adminService.getOneProduct(id));
    }

    @GetMapping("/cate/view/{id}/product")
    public ResponseEntity<?> getALLProductFromCategory(@PathVariable Integer id,@RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit){
        return ResponseEntity.ok(adminService.getAllProductFromCategory(id,page,limit));
    }

    @GetMapping(value = "/post/view/all")
    public ResponseEntity<ListAllPostResponse> getAllPosts(@RequestParam(name = "page") String page,
                                                           @RequestParam(name = "limit") String limit){
        return  ResponseEntity.ok(adminService.getAllPost(page,limit));
    }

    @GetMapping(value = "/post/view/type/all")
    public ResponseEntity<ListAllPostResponse> getAllPostsByTye(@RequestParam(name = "page") String page,
                                                                @RequestParam(name = "limit") String limit,
                                                                @RequestParam(name = "type") Type_Post typePost){
        return  ResponseEntity.ok(adminService.getAllPostByType(page,limit,typePost));
    }

    @GetMapping(value = "/list-category")
    public ResponseEntity<ListCategoryResponse> listAllCategory(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    ) {
        return ResponseEntity.ok(adminService.listCategory(page, limit));
    }

    @PostMapping("/shipment/activate")
    public ResponseEntity<?> activeShipment(@RequestBody AdminActivateShipmentReq req) {
        return shipmentService.activate_Admin(req.getShipmentId(), req.getStatus());
    }

    @GetMapping("/list-payment-refund")
    public ResponseEntity<?> handleListPayment(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit) {
        return  paymentService.listAllPaymentRefund(page, limit);
    }

    @PutMapping("/activate/refund")
    public ResponseEntity<?> handleRefund(
            @RequestBody IdReq idReq) {
        return  paymentService.activateRefund(idReq.getId());
    }

}
