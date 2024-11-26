package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import jakarta.transaction.Transactional;
import org.sparkproject.jetty.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public ResponseEntity<?> insertFavouriteItem(InsertItemToFavourite req)
    {
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND_404).body("User not found");
        }
        Product product= productRepository.findByProId(req.getProId());
        if(product == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND_404).body("Product not found");
        }
        if(product.getIsDeleted())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST_400).body("Product is deleted");
        }
        ProductVariants productVariants = productVariantsRepository.findBySizeAndProduct_ProIdAndIsDeletedFalse(req.getSize(),req.getProId());
        if(productVariants == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND_404).body("production size not exists");
        }
        Favourite favourite= favouriteRepository.findByUserUserId(req.getUserId());
        if(favourite == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND_404).body("Favourite for userId not exists");
        }
        Favourite favourite1 = favouriteRepository.findByFavId(req.getFavId());
        if(favourite1 == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND_404).body("Favourite not found");
        }
        FavouriteItem favouriteItem1 = favouriteItemRepository.findByProductVariants_VarIdAndProductVariants_SizeAndFavourite_FavId(req.getFavId(),req.getSize(),req.getFavId());
        FavouriteItem favouriteItem = new FavouriteItem();
        if(favouriteItem1 == null)
        {
            favouriteItem.setFavourite(favourite);
            favouriteItem.setProductVariants(productVariants);
            favouriteItemRepository.save(favouriteItem);
            favourite.setDateUpdated(LocalDateTime.now());
            favouriteRepository.save(favourite);
            return ResponseEntity.status(HttpStatus.OK_200).body(new CRUDFavouriteItemResponse (
                    favouriteItem.getFavItemId(),
                    favouriteItem.getFavourite().getFavId(),
                    favouriteItem.getProductVariants().getProduct().getProId(),
                    favouriteItem.getProductVariants().getSize()
            ));
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST_400).body("Favourite item already exists");
        }
    }

    public ResponseEntity<?> deleteOneItem(DeleteOneFavouriteItemReq req)
    {
        FavouriteItem favouriteItem = favouriteItemRepository.findByFavItemId(req.getFavItemId());
        if(favouriteItem == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND_404).body("Favourite item not found");
        }
        favouriteItemRepository.delete(favouriteItem);
        Favourite favourite = favouriteRepository.findByFavId(favouriteItem.getFavourite().getFavId());
        favourite.setDateUpdated(LocalDateTime.now());
        favouriteRepository.save(favourite);
        return ResponseEntity.status(HttpStatus.OK_200).body(new DeleteFavouriteItemResponse(
                "Delete item success"
        ));
    }

    public ResponseEntity<?> deleteAllFavouriteItem(DeleteAllFavouriteItemReq req)
    {
        Favourite favourite = favouriteRepository.findByFavId(req.getFavId());
        if(favourite == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND_404).body("Favourite not found");
        }
        List<FavouriteItem> favouriteItems = favouriteItemRepository.findByFavourite_FavId(req.getFavId());
        favouriteItemRepository.deleteAll(favouriteItems);
        favourite.setDateUpdated(LocalDateTime.now());
        favouriteRepository.save(favourite);
        return ResponseEntity.status(HttpStatus.OK_200).body(new DeleteFavouriteItemResponse(
                "Delete all item success"
        ));
    }

    @Transactional
    public ResponseEntity<?> listAllTotalFavouriteByProId() {
        Map<Integer, Integer> favouriteCountMap = new HashMap<>();
        List<FavouriteItem> favouriteItems = favouriteItemRepository.findAll();

        for (FavouriteItem favouriteItem : favouriteItems) {
            int proId = favouriteItem.getProductVariants().getProduct().getProId();
            favouriteCountMap.put(proId, favouriteCountMap.getOrDefault(proId, 0) + 1);
        }

        List<TotalCountFavorite> totalCountFavorites = favouriteCountMap.entrySet().stream()
                .map(entry -> {
                    int proId = entry.getKey();
                    int totalCount = entry.getValue();
                    Product product = productRepository.findByProId(proId);
                    String productName = (product != null) ? product.getProName() : "Unknown";

                    return new TotalCountFavorite(proId, productName, totalCount);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ListAllTotalCountFavorite(totalCountFavorites.size(), totalCountFavorites));
    }


}