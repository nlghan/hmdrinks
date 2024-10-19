package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.*;
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
public class ReviewService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    public CRUDReviewResponse createReview(CreateNewReview req)
    {
        Product product = productRepository.findByProId(req.getProId());
        if(product == null)
        {
            throw new BadRequestException("Product not found");
        }
        User user = userRepository.findByUserId(req.getUserId());
        if(user == null){
            throw new BadRequestException("User not found");
        }
        Review review = new Review();
        review.setContent(req.getContent());
        review.setRatingStar(req.getRatingStart());
        review.setUser(user);
        review.setDateCreated(LocalDate.now());
        review.setProduct(product);
        review.setIsDeleted(false);
        reviewRepository.save(review);

        return new CRUDReviewResponse (
                review.getReviewId(),
                review.getUser().getUserId(),
                review.getProduct().getProId(),
                review.getUser().getFullName(),
                review.getContent(),
                review.getRatingStar(),
                review.getIsDeleted(),
                review.getDateDeleted(),
                review.getDateUpdated(),
                review.getDateCreated()
        );
    }

    public CRUDReviewResponse updateReview(CRUDReviewReq req)
    {
        Review review = reviewRepository.findByReviewIdAndUser_UserId(req.getReviewId(), req.getUserId());
        if (review == null)
        {
            throw new BadRequestException("Review not found");
        }
        review.setContent(req.getContent());
        review.setRatingStar(req.getRatingStart());
        review.setDateUpdated(LocalDate.now());
        reviewRepository.save(review);
        return new CRUDReviewResponse (
                review.getReviewId(),
                review.getUser().getUserId(),
                review.getProduct().getProId(),
                review.getUser().getFullName(),
                review.getContent(),
                review.getRatingStar(),
                review.getIsDeleted(),
                review.getDateDeleted(),
                review.getDateUpdated(),
                review.getDateCreated()
        );
    }

    public String deleteOneReview(DeleteReviewReq req)
    {
        Review review = reviewRepository.findByReviewIdAndUser_UserId(req.getReviewId(), req.getUserId());
        if (review == null)
        {
            throw new BadRequestException("Review not found");
        }

        reviewRepository.delete(review);
        return "Review deleted";
    }

//    public String deleteAllReviewUserFromProduct(DeleteReviewProductReq req)
//    {
//        List<Review> reviews = reviewRepository.findByProduct_ProId(req.getProId());
//        for(Review review : reviews){
//            reviewRepository.delete(review);
//        }
//        return "Review deleted";
//    }

    public ListAllReviewProductResponse getAllReview(String pageFromParam, String limitFromParam,int proId)
    {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Review> reviews = reviewRepository.findByProduct_ProId(proId,pageable);
        List<CRUDReviewResponse> crudReviewResponseList = new ArrayList<>();
        for(Review review : reviews)
        {
            crudReviewResponseList.add(new CRUDReviewResponse (
                    review.getReviewId(),
                    review.getUser().getUserId(),
                    review.getProduct().getProId(),
                    review.getUser().getFullName(),
                    review.getContent(),
                    review.getRatingStar(),
                    review.getIsDeleted(),
                    review.getDateDeleted(),
                    review.getDateUpdated(),
                    review.getDateCreated()
            ));
        }

        return new ListAllReviewProductResponse(page,
                reviews.getTotalPages(),
                limit,
                crudReviewResponseList);
    }
}