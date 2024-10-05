package com.hmdrinks.Repository;

import com.hmdrinks.Entity.CartItem;
import com.hmdrinks.Enum.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Integer> {
    List<CartItem> findByCart_CartId(Integer id);

    CartItem findByCartItemId(int id);

    CartItem findByProductVariants_VarIdAndProductVariants_SizeAndCart_CartId(Integer varId, Size size, Integer cartId);
}
