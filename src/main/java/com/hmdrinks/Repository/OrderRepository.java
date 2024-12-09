package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Orders;
import com.hmdrinks.Entity.Post;
import com.hmdrinks.Enum.Status_Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OrderRepository extends JpaRepository<Orders, Integer> {
  Orders findByOrderId(int id);

  Orders findByOrderIdAndUserUserIdAndIsDeletedFalse(int orderId, int id);

  Orders findByOrderIdAndIsDeletedFalse(int id);

  List<Orders> findAllByUserUserId(int userId);
  List<Orders> findAllByUserUserId(int userId, Sort sort);
  Page<Orders> findAllByUserUserId(int userId,Pageable pageable);

  List<Orders> findAllByStatus(Status_Order status);
  List<Orders> findAllByIsDeletedFalse();

  List<Orders> findAllByUserUserIdAndStatus(int userId,Status_Order statusOrder);

  Orders findByOrderIdAndStatus(int orderId, Status_Order status);
  Orders findByOrderIdAndStatusAndIsDeletedFalse(int orderId, Status_Order status);


  Page<Orders> findAllByUserUserIdAndIsDeletedFalse(int userId,Pageable pageable);

  Page<Orders> findAllByUserUserIdAndStatusAndIsDeletedFalse(int userId,Status_Order statusOrder,Pageable pageable);

}
