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
import java.util.List;

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
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private PaymentRepository paymentRepository;

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
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDProductResponse(
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
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDProductResponse(
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
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDProductResponse(
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
            total++;
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListProductResponse(page, productList.getTotalPages(), limit, total,crudProductResponseList));
    }

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

    public ResponseEntity<?> totalSearchProduct(String keyword, String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Product> productList = productRepository.findByProNameContainingAndIsDeletedFalse(keyword, pageable);
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
        return ResponseEntity.status(HttpStatus.OK).body(new TotalSearchProductResponse(page, productList.getTotalPages(), limit, crudProductResponseList));
    }

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

    public ResponseEntity<?> filterProduct(FilterProductBox req) {
        List<CRUDProductVarResponse> crudProductVarResponseList = new ArrayList<>();
        List<CRUDProductVarFilterResponse> crudProductVarFilterResponseList = new ArrayList<>();
        Category category = categoryRepository.findByCateIdAndIsDeletedFalse(req.getC());
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("category not exists");
        }
        for (Integer id : req.getP()) {
            Product product = productRepository.findByProIdAndIsDeletedFalse(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product not exists");
            }
        }

        if (req.getO() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("o must be greater than 0");
        }
        Sort sort;
        int total = 0;
        if (req.getO() == 1) {
            sort = Sort.by(Sort.Direction.DESC, "price");
            List<ProductVariants> productVariants = productVariantsRepository.findByProduct_Category_CateIdAndProduct_ProIdInAndIsDeletedFalse(req.getC(), req.getP(), sort);
            for (ProductVariants productVariant : productVariants) {
                Double avgRating = productRepository.findAverageRatingByProductId(req.getC(),productVariant.getProduct().getProId());
                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
                        Math.round(avgRating * 10) / 10.0,
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
            List<ProductVariants> productVariants = productVariantsRepository.findByProduct_Category_CateIdAndProduct_ProIdInAndIsDeletedFalse(req.getC(), req.getP(), sort);
            for (ProductVariants productVariant : productVariants) {
                Double avgRating = productRepository.findAverageRatingByProductId(req.getC(),productVariant.getProduct().getProId());
                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
                        Math.round(avgRating * 10) / 10.0,
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
                    .findByProduct_Category_CateIdAndProduct_ProIdInAndIsDeletedFalse(
                            req.getC(),
                            req.getP(),
                            Sort.by(Sort.Direction.DESC, "dateCreated")
                    );

            for (ProductVariants productVariant : productVariants) {
                Double avgRating = productRepository.findAverageRatingByProductId(req.getC(),productVariant.getProduct().getProId());
                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
                        Math.round(avgRating * 10) / 10.0,
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
            List<Product> product = productRepository.findTopRatedProductsDesc(req.getC(), req.getP());
            for (Product product1 : product) {
                List<ProductVariants> productVariants = productVariantsRepository.findByProduct_ProId(product1.getProId());
                Double  avgRating = productRepository.findAverageRatingByProductId(req.getC(),product1.getProId());
                for (ProductVariants productVariant : productVariants) {
                    crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(Math.round(avgRating * 10) / 10.0,
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
        } else if (req.getO() == 5) {
            List<Product> product = productRepository.findTopRatedProductsAsc(req.getC(), req.getP());
            Double avgRating = 0.0;
            for (Product product1 : product) {
                List<ProductVariants> productVariants = productVariantsRepository.findByProduct_ProId(product1.getProId());
                avgRating = productRepository.findAverageRatingByProductId(req.getC(),product1.getProId());
                for (ProductVariants productVariant : productVariants) {
                    avgRating = productRepository.findAverageRatingByProductId(req.getC(),product1.getProId());
                  crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
                          Math.round(avgRating * 10) / 10.0,
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
        }

            return ResponseEntity.status(HttpStatus.OK).body(new FilterProductBoxResponse(
                    true,
                    total,
                    crudProductVarFilterResponseList,
                    "OK",
                    false
            ));
        }

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
        return ResponseEntity.status(HttpStatus.OK).body( new CRUDProductResponse(
                product.getProId(),
                product.getCategory().getCateId(),
                product.getProName(),
                productImageResponses,
                product.getDescription(),
                product.getIsDeleted(),
                product.getDateDeleted(),
                product.getDateCreated(),
                product.getDateUpdated()
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
        return ResponseEntity.status(HttpStatus.OK).body( new CRUDProductResponse(
                product.getProId(),
                product.getCategory().getCateId(),
                product.getProName(),
                productImageResponses,
                product.getDescription(),
                product.getIsDeleted(),
                product.getDateDeleted(),
                product.getDateCreated(),
                product.getDateUpdated()
        ));
    }
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
}
