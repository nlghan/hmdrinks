package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Product;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Enum.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantsRepository extends JpaRepository<ProductVariants,Integer> {
    ProductVariants findByVarId(Integer varId);
    ProductVariants findBySizeAndProduct_ProId(Size size, Integer productId);

    ProductVariants findBySizeAndProduct_ProIdAndVarIdNot(Size size, Integer productId, Integer variantId);

    List<ProductVariants> findByProduct_ProId(Integer proId);

    List<ProductVariants> findAll();

    Page<ProductVariants> findAll(Pageable pageable);
}
