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
//  List<Review> findByProduct_ProId(Integer productId);

  Page<Review> findByProduct_ProId(Integer productId, Pageable pageable);

  Review findByReviewId(Integer reviewId);
  Review findByReviewIdAndUser_UserId(Integer reviewId, Integer userId);
}
