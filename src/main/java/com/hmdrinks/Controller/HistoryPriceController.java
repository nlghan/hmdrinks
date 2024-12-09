package com.hmdrinks.Controller;

import com.hmdrinks.Enum.Type_Post;
import com.hmdrinks.Request.CRUDPostReq;
import com.hmdrinks.Request.CreateNewPostReq;
import com.hmdrinks.Response.ListAllPostResponse;
import com.hmdrinks.Service.PostService;
import com.hmdrinks.Service.PriceHistoryService;
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
@RequestMapping("/api/price-history")
public class HistoryPriceController {
    @Autowired
    private PriceHistoryService priceHistoryService;

    @GetMapping(value = "/view/all")
    public ResponseEntity<?> createPost(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    )
    {
        return priceHistoryService.getListAllHistoryPrice(page, limit);
    }

    @GetMapping(value ="/view/productVar")
    public ResponseEntity<?> getOneProductVar(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit,
            @RequestParam(name = "proVarId") int proVarId
    )
    {
        return priceHistoryService.getListAllHistoryPriceByProductVarId(page,limit, proVarId);
    }

}