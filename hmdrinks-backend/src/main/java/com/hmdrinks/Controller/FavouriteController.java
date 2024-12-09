package com.hmdrinks.Controller;

import com.hmdrinks.Request.CreateNewCart;
import com.hmdrinks.Request.CreateNewFavourite;
import com.hmdrinks.Request.DeleteAllCartItemReq;
import com.hmdrinks.Request.DeleteAllFavouriteItemReq;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.*;
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
@RequestMapping("/api/fav")
@RequiredArgsConstructor
public class FavouriteController {
    @Autowired
    private FavouriteService favouriteService;
    @Autowired
    private FavouriteItemService favouriteItemService;
    @Autowired
    private SupportFunction supportFunction;

    @PostMapping(value = "/create")
    public ResponseEntity<?> createFavourite(@RequestBody CreateNewFavourite req, HttpServletRequest httpRequest){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return ResponseEntity.ok(favouriteService.createFavourite(req));
    }

    @GetMapping(value = "/list-favItem/{id}")
    public ResponseEntity<?> listAllFavouriteItem(
            @PathVariable Integer id

    ) {
        return favouriteService.getAllItemFavourite(id);
    }

    @DeleteMapping(value = "/delete-allItem/{id}")
    public ResponseEntity<?> deleteAllItem(@RequestBody DeleteAllFavouriteItemReq req, HttpServletRequest httpRequest){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return favouriteItemService.deleteAllFavouriteItem(req);
    }

    @GetMapping(value = "/list-fav/{userId}")
    public ResponseEntity<?> getFavoriteById(
            @PathVariable Integer userId

    ) {
        return favouriteService.getFavoriteById(userId);
    }
}