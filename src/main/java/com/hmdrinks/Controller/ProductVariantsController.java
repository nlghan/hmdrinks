package com.hmdrinks.Controller;

import com.hmdrinks.Request.CRUDProductReq;
import com.hmdrinks.Request.CRUDProductVarReq;
import com.hmdrinks.Request.CreateProductReq;
import com.hmdrinks.Request.CreateProductVarReq;
import com.hmdrinks.Response.CRUDProductResponse;
import com.hmdrinks.Response.CRUDProductVarResponse;
import com.hmdrinks.Response.ListProductResponse;
import com.hmdrinks.Response.ListProductVarResponse;
import com.hmdrinks.Service.ProductService;
import com.hmdrinks.Service.ProductVarService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")

@RequestMapping("/api/productVar")
@RequiredArgsConstructor
public class ProductVariantsController {
    @Autowired
    private ProductVarService productVarService;
    @PostMapping(value = "/create")
    public ResponseEntity<?> createProductVariants(@RequestBody CreateProductVarReq req){
        return productVarService.crateProductVariants(req);
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> updateProductVariants(@RequestBody CRUDProductVarReq req){
        return productVarService.updateProduct(req);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> getOneProductVariants( @PathVariable Integer id){
        return productVarService.getOneVarProduct(id);
    }

    @GetMapping(value = "/list-product")
    public ResponseEntity<ListProductVarResponse> listAllProductVariants(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    ) {
        return ResponseEntity.ok(productVarService.listProduct(page,limit));
    }
}
