package com.hmdrinks.Controller;

import com.hmdrinks.Request.CreateNewCart;
import com.hmdrinks.Request.CreateProductReq;
import com.hmdrinks.Response.CRUDProductResponse;
import com.hmdrinks.Response.CreateNewCartResponse;
import com.hmdrinks.Response.ListItemCartResponse;
import com.hmdrinks.Response.ListProductResponse;
import com.hmdrinks.Service.CartService;
import com.hmdrinks.Service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    @Autowired
    private CartService cartService;
    @PostMapping(value = "/create")
    public ResponseEntity<CreateNewCartResponse> create(@RequestBody CreateNewCart req){
        return ResponseEntity.ok(cartService.createCart(req));
    }

    @GetMapping(value = "/list-caritem/{id}")
    public ResponseEntity<ListItemCartResponse> listAllUser(
            @PathVariable Integer id

    ) {
        return ResponseEntity.ok(cartService.getAllItemCart(id));
    }
}
