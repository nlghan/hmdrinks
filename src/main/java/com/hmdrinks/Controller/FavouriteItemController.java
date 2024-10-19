package com.hmdrinks.Controller;

import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.CartItemService;
import com.hmdrinks.Service.FavouriteItemService;
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
@RequestMapping("/api/fav-item")
@RequiredArgsConstructor
public class FavouriteItemController {
    @Autowired
    private FavouriteItemService favouriteItemService;

    @Autowired
    private SupportFunction supportFunction;
    @Autowired
    private JwtService jwtService;
    @PostMapping(value = "/insert")
    public ResponseEntity<CRUDFavouriteItemResponse> createCartItem(@RequestBody InsertItemToFavourite req,HttpServletRequest httpRequest){
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return ResponseEntity.ok(favouriteItemService.insertFavouriteItem(req));
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<DeleteFavouriteItemResponse> deleteOneItem(@RequestBody DeleteOneFavouriteItemReq req, HttpServletRequest httpRequest){

        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return ResponseEntity.ok(favouriteItemService.deleteOneItem(req));
    }
}