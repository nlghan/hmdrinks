package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Cart;
import com.hmdrinks.Enum.Status_Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart,Integer> {
    Cart findByUserUserIdAndStatus(int userId, Status_Cart status);

    List<Cart> findByUserUserId(int userId);




    Cart findByCartId(int cartId);

    Cart findByCartIdAndStatus(int cartId,Status_Cart statusCart);
}

