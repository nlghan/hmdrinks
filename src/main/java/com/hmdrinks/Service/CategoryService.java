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
import org.springframework.stereotype.Service;

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

        Category cate = new Category();
        cate.setCateName(req.getCateName());
        cate.setCateImg(req.getCateImg());

        categoryRepository.save(cate);
        return new CRUDCategoryResponse(
                cate.getCateId(),
                cate.getCateName(),
                cate.getCateImg()
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
                category.getCateImg()
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
        category.setCateName(category.getCateName());
        category.setCateImg(category.getCateImg());
        categoryRepository.save(category);
        return new CRUDCategoryResponse(
                req.getCateId(),
                req.getCateName(),
                req.getCateImg()
        );
    }

    public ListCategoryResponse listCategory()
    {
        List<Category> categoryList = categoryRepository.findAll();
        List<CRUDCategoryResponse> crudCategoryResponseList = new ArrayList<>();
        for(Category category: categoryList){
            crudCategoryResponseList.add(new CRUDCategoryResponse(
                    category.getCateId(),
                    category.getCateName(),
                    category.getCateImg()
            ));
        }
        return new ListCategoryResponse(crudCategoryResponseList);
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
            crudProductResponseList.add(new CRUDProductResponse(
                    product1.getProId(),
                    product1.getCategory().getCateId(),
                    product1.getProName(),
                    product1.getProImg(),
                    product1.getDescription(),
                    product1.getIsDeleted(),
                    product1.getDateDeleted()
            ));
        }

        return new GetViewProductCategoryResponse(
                id,
                crudProductResponseList
        );

    }

}
