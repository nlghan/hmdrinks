package com.hmdrinks.Controller;

import com.hmdrinks.Request.CreateNewCart;
import com.hmdrinks.Request.IncreaseDecreaseItemQuantityReq;
import com.hmdrinks.Request.InsertItemToCart;
import com.hmdrinks.Response.CRUDCartItemResponse;
import com.hmdrinks.Response.CreateNewCartResponse;
import com.hmdrinks.Response.IncreaseDecreaseItemQuantityResponse;
import com.hmdrinks.Service.CartItemService;
import com.hmdrinks.Service.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @PostMapping(value = "/insert")
    public ResponseEntity<CRUDCartItemResponse> create(@RequestBody InsertItemToCart req){
        return ResponseEntity.ok(cartItemService.insertCartItem(req));
    }

    @PutMapping(value = "/increase")
    public ResponseEntity<IncreaseDecreaseItemQuantityResponse> create(@RequestBody IncreaseDecreaseItemQuantityReq req){
        return ResponseEntity.ok(cartItemService.increaseCartItemQuantity(req));
    }

    @PutMapping(value = "/decrease")
    public ResponseEntity<IncreaseDecreaseItemQuantityResponse> decreaseItemQuantityResponseResponseEntity(@RequestBody IncreaseDecreaseItemQuantityReq req){
        return ResponseEntity.ok(cartItemService.decreaseCartItemQuantity(req));
    }
}
