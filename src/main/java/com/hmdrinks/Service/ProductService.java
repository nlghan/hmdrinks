package com.hmdrinks.Service;

import com.hmdrinks.Entity.Category;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.NotFoundException;
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
        product1.setProName(req.getProName());
        product1.setListProImg("");
        product1.setIsDeleted(false);
        product1.setDateCreated(currentDate);
        productRepository.save(product1);
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        String currentProImg = product1.getListProImg();
        if(currentProImg != null && !currentProImg.trim().isEmpty())
        {
            String[] imageEntries1 = currentProImg.split(", ");
            for (String imageEntry : imageEntries1) {
                String[] parts = imageEntry.split(": ");
                int stt = Integer.parseInt(parts[0]);
                String url = parts[1];
                productImageResponses.add(new ProductImageResponse(stt, url));
            }
        }
        return new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                productImageResponses,
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
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        String currentProImg = product1.getListProImg();
        if(currentProImg != null && !currentProImg.trim().isEmpty())
        {
            String[] imageEntries1 = currentProImg.split(", ");
            for (String imageEntry : imageEntries1) {
                String[] parts = imageEntry.split(": ");
                int stt = Integer.parseInt(parts[0]);
                String url = parts[1];
                productImageResponses.add(new ProductImageResponse(stt, url));
            }
        }
        return new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                productImageResponses,
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
        product1.setProName(req.getProName());
        product1.setDateUpdated(LocalDate.now());
        productRepository.save(product1);
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        String currentProImg = product1.getListProImg();
        if(currentProImg != null && !currentProImg.trim().isEmpty())
        {
            String[] imageEntries1 = currentProImg.split(", ");
            for (String imageEntry : imageEntries1) {
                String[] parts = imageEntry.split(": ");
                int stt = Integer.parseInt(parts[0]);
                String url = parts[1];
                productImageResponses.add(new ProductImageResponse(stt, url));
            }
        }
        return new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                productImageResponses,
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
            List<ProductImageResponse> productImageResponses = new ArrayList<>();
            String currentProImg = product1.getListProImg();
            //String currentProImg = product1.getListProImg();
            if(currentProImg != null && !currentProImg.trim().isEmpty())
            {
                String[] imageEntries1 = currentProImg.split(", ");
                for (String imageEntry : imageEntries1) {
                    String[] parts = imageEntry.split(": ");
                    int stt = Integer.parseInt(parts[0]);
                    String url = parts[1];
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
            List<ProductImageResponse> productImageResponses = new ArrayList<>();
            String currentProImg = product1.getListProImg();
            if(currentProImg != null && !currentProImg.trim().isEmpty())
            {
                String[] imageEntries1 = currentProImg.split(", ");
                for (String imageEntry : imageEntries1) {
                    String[] parts = imageEntry.split(": ");
                    int stt = Integer.parseInt(parts[0]);
                    String url = parts[1];
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
        return new TotalSearchProductResponse(page, productList.getTotalPages(), limit, crudProductResponseList);
    }

    public ListProductImageResponse deleteImageFromProduct(int proId, int deleteStt ) {
        Product product = productRepository.findByProId(proId);
        if (product == null) {
            throw new NotFoundException("Not found product with ID: " + proId);
        }
        String currentProImg = product.getListProImg();
        if (currentProImg == null || currentProImg.trim().isEmpty()) {
            throw new BadRequestException("No images found for this product.");
        }
        String[] imageEntries = currentProImg.split(", ");
        List<String> updatedImageEntries = new ArrayList<>();
        int currentStt = 1;
        for (String imageEntry : imageEntries) {
            String[] parts = imageEntry.split(": ");  // Phân tách stt và url
            int stt = Integer.parseInt(parts[0]);      // Lấy số thứ tự hiện tại
            String url = parts[1];                     // Lấy URL

            if (stt == deleteStt) {
                continue;
            }
            updatedImageEntries.add(currentStt + ": " + url);
            currentStt++;
        }
        String updatedProImg = String.join(", ", updatedImageEntries);
        product.setListProImg(updatedProImg);
        product.setDateUpdated(LocalDate.now());
        productRepository.save(product);
        String currentProImg1 = product.getListProImg();
        if (currentProImg == null || currentProImg.trim().isEmpty()) {
            throw new BadRequestException("No images found for this product.");
        }
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        String[] imageEntries1 = currentProImg.split(", ");
        for (String imageEntry : imageEntries1) {
            String[] parts = imageEntry.split(": ");
            int stt = Integer.parseInt(parts[0]);
            String url = parts[1];
            productImageResponses.add(new ProductImageResponse(stt, url));
        }
        return new ListProductImageResponse(proId,productImageResponses);
    }

    public ImgResponse deleteAllImageFromProduct(int proId) {
        Product product = productRepository.findByProId(proId);
        if (product == null) {
            throw new NotFoundException("Not found product with ID: " + proId);
        }
        product.setListProImg("");
        product.setDateUpdated(LocalDate.now());
        productRepository.save(product);
        return new ImgResponse(product.getListProImg());
    }

    public ListProductImageResponse getAllProductImages(int proId) {
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        Product product = productRepository.findByProId(proId);
        if (product == null) {
            throw new NotFoundException("Not found product with ID: " + proId);
        }
        String currentProImg = product.getListProImg();
        if(currentProImg != null && !currentProImg.trim().isEmpty())
        {
            String[] imageEntries = currentProImg.split(", ");
            for (String imageEntry : imageEntries) {
                String[] parts = imageEntry.split(": ");
                int stt = Integer.parseInt(parts[0]);
                String url = parts[1];
                productImageResponses.add(new ProductImageResponse(stt, url));
            }
        }
        return new ListProductImageResponse(proId,productImageResponses);
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
                            Sort.by(Sort.Direction.DESC, "dateCreated")
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