package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Cart;
import com.hmdrinks.Entity.Favourite;
import com.hmdrinks.Entity.FavouriteItem;
import com.hmdrinks.Enum.Status_Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavouriteRepository extends JpaRepository<Favourite,Integer> {

    Favourite findByUserUserId(int userId);

    Favourite findByFavId(int cartId);

}

