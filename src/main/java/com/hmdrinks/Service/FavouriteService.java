package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.CreateNewCart;
import com.hmdrinks.Request.CreateNewFavourite;
import com.hmdrinks.Response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FavouriteService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FavouriteRepository favouriteRepository;
    @Autowired
    private FavouriteItemRepository favouriteItemRepository;

    public CreateNewFavouriteResponse createFavourite(CreateNewFavourite req)
    {
        User user = userRepository.findByUserId(req.getUserId());
        if(user == null)
        {
            throw new BadRequestException("UserId not exists");
        }
        Favourite favourite = favouriteRepository.findByUserUserId(user.getUserId());

        if(favourite != null)
        {
            throw  new BadRequestException("Favourite exists");
        }
        Favourite favourite1 = new Favourite();
        favourite1.setUser(user);
        favourite1.setIsDeleted(false);
        favourite1.setDateCreated(LocalDate.now());
        favouriteRepository.save(favourite1);

        return new CreateNewFavouriteResponse(
                favourite1.getFavId(),
                favourite1.getUser().getUserId(),
                favourite1.getIsDeleted(),
                favourite1.getDateDeleted(),
                favourite1.getDateUpdated(),
                favourite1.getDateCreated()
        );
    }

    public ListItemFavouriteResponse getAllItemFavourite(int id){

        Favourite favourite = favouriteRepository.findByFavId(id);
        if(favourite == null)
        {
            throw  new BadRequestException("Favourite not exists");
        }
        List<FavouriteItem> favouriteItems = favouriteItemRepository.findByFavourite_FavId(id);;
        List<CRUDFavouriteItemResponse> crudFavouriteItemResponses = new ArrayList<>();
        for(FavouriteItem favouriteItem : favouriteItems)
        {
            crudFavouriteItemResponses.add(new CRUDFavouriteItemResponse(
                    favouriteItem.getFavItemId(),
                    favouriteItem.getFavourite().getFavId(),
                    favouriteItem.getProductVariants().getProduct().getProId(),
                    favouriteItem.getProductVariants().getSize()
            ));
        }
        return new ListItemFavouriteResponse(
                id,
                crudFavouriteItemResponses
        );
    }
}