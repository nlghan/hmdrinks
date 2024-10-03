package com.hmdrinks.Service;

import com.hmdrinks.Entity.Category;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.CategoryRepository;
import com.hmdrinks.Repository.ProductRepository;
import com.hmdrinks.Repository.ProductVariantsRepository;
import com.hmdrinks.Request.CRUDCategoryRequest;
import com.hmdrinks.Request.CRUDProductReq;
import com.hmdrinks.Request.CreateCategoryRequest;
import com.hmdrinks.Request.CreateProductReq;
import com.hmdrinks.Response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductVariantsRepository productVariantsRepository;

    public CRUDProductResponse crateProduct(CreateProductReq req)
    {
        Category category = categoryRepository.findByCateId(req.getCateId());
        if(category == null)
        {
            throw new BadRequestException("cateId not exists");
        }
        Product product = productRepository.findByProName(req.getProName());
        if(product != null)
        {
            throw new BadRequestException("production name exists");
        }
        Product product1 = new Product();
        product1.setCategory(category);
        product1.setDescription(req.getDescription());
        product1.setProImg(req.getProImg());
        product1.setProName(req.getProName());
        product1.setIsDeleted(false);
        productRepository.save(product1);

        return new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                product1.getProImg(),
                product1.getDescription(),
                product1.getIsDeleted(),
                product1.getDateDeleted()
        );
    }

    public CRUDProductResponse getOneProduct(Integer id)
    {
        Product product1 = productRepository.findByProId(id);
        if(product1 == null)
        {
            throw new BadRequestException("production id not exists");
        }

        return new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                product1.getProImg(),
                product1.getDescription(),
                product1.getIsDeleted(),
                product1.getDateDeleted()
        );
    }

    public CRUDProductResponse updateProduct(CRUDProductReq req)
    {
        Category category = categoryRepository.findByCateId(req.getCateId());
        if(category == null)
        {
            throw new BadRequestException("cateId not exists");
        }
        Product product = productRepository.findByProNameAndProIdNot(req.getProName(), req.getProId());
        if(product != null)
        {
            throw new BadRequestException("production name exists");
        }
        Product product1 = productRepository.findByProId(req.getProId());
        if(product1 == null)
        {
            throw new BadRequestException("proId not exists");
        }

        product1.setCategory(category);
        product1.setDescription(req.getDescription());
        product1.setProImg(req.getProImg());
        product1.setProName(req.getProName());
        productRepository.save(product1);
        return new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                product1.getProImg(),
                product1.getDescription(),
                product1.getIsDeleted(),
                product1.getDateDeleted()
        );
    }

    public ListProductResponse listProduct()
    {
        List<Product> productList = productRepository.findAll();
        List<CRUDProductResponse> crudProductResponseList = new ArrayList<>();
        for(Product product1: productList){
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
        return new ListProductResponse(crudProductResponseList);
    }

    public GetProductVariantFromProductIdResponse getAllProductVariantFromProduct(int id)
    {

        Product product = productRepository.findByProId(id);
        if(product == null)
        {
            throw new BadRequestException("cateId not exists");
        }
        List<ProductVariants> productList =  productVariantsRepository.findByProduct_ProId(id);

        List<CRUDProductVarResponse> crudProductVarResponseList = new ArrayList<>();

        for(ProductVariants product1: productList)
        {
            crudProductVarResponseList.add(new CRUDProductVarResponse(
                    product1.getVarId(),
                    product1.getProduct().getProId(),
                    product1.getSize(),
                    product1.getPrice(),
                    product1.getStock(),
                    product1.getIsDeleted(),
                    product1.getDateDeleted()
            ));
        }

        return new GetProductVariantFromProductIdResponse(
                id,
                crudProductVarResponseList
        );

    }

}