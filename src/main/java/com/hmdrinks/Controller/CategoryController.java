package com.hmdrinks.Controller;

import com.hmdrinks.Request.CRUDCategoryRequest;
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
    public ResponseEntity<ListCategoryResponse> listAllCategory(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    ) {
        return ResponseEntity.ok(categoryService.listCategory(page, limit));
    }

    @PostMapping(value = "/create-category")
    public ResponseEntity<CRUDCategoryResponse> createCategory(@RequestBody CreateCategoryRequest req){
        return ResponseEntity.ok(categoryService.crateCategory(req));
    }

    @PutMapping(value = "/update")
    public ResponseEntity<CRUDCategoryResponse> updateCategory(@RequestBody CRUDCategoryRequest req){
        return ResponseEntity.ok(categoryService.updateCategory(req));
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<CRUDCategoryResponse> getOneCategory( @PathVariable Integer id){
        return ResponseEntity.ok(categoryService.getOneCategory(id));
    }

    @GetMapping("/view/{id}/product")
    public ResponseEntity<GetViewProductCategoryResponse> getALLProductFromCategory(@PathVariable Integer id){
        return ResponseEntity.ok(categoryService.getAllProductFromCategory(id));
    }

    @GetMapping(value = "/search")
    public ResponseEntity<?> searchByCategoryName(@RequestParam(name = "keyword") String keyword, @RequestParam(name = "page") int page, @RequestParam(name = "limit") int limit) {
        return ResponseEntity.ok(categoryService.totalSearchCategory(keyword,page,limit));
    }
}