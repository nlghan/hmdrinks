package com.hmdrinks.Controller;

import com.hmdrinks.Entity.Product;
import com.hmdrinks.Repository.ProductRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.*;
import com.hmdrinks.Service.ProductService;
import com.hmdrinks.Service.Recommender;
import com.hmdrinks.Service.ReviewService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.yarn.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private SupportFunction supportFunction;
    @Autowired
    private Recommender recommender;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    @PostMapping(value = "/create")
    public ResponseEntity<?> createProduct(@RequestBody CreateProductReq req){
        return productService.crateProduct(req);
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> update(@RequestBody CRUDProductReq req){
        return productService.updateProduct(req);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> update( @PathVariable Integer id){
        return productService.getOneProduct(id);
    }

    @GetMapping(value = "/list-product")
    public ResponseEntity<?> listAllProduct(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    )
    {
        return productService.listProduct(page, limit);
    }

    @GetMapping(value = "/list-rating")
    public ResponseEntity<?> listAllProductRating()
    {
        return productService.listAvgRatingProduct();
    }

    @GetMapping( value = "/variants/{id}")
    public ResponseEntity<?> viewProduct(@PathVariable Integer id){
        return productService.getAllProductVariantFromProduct(id);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<?> searchByCategoryName(@RequestParam(name = "keyword") String keyword,
                                                  @RequestParam(name = "page") String page,
                                                  @RequestParam(name = "limit") String limit) {
        return productService.totalSearchProduct(keyword,page,limit);
    }

    @GetMapping(value = "/cate/search")
    public ResponseEntity<?> searchByCategoryName1(@RequestParam(name = "keyword") String keyword,
                                                  @RequestParam(name ="cateId") Integer cateId,
                                                  @RequestParam(name = "page") String page,
                                                  @RequestParam(name = "limit") String limit) {
        return productService.totalSearchProductByCategory(keyword,cateId,page,limit);
    }

    @GetMapping(value = "/list-review")
    public ResponseEntity<?> listReview(@RequestParam(name = "proId") Integer proId,
                                        @RequestParam(name = "page") String page,
                                        @RequestParam(name = "limit") String limit) {
        return reviewService.getAllReview(page,limit,proId);
    }


    @GetMapping("/list-image/{proId}")
    public ResponseEntity<?> getListImage(@PathVariable Integer proId){
        return productService.getAllProductImages(proId);
    }
    @GetMapping(value = "/list-with-avg-rating")
    public ResponseEntity<?> listProductsWithAverageRating(
            @RequestParam(name = "page") String pageFromParam,
            @RequestParam(name = "limit") String limitFromParam
    ) {
        return productService.listProductsWithAverageRating(pageFromParam, limitFromParam);
    }
    @PostMapping("/filter-product")
    public ResponseEntity<?> filterProduct(
           @RequestBody FilterProductBox req
            ) {

        return productService.filterProduct(req);
    }

    @DeleteMapping(value = "/image/deleteOne")
    public ResponseEntity<?> deleteAllItem(@RequestBody DeleteProductImgReq req, HttpServletRequest httpRequest) {
        return productService.deleteImageFromProduct(req.getProId(), req.getId());
    }

    @DeleteMapping(value = "/image/deleteAll")
    public ResponseEntity<?> deleteOneItem(@RequestBody DeleteAllProductImgReq req, HttpServletRequest httpRequest){
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());
        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return productService.deleteAllImageFromProduct(req.getProId());
    }

    @PutMapping(value = "/enable")
    public ResponseEntity<?> enableProduct(@RequestBody IdReq req, HttpServletRequest httpRequest) {
        return  productService.enableProduct(req.getId());
    }

    @PutMapping(value = "/disable")
    public ResponseEntity<?> disableProduct(@RequestBody IdReq req, HttpServletRequest httpRequest) {
        return  productService.disableProduct(req.getId());
    }

    @GetMapping(value = "/reset")
    public ResponseEntity<?> resetQuantityProduct() {
        return productService.resetAllQuantityProduct();
    }

    @GetMapping(value = "/recommended/{userId}")
    public ResponseEntity<?> getRecommendedBooksByUserId(@PathVariable   Long userId)
            throws ResourceNotFoundException {
        return recommender.recommendedBooks(userId,userRepository,productRepository);
    }

}
