package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Category;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Entity.ProductVariants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Integer> {

    Product findByProId(Integer proId);

    Product findByProIdAndIsDeletedFalse(Integer proId);

    Product findByProName(String proName);

    Product findByProNameAndIsDeletedFalse(String proName);

    Product findByProNameAndProIdNot(String proName,Integer proId);
    Page<Product> findByIsDeletedFalse(Pageable pageable);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByProNameContaining(String proName, Pageable pageable);

    Page<Product> findByProNameContainingAndIsDeletedFalse(String proName, Pageable pageable);
    Page<Product> findByCategory_CateId(int cateId, Pageable pageable);

    List<Product> findByCategory_CateIdAndIsDeletedFalse(int cateId);

    @Query(value = "SELECT COUNT(pro_id) FROM product", nativeQuery = true)
    int TotalNumberOfProduct();

//    @Query("SELECT pv FROM Product pv " +
//            "LEFT JOIN pv.reviews r " +
//            "WHERE pv.category.cateId = :categoryId " +
//            "AND pv.proId IN :productIds " +
//            "GROUP BY pv.varId " +
//            "HAVING COUNT(r) > 0 " + // Có ít nhất 1 đánh giá
//            "ORDER BY AVG(r.ratingStar) DESC")
//    List<Product> findTopRatedProductsDesc(
//            @Param("categoryId") int categoryId,
//            @Param("productIds") List<Integer> productIds
//    );
//
//    @Query("SELECT pv FROM Product pv " +
//            "LEFT JOIN pv.reviews r " +
//            "WHERE pv.category.cateId = :categoryId " +
//            "AND pv.proId IN :productIds " +
//            "GROUP BY pv.proId " +
//            "HAVING COUNT(r) > 0 " +
//            "ORDER BY AVG(r.ratingStar) ASC")
//    List<ProductVariants> findTopRatedProductsAsc(
//            @Param("categoryId") int categoryId,
//            @Param("productIds") List<Integer> productIds
//    );
}
