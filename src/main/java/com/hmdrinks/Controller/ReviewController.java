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
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private SupportFunction supportFunction;

    @PostMapping(value = "/create")
    public ResponseEntity<?> createReview(@RequestBody CreateNewReview req,HttpServletRequest httpRequest){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());
        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        };
        return reviewService.createReview(req);
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> updateReview(@RequestBody CRUDReviewReq req,HttpServletRequest httpRequest){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return reviewService.updateReview(req);
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<?> deleteReview(@RequestBody DeleteReviewReq req, HttpServletRequest httpRequest) {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return reviewService.deleteOneReview(req);
    }

}
