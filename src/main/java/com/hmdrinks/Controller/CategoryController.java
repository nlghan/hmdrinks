package com.hmdrinks.Controller;

import com.hmdrinks.Request.CRUDCategoryRequest;
import com.hmdrinks.Request.CreateAccountUserReq;
import com.hmdrinks.Request.CreateCategoryRequest;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/cate")
@RequiredArgsConstructor
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping(value = "/list-category")
    public ResponseEntity<ListCategoryResponse> listAllUser(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    ) {
        return ResponseEntity.ok(categoryService.listCategory(page, limit));
    }

    @PostMapping(value = "/create-category")
    public ResponseEntity<CRUDCategoryResponse> createAccount(@RequestBody CreateCategoryRequest req){
        return ResponseEntity.ok(categoryService.crateCategory(req));
    }

    @PutMapping(value = "/update")
    public ResponseEntity<CRUDCategoryResponse> update(@RequestBody CRUDCategoryRequest req){
        return ResponseEntity.ok(categoryService.updateCategory(req));
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<CRUDCategoryResponse> update( @PathVariable Integer id){
        return ResponseEntity.ok(categoryService.getOneCategory(id));
    }

    @GetMapping("/view/{id}/product")
    public ResponseEntity<GetViewProductCategoryResponse> view(@PathVariable Integer id){
        return ResponseEntity.ok(categoryService.getAllProductFromCategory(id));
    }

}
