package com.hmdrinks.Controller;

import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.ProductService;
import com.hmdrinks.Service.ReviewService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private SupportFunction supportFunction;

    @PostMapping(value = "/create")
    public ResponseEntity<?> createProduct(@RequestBody CreateProductReq req){
        return productService.crateProduct(req);
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> update(@RequestBody CRUDProductReq req){
        return productService.updateProduct(req);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> update( @PathVariable Integer id){
        return productService.getOneProduct(id);
    }

    @GetMapping(value = "/list-product")
    public ResponseEntity<?> listAllProduct(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    ) {
        return productService.listProduct(page, limit);
    }

    @GetMapping( value = "/variants/{id}")
    public ResponseEntity<?> viewProduct(@PathVariable Integer id){
        return productService.getAllProductVariantFromProduct(id);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<?> searchByCategoryName(@RequestParam(name = "keyword") String keyword, @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit) {
        return productService.totalSearchProduct(keyword,page,limit);
    }

    @GetMapping(value = "/list-review")
    public ResponseEntity<?> listReview(@RequestParam(name = "proId") Integer proId, @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit) {
        return reviewService.getAllReview(page,limit,proId);
    }

    @GetMapping("/list-image/{proId}")
    public ResponseEntity<?> getListImage(@PathVariable Integer proId){
        return productService.getAllProductImages(proId);
    }

    @PostMapping("/filter-product")
    public ResponseEntity<?> filterProduct(
           @RequestBody FilterProductBox req
            ) {

        return productService.filterProduct(req);
    }

    @DeleteMapping(value = "/image/deleteOne")
    public ResponseEntity<?> deleteAllItem(@RequestBody DeleteProductImgReq req, HttpServletRequest httpRequest) {
        return productService.deleteImageFromProduct(req.getProId(), req.getId());
    }

    @DeleteMapping(value = "/image/deleteAll")
    public ResponseEntity<?> deleteOneItem(@RequestBody DeleteAllProductImgReq req, HttpServletRequest httpRequest){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return productService.deleteAllImageFromProduct(req.getProId());
    }

    @GetMapping(value = "/reset")
    public ResponseEntity<?> resetQuantityProduct() {
        return productService.resetAllQuantityProduct();
    }
}