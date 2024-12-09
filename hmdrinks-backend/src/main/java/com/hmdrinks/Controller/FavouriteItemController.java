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
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> createCartItem(@RequestBody InsertItemToFavourite req,HttpServletRequest httpRequest){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return favouriteItemService.insertFavouriteItem(req);
    }

    @GetMapping(value = "/list")
    public ResponseEntity<?> ListFavourite(){
        return favouriteItemService.listAllTotalFavouriteByProId();
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteOneItem(@RequestBody DeleteOneFavouriteItemReq req, HttpServletRequest httpRequest){

        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return favouriteItemService.deleteOneItem(req);
    }
}