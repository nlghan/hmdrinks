package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FavouriteItemService {
    @Autowired
    private FavouriteRepository favouriteRepository;
    @Autowired
    private ProductVariantsRepository productVariantsRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private FavouriteItemRepository favouriteItemRepository;
    @Autowired
    private UserRepository userRepository;

    public CRUDFavouriteItemResponse insertFavouriteItem(InsertItemToFavourite req)
    {
        User user = userRepository.findByUserId(req.getUserId());
        if(user == null)
        {
            throw  new BadRequestException("Not found user");
        }
        Product product= productRepository.findByProId(req.getProId());
        if(product == null)
        {
            throw new BadRequestException("proId not exists");
        }
        ProductVariants productVariants = productVariantsRepository.findBySizeAndProduct_ProId(req.getSize(),req.getProId());
        if(productVariants == null)
        {
            throw new BadRequestException("production size not exists");
        }
        Favourite favourite= favouriteRepository.findByUserUserId(req.getUserId());
        if(favourite == null)
        {
            throw  new BadRequestException("Favourite for userId not exists");
        }
        Favourite favourite1 = favouriteRepository.findByFavId(req.getFavId());
        if(favourite1 == null)
        {
            throw  new BadRequestException("Not found favourite");
        }
        FavouriteItem favouriteItem1 = favouriteItemRepository.findByProductVariants_VarIdAndProductVariants_SizeAndFavourite_FavId(req.getFavId(),req.getSize(),req.getFavId());
        FavouriteItem favouriteItem = new FavouriteItem();
        if(favouriteItem1 == null)
        {
            favouriteItem.setFavourite(favourite);
            favouriteItem.setProductVariants(productVariants);
            favouriteItemRepository.save(favouriteItem);
            favourite.setDateUpdated(LocalDate.now());
            favouriteRepository.save(favourite);
            return new CRUDFavouriteItemResponse (
                    favouriteItem.getFavItemId(),
                    favouriteItem.getFavourite().getFavId(),
                    favouriteItem.getProductVariants().getProduct().getProId(),
                    favouriteItem.getProductVariants().getSize()
            );
        }
        else {
            throw  new BadRequestException("Favourite item already exists");
        }
    }

    public DeleteFavouriteItemResponse deleteOneItem(DeleteOneFavouriteItemReq req)
    {
        FavouriteItem favouriteItem = favouriteItemRepository.findByFavItemId(req.getFavItemId());
        if(favouriteItem == null)
        {
            throw  new BadRequestException("Not found FavouriteItem");
        }
        favouriteItemRepository.delete(favouriteItem);
        Favourite favourite = favouriteRepository.findByFavId(favouriteItem.getFavourite().getFavId());
        favourite.setDateUpdated(LocalDate.now());
        favouriteRepository.save(favourite);
        return new DeleteFavouriteItemResponse(
                "Delete item success"
        );
    }

    public DeleteFavouriteItemResponse deleteAllFavouriteItem(DeleteAllFavouriteItemReq req)
    {
        Favourite favourite = favouriteRepository.findByFavId(req.getFavId());
        if(favourite == null)
        {
            throw  new BadRequestException("Not found favourite");
        }
        List<FavouriteItem> favouriteItems = favouriteItemRepository.findByFavourite_FavId(req.getFavId());
        for(FavouriteItem favouriteItem: favouriteItems)
        {
            favouriteItemRepository.delete(favouriteItem);
        }
        favourite.setDateUpdated(LocalDate.now());
        favouriteRepository.save(favourite);
        return new DeleteFavouriteItemResponse(
                "Delete all item success"
        );
    }
}