package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Sex;
import com.hmdrinks.Enum.TypeLogin;
import com.hmdrinks.Enum.Type_Post;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.ConflictException;
import com.hmdrinks.Exception.NotFoundException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.CreateAccountUserReq;
import com.hmdrinks.Request.DeleteReviewReq;
import com.hmdrinks.Request.FilterProductBox;
import com.hmdrinks.Request.UpdateAccountUserReq;
import com.hmdrinks.Response.*;
import com.hmdrinks.SupportFunction.SupportFunction;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private SupportFunction supportFunction;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ProductVariantsRepository productVariantsRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public ResponseEntity<?> createAccountUser(CreateAccountUserReq req){
        Optional<User> user = userRepository.findByUserNameAndIsDeletedFalse(req.getUserName());

        if (user.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User name already exists");
        }

        if (!supportFunction.checkRole(req.getRole().toString())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role is wrong");
        }

        User userWithEmail = userRepository.findByEmail(req.getEmail());
        if (userWithEmail != null ) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        LocalDate currentDate = LocalDate.now();
        User user1 = new User();
        user1.setType(TypeLogin.BASIC);
        user1.setEmail(req.getEmail());
        user1.setRole(req.getRole());
        user1.setIsDeleted(false);
        user1.setUserName(req.getUserName());
        user1.setAvatar("");
        user1.setDistrict("");
        user1.setWard("");
        user1.setCity("");
        user1.setStreet("");
        user1.setSex(Sex.OTHER);
        user1.setDateCreated(Date.valueOf(currentDate));
        user1.setPhoneNumber("");
        user1.setPassword(passwordEncoder.encode(req.getPassword()));
        user1.setFullName(req.getFullName());
        userRepository.save(user1);

        Optional<User> userNewq = userRepository.findByUserNameAndIsDeletedFalse(req.getUserName());
        User userNew = userNewq.get();
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDAccountUserResponse(
                userNew.getUserId(),
                userNew.getUserName(),
                userNew.getFullName(),
                userNew.getAvatar(),
                userNew.getBirthDate(),
                "",
                userNew.getEmail(),
                userNew.getPhoneNumber(),
                userNew.getSex().toString(),
                userNew.getType().toString(),
                userNew.getIsDeleted(),
                userNew.getDateDeleted(),
                userNew.getDateUpdated(),
                userNew.getDateCreated(),
                userNew.getRole().toString()
        ));
    }
    public ResponseEntity<?>  updateAccountUser(UpdateAccountUserReq req) {
        Optional<User> existingUserOptional = userRepository.findById(req.getUserId());
        if (existingUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        }
        User existingUser = existingUserOptional.get();
        if (req.getIsDeleted() != null && !req.getIsDeleted() && existingUser.getIsDeleted()) {
            existingUser.setIsDeleted(false);
        }

        if (req.getFullName() != null && !req.getFullName().isEmpty()) {
            existingUser.setFullName(req.getFullName());
        }

        if (req.getUserName() != null && !req.getUserName().isEmpty()) {
            Optional<User> userWithSameUserName = userRepository.findByUserNameAndIsDeletedFalse(req.getUserName());
            if (userWithSameUserName.isPresent() && userWithSameUserName.get().getUserId() != req.getUserId()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User name already exists");
            }
            existingUser.setUserName(req.getUserName());
        }

        if (req.getEmail() != null && !req.getEmail().isEmpty()) {
            User userWithEmail = userRepository.findByEmail(req.getEmail());
            if (userWithEmail != null && userWithEmail.getUserId() != req.getUserId()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            }
            existingUser.setEmail(req.getEmail());
        }

        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        if (req.getRole() != null) {
            if (!supportFunction.checkRole(req.getRole().toString())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role");
            }
            existingUser.setRole(req.getRole());
        }

        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isEmpty()) {
            existingUser.setPhoneNumber(req.getPhoneNumber());
        }
        if (req.getIsDeleted() != null) {
            existingUser.setIsDeleted(req.getIsDeleted());
        }

        userRepository.save(existingUser);
        String fullLocation = existingUser.getStreet() + "," + existingUser.getWard() +
                existingUser.getDistrict() + ","+ existingUser.getCity();
        // Return updated user information as response
        return ResponseEntity.status(HttpStatus.OK).body( new CRUDAccountUserResponse(
                existingUser.getUserId(),
                existingUser.getUserName(),
                existingUser.getFullName(),
                existingUser.getAvatar(),
                existingUser.getBirthDate(),
                fullLocation,
                existingUser.getEmail(),
                existingUser.getPhoneNumber(),
                existingUser.getSex().toString(),
                existingUser.getType().toString(),
                existingUser.getIsDeleted(),
                existingUser.getDateDeleted(),
                existingUser.getDateUpdated(),
                existingUser.getDateCreated(),
                existingUser.getRole().toString()
        ));
    }
    public String deleteOneReview(int reviewId)
    {
        Review review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId);
        if (review == null)
        {
            throw new BadRequestException("Review not found");
        }
        review.setIsDeleted(true);
        review.setDateDeleted(LocalDateTime.now());
        reviewRepository.save(review);
        return "Review deleted";
    }

    public String deleteALlReviewProduct(int proId){
        List<Review> reviewList = reviewRepository.findByProduct_ProIdAndIsDeletedFalse(proId);
        if(reviewList != null)
        {
            for(Review review : reviewList){
                review.setIsDeleted(true);
                review.setDateDeleted(LocalDateTime.now());
                reviewRepository.save(review);
            }
        }

        return "All review product deleted";
    }

    public ResponseEntity<?> getAllProductImages(int proId) {
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        Product product = productRepository.findByProId(proId);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found product with ID: " + proId);
        }
        String currentProImg = product.getListProImg();
        int total = 0;
        if(currentProImg != null && !currentProImg.trim().isEmpty())
        {
            String[] imageEntries = currentProImg.split(", ");
            for (String imageEntry : imageEntries) {
                String[] parts = imageEntry.split(": ");
                int stt = Integer.parseInt(parts[0]);
                String url = parts[1];
                productImageResponses.add(new ProductImageResponse(stt, url));
                total++;
            }
        }
        return  ResponseEntity.status(HttpStatus.OK).body(new ListProductImageResponse(proId,total,productImageResponses));
    }

    @Transactional
    public TotalSearchProductResponse totalSearchProduct(String keyword, String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Product> productList = productRepository.findByProNameContaining(keyword, pageable);

        // Đếm tổng số sản phẩm
        int total = (int) productList.getTotalElements();

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
        return new TotalSearchProductResponse(page, productList.getTotalPages(), limit, total, crudProductResponseList);
    }


    public GetProductVariantFromProductIdResponse getAllProductVariantFromProduct(int id) {
        Product product = productRepository.findByProId(id);
        if (product == null) {
            throw new BadRequestException("proId not exists");
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

    public FilterProductBoxResponse filterProduct(FilterProductBox req) {
//        List<CRUDProductVarFilterResponse> crudProductVarFilterResponseList = new ArrayList<>();
//        Category category = categoryRepository.findByCateId(req.getC());
//        if (category == null) {
//            throw new BadRequestException("cateId not exists");
//        }
//        for (Integer id : req.getP()) {
//            Product product = productRepository.findByProId(id);
//            if (product == null) {
//                throw new BadRequestException("productId not exists");
//            }
//        }
//
//        if (req.getO() <= 0) {
//            throw new BadRequestException("o must be greater than 0");
//        }
//
//        Sort sort;
//        int total = 0;
//        if (req.getO() == 1) {
//            sort = Sort.by(Sort.Direction.DESC, "price");
//            List<ProductVariants> productVariants = productVariantsRepository.findByProduct_Category_CateIdAndProduct_ProIdIn(req.getC(), req.getP(), sort);
//            for (ProductVariants productVariant : productVariants) {
//                Double avgRating = productRepository.findAverageRatingByProductIdAdmin(req.getC(),productVariant.getProduct().getProId());
//                if(avgRating == null) {
//                    avgRating = 0.0;
//                }
//                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
//                        Math.round(avgRating * 10) / 10.0,
//                        productVariant.getVarId(),
//                        productVariant.getProduct().getProId(),
//                        productVariant.getSize(),
//                        productVariant.getPrice(),
//                        productVariant.getStock(),
//                        productVariant.getIsDeleted(),
//                        productVariant.getDateDeleted(),
//                        productVariant.getDateCreated(),
//                        productVariant.getDateUpdated()
//                ));
//                total += 1;
//            }
//        } else if (req.getO() == 2) {
//            sort = Sort.by(Sort.Direction.ASC, "price");
//            List<ProductVariants> productVariants = productVariantsRepository.findByProduct_Category_CateIdAndProduct_ProIdIn(req.getC(), req.getP(), sort);
//            for (ProductVariants productVariant : productVariants) {
//                Double avgRating = productRepository.findAverageRatingByProductIdAdmin(req.getC(),productVariant.getProduct().getProId());
//                if(avgRating == null) {
//                    avgRating = 0.0;
//                }
//                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
//                        Math.round(avgRating * 10) / 10.0,
//                        productVariant.getVarId(),
//                        productVariant.getProduct().getProId(),
//                        productVariant.getSize(),
//                        productVariant.getPrice(),
//                        productVariant.getStock(),
//                        productVariant.getIsDeleted(),
//                        productVariant.getDateDeleted(),
//                        productVariant.getDateCreated(),
//                        productVariant.getDateUpdated()
//                ));
//                total += 1;
//            }
//        } else if (req.getO() == 3) {
//            List<ProductVariants> productVariants = productVariantsRepository
//                    .findByProduct_Category_CateIdAndProduct_ProIdIn(
//                            req.getC(),
//                            req.getP(),
//                            Sort.by(Sort.Direction.DESC, "dateCreated")
//                    );
//
//            for (ProductVariants productVariant : productVariants) {
//                Double avgRating = productRepository.findAverageRatingByProductIdAdmin(req.getC(),productVariant.getProduct().getProId());
//                if(avgRating == null) {
//                    avgRating = 0.0;
//                }
//                crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
//                        Math.round(avgRating * 10) / 10.0,
//                        productVariant.getVarId(),
//                        productVariant.getProduct().getProId(),
//                        productVariant.getSize(),
//                        productVariant.getPrice(),
//                        productVariant.getStock(),
//                        productVariant.getIsDeleted(),
//                        productVariant.getDateDeleted(),
//                        productVariant.getDateCreated(),
//                        productVariant.getDateUpdated()
//                ));
//                total += 1;
//            }
//        } else if (req.getO() == 4) {
//            List<Product> product = productRepository.findTopRatedProductsDescByAdmin(req.getC(), req.getP());
//            for (Product product1 : product) {
//                List<ProductVariants> productVariants = productVariantsRepository.findByProduct_ProId(product1.getProId());
//                Double  avgRating = productRepository.findAverageRatingByProductIdAdmin(req.getC(),product1.getProId());
//                for (ProductVariants productVariant : productVariants) {
//                    if(avgRating == null) {
//                        avgRating = 0.0;
//                    }
//                    crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
//                            Math.round(avgRating * 10) / 10.0,
//                            productVariant.getVarId(),
//                            productVariant.getProduct().getProId(),
//                            productVariant.getSize(),
//                            productVariant.getPrice(),
//                            productVariant.getStock(),
//                            productVariant.getIsDeleted(),
//                            productVariant.getDateDeleted(),
//                            productVariant.getDateCreated(),
//                            productVariant.getDateUpdated()
//                    ));
//                    total += 1;
//                }
//
//            }
//        } else if (req.getO() == 5) {
//            List<Product> product = productRepository.findTopRatedProductsAscByAdmin(req.getC(), req.getP());
//            for (Product product1 : product) {
//                List<ProductVariants> productVariants = productVariantsRepository.findByProduct_ProId(product1.getProId());
//                for (ProductVariants productVariant : productVariants) {
//                    Double avgRating = productRepository.findAverageRatingByProductIdAdmin(req.getC(),product1.getProId());
//                    if(avgRating == null) {
//                        avgRating = 0.0;
//                    }
//                    crudProductVarFilterResponseList.add(new CRUDProductVarFilterResponse(
//                            Math.round(avgRating * 10) / 10.0,
//                            productVariant.getVarId(),
//                            productVariant.getProduct().getProId(),
//                            productVariant.getSize(),
//                            productVariant.getPrice(),
//                            productVariant.getStock(),
//                            productVariant.getIsDeleted(),
//                            productVariant.getDateDeleted(),
//                            productVariant.getDateCreated(),
//                            productVariant.getDateUpdated()
//                    ));
//                    total += 1;
//                }
//            }
//        }

        return new FilterProductBoxResponse(
//                true,
//                1,
//                new ArrayList<>(),
//                "OK",
//                false
        );
    }

    @Transactional
    public ListProductResponse listProduct(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Product> productList = productRepository.findAll(pageable);
        List<Product> productList1 = productRepository.findAllByIsDeletedFalse();
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
        return new ListProductResponse(page, productList.getTotalPages(), limit,productList1.size(), crudProductResponseList);
    }

    @Transactional
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

        return new CRUDProductResponse(
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
        );
    }

    @Transactional
    public ResponseEntity<?> getAllProductFromCategory(int id,String pageFromParam, String limitFromParam)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Category category = categoryRepository.findByCateId(id);
        if(category == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("cateId not exists");
        }
        Page<Product> productList = productRepository.findByCategory_CateId(id,pageable);
        List<Product> productList1 = productRepository.findByCategory_CateId(id);
        List<CRUDProductResponse> crudProductResponseList = new ArrayList<>();
        int total =0 ;
        for(Product product1: productList)
        {
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

        return ResponseEntity.status(HttpStatus.OK).body(new GetViewProductCategoryResponse(
                page,
                productList.getTotalPages(),
                limit,
                productList1.size(),
                crudProductResponseList
        ));
    }

    @Transactional
    public ListAllPostResponse getAllPostByType(String pageFromParam, String limitFromParam, Type_Post typePost) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Post> posts = postRepository.findAllByType(typePost,pageable);
        List<Post> posts1 = postRepository.findAllByType(typePost);
        List<CRUDPostAndVoucherResponse> responses = new ArrayList<>();
        for(Post post : posts) {
            Voucher voucher = post.getVoucher();
            responses.add(new CRUDPostAndVoucherResponse(
                    post.getPostId(),
                    post.getType(),
                    post.getBannerUrl(),
                    post.getDescription(),
                    post.getTitle(),
                    post.getShortDes(),
                    post.getUser().getUserId(),
                    post.getIsDeleted(),
                    post.getDateDeleted(),
                    post.getDateCreate(),
                    new CRUDVoucherResponse(
                            voucher.getVoucherId(),
                            voucher.getKey(),
                            voucher.getNumber(),
                            voucher.getStartDate(),
                            voucher.getEndDate(),
                            voucher.getDiscount(),
                            voucher.getStatus(),
                            voucher.getPost().getPostId()
                    )
            ));
        }
        return new ListAllPostResponse(
                page,
                posts.getTotalPages(),
                limit,
                posts1.size(),
                responses
        );
    }

    @Transactional
    public ListAllPostResponse getAllPost(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Post> posts = postRepository.findAll(pageable);
        List<Post> posts1 = postRepository.findAllByIsDeletedFalse();

        List<CRUDPostAndVoucherResponse> responses = new ArrayList<>();
        int total = 0;
        for(Post post : posts) {
            Voucher voucher = post.getVoucher();
            responses.add(new CRUDPostAndVoucherResponse(
                    post.getPostId(),
                    post.getType(),
                    post.getBannerUrl(),
                    post.getDescription(),
                    post.getTitle(),
                    post.getShortDes(),
                    post.getUser().getUserId(),
                    post.getIsDeleted(),
                    post.getDateDeleted(),
                    post.getDateCreate(),
                    new CRUDVoucherResponse(
                            voucher.getVoucherId(),
                            voucher.getKey(),
                            voucher.getNumber(),
                            voucher.getStartDate(),
                            voucher.getEndDate(),
                            voucher.getDiscount(),
                            voucher.getStatus(),
                            voucher.getPost().getPostId()
                    )
            ));
            total++;
        }
        return new ListAllPostResponse(
                page,
                posts.getTotalPages(),
                limit,
                posts1.size(),
                responses
        );
    }

    public ListCategoryResponse listCategory(String pageFromParam, String limitFromParam)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Category> categoryList = categoryRepository.findAll(pageable);
        List<Category> categoryList1 = categoryRepository.findAllByIsDeletedFalse();
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
        return new ListCategoryResponse(page,categoryList.getTotalPages(),limit,categoryList1.size(),crudCategoryResponseList);
    }
}
