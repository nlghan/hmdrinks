package com.hmdrinks.Repository;

import com.hmdrinks.Entity.CartItem;
import com.hmdrinks.Entity.Favourite;
import com.hmdrinks.Entity.FavouriteItem;
import com.hmdrinks.Enum.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavouriteItemRepository extends JpaRepository<FavouriteItem,Integer> {
    List<FavouriteItem> findByFavourite_FavId(Integer id);
    List<FavouriteItem> findByFavourite_FavIdAndIsDeletedFalse(Integer id);
    List<FavouriteItem> findByProductVariants_VarId(Integer id);

    FavouriteItem findByFavItemId(int id);

    FavouriteItem findByProductVariants_VarIdAndProductVariants_SizeAndFavourite_FavId(Integer varId, Size size, Integer favId);
}
