package com.hmdrinks.Service;

import com.hmdrinks.Entity.Cart;
import com.hmdrinks.Entity.CartItem;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.CartItemRepository;
import com.hmdrinks.Repository.CartRepository;
import com.hmdrinks.Repository.ProductVariantsRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.CreateNewCart;
import com.hmdrinks.Response.CRUDCartItemResponse;
import com.hmdrinks.Response.CreateNewCartResponse;
import com.hmdrinks.Response.ListAllCartUserResponse;
import com.hmdrinks.Response.ListItemCartResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    public ResponseEntity<?> createCart(CreateNewCart req)
    {
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserId not found");
        }

        Cart cart = cartRepository.findByUserUserIdAndStatus(user.getUserId(), Status_Cart.NEW);
        if(cart != null)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cart already exists");
        }
        Cart cart1 = new Cart();
        cart1.setTotalPrice(0);
        cart1.setUser(user);
        cart1.setStatus(Status_Cart.NEW);
        cart1.setTotalProduct(0);
        cartRepository.save(cart1);
        return ResponseEntity.status(HttpStatus.OK).body( new CreateNewCartResponse(
                cart1.getCartId(),
                cart1.getTotalPrice(),
                cart1.getTotalProduct(),
                cart1.getUser().getUserId(),
                cart1.getStatus()
        ));
    }

    public ResponseEntity<?> getAllCartFromUser(int userId)
    {
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId);
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserId not found");
        }

        List<Cart> carts = cartRepository.findByUserUserId(userId);
        List<CreateNewCartResponse> cartResponses = new ArrayList<>();
        for(Cart cart : carts)
        {
            cartResponses.add(new CreateNewCartResponse(
                    cart.getCartId(),
                    cart.getTotalPrice(),
                    cart.getTotalProduct(),
                    cart.getUser().getUserId(),
                    cart.getStatus()
            ));
        }
        return ResponseEntity.status(HttpStatus.OK).body( new ListAllCartUserResponse(userId, carts.size(), cartResponses));
    }

    @Transactional
    public ResponseEntity<?> getAllItemCart(int id){
        Cart cart = cartRepository.findByCartId(id);
        if(cart == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found cart");
        }
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(id);
        List<CRUDCartItemResponse> crudCartItemResponses = new ArrayList<>();
        for(CartItem cartItem : cartItems)
        {
            crudCartItemResponses.add( new CRUDCartItemResponse(
                    cartItem.getCartItemId(),
                    cartItem.getProductVariants().getProduct().getProId(),
                    cartItem.getProductVariants().getProduct().getProName(),
                    cartItem.getCart().getCartId(),
                    cartItem.getProductVariants().getSize(),
                    cartItem.getTotalPrice(),
                    cartItem.getQuantity()
            ));

        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListItemCartResponse(
                id,
                cartItems.size(),
                crudCartItemResponses
        ));
    }
}
