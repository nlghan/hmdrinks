package com.hmdrinks.Controller;

import com.hmdrinks.Request.CRUDCategoryRequest;
import com.hmdrinks.Request.CRUDProductReq;
import com.hmdrinks.Request.CreateCategoryRequest;
import com.hmdrinks.Request.CreateProductReq;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.ProductImageService;
import com.hmdrinks.Service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ProductImageService productImageService;
    @PostMapping(value = "/create")
    public ResponseEntity<CRUDProductResponse> createAccount(@RequestBody CreateProductReq req){
        return ResponseEntity.ok(productService.crateProduct(req));
    }

    @PutMapping(value = "/update")
    public ResponseEntity<CRUDProductResponse> update(@RequestBody CRUDProductReq req){
        return ResponseEntity.ok(productService.updateProduct(req));
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<CRUDProductResponse> update( @PathVariable Integer id){
        return ResponseEntity.ok(productService.getOneProduct(id));
    }

    @GetMapping(value = "/list-product")
    public ResponseEntity<ListProductResponse> listAllProduct(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    ) {
        return ResponseEntity.ok(productService.listProduct(page, limit));
    }

    @GetMapping( value = "/variants/{id}")
    public ResponseEntity<GetProductVariantFromProductIdResponse> viewProduct(@PathVariable Integer id){
        return ResponseEntity.ok(productService.getAllProductVariantFromProduct(id));
    }

    @GetMapping(value = "/search")
    public ResponseEntity<?> searchByCategoryName(@RequestParam(name = "keyword") String keyword, @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit) {
        return ResponseEntity.ok(productService.totalSearchProduct(keyword,page,limit));
    }

    @GetMapping("/list-image/{proId}")
    public ResponseEntity<?> getListImage(@PathVariable Integer proId){
        return ResponseEntity.ok(productImageService.getAllProductImages(proId));
    }
}
