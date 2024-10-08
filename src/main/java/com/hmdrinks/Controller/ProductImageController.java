package com.hmdrinks.Controller;

import com.hmdrinks.Request.DeleteAllCartItemReq;
import com.hmdrinks.Request.DeleteAllProductImgReq;
import com.hmdrinks.Request.DeleteOneCartItemReq;
import com.hmdrinks.Request.DeleteProductImgReq;
import com.hmdrinks.Response.DeleteCartItemResponse;
import com.hmdrinks.Service.ProductImageService;
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
@RequestMapping("/api/product-image")
public class ProductImageController {
    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private SupportFunction supportFunction;

    @DeleteMapping(value = "/delete-image")
    public ResponseEntity<?> deleteAllItem(@RequestBody DeleteProductImgReq req, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(productImageService.deleteImageFromProduct(req.getProId(), req.getSTT()));
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteOneItem(@RequestBody DeleteAllProductImgReq req, HttpServletRequest httpRequest){
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return ResponseEntity.ok(productImageService.deleteAllImageFromProduct(req.getProId()));
    }

}
