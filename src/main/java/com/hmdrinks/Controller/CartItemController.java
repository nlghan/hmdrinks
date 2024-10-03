package com.hmdrinks.Controller;

import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.CRUDCartItemResponse;
import com.hmdrinks.Response.CreateNewCartResponse;
import com.hmdrinks.Response.DeleteCartItemResponse;
import com.hmdrinks.Response.IncreaseDecreaseItemQuantityResponse;
import com.hmdrinks.Service.CartItemService;
import com.hmdrinks.Service.CartService;
import com.hmdrinks.Service.JwtService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/cart-item")
@RequiredArgsConstructor
public class CartItemController {
    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private SupportFunction supportFunction;
    @Autowired
    private JwtService jwtService;
    @PostMapping(value = "/insert")
    public ResponseEntity<CRUDCartItemResponse> create(@RequestBody InsertItemToCart req){
        return ResponseEntity.ok(cartItemService.insertCartItem(req));
    }

    @PutMapping(value = "/increase")
    public ResponseEntity<IncreaseDecreaseItemQuantityResponse> create(@RequestBody IncreaseDecreaseItemQuantityReq req,HttpServletRequest httpRequest){
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return ResponseEntity.ok(cartItemService.increaseCartItemQuantity(req));
    }

    @PutMapping(value = "/decrease")
    public ResponseEntity<IncreaseDecreaseItemQuantityResponse> decreaseItemQuantityResponseResponseEntity(@RequestBody IncreaseDecreaseItemQuantityReq req,HttpServletRequest httpRequest){
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return ResponseEntity.ok(cartItemService.decreaseCartItemQuantity(req));
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<DeleteCartItemResponse> deleteOneItem(@RequestBody DeleteOneCartItemReq req, HttpServletRequest httpRequest){

        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return ResponseEntity.ok(cartItemService.deleteOneItem(req));
    }


}
