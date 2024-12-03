package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Category;
import com.hmdrinks.Entity.Product;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Enum.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Integer> {
    Product findByProId(Integer proId);
    Product findByProIdAndIsDeletedFalse(Integer proId);
    Product findByProNameAndIsDeletedFalse(String proName);
    Product findByProNameAndProIdNot(String proName,Integer proId);
    Page<Product> findByIsDeletedFalse(Pageable pageable);
    List<Product> findByIsDeletedFalse();
    Page<Product> findAll(Pageable pageable);
    List<Product> findAll();
    List<Product> findAllByIsDeletedFalse();

    Page<Product> findByProNameContaining(String proName, Pageable pageable);

    Page<Product> findByProNameContainingAndIsDeletedFalse(String proName, Pageable pageable);
    Page<Product> findByCategory_CateId(int cateId, Pageable pageable);
    List<Product> findByCategory_CateId(int cateId);

    Page<Product> findByCategory_CateIdAndIsDeletedFalse(int cateId,Pageable pageable);
    List<Product> findByCategory_CateIdAndIsDeletedFalse(int cateId);


    @Query("SELECT pv FROM Product pv " +
            "LEFT JOIN pv.reviews r " +
            "WHERE pv.category.cateId = :categoryId " +
            "AND pv.proId IN :productIds " +
            "AND pv.isDeleted = false " +
            "AND r.isDeleted = false " +
            "GROUP BY pv.proId " +
            "HAVING COUNT(r) > 0 " +
            "ORDER BY AVG(r.ratingStar) DESC")
    Page<Product> findTopRatedProductsDesc(
            @Param("categoryId") int categoryId,
            @Param("productIds") List<Integer> productIds,
            Pageable pageable
    );

    @Query("SELECT pv FROM Product pv " +
            "LEFT JOIN pv.reviews r " +
            "WHERE pv.category.cateId = :categoryId " +
            "AND pv.isDeleted = false " +
            "AND r.isDeleted = false " +
            "GROUP BY pv.proId " +
            "HAVING COUNT(r) > 0 " +
            "ORDER BY AVG(r.ratingStar) DESC")
    Page<Product> findTopRatedProductsDescByCategory(
            @Param("categoryId") int categoryId,
            Pageable pageable
    );


    @Query("SELECT pv FROM Product pv " +
            "LEFT JOIN pv.reviews r " +
            "WHERE pv.isDeleted = false " +
            "AND (r.isDeleted = false OR r IS NULL) " +
            "GROUP BY pv.proId " +
            "ORDER BY AVG(CASE WHEN r.ratingStar IS NOT NULL THEN r.ratingStar ELSE 0 END) DESC")
    Page<Product> findTopRatedProductsDesc(Pageable pageable);





    @Query("SELECT AVG(r.ratingStar) " +
            "FROM Product pv " +
            "LEFT JOIN pv.reviews r " +
            "WHERE pv.category.cateId = :categoryId " +
            "AND pv.proId = :productId " +
            "AND pv.isDeleted = false " +
            "AND r.isDeleted = false " +
            "GROUP BY pv.proId " +
            "HAVING COUNT(r) > 0") // Có ít nhất 1 đánh giá
    Double findAverageRatingByProductId(
            @Param("categoryId") int categoryId,
            @Param("productId") int productId
    );


    @Query("SELECT pv FROM Product pv " +
            "LEFT JOIN pv.reviews r " +
            "WHERE pv.category.cateId = :categoryId " +
            "AND pv.proId IN :productIds " +
            "AND pv.isDeleted = false " +
            "AND r.isDeleted = false " +
            "GROUP BY pv.proId " +
            "HAVING COUNT(r) > 0 " +
            "ORDER BY AVG(r.ratingStar) ASC")
    Page<Product> findTopRatedProductsAsc(
            @Param("categoryId") int categoryId,
            @Param("productIds") List<Integer> productIds,
            Pageable pageable
    );

    @Query("SELECT pv FROM Product pv " +
            "LEFT JOIN pv.reviews r " +
            "WHERE pv.category.cateId = :categoryId " +
            "AND pv.isDeleted = false " +
            "AND r.isDeleted = false " +
            "GROUP BY pv.proId " +
            "HAVING COUNT(r) > 0 " +
            "ORDER BY AVG(r.ratingStar) ASC")
    Page<Product> findTopRatedProductsAscByCategory(
            @Param("categoryId") int categoryId,
            Pageable pageable
    );

    @Query("SELECT pv FROM Product pv " +
            "LEFT JOIN pv.reviews r " +
            "WHERE pv.isDeleted = false " +
            "AND (r.isDeleted = false OR r IS NULL) " +
            "GROUP BY pv.proId " +
            "ORDER BY AVG(CASE WHEN r.ratingStar IS NOT NULL THEN r.ratingStar ELSE 0 END) ASC")
    Page<Product> findTopRatedProductsAsc(Pageable pageable);

    @Query(value = "SELECT p.*, " +
            "(SELECT MIN(v.price) " +
            " FROM product_variants v " +
            " WHERE v.pro_id = p.pro_id " +
            "   AND v.is_deleted = false) AS minPrice " +
            "FROM product p " +
            "WHERE p.category_id = :categoryId " +
            "  AND p.pro_id IN :productIds " +
            "  AND p.is_deleted = false " +
            "ORDER BY minPrice ASC",
            countQuery = "SELECT COUNT(*) " +
                    "FROM product p " +
                    "WHERE p.category_id = :categoryId " +
                    "  AND p.pro_id IN :productIds " +
                    "  AND p.is_deleted = false",
            nativeQuery = true)
    Page<Product> findProductsWithMinPrice(@Param("categoryId") int categoryId,
                                           @Param("productIds") List<Integer> productIds,
                                           Pageable pageable);

    @Query(value = "SELECT p.*, " +
            "(SELECT MIN(v.price) " +
            " FROM product_variants v " +
            " WHERE v.pro_id = p.pro_id " +
            "   AND v.is_deleted = false) AS minPrice " +
            "FROM product p " +
            "WHERE p.category_id = :categoryId " +
            "  AND p.is_deleted = false " +
            "ORDER BY minPrice ASC",
            countQuery = "SELECT COUNT(*) " +
                    "FROM product p " +
                    "WHERE p.category_id = :categoryId " +
                    "  AND p.is_deleted = false",
            nativeQuery = true)
    Page<Product> findProductsWithMinPriceNoProduct(@Param("categoryId") int categoryId, Pageable pageable);


    @Query(value = "SELECT p.*, " +
            "(SELECT MIN(v.price) " +
            " FROM product_variants v " +
            " WHERE v.pro_id = p.pro_id " +
            "   AND v.is_deleted = false) AS minPrice " +
            "FROM product p " +
            "WHERE p.is_deleted = false " +
            "ORDER BY minPrice ASC",
            countQuery = "SELECT COUNT(*) FROM product p WHERE p.is_deleted = false",
            nativeQuery = true)
    Page<Product> findAllProductsWithMinPriceNoCategory(Pageable pageable);


    @Query(value = "SELECT p.*, " +
            "(SELECT MAX(v.price) " +
            " FROM product_variants v " +
            " WHERE v.pro_id = p.pro_id " +
            "   AND v.is_deleted = false) AS maxPrice " +
            "FROM product p " +
            "WHERE p.category_id = :categoryId " +
            "  AND p.is_deleted = false " +
            "ORDER BY maxPrice DESC",
            countQuery = "SELECT COUNT(*) " +
                    "FROM product p " +
                    "WHERE p.category_id = :categoryId " +
                    "  AND p.is_deleted = false",
            nativeQuery = true)
    Page<Product> findProductsWithMaxPriceNoProduct(@Param("categoryId") int categoryId, Pageable pageable);

    @Query(value = "SELECT p.*, " +
            "(SELECT MAX(v.price) " +
            " FROM product_variants v " +
            " WHERE v.pro_id = p.pro_id " +
            "   AND v.is_deleted = false) AS maxPrice " +
            "FROM product p " +
            "WHERE p.is_deleted = false " +
            "ORDER BY maxPrice DESC",
            countQuery = "SELECT COUNT(*) " +
                    "FROM product p " +
                    "WHERE p.is_deleted = false",
            nativeQuery = true)
    Page<Product> findAllProductsWithMaxPriceNoCategory(Pageable pageable);

    @Query(value = "SELECT p.*, " +
            "(SELECT MAX(v.price) " +
            " FROM product_variants v " +
            " WHERE v.pro_id = p.pro_id " +
            "   AND v.is_deleted = false) AS maxPrice " +
            "FROM product p " +
            "WHERE p.category_id = :categoryId " +
            "  AND p.pro_id IN :productIds " +
            "  AND p.is_deleted = false " +
            "ORDER BY maxPrice DESC",
            countQuery = "SELECT COUNT(*) " +
                    "FROM product p " +
                    "WHERE p.category_id = :categoryId " +
                    "  AND p.pro_id IN :productIds " +
                    "  AND p.is_deleted = false",
            nativeQuery = true)
    Page<Product> findProductsWithMaxPrice(@Param("categoryId") int categoryId,
                                           @Param("productIds") List<Integer> productIds,
                                           Pageable pageable);



    Page<Product> findByCategory_CateIdAndProIdInAndIsDeletedFalse(
            int categoryId,
            List<Integer> productIds,
            Pageable pageable
    );



}