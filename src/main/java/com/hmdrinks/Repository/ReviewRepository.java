package com.hmdrinks.Repository;

import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Entity.Review;
import com.hmdrinks.Enum.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Integer> {
  List<Review> findByProduct_ProIdAndIsDeletedFalse(Integer productId);
  List<Review> findByUser_UserId(Integer userId);
  Page<Review> findByProduct_ProId(Integer productId, Pageable pageable);
  List<Review> findAll();
  Review findByReviewIdAndIsDeletedFalse(Integer reviewId);
  Review findByReviewIdAndUser_UserIdAndIsDeletedFalse(Integer reviewId, Integer userId);
}
