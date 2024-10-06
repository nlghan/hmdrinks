package com.hmdrinks.Service;

import com.hmdrinks.Entity.Category;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.CategoryRepository;
import com.hmdrinks.Repository.ProductRepository;
import com.hmdrinks.Repository.ProductVariantsRepository;
import com.hmdrinks.Request.CRUDProductReq;
import com.hmdrinks.Request.CRUDProductVarReq;
import com.hmdrinks.Request.CreateProductReq;
import com.hmdrinks.Request.CreateProductVarReq;
import com.hmdrinks.Response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductVarService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantsRepository proVarRepository;

    public CRUDProductVarResponse crateProductVariants(CreateProductVarReq req)
    {
        Product product= productRepository.findByProId(req.getProId());
        if(product == null)
        {
            throw new BadRequestException("proId not exists");
        }
        ProductVariants productVariants = proVarRepository.findBySizeAndProduct_ProId(req.getSize(),req.getProId());
        if(productVariants != null)
        {
            throw new BadRequestException("production size exists");
        }
        ProductVariants productVariants1 = new ProductVariants();
        productVariants1.setProduct(product);
        productVariants1.setSize(req.getSize());
        productVariants1.setStock(req.getStock());
        productVariants1.setPrice(req.getPrice());
        productVariants1.setIsDeleted(false);
        productVariants1.setDateCreated(LocalDate.now());

        proVarRepository.save(productVariants1);


        return new CRUDProductVarResponse(
                productVariants1.getVarId(),
                productVariants1.getProduct().getProId(),
                productVariants1.getSize(),
                productVariants1.getPrice(),
                productVariants1.getStock(),
                productVariants1.getIsDeleted(),
                productVariants1.getDateDeleted(),
                productVariants1.getDateCreated(),
                productVariants1.getDateUpdated()
        );
    }

    public CRUDProductVarResponse getOneVarProduct(Integer id)
    {
        ProductVariants productVariants1 = proVarRepository.findByVarId(id);
        if(productVariants1 == null)
        {
            throw new BadRequestException("production id not exists");
        }

        return new CRUDProductVarResponse(
                productVariants1.getVarId(),
                productVariants1.getProduct().getProId(),
                productVariants1.getSize(),
                productVariants1.getPrice(),
                productVariants1.getStock(),
                productVariants1.getIsDeleted(),
                productVariants1.getDateDeleted(),
                productVariants1.getDateCreated(),
                productVariants1.getDateUpdated()
        );
    }

    public CRUDProductVarResponse updateProduct(CRUDProductVarReq req)
    {
        Product product= productRepository.findByProId(req.getProId());
        if(product == null)
        {
            throw new BadRequestException("proId not exists");
        }
        ProductVariants productVariants = proVarRepository.findBySizeAndProduct_ProIdAndVarIdNot(req.getSize(),req.getProId(), req.getVarId());
        if(productVariants != null)
        {
            throw new BadRequestException("production size exists");
        }
        ProductVariants productVariants1 = proVarRepository.findByVarId(req.getVarId());
        if(productVariants1 == null)
        {
            throw new BadRequestException("production variations id not exists");
        }

        productVariants1.setProduct(product);
        productVariants1.setSize(req.getSize());
        productVariants1.setStock(req.getStock());
        productVariants1.setPrice(req.getPrice());
        productVariants1.setDateUpdated(LocalDate.now());

        proVarRepository.save(productVariants1);

        return new CRUDProductVarResponse(
                productVariants1.getVarId(),
                productVariants1.getProduct().getProId(),
                productVariants1.getSize(),
                productVariants1.getPrice(),
                productVariants1.getStock(),
                productVariants1.getIsDeleted(),
                productVariants1.getDateDeleted(),
                productVariants1.getDateCreated(),
                productVariants1.getDateUpdated()
        );
    }

    public ListProductVarResponse listProduct(String pageFromParam, String limitFromParam)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ProductVariants> productList = proVarRepository.findAll(pageable);
        List<CRUDProductVarResponse> crudProductVarResponseList = new ArrayList<>();
        for(ProductVariants product1: productList){
            crudProductVarResponseList.add(new CRUDProductVarResponse(
                    product1.getVarId(),
                    product1.getProduct().getProId(),
                    product1.getSize(),
                    product1.getPrice(),
                    product1.getStock(),
                    product1.getIsDeleted(),
                    product1.getDateDeleted(),
                    product1.getDateCreated(),
                    product1.getDateUpdated()
            ));
        }
        return new ListProductVarResponse(page,productList.getTotalPages(),limit,crudProductVarResponseList);
    }




}
