package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Orders;
import com.hmdrinks.Entity.Post;
import com.hmdrinks.Enum.Status_Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OrderRepository extends JpaRepository<Orders, Integer> {
  Orders findByOrderId(int id);

  Orders findByOrderIdAndUserUserIdAndIsDeletedFalse(int orderId, int id);

  Orders findByOrderIdAndIsDeletedFalse(int id);

  Orders findByOrderIdAndStatus(int orderId, Status_Order status);

  Page<Orders> findAllByUserUserIdAndIsDeletedFalse(int userId,Pageable pageable);

  Page<Orders> findAllByUserUserIdAndStatusAndIsDeletedFalse(int userId,Status_Order statusOrder,Pageable pageable);

}
