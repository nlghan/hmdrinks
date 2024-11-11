package com.hmdrinks.Service;

import com.cloudinary.api.exceptions.BadRequest;
import com.hmdrinks.Entity.Category;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Entity.Review;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.CategoryRepository;
import com.hmdrinks.Repository.ProductRepository;
import com.hmdrinks.Repository.ProductVariantsRepository;
import com.hmdrinks.Repository.ReviewRepository;
import com.hmdrinks.Request.CRUDCategoryRequest;
import com.hmdrinks.Request.CreateCategoryRequest;
import com.hmdrinks.Response.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductVariantsRepository productVariantsRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    public ResponseEntity<?> crateCategory(CreateCategoryRequest req)
    {
        Category category = categoryRepository.findByCateName(req.getCateName());
        if(category != null)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("cateName exists");
        }
        LocalDateTime now = LocalDateTime.now();
        Category cate = new Category();
        cate.setCateName(req.getCateName());
        cate.setCateImg(req.getCateImg());
        cate.setIsDeleted(false);
        cate.setDateCreated(LocalDateTime.now());

        categoryRepository.save(cate);
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDCategoryResponse(
                cate.getCateId(),
                cate.getCateName(),
                cate.getCateImg(),
                cate.getIsDeleted(),
                cate.getDateCreated(),
                cate.getDateUpdated(),
                cate.getDateDeleted()
        ));
    }

    public ResponseEntity<?> getOneCategory(Integer id)
    {
        Category category = categoryRepository.findByCateId(id);
        if(category == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDCategoryResponse(
                category.getCateId(),
                category.getCateName(),
                category.getCateImg(),
                category.getIsDeleted(),
                category.getDateCreated(),
                category.getDateUpdated(),
                category.getDateDeleted()
        ));
    }

    public ResponseEntity<?> updateCategory(CRUDCategoryRequest req)
    {
        Category category = categoryRepository.findByCateId(req.getCateId());
        if(category == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not found");
        }
        Category category1 = categoryRepository.findByCateNameAndCateIdNot(req.getCateName(),req.getCateId());
        if(category1 != null)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("category already exists");
        }
        LocalDateTime currentDateTime = LocalDateTime.now();
        category.setCateName(category.getCateName());
        category.setCateImg(category.getCateImg());
        category.setDateUpdated(LocalDateTime.now());
        categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDCategoryResponse(
                req.getCateId(),
                req.getCateName(),
                req.getCateImg(),
                category.getIsDeleted(),
                category.getDateCreated(),
                category.getDateUpdated(),
                category.getDateDeleted()
        ));
    }

    public ListCategoryResponse listCategory(String pageFromParam, String limitFromParam)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Category> categoryList = categoryRepository.findAll(pageable);
        List<CRUDCategoryResponse> crudCategoryResponseList = new ArrayList<>();
        int total = 0;
        for(Category category: categoryList){
            crudCategoryResponseList.add(new CRUDCategoryResponse(
                    category.getCateId(),
                    category.getCateName(),
                    category.getCateImg(),
                    category.getIsDeleted(),
                    category.getDateCreated(),
                    category.getDateUpdated(),
                    category.getDateDeleted()
            ));
            total++;
        }
        return new ListCategoryResponse(page,categoryList.getTotalPages(),limit,total,crudCategoryResponseList);
    }

    public ResponseEntity<?> getAllProductFromCategory(int id,String pageFromParam, String limitFromParam)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Category category = categoryRepository.findByCateIdAndIsDeletedFalse(id);
        if(category == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not found");
        }
        Page<Product> productList = productRepository.findByCategory_CateIdAndIsDeletedFalse(id,pageable);
        List<CRUDProductResponse> crudProductResponseList = new ArrayList<>();
        int total =0;
        for(Product product1: productList)
        {
            List<ProductImageResponse> productImageResponses = new ArrayList<>();
            String currentProImg = product1.getListProImg();
            if(currentProImg != null && !currentProImg.trim().isEmpty())
            {
                String[] imageEntries1 = currentProImg.split(", ");
                for (String imageEntry : imageEntries1) {
                    String[] parts = imageEntry.split(": ");  // Phân tách stt và url
                    int stt = Integer.parseInt(parts[0]);      // Lấy số thứ tự hiện tại
                    String url = parts[1];                     // Lấy URL
                    productImageResponses.add(new ProductImageResponse(stt, url));
                }
            }

            crudProductResponseList.add(new CRUDProductResponse(
                    product1.getProId(),
                    product1.getCategory().getCateId(),
                    product1.getProName(),
                    productImageResponses,
                    product1.getDescription(),
                    product1.getIsDeleted(),
                    product1.getDateDeleted(),
                    product1.getDateCreated(),
                    product1.getDateUpdated()
            ));
            total++;
        }

        return ResponseEntity.status(HttpStatus.OK).body( new GetViewProductCategoryResponse(
                page,
                productList.getTotalPages(),
                limit,
                total,
                crudProductResponseList
        ));

    }

    public TotalSearchCategoryResponse totalSearchCategory(String keyword, int page, int limit)
    {
        if (limit > 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Category> categoryList = categoryRepository.findByCateNameContaining(keyword,pageable);
        List<CRUDCategoryResponse> crudCategoryResponseList = new ArrayList<>();
        for(Category category: categoryList){
            crudCategoryResponseList.add(new CRUDCategoryResponse(
                    category.getCateId(),
                    category.getCateName(),
                    category.getCateImg(),
                    category.getIsDeleted(),
                    category.getDateCreated(),
                    category.getDateUpdated(),
                    category.getDateDeleted()
            ));
        }
        return new TotalSearchCategoryResponse(page,categoryList.getTotalPages(),limit,crudCategoryResponseList);
    }

    @Transactional
    public ResponseEntity<?> disableCategory(int cateId)
    {
        Category category = categoryRepository.findByCateId(cateId);
        if(category == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not found");
        }
        if(category.getIsDeleted())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category already disable");
        }
        List<Product> productList = productRepository.findByCategory_CateId(cateId);
        for (Product product: productList)
        {
            List<ProductVariants> productVariants = productVariantsRepository.findByProduct_ProId(product.getProId());
            for(ProductVariants productVariant: productVariants)
            {
                productVariant.setIsDeleted(true);
                productVariant.setDateDeleted(LocalDateTime.now());
                productVariantsRepository.save(productVariant);
            }
            List<Review> reviews = reviewRepository.findAllByProduct_ProId(product.getProId());
            for(Review review: reviews)
            {
                review.setIsDeleted(true);
                review.setDateDeleted(LocalDateTime.now());
                reviewRepository.save(review);
            }
            product.setIsDeleted(true);
            product.setDateDeleted(LocalDateTime.now());
            productRepository.save(product);
        }
        category.setIsDeleted(true);
        category.setDateDeleted(LocalDateTime.now());
        categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDCategoryResponse(
                category.getCateId(),
                category.getCateName(),
                category.getCateImg(),
                category.getIsDeleted(),
                category.getDateCreated(),
                category.getDateUpdated(),
                category.getDateDeleted()));
    }

    @Transactional
    public ResponseEntity<?> enableCategory(int cateId)
    {
        Category category = categoryRepository.findByCateId(cateId);
        if(category == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not found");
        }
        if(!category.getIsDeleted())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category already enable");
        }
        List<Product> productList = productRepository.findByCategory_CateId(cateId);
        for (Product product:productList)
        {
            List<ProductVariants> productVariants = productVariantsRepository.findByProduct_ProId(product.getProId());
            for(ProductVariants productVariant: productVariants)
            {
                productVariant.setIsDeleted(false);
                productVariant.setDateDeleted(null);
                productVariantsRepository.save(productVariant);
            }
            List<Review> reviews = reviewRepository.findAllByProduct_ProId(product.getProId());
            for(Review review: reviews)
            {
                review.setIsDeleted(false);
                review.setDateDeleted(null);
                reviewRepository.save(review);
            }
            product.setIsDeleted(false);
            product.setDateDeleted(null);
            productRepository.save(product);
        }
        category.setIsDeleted(false);
        category.setDateDeleted(null);
        categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDCategoryResponse(
                category.getCateId(),
                category.getCateName(),
                category.getCateImg(),
                category.getIsDeleted(),
                category.getDateCreated(),
                category.getDateUpdated(),
                category.getDateDeleted()));
    }
}