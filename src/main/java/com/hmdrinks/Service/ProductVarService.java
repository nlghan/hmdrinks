package com.hmdrinks.Service;

import com.hmdrinks.Entity.Category;
import com.hmdrinks.Entity.PriceHistory;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.CategoryRepository;
import com.hmdrinks.Repository.PriceHistoryRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductVarService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductVariantsRepository proVarRepository;
    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    public ResponseEntity<?> crateProductVariants(CreateProductVarReq req)
    {
        Product product= productRepository.findByProId(req.getProId());
        if(product == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found");
        }
        ProductVariants productVariants = proVarRepository.findBySizeAndProduct_ProId(req.getSize(),req.getProId());
        if(productVariants != null)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Product Variant Size Already Exists");
        }
        if(req.getStock() <=0)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Stock greater than 0");
        }
        if(req.getPrice() < 1000.0)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Price greater than 1000");
        }
        ProductVariants productVariants1 = new ProductVariants();
        productVariants1.setProduct(product);
        productVariants1.setSize(req.getSize());
        productVariants1.setStock(req.getStock());
        productVariants1.setPrice(req.getPrice());
        productVariants1.setIsDeleted(false);
        productVariants1.setDateCreated(LocalDateTime.now());
        proVarRepository.save(productVariants1);

        return  ResponseEntity.status(HttpStatus.OK).body(new CRUDProductVarResponse(
                productVariants1.getVarId(),
                productVariants1.getProduct().getProId(),
                productVariants1.getSize(),
                productVariants1.getPrice(),
                productVariants1.getStock(),
                productVariants1.getIsDeleted(),
                productVariants1.getDateDeleted(),
                productVariants1.getDateCreated(),
                productVariants1.getDateUpdated()
        ));
    }

    public ResponseEntity<?> getOneVarProduct(Integer id)
    {
        ProductVariants productVariants1 = proVarRepository.findByVarId(id);
        if(productVariants1 == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDProductVarResponse(
                productVariants1.getVarId(),
                productVariants1.getProduct().getProId(),
                productVariants1.getSize(),
                productVariants1.getPrice(),
                productVariants1.getStock(),
                productVariants1.getIsDeleted(),
                productVariants1.getDateDeleted(),
                productVariants1.getDateCreated(),
                productVariants1.getDateUpdated()
        ));
    }

    public ResponseEntity<?> updateProduct(CRUDProductVarReq req)
    {
        Product product= productRepository.findByProId(req.getProId());
        if(product == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found");
        }
        ProductVariants productVariants = proVarRepository.findBySizeAndProduct_ProIdAndVarIdNot(req.getSize(),req.getProId(), req.getVarId());
        if(productVariants != null)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Product Variant Size Already Exists");
        }
        ProductVariants productVariants1 = proVarRepository.findByVarId(req.getVarId());
        if(productVariants1 == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Variant Not Found");
        }
        if(req.getStock() <=0)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Stock greater than 0");
        }
        if(req.getPrice() < 1000.0)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Price greater than 1000");
        }
        if(req.getPrice() != productVariants1.getPrice())
        {
            PriceHistory priceHistory = new PriceHistory();
            priceHistory.setProductVariant(productVariants1);
            priceHistory.setNewPrice(req.getPrice());
            priceHistory.setOldPrice(productVariants1.getPrice());
            priceHistory.setDateChanged(LocalDateTime.now());
            priceHistory.setChangeReason("Update Price");
            priceHistoryRepository.save(priceHistory);
        }
        productVariants1.setProduct(product);
        productVariants1.setSize(req.getSize());
        productVariants1.setStock(req.getStock());
        productVariants1.setPrice(req.getPrice());
        productVariants1.setDateUpdated(LocalDateTime.now());
        proVarRepository.save(productVariants1);
        return ResponseEntity.status(HttpStatus.OK).body(new  CRUDProductVarResponse(
                productVariants1.getVarId(),
                productVariants1.getProduct().getProId(),
                productVariants1.getSize(),
                productVariants1.getPrice(),
                productVariants1.getStock(),
                productVariants1.getIsDeleted(),
                productVariants1.getDateDeleted(),
                productVariants1.getDateCreated(),
                productVariants1.getDateUpdated()
        ));
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