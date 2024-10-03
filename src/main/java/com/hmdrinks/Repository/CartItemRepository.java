package com.hmdrinks.Repository;

import com.hmdrinks.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Integer> {
    List<CartItem> findByCart_CartId(Integer id);

    CartItem findByCartItemId(int id);

    CartItem findByProductVariant_VarIdAndCart_CartId(Integer varId,Integer cartId);
}
