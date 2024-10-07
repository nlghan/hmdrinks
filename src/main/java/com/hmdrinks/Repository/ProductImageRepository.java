package com.hmdrinks.Repository;

import com.hmdrinks.Entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    ProductImage findByProduct_ProId(int productId);
}
