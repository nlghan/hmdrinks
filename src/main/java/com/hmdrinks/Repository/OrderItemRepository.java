package com.hmdrinks.Repository;

import com.hmdrinks.Entity.OrderItem;
import com.hmdrinks.Entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
       OrderItem findByUserUserIdAndCartCartId(Integer userId, Integer cartId);
}
