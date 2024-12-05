package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.NotFoundException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductVariantsRepository productVariantsRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private FavouriteItemRepository favouriteItemRepository;

    @Transactional
    public ResponseEntity<?> crateProduct(CreateProductReq req) {
        Category category = categoryRepository.findByCateIdAndIsDeletedFalse(req.getCateId());
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("cateId not exists");
        }
        Product product = productRepository.findByProNameAndIsDeletedFalse(req.getProName());
        if (product != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("product already exists");
        }

        Product product1 = new Product();
        product1.setCategory(category);
        product1.setDescription(req.getDescription());
        product1.setProName(req.getProName());
        product1.setListProImg("");
        product1.setIsDeleted(false);
        product1.setDateCreated(LocalDateTime.now());
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
        List<CRUDProductVarResponse> variantResponses = Optional.ofNullable(product1.getProductVariants())
                .orElse(Collections.emptyList())
                .stream()
                .map(variant -> new CRUDProductVarResponse(
                        variant.getVarId(),
                        variant.getProduct().getProId(),
                        variant.getSize(),
                        variant.getPrice(),
                        variant.getStock(),
                        variant.getIsDeleted(),
                        variant.getDateDeleted(),
                        variant.getDateCreated(),
                        variant.getDateUpdated()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                productImageResponses,
                product1.getDescription(),
                product1.getIsDeleted(),
                product1.getDateDeleted(),
                product1.getDateCreated(),
                product1.getDateUpdated(),
                variantResponses
        ));
    }

    @Transactional
    public ResponseEntity<?> getOneProduct(Integer id) {
        Product product1 = productRepository.findByProIdAndIsDeletedFalse(id);
        if (product1 == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists");
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
        List<CRUDProductVarResponse> variantResponses = Optional.ofNullable(product1.getProductVariants())
                .orElse(Collections.emptyList()) // Trả về danh sách rỗng nếu là null
                .stream()
                .map(variant -> new CRUDProductVarResponse(
                        variant.getVarId(),
                        variant.getProduct().getProId(),
                        variant.getSize(),
                        variant.getPrice(),
                        variant.getStock(),
                        variant.getIsDeleted(),
                        variant.getDateDeleted(),
                        variant.getDateCreated(),
                        variant.getDateUpdated()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                productImageResponses,
                product1.getDescription(),
                product1.getIsDeleted(),
                product1.getDateDeleted(),
                product1.getDateCreated(),
                product1.getDateUpdated(),
                variantResponses
        ));
    }

    @Transactional
    public ResponseEntity<?> updateProduct(CRUDProductReq req) {
        Category category = categoryRepository.findByCateIdAndIsDeletedFalse(req.getCateId());
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not exists");
        }
        Product product = productRepository.findByProNameAndProIdNot(req.getProName(), req.getProId());
        if (product != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("product already exists");
        }
        Product product1 = productRepository.findByProIdAndIsDeletedFalse(req.getProId());
        if (product1 == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists");
        }
        product1.setCategory(category);
        product1.setDescription(req.getDescription());
        product1.setProName(req.getProName());
        product1.setDateUpdated(LocalDateTime.now());
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
        List<CRUDProductVarResponse> variantResponses = Optional.ofNullable(product1.getProductVariants())
                .orElse(Collections.emptyList()) // Trả về danh sách rỗng nếu là null
                .stream()
                .map(variant -> new CRUDProductVarResponse(
                        variant.getVarId(),
                        variant.getProduct().getProId(),
                        variant.getSize(),
                        variant.getPrice(),
                        variant.getStock(),
                        variant.getIsDeleted(),
                        variant.getDateDeleted(),
                        variant.getDateCreated(),
                        variant.getDateUpdated()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDProductResponse(
                product1.getProId(),
                product1.getCategory().getCateId(),
                product1.getProName(),
                productImageResponses,
                product1.getDescription(),
                product1.getIsDeleted(),
                product1.getDateDeleted(),
                product1.getDateCreated(),
                product1.getDateUpdated(),
                variantResponses
        ));
    }

    @Transactional
    public ResponseEntity<?> listProduct(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Product> productList = productRepository.findByIsDeletedFalse(pageable);
        List<CRUDProductResponse> crudProductResponseList = new ArrayList<>();
        int total =0;
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
            List<CRUDProductVarResponse> variantResponses = Optional.ofNullable(product1.getProductVariants())
                    .orElse(Collections.emptyList()) // Trả về danh sách rỗng nếu là null
                    .stream()
                    .map(variant -> new CRUDProductVarResponse(
                            variant.getVarId(),
                            variant.getProduct().getProId(),
                            variant.getSize(),
                            variant.getPrice(),
                            variant.getStock(),
                            variant.getIsDeleted(),
                            variant.getDateDeleted(),
                            variant.getDateCreated(),
                            variant.getDateUpdated()
                    ))
                    .toList();

            crudProductResponseList.add(new CRUDProductResponse(
                    product1.getProId(),
                    product1.getCategory().getCateId(),
                    product1.getProName(),
                    productImageResponses,
                    product1.getDescription(),
                    product1.getIsDeleted(),
                    product1.getDateDeleted(),
                    product1.getDateCreated(),
                    product1.getDateUpdated(),
                    variantResponses
            ));
            total++;
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListProductResponse(page, productList.getTotalPages(), limit, total,crudProductResponseList));
    }

    @Transactional
    public ResponseEntity<?> getAllProductVariantFromProduct(int id) {
        Product product = productRepository.findByProIdAndIsDeletedFalse(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists");
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
        return ResponseEntity.status(HttpStatus.OK).body(new GetProductVariantFromProductIdResponse(
                id,
                crudProductVarResponseList
        ));
    }

    @Transactional
    public ResponseEntity<?> totalSearchProduct(String keyword, String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);

        // Lấy danh sách sản phẩm và tổng số phần tử
        Page<Product> productList = productRepository.findByProNameContainingAndIsDeletedFalse(keyword, pageable);
        int total = (int) productList.getTotalElements(); // Đếm tổng số sản phẩm

        List<CRUDProductResponse> crudProductResponseList = new ArrayList<>();
        for (Product product1 : productList) {
            List<ProductImageResponse> productImageResponses = new ArrayList<>();
            String currentProImg = product1.getListProImg();
            if (currentProImg != null && !currentProImg.trim().isEmpty()) {
                String[] imageEntries1 = currentProImg.split(", ");
                for (String imageEntry : imageEntries1) {
                    String[] parts = imageEntry.split(": ");
                    int stt = Integer.parseInt(parts[0]);
                    String url = parts[1];
                    productImageResponses.add(new ProductImageResponse(stt, url));
                }
            }

            List<CRUDProductVarResponse> variantResponses = Optional.ofNullable(product1.getProductVariants())
                    .orElse(Collections.emptyList()) // Trả về danh sách rỗng nếu là null
                    .stream()
                    .map(variant -> new CRUDProductVarResponse(
                            variant.getVarId(),
                            variant.getProduct().getProId(),
                            variant.getSize(),
                            variant.getPrice(),
                            variant.getStock(),
                            variant.getIsDeleted(),
                            variant.getDateDeleted(),
                            variant.getDateCreated(),
                            variant.getDateUpdated()
                    ))
                    .toList();

            crudProductResponseList.add(new CRUDProductResponse(
                    product1.getProId(),
                    product1.getCategory().getCateId(),
                    product1.getProName(),
                    productImageResponses,
                    product1.getDescription(),
                    product1.getIsDeleted(),
                    product1.getDateDeleted(),
                    product1.getDateCreated(),
                    product1.getDateUpdated(),
                    variantResponses
            ));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new TotalSearchProductResponse(page, productList.getTotalPages(), limit, total, crudProductResponseList));
    }

    @Transactional
    public ResponseEntity<?> deleteImageFromProduct(int proId, int deleteStt ) {
        Product product = productRepository.findByProIdAndIsDeletedFalse(proId);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists");
        }
        String currentProImg = product.getListProImg();
        if (currentProImg == null || currentProImg.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No images found for this product.");
        }
        String[] imageEntries = currentProImg.split(", ");
        List<String> updatedImageEntries = new ArrayList<>();
        int currentStt = 1;
        for (String imageEntry : imageEntries) {
            String[] parts = imageEntry.split(": ");
            int stt = Integer.parseInt(parts[0]);
            String url = parts[1];

            if (stt == deleteStt) {
                continue;
            }
            updatedImageEntries.add(currentStt + ": " + url);
            currentStt++;
        }
        String updatedProImg = String.join(", ", updatedImageEntries);
        product.setListProImg(updatedProImg);
        product.setDateUpdated(LocalDateTime.now());
        productRepository.save(product);
        String currentProImg1 = product.getListProImg();
        if (currentProImg == null || currentProImg.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No images found for this product.");
        }
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        String[] imageEntries1 = currentProImg.split(", ");
        for (String imageEntry : imageEntries1) {
            String[] parts = imageEntry.split(": ");
            int stt = Integer.parseInt(parts[0]);
            String url = parts[1];
            productImageResponses.add(new ProductImageResponse(stt, url));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListProductImageResponse(proId,productImageResponses.size(),productImageResponses));
    }

    public ResponseEntity<?> deleteAllImageFromProduct(int proId) {
        Product product = productRepository.findByProIdAndIsDeletedFalse(proId);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists");
        }
        product.setListProImg("");
        product.setDateUpdated(LocalDateTime.now());

        productRepository.save(product);
        return ResponseEntity.status(HttpStatus.OK).body(new ImgResponse(product.getListProImg()));
    }

    @Transactional
    public ResponseEntity<?> getAllProductImages(int proId) {
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        Product product = productRepository.findByProId(proId);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists");
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
        return ResponseEntity.status(HttpStatus.OK).body(new ListProductImageResponse(proId,productImageResponses.size(),productImageResponses));
    }

    @Transactional
    public ResponseEntity<?> filterProduct(FilterProductBox req) {
        int page = Integer.parseInt(req.getPage());
        int limit = Integer.parseInt(req.getLimit());

        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        // c -1: lay tat ca
        List<CRUDProductVarFilterResponse> crudProductVarFilterResponseList = new ArrayList<>();
        if(req.getC() != -1)
        {
            Category category = categoryRepository.findByCateIdAndIsDeletedFalse(req.getC());
            if (category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not exists");
            }
        }
        if(req.getP() != null && !req.getP().isEmpty())
        {
            for (Integer id : req.getP() ) {
                Product product = productRepository.findByProIdAndIsDeletedFalse(id);
                if (product == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists");
                }
            }
        }

        if (req.getO() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("o must be greater than 0");
        }
        //option:
        // 1: gia thap den cao
        // 2 gia cao den thap
        // 3: theo ngay moi nhat
        // 4: theo rating thap den cao
        // 5: theo rating cao den thap
        int total = 0;
        long totalPages = 0;
        if (req.getO() == 1) {
            Page<Product> productWithPrices;
            if(req.getC() == -1){
                productWithPrices = productRepository.findAllProductsWithMinPriceNoCategory(pageable);
            } else if (req.getP() == null || req.getP().isEmpty()) {
                productWithPrices = productRepository.findProductsWithMinPriceNoProduct(req.getC(),pageable);
            }
            else {
                productWithPrices = productRepository.findProductsWithMinPrice(req.getC(), req.getP(),pageable);
            }
            totalPages = productWithPrices.getTotalPages();
            for (Product product: productWithPrices) {
                List<CRUDProductVarResponse> variantResponses = product.getProductVariants().stream()
                        .map(variant -> new CRUDProductVarResponse(
                                variant.getVarId(),
                                variant.getProduct().getProId(),
                                variant.getSize(),
                                variant.getPrice(),
                                variant.getStock(),
                                variant.getIsDeleted(),
                                variant.getDateDeleted(),
                                variant.getDateCreated(),
                                variant.getDateUpdated()
                        ))
                        .toList();

                Double avgRating = productRepository.findAverageRatingByProductId(product.getCategory().getCateId(), product.getProId());
                if (avgRating == null) avgRating = 0.0;
                List<ProductImageResponse> productImageResponses = new ArrayList<>();
                String currentProImg = product.getListProImg();
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
                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
                        Math.round(avgRating * 10) / 10.0,
                        product.getProId(),
                        product.getProName(),
                        productImageResponses,
                        product.getIsDeleted(),
                        product.getDateDeleted(),
                        product.getDateCreated(),
                        product.getDateUpdated(),
                        variantResponses
                ));
                total ++;
            }

        } else if (req.getO() == 2) {
            Page<Product> productWithPrices;
            if(req.getC() == -1) {
                productWithPrices = productRepository.findAllProductsWithMaxPriceNoCategory(pageable);
            }
            else if (req.getP() == null || req.getP().isEmpty()) {
                productWithPrices = productRepository.findProductsWithMaxPriceNoProduct(req.getC(),pageable);
            } else {
                productWithPrices = productRepository.findProductsWithMaxPrice(req.getC(), req.getP(),pageable);
            }
            totalPages = productWithPrices.getTotalPages();
            for (Product product : productWithPrices) {
                List<CRUDProductVarResponse> variantResponses = product.getProductVariants().stream()
                        .map(variant -> new CRUDProductVarResponse(
                                variant.getVarId(),
                                variant.getProduct().getProId(),
                                variant.getSize(),
                                variant.getPrice(),
                                variant.getStock(),
                                variant.getIsDeleted(),
                                variant.getDateDeleted(),
                                variant.getDateCreated(),
                                variant.getDateUpdated()
                        ))
                        .toList();

                Double avgRating = productRepository.findAverageRatingByProductId(product.getCategory().getCateId(), product.getProId());
                if (avgRating == null) avgRating = 0.0;
                List<ProductImageResponse> productImageResponses = new ArrayList<>();
                String currentProImg = product.getListProImg();
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
                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
                        Math.round(avgRating * 10) / 10.0,
                        product.getProId(),
                        product.getProName(),
                        productImageResponses,
                        product.getIsDeleted(),
                        product.getDateDeleted(),
                        product.getDateCreated(),
                        product.getDateUpdated(),
                        variantResponses
                ));
                total++;
            }
        }
         else if (req.getO() == 3) {
            Page<Product> productWithPrices;

            Sort sort = Sort.by(Sort.Direction.DESC, "dateCreated");
            if (req.getC() == -1) {
                Pageable pageable1 = PageRequest.of(page, limit, sort);
                productWithPrices = productRepository.findByIsDeletedFalse(pageable1);
            } else if (req.getP() == null || req.getP().isEmpty()) {
                Pageable pageable1 = PageRequest.of(page, limit, sort);
                productWithPrices = productRepository.findByCategory_CateIdAndIsDeletedFalse(req.getC(), pageable1);
            } else {
                Pageable pageable1 = PageRequest.of(page, limit, sort);
                productWithPrices = productRepository.findByCategory_CateIdAndProIdInAndIsDeletedFalse(req.getC(), req.getP(), pageable1);
            }

            totalPages = productWithPrices.getTotalPages();
            for (Product product: productWithPrices) {
                List<CRUDProductVarResponse> variantResponses = product.getProductVariants().stream()
                        .map(variant -> new CRUDProductVarResponse(
                                variant.getVarId(),
                                variant.getProduct().getProId(),
                                variant.getSize(),
                                variant.getPrice(),
                                variant.getStock(),
                                variant.getIsDeleted(),
                                variant.getDateDeleted(),
                                variant.getDateCreated(),
                                variant.getDateUpdated()
                        ))
                        .toList();

                Double avgRating = productRepository.findAverageRatingByProductId(product.getCategory().getCateId(), product.getProId());
                if (avgRating == null) avgRating = 0.0;
                List<ProductImageResponse> productImageResponses = new ArrayList<>();
                String currentProImg = product.getListProImg();
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
                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
                        Math.round(avgRating * 10) / 10.0,
                        product.getProId(),
                        product.getProName(),
                        productImageResponses,
                        product.getIsDeleted(),
                        product.getDateDeleted(),
                        product.getDateCreated(),
                        product.getDateUpdated(),
                        variantResponses
                ));
                total ++;
            }
        }
        else if (req.getO() == 4) {
            Page<Product> productWithPrices;
            if(req.getC() == -1)
            {
                productWithPrices = productRepository.findTopRatedProductsDesc(pageable);
            }
            else if (req.getP() == null || req.getP().isEmpty()) {
                productWithPrices = productRepository.findTopRatedProductsDescByCategory(req.getC(),pageable);
            } else {
                productWithPrices = productRepository.findTopRatedProductsDesc(req.getC(), req.getP(),pageable);
            }
            totalPages = productWithPrices.getTotalPages();
            for (Product product: productWithPrices) {
                List<CRUDProductVarResponse> variantResponses = product.getProductVariants().stream()
                        .map(variant -> new CRUDProductVarResponse(
                                variant.getVarId(),
                                variant.getProduct().getProId(),
                                variant.getSize(),
                                variant.getPrice(),
                                variant.getStock(),
                                variant.getIsDeleted(),
                                variant.getDateDeleted(),
                                variant.getDateCreated(),
                                variant.getDateUpdated()
                        ))
                        .toList();

                Double avgRating = productRepository.findAverageRatingByProductId(product.getCategory().getCateId(), product.getProId());
                if (avgRating == null) avgRating = 0.0;
                List<ProductImageResponse> productImageResponses = new ArrayList<>();
                String currentProImg = product.getListProImg();
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
                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
                        Math.round(avgRating * 10) / 10.0,
                        product.getProId(),
                        product.getProName(),
                        productImageResponses,
                        product.getIsDeleted(),
                        product.getDateDeleted(),
                        product.getDateCreated(),
                        product.getDateUpdated(),
                        variantResponses
                ));
                total ++;
            }
        }
         else if (req.getO() == 5) {
            Page<Product> productWithPrices;
            if(req.getC() == -1)
            {
                productWithPrices = productRepository.findTopRatedProductsAsc(pageable);
            }
            else  if (req.getP() == null || req.getP().isEmpty()) {
                productWithPrices = productRepository.findTopRatedProductsAscByCategory(req.getC(),pageable);

            } else {
                productWithPrices = productRepository.findTopRatedProductsAsc(req.getC(), req.getP(),pageable);
            }
            totalPages = productWithPrices.getTotalPages();
            for (Product product: productWithPrices) {
                List<CRUDProductVarResponse> variantResponses = product.getProductVariants().stream()
                        .map(variant -> new CRUDProductVarResponse(
                                variant.getVarId(),
                                variant.getProduct().getProId(),
                                variant.getSize(),
                                variant.getPrice(),
                                variant.getStock(),
                                variant.getIsDeleted(),
                                variant.getDateDeleted(),
                                variant.getDateCreated(),
                                variant.getDateUpdated()
                        ))
                        .toList();

                Double avgRating = productRepository.findAverageRatingByProductId(product.getCategory().getCateId(), product.getProId());
                if (avgRating == null) avgRating = 0.0;
                List<ProductImageResponse> productImageResponses = new ArrayList<>();
                String currentProImg = product.getListProImg();
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
                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
                        Math.round(avgRating * 10) / 10.0,
                        product.getProId(),
                        product.getProName(),
                        productImageResponses,
                        product.getIsDeleted(),
                        product.getDateDeleted(),
                        product.getDateCreated(),
                        product.getDateUpdated(),
                        variantResponses
                ));
                total ++;
            }
        }

         return ResponseEntity.status(HttpStatus.OK).body(new FilterProductBoxResponse(
                    page,
                    totalPages,
                    limit,
                    total,
                    crudProductVarFilterResponseList
            ));
        }

    @Transactional
    public ResponseEntity<?> resetAllQuantityProduct()
    {
        List<Product> productList = productRepository.findAll();
        for(Product product : productList)
        {
            List<ProductVariants> productVariants = productVariantsRepository.findByProduct_ProId(product.getProId());
            for(ProductVariants productVariant : productVariants)
            {
                productVariant.setStock(50);
                productVariantsRepository.save(productVariant);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body("Reset success");
    }

    @Transactional
    public ResponseEntity<?> disableProduct(int proId){
        Product product = productRepository.findByProId(proId);
        if(product == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        if(product.getIsDeleted()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product is deleted");
        }
        List<ProductVariants> productVariants = productVariantsRepository.findByProduct_ProId(product.getProId());
        for(ProductVariants productVariant: productVariants)
        {
            productVariant.setIsDeleted(true);
            productVariant.setDateDeleted(LocalDateTime.now());
            productVariantsRepository.save(productVariant);
            List<FavouriteItem> favouriteItems = favouriteItemRepository.findByProductVariants_VarId(productVariant.getVarId());
            for(FavouriteItem favouriteItem: favouriteItems)
            {
                favouriteItem.setIsDeleted(true);
                favouriteItem.setDateDeleted(LocalDateTime.now());
                favouriteItemRepository.save(favouriteItem);
            }
            List<CartItem> cartItems = cartItemRepository.findByProductVariants_VarId(productVariant.getVarId());
            for(CartItem cartItem : cartItems)
            {
                cartItem.setIsDeleted(true);
                cartItem.setDateDeleted(LocalDateTime.now());
                cartItemRepository.save(cartItem);
            }
            List<Cart> carts = cartRepository.findAll();
            for(Cart cart : carts){
                List<CartItem> cartItems1 = cartItemRepository.findByCart_CartIdAndIsDeletedFalse(cart.getCartId());
                double total = 0.0 ;
                int quantity = 0;
                for(CartItem cartItem1 : cartItems1)
                {
                    total += cartItem1.getTotalPrice();
                    quantity += cartItem1.getQuantity();
                }
                cart.setTotalPrice(total);
                cart.setTotalProduct(quantity);

                cartRepository.save(cart);
            }
        }
        List<Review> reviewList = reviewRepository.findAllByProduct_ProId(product.getProId());
        for(Review review : reviewList)
        {
            review.setIsDeleted(true);
            review.setDateDeleted(LocalDateTime.now());
            reviewRepository.save(review);
        }
        product.setIsDeleted(true);
        product.setDateDeleted(LocalDateTime.now());
        productRepository.save(product);
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        String currentProImg = product.getListProImg();
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
        List<CRUDProductVarResponse> variantResponses = Optional.ofNullable(product.getProductVariants())
                .orElse(Collections.emptyList()) // Trả về danh sách rỗng nếu là null
                .stream()
                .map(variant -> new CRUDProductVarResponse(
                        variant.getVarId(),
                        variant.getProduct().getProId(),
                        variant.getSize(),
                        variant.getPrice(),
                        variant.getStock(),
                        variant.getIsDeleted(),
                        variant.getDateDeleted(),
                        variant.getDateCreated(),
                        variant.getDateUpdated()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body( new CRUDProductResponse(
                product.getProId(),
                product.getCategory().getCateId(),
                product.getProName(),
                productImageResponses,
                product.getDescription(),
                product.getIsDeleted(),
                product.getDateDeleted(),
                product.getDateCreated(),
                product.getDateUpdated(),
                variantResponses
        ));
    }

    @Transactional
    public ResponseEntity<?> enableProduct(int proId){
        Product product = productRepository.findByProId(proId);
        if(product == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        if(!product.getIsDeleted()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product is enable");
        }
        List<ProductVariants> productVariants = productVariantsRepository.findByProduct_ProId(product.getProId());
        for(ProductVariants productVariant: productVariants)
        {
            productVariant.setIsDeleted(false);
            productVariant.setDateDeleted(null);
            productVariantsRepository.save(productVariant);
            List<FavouriteItem> favouriteItems = favouriteItemRepository.findByProductVariants_VarId(productVariant.getVarId());
            for(FavouriteItem favouriteItem: favouriteItems)
            {
                favouriteItem.setIsDeleted(false);
                favouriteItem.setDateDeleted(null);
                favouriteItemRepository.save(favouriteItem);
            }
            List<CartItem> cartItems = cartItemRepository.findByProductVariants_VarId(productVariant.getVarId());
            for(CartItem cartItem : cartItems)
            {
                cartItem.setIsDeleted(false);
                cartItem.setDateDeleted(null);
                cartItemRepository.save(cartItem);
            }
            List<Cart> carts = cartRepository.findAll();
            for(Cart cart : carts){
                List<CartItem> cartItems1 = cartItemRepository.findByCart_CartIdAndIsDeletedFalse(cart.getCartId());
                double total = 0.0 ;
                int quantity = 0;
                for(CartItem cartItem1 : cartItems1)
                {
                    total += cartItem1.getTotalPrice();
                    quantity += cartItem1.getQuantity();
                }
                cart.setTotalPrice(total);
                cart.setTotalProduct(quantity);

                cartRepository.save(cart);
            }
        }
        List<Review> reviewList = reviewRepository.findAllByProduct_ProId(product.getProId());
        for(Review review : reviewList)
        {
            review.setIsDeleted(false);
            review.setDateDeleted(null);
            reviewRepository.save(review);
        }
        product.setIsDeleted(false);
        product.setDateDeleted(null);
        productRepository.save(product);
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        String currentProImg = product.getListProImg();
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
        List<CRUDProductVarResponse> variantResponses = Optional.ofNullable(product.getProductVariants())
                .orElse(Collections.emptyList()) // Trả về danh sách rỗng nếu là null
                .stream()
                .map(variant -> new CRUDProductVarResponse(
                        variant.getVarId(),
                        variant.getProduct().getProId(),
                        variant.getSize(),
                        variant.getPrice(),
                        variant.getStock(),
                        variant.getIsDeleted(),
                        variant.getDateDeleted(),
                        variant.getDateCreated(),
                        variant.getDateUpdated()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body( new CRUDProductResponse(
                product.getProId(),
                product.getCategory().getCateId(),
                product.getProName(),
                productImageResponses,
                product.getDescription(),
                product.getIsDeleted(),
                product.getDateDeleted(),
                product.getDateCreated(),
                product.getDateUpdated(),
                variantResponses
        ));
    }
    @Transactional
    public ResponseEntity<?> listProductsWithAverageRating(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<Product> productList = productRepository.findByIsDeletedFalse(pageable);
        List<ProductWithAvgRatingResponse> productWithAvgRatingResponses = new ArrayList<>();
        int total = 0;

        for (Product product : productList) {
            // Fetch all reviews for this product
            List<Review> reviews = reviewRepository.findByProduct_ProIdAndIsDeletedFalse(product.getProId());
            double avgRating = 0.0;

            if (!reviews.isEmpty()) {
                // Calculate average rating
                avgRating = reviews.stream()
                        .mapToInt(Review::getRatingStar)
                        .average()
                        .orElse(0.0);
            }

            // Process product images
            List<ProductImageResponse> productImageResponses = new ArrayList<>();
            String currentProImg = product.getListProImg();
            if (currentProImg != null && !currentProImg.trim().isEmpty()) {
                String[] imageEntries = currentProImg.split(", ");
                for (String imageEntry : imageEntries) {
                    String[] parts = imageEntry.split(": ");
                    int stt = Integer.parseInt(parts[0]);
                    String url = parts[1];
                    productImageResponses.add(new ProductImageResponse(stt, url));
                }
            }

            // Add product and its avg rating to response list
            productWithAvgRatingResponses.add(new ProductWithAvgRatingResponse(
                    product.getProId(),
                    product.getCategory().getCateId(),
                    product.getProName(),
                    productImageResponses,
                    product.getDescription(),
                    avgRating,
                    product.getIsDeleted(),
                    product.getDateDeleted(),
                    product.getDateCreated(),
                    product.getDateUpdated()
            ));
            total++;
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ListProductWithAvgRatingResponse(
                page,
                productList.getTotalPages(),
                limit,
                total,
                productWithAvgRatingResponses
        ));
    }

    @Transactional
    public  ResponseEntity<?> listAvgRatingProduct()
    {
        List<Product> productList = productRepository.findByIsDeletedFalse();
        List<AvgRatingProduct> avgRatingProductList = new ArrayList<>();
        for(Product product : productList)
        {
            Double avgRating = productRepository.findAverageRatingByProductId(product.getCategory().getCateId(), product.getProId());
            if(avgRating == null) {
                avgRating = 0.0;
            }
            AvgRatingProduct avgRatingProduct = new AvgRatingProduct(product.getProId(),avgRating);
            avgRatingProductList.add(avgRatingProduct);
        }
        return new ResponseEntity<>(new ListAllProductRating(avgRatingProductList.size(),avgRatingProductList), HttpStatus.OK);
    }
}
