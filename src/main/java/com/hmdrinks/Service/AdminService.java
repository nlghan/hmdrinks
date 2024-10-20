package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Sex;
import com.hmdrinks.Enum.TypeLogin;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private ProductVariantsRepository productVariantsRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public CRUDAccountUserResponse createAccountUser(CreateAccountUserReq req){
        Optional<User> user = userRepository.findByUserNameAndIsDeletedFalse(req.getUserName());

        if (user.isPresent()) {
            throw new ConflictException("User name already exists");
        }

        if (!supportFunction.checkRole(req.getRole().toString())) {
            throw new BadRequestException("Role is wrong");
        }

        User userWithEmail = userRepository.findByEmail(req.getEmail());
        if (userWithEmail != null && !(userWithEmail.getUserName() == (req.getUserName()))) {
            // Nếu tìm thấy người dùng có email này nhưng không phải chính người đang tạo
            throw new ConflictException("Email already exists with another user");
        }
        // Gọi hàm checkPhoneNumber từ SupportFunction
        LocalDate currentDate = LocalDate.now();
        User user1 = new User();
        user1.setType(TypeLogin.BASIC);
        user1.setEmail(req.getEmail());
        user1.setRole(req.getRole());
        user1.setIsDeleted(false);
        user1.setUserName(req.getUserName());
        user1.setAvatar("");
        user1.setDistrict("");
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
        return new CRUDAccountUserResponse(
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
        );
    }
    public CRUDAccountUserResponse updateAccountUser(UpdateAccountUserReq req) {
        Optional<User> existingUserOptional = userRepository.findById(req.getUserId());
        if (existingUserOptional.isEmpty()) {
            throw new BadRequestException("User not found");
        }
        User existingUser = existingUserOptional.get();
        if (req.getIsDeleted() != null && !req.getIsDeleted() && existingUser.getIsDeleted()) {
            existingUser.setIsDeleted(false); // Khôi phục người dùng
        }
        // Update user details only if provided in the request
        if (req.getFullName() != null && !req.getFullName().isEmpty()) {
            existingUser.setFullName(req.getFullName());
        }

        if (req.getUserName() != null && !req.getUserName().isEmpty()) {
            Optional<User> userWithSameUserName = userRepository.findByUserNameAndIsDeletedFalse(req.getUserName());
            if (userWithSameUserName.isPresent() && userWithSameUserName.get().getUserId() != req.getUserId()) {
                throw new ConflictException("User name already exists");
            }
            existingUser.setUserName(req.getUserName());
        }

        if (req.getEmail() != null && !req.getEmail().isEmpty()) {
            User userWithEmail = userRepository.findByEmail(req.getEmail());
            if (userWithEmail != null && userWithEmail.getUserId() != req.getUserId()) {
                throw new ConflictException("Email already exists with another user");
            }
            existingUser.setEmail(req.getEmail());
        }

        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        if (req.getRole() != null) {
            if (!supportFunction.checkRole(req.getRole().toString())) {
                throw new BadRequestException("Invalid role");
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

        // Return updated user information as response
        return new CRUDAccountUserResponse(
                existingUser.getUserId(),
                existingUser.getUserName(),
                existingUser.getFullName(),
                existingUser.getAvatar(),
                existingUser.getBirthDate(),
                "",
                existingUser.getEmail(),
                existingUser.getPhoneNumber(),
                existingUser.getSex().toString(),
                existingUser.getType().toString(),
                existingUser.getIsDeleted(),
                existingUser.getDateDeleted(),
                existingUser.getDateUpdated(),
                existingUser.getDateCreated(),
                existingUser.getRole().toString()
        );
    }
    public String deleteOneReview(int reviewId)
    {
        Review review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId);
        if (review == null)
        {
            throw new BadRequestException("Review not found");
        }
        review.setIsDeleted(true);
        review.setDateDeleted(Date.valueOf(LocalDate.now()));
        reviewRepository.save(review);
        return "Review deleted";
    }

    public String deleteALlReviewProduct(int proId){
        List<Review> reviewList = reviewRepository.findByProduct_ProIdAndIsDeletedFalse(proId);
        if(reviewList != null)
        {
            for(Review review : reviewList){
                review.setIsDeleted(true);
                review.setDateDeleted(Date.valueOf(LocalDate.now()));
                reviewRepository.save(review);
            }
        }

        return "All review product deleted";
    }

    public ListProductImageResponse getAllProductImages(int proId) {
        List<ProductImageResponse> productImageResponses = new ArrayList<>();
        Product product = productRepository.findByProIdAndIsDeletedFalse(proId);
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
        //5: loc da xoa
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
//        } else if (req.getO() == 4) {
//            List<ProductVariants> productVariants = productRepository.findTopRatedProductsDesc(req.getC(), req.getP());
//            for (ProductVariants productVariant : productVariants) {
//                crudProductVarResponseList.add(new CRUDProductVarResponse(
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
//        } else if (req.getO() == 5) {
//            List<ProductVariants> productVariants = productRepository.findTopRatedProductsDesc(req.getC(), req.getP());
//            for (ProductVariants productVariant : productVariants) {
//                crudProductVarResponseList.add(new CRUDProductVarResponse(
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
        }
        return new FilterProductBoxResponse(
                true,
                total,
                crudProductVarResponseList,
                "OK",
                false
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

    public GetViewProductCategoryResponse getAllProductFromCategory(int id,String pageFromParam, String limitFromParam)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Category category = categoryRepository.findByCateId(id);
        if(category == null)
        {
            throw new BadRequestException("cateId not exists");
        }
        Page<Product> productList = productRepository.findByCategory_CateId(id,pageable);
        List<CRUDProductResponse> crudProductResponseList = new ArrayList<>();

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
                page,
                productList.getTotalPages(),
                limit,
                crudProductResponseList
        );

    }

}