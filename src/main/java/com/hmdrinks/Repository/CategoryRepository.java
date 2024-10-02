package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Integer> {
    Category findByCateId(Integer cateId);

    Category findByCateName(String cateName);

    Category findByCateNameAndCateIdNot(String cateName,Integer cateId);
    List<Category> findAll();
}
