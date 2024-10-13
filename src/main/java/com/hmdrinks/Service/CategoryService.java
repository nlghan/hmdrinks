package com.hmdrinks.Service;

import com.cloudinary.api.exceptions.BadRequest;
import com.hmdrinks.Entity.Category;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.CategoryRepository;
import com.hmdrinks.Repository.ProductRepository;
import com.hmdrinks.Request.CRUDCategoryRequest;
import com.hmdrinks.Request.CreateCategoryRequest;
import com.hmdrinks.Response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public CRUDCategoryResponse crateCategory(CreateCategoryRequest req)
    {
        Category category = categoryRepository.findByCateName(req.getCateName());
        if(category != null)
        {
            throw new BadRequestException("cateName exists");
        }
        LocalDateTime now = LocalDateTime.now();
        Category cate = new Category();
        cate.setCateName(req.getCateName());
        cate.setCateImg(req.getCateImg());
        cate.setIsDeleted(false);
        cate.setDateCreated(LocalDate.from(now));

        categoryRepository.save(cate);
        return new CRUDCategoryResponse(
                cate.getCateId(),
                cate.getCateName(),
                cate.getCateImg(),
                cate.getIsDeleted(),
                cate.getDateCreated(),
                cate.getDateUpdated(),
                cate.getDateDeleted()
        );
    }

    public CRUDCategoryResponse getOneCategory(Integer id)
    {
        Category category = categoryRepository.findByCateId(id);
        if(category == null)
        {
            throw new BadRequestException("cateId not exists");
        }
        return new CRUDCategoryResponse(
                category.getCateId(),
                category.getCateName(),
                category.getCateImg(),
                category.getIsDeleted(),
                category.getDateCreated(),
                category.getDateUpdated(),
                category.getDateDeleted()
        );
    }

    public CRUDCategoryResponse updateCategory(CRUDCategoryRequest req)
    {
        Category category = categoryRepository.findByCateId(req.getCateId());
        if(category == null)
        {
            throw new BadRequestException("cateId not exists");
        }
        Category category1 = categoryRepository.findByCateNameAndCateIdNot(req.getCateName(),req.getCateId());
        if(category1 != null)
        {
            throw new BadRequestException("cateName exists");
        }
        LocalDateTime currentDateTime = LocalDateTime.now();
        category.setCateName(category.getCateName());
        category.setCateImg(category.getCateImg());
        category.setDateUpdated(LocalDate.from(currentDateTime));
        categoryRepository.save(category);
        return new CRUDCategoryResponse(
                req.getCateId(),
                req.getCateName(),
                req.getCateImg(),
                category.getIsDeleted(),
                category.getDateCreated(),
                category.getDateUpdated(),
                category.getDateDeleted()
        );
    }

    public ListCategoryResponse listCategory(String pageFromParam, String limitFromParam)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Category> categoryList = categoryRepository.findAll(pageable);
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
        return new ListCategoryResponse(page,categoryList.getTotalPages(),limit,crudCategoryResponseList);
    }

    public GetViewProductCategoryResponse getAllProductFromCategory(int id)
    {
        Category category = categoryRepository.findByCateId(id);
        if(category == null)
        {
            throw new BadRequestException("cateId not exists");
        }
        List<Product> productList = productRepository.findByCategory_CateId(id);
        List<CRUDProductResponse> crudProductResponseList = new ArrayList<>();

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
        }

        return new GetViewProductCategoryResponse(
                id,
                crudProductResponseList
        );

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
}