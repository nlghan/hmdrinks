package com.hmdrinks.Controller;

import com.hmdrinks.Request.CRUDCategoryRequest;
import com.hmdrinks.Request.CreateCategoryRequest;
import com.hmdrinks.Request.IdReq;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<?> createCategory(@RequestBody CreateCategoryRequest req){
        return categoryService.crateCategory(req);
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> updateCategory(@RequestBody CRUDCategoryRequest req){
        return categoryService.updateCategory(req);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> getOneCategory( @PathVariable Integer id){
        return categoryService.getOneCategory(id);
    }

    @GetMapping("/view/{id}/product")
    public ResponseEntity<?> getALLProductFromCategory(@PathVariable Integer id,@RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit){
        return categoryService.getAllProductFromCategory(id,page,limit);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<?> searchByCategoryName(@RequestParam(name = "keyword") String keyword, @RequestParam(name = "page") int page, @RequestParam(name = "limit") int limit) {
        return ResponseEntity.ok(categoryService.totalSearchCategory(keyword,page,limit));
    }

    @PutMapping(value = "/enable")
    public ResponseEntity<?> enableCategory(@RequestBody IdReq req, HttpServletRequest httpRequest) {
        return  categoryService.enableCategory(req.getId());
    }

    @PutMapping(value = "/disable")
    public ResponseEntity<?> disableCategory(@RequestBody IdReq req, HttpServletRequest httpRequest) {
        return  categoryService.disableCategory(req.getId());
    }

}