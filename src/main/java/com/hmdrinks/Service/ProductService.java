package com.hmdrinks.Service;

import com.hmdrinks.Entity.Category;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.CategoryRepository;
import com.hmdrinks.Repository.ProductRepository;
import com.hmdrinks.Repository.ProductVariantsRepository;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public CRUDProductResponse crateProduct(CreateProductReq req) {
        Category category = categoryRepository.findByCateId(req.getCateId());
        if (category == null) {
            throw new BadRequestException("cateId not exists");
        }
        Product product = productRepository.findByProName(req.getProName());
        if (product != null) {
            throw new BadRequestException("production name exists");
        }
        LocalDate currentDate = LocalDate.now();
        Product product1 = new Product();
        product1.setCategory(category);
        product1.setDescription(req.getDescription());
        product1.setProImg(req.getProImg());
        product1.setProName(req.getProName());
        product1.setIsDeleted(false);
        product1.setDateCreated(currentDate);
        productRepository.save(product1);

        return new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                product1.getProImg(),
                product1.getDescription(),
                product1.getIsDeleted(),
                product1.getDateDeleted(),
                product1.getDateCreated(),
                product1.getDateUpdated()
        );
    }

    public CRUDProductResponse getOneProduct(Integer id) {
        Product product1 = productRepository.findByProId(id);
        if (product1 == null) {
            throw new BadRequestException("production id not exists");
        }

        return new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                product1.getProImg(),
                product1.getDescription(),
                product1.getIsDeleted(),
                product1.getDateDeleted(),
                product1.getDateCreated(),
                product1.getDateUpdated()
        );
    }

    public CRUDProductResponse updateProduct(CRUDProductReq req) {
        Category category = categoryRepository.findByCateId(req.getCateId());
        if (category == null) {
            throw new BadRequestException("cateId not exists");
        }
        Product product = productRepository.findByProNameAndProIdNot(req.getProName(), req.getProId());
        if (product != null) {
            throw new BadRequestException("production name exists");
        }
        Product product1 = productRepository.findByProId(req.getProId());
        if (product1 == null) {
            throw new BadRequestException("proId not exists");
        }

        product1.setCategory(category);
        product1.setDescription(req.getDescription());
        product1.setProImg(req.getProImg());
        product1.setProName(req.getProName());
        product1.setDateUpdated(LocalDate.now());
        productRepository.save(product1);
        return new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                product1.getProImg(),
                product1.getDescription(),
                product1.getIsDeleted(),
                product1.getDateDeleted(),
                product1.getDateCreated(),
                product1.getDateUpdated()
        );
    }

    public ListProductResponse listProduct(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Product> productList = productRepository.findAll(pageable);
        List<CRUDProductResponse> crudProductResponseList = new ArrayList<>();
        for (Product product1 : productList) {
            crudProductResponseList.add(new CRUDProductResponse(
                    product1.getProId(),
                    product1.getCategory().getCateId(),
                    product1.getProName(),
                    product1.getProImg(),
                    product1.getDescription(),
                    product1.getIsDeleted(),
                    product1.getDateDeleted(),
                    product1.getDateCreated(),
                    product1.getDateUpdated()
            ));
        }
        return new ListProductResponse(page, productList.getTotalPages(), limit, crudProductResponseList);
    }

    public GetProductVariantFromProductIdResponse getAllProductVariantFromProduct(int id) {

        Product product = productRepository.findByProId(id);
        if (product == null) {
            throw new BadRequestException("cateId not exists");
        }
        List<ProductVariants> productList = productVariantsRepository.findByProduct_ProId(id);

        List<CRUDProductVarResponse> crudProductVarResponseList = new ArrayList<>();

        for (ProductVariants product1 : productList) {
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

        return new GetProductVariantFromProductIdResponse(
                id,
                crudProductVarResponseList
        );

    }

    public TotalSearchProductResponse totalSearchProduct(String keyword, String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Product> productList = productRepository.findByProNameContaining(keyword, pageable);
        List<CRUDProductResponse> crudProductResponseList = new ArrayList<>();
        for (Product product1 : productList) {
            crudProductResponseList.add(new CRUDProductResponse(
                    product1.getProId(),
                    product1.getCategory().getCateId(),
                    product1.getProName(),
                    product1.getProImg(),
                    product1.getDescription(),
                    product1.getIsDeleted(),
                    product1.getDateDeleted(),
                    product1.getDateCreated(),
                    product1.getDateUpdated()
            ));
        }
        return new TotalSearchProductResponse(page, productList.getTotalPages(), limit, crudProductResponseList);
    }

    public FilterProductBoxResponse filterProduct(FilterProductBox req) {
        List<CRUDProductVarResponse> crudProductVarResponseList = new ArrayList<>();
        Category category = categoryRepository.findByCateId(req.getC());
        if (category == null) {
            throw new BadRequestException("cateId not exists");
        }
        for (Integer id : req.getP()) {
            Product product = productRepository.findByProId(id);
            if (product == null) {
                throw new BadRequestException("productId not exists");
            }
        }
        // 1: gia cao den thap
        // 2: gia thap den cao
        // 3: ngày tạo mới nhất
        // 4: top đánh gia cao đến thấp
        // 5: tu thấp đến cao star

        if (req.getO() <= 0) {
            throw new BadRequestException("o must be greater than 0");
        }
        Sort sort;
        int total = 0;
        if (req.getO() == 1) {

            sort = Sort.by(Sort.Direction.DESC, "price");
            List<ProductVariants> productVariants = productVariantsRepository.findByProduct_Category_CateIdAndProduct_ProIdIn(req.getC(), req.getP(), sort);
            for (ProductVariants productVariant : productVariants) {
                crudProductVarResponseList.add(new CRUDProductVarResponse(
                        productVariant.getVarId(),
                        productVariant.getProduct().getProId(),
                        productVariant.getSize(),
                        productVariant.getPrice(),
                        productVariant.getStock(),
                        productVariant.getIsDeleted(),
                        productVariant.getDateDeleted(),
                        productVariant.getDateCreated(),
                        productVariant.getDateUpdated()
                ));
                total += 1;
            }

        } else if (req.getO() == 2) {
            sort = Sort.by(Sort.Direction.ASC, "price");
            List<ProductVariants> productVariants = productVariantsRepository.findByProduct_Category_CateIdAndProduct_ProIdIn(req.getC(), req.getP(), sort);
            for (ProductVariants productVariant : productVariants) {
                crudProductVarResponseList.add(new CRUDProductVarResponse(
                        productVariant.getVarId(),
                        productVariant.getProduct().getProId(),
                        productVariant.getSize(),
                        productVariant.getPrice(),
                        productVariant.getStock(),
                        productVariant.getIsDeleted(),
                        productVariant.getDateDeleted(),
                        productVariant.getDateCreated(),
                        productVariant.getDateUpdated()
                ));
                total += 1;
            }
        } else if (req.getO() == 3) {
            List<ProductVariants> productVariants = productVariantsRepository
                    .findByProduct_Category_CateIdAndProduct_ProIdIn(
                            req.getC(),
                            req.getP(),
                            Sort.by(Sort.Direction.DESC, "createDate")
                    );
            for (ProductVariants productVariant : productVariants) {
                crudProductVarResponseList.add(new CRUDProductVarResponse(
                        productVariant.getVarId(),
                        productVariant.getProduct().getProId(),
                        productVariant.getSize(),
                        productVariant.getPrice(),
                        productVariant.getStock(),
                        productVariant.getIsDeleted(),
                        productVariant.getDateDeleted(),
                        productVariant.getDateCreated(),
                        productVariant.getDateUpdated()
                ));
                total += 1;
            }
        } else if (req.getO() == 4) {
            List<ProductVariants> productVariants = productVariantsRepository.findTopRatedProductsDesc(req.getC(), req.getP());
            for (ProductVariants productVariant : productVariants) {
                crudProductVarResponseList.add(new CRUDProductVarResponse(
                        productVariant.getVarId(),
                        productVariant.getProduct().getProId(),
                        productVariant.getSize(),
                        productVariant.getPrice(),
                        productVariant.getStock(),
                        productVariant.getIsDeleted(),
                        productVariant.getDateDeleted(),
                        productVariant.getDateCreated(),
                        productVariant.getDateUpdated()
                ));
                total += 1;
            }
        } else if (req.getO() == 5) {
            List<ProductVariants> productVariants = productVariantsRepository.findTopRatedProductsDesc(req.getC(), req.getP());
            for (ProductVariants productVariant : productVariants) {
                crudProductVarResponseList.add(new CRUDProductVarResponse(
                        productVariant.getVarId(),
                        productVariant.getProduct().getProId(),
                        productVariant.getSize(),
                        productVariant.getPrice(),
                        productVariant.getStock(),
                        productVariant.getIsDeleted(),
                        productVariant.getDateDeleted(),
                        productVariant.getDateCreated(),
                        productVariant.getDateUpdated()
                ));
                total += 1;
            }
        }

            return new FilterProductBoxResponse(
                    true,
                    total,
                    crudProductVarResponseList,
                    "OK",
                    false
            );
        }
}