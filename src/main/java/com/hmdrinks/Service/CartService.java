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
import com.hmdrinks.Response.ListItemCartResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ProductVariantsRepository productVariantsRepository;

    public CreateNewCartResponse createCart(CreateNewCart req)
    {
        User user = userRepository.findByUserId(req.getUserId());
        if(user == null)
        {
            throw new BadRequestException("UserId not exists");
        }

        Cart cart = cartRepository.findByUserUserIdAndStatus(user.getUserId(), Status_Cart.NEW);
        if(cart != null)
        {
            throw  new BadRequestException("NewCart exists");
        }


        Cart cart1 = new Cart();
        cart1.setTotalPrice(0);
        cart1.setUser(user);
        cart1.setStatus(Status_Cart.NEW);
        cart1.setTotalProduct(0);

        cartRepository.save(cart1);

        return new CreateNewCartResponse(
                cart1.getCartId(),
                cart1.getTotalPrice(),
                cart1.getTotalProduct(),
                cart1.getUser().getUserId(),
                cart1.getStatus()

        );

    }

    public ListItemCartResponse getAllItemCart(int id){
        Cart cart = cartRepository.findByCartId(id);
        if(cart == null)
        {
            throw  new BadRequestException("Cart not exists");
        }
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(id);
        List<CRUDCartItemResponse> crudCartItemResponses = new ArrayList<>();
        for(CartItem cartItem : cartItems)
        {

            crudCartItemResponses.add( new CRUDCartItemResponse(
                cartItem.getCartItemId(),
                cartItem.getProductVariants().getProduct().getProId(),
                cartItem.getCart().getCartId(),
                cartItem.getProductVariants().getSize(),
                cartItem.getTotalPrice(),
                cartItem.getQuantity()
        ));
        }
        return new ListItemCartResponse(
               id,
                crudCartItemResponses
        );
    }
}
