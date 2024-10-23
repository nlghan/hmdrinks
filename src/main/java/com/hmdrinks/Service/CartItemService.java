package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.DeleteAllCartItemReq;
import com.hmdrinks.Request.DeleteOneCartItemReq;
import com.hmdrinks.Request.IncreaseDecreaseItemQuantityReq;
import com.hmdrinks.Request.InsertItemToCart;
import com.hmdrinks.Response.CRUDCartItemResponse;
import com.hmdrinks.Response.DeleteCartItemResponse;
import com.hmdrinks.Response.IncreaseDecreaseItemQuantityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductVariantsRepository productVariantsRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;

    public CRUDCartItemResponse insertCartItem(InsertItemToCart req)
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
        Cart cart1 = cartRepository.findByUserUserIdAndStatus(user.getUserId(), Status_Cart.NEW);
        if(cart1 == null)
        {
            throw  new BadRequestException("Cart for userId not exists");
        }
        Cart cart = cartRepository.findByCartId(req.getCartId());
        if(cart == null)
        {
            throw  new BadRequestException("Not found cart");
        }
        if(req.getQuantity() < 0){
            throw  new BadRequestException("quantity must larger 0");
        }
        CartItem cartItem1 = cartItemRepository.findByProductVariants_VarIdAndProductVariants_SizeAndCart_CartId(productVariants.getVarId(),req.getSize(),req.getCartId());
        CartItem cartItem = new CartItem();
        if(cartItem1 == null)
        {
            if(req.getQuantity() > productVariants.getStock())
            {
                throw  new BadRequestException("hết hàng rồi hmm");
            }
            Double totalPrice = req.getQuantity() * productVariants.getPrice();
            Integer stock_quantity = productVariants.getStock() - req.getQuantity();
            cartItem.setCart(cart);
            cartItem.setQuantity(req.getQuantity());
            cartItem.setProductVariants(productVariants);
            cartItem.setTotalPrice(totalPrice);
            cartItemRepository.save(cartItem);
            List<CartItem> cartItemList = cartItemRepository.findByCart_CartId(req.getCartId());
            Double Price = 0.0;
            Integer Quantity=0;
            for(CartItem cartItem2: cartItemList)
            {
                Price = Price + Double.valueOf(cartItem2.getTotalPrice());
                Quantity = Quantity + cartItem2.getQuantity();
            }
            cart.setTotalProduct(Quantity);
            cart.setTotalPrice(Price);
            cartRepository.save(cart);
            return new CRUDCartItemResponse(
                    cartItem.getCartItemId(),
                    cartItem.getProductVariants().getProduct().getProId(),
                    cartItem.getCart().getCartId(),
                    cartItem.getProductVariants().getSize(),
                    cartItem.getTotalPrice(),
                    cartItem.getQuantity()
            );
        }
        else
        {
            if((req.getQuantity()+cartItem1.getQuantity()) > productVariants.getStock())
            {
                throw  new BadRequestException("hết hàng rồi hmm");
            }
            Integer stock_quantity = productVariants.getStock() - req.getQuantity();
            Double totalPrice = (req.getQuantity()+cartItem1.getQuantity()) * productVariants.getPrice();
            cartItem1.setTotalPrice(totalPrice);
            cartItem1.setQuantity((req.getQuantity()+cartItem1.getQuantity()));
            cartItemRepository.save(cartItem1);
            List<CartItem> cartItemList = cartItemRepository.findByCart_CartId(req.getCartId());
            Double Price = 0.0;
            Integer Quantity=0;
            for(CartItem cartItem2: cartItemList)
            {
                Price = Price + Double.valueOf(cartItem2.getTotalPrice());
                Quantity = Quantity + cartItem2.getQuantity();
            }
            cart.setTotalProduct(Quantity);
            cart.setTotalPrice(Price);
            cartRepository.save(cart);
            return new CRUDCartItemResponse(
                    cartItem1.getCartItemId(),
                    cartItem1.getProductVariants().getProduct().getProId(),
                    cartItem1.getCart().getCartId(),
                    cartItem1.getProductVariants().getSize(),
                    cartItem1.getTotalPrice(),
                    cartItem1.getQuantity()
            );
        }
    }

    public IncreaseDecreaseItemQuantityResponse increaseCartItemQuantity(IncreaseDecreaseItemQuantityReq req)
    {
        CartItem cartItem = cartItemRepository.findByCartItemId(req.getCartItemId());
        if(cartItem == null)
        {
            throw  new BadRequestException("Not found cartItem");
        }
        if(req.getQuantity() < 0){
            throw  new BadRequestException("quantity must larger 0");
        }
        ProductVariants productVariants = productVariantsRepository.findByVarId(cartItem.getProductVariants().getProduct().getProId());
        Integer Present_Quantity = cartItem.getQuantity() + 1 ;

        if((req.getQuantity() + 1 ) > productVariants.getStock())
        {
            throw  new BadRequestException("hết hàng rồi lì như trâu ");
        }
        cartItem.setQuantity((Present_Quantity));
        cartItemRepository.save(cartItem);
        Cart cart = cartRepository.findByCartId(cartItem.getCart().getCartId());
        if(cart == null)
        {
            throw  new BadRequestException("Not found cart");
        }
        List<CartItem> cartItemList = cartItemRepository.findByCart_CartId(cartItem.getCart().getCartId());
        Double Price = 0.0;
        Integer Quantity=0;
        for(CartItem cartItem2: cartItemList)
        {
            Price = Price + Double.valueOf(cartItem2.getTotalPrice());
            Quantity = Quantity + cartItem2.getQuantity();
        }
        cart.setTotalProduct(Quantity);
        cart.setTotalPrice(Price);
        cartRepository.save(cart);
        return new IncreaseDecreaseItemQuantityResponse(
                Present_Quantity
        );
    }

    public IncreaseDecreaseItemQuantityResponse  decreaseCartItemQuantity(IncreaseDecreaseItemQuantityReq req)
    {
        CartItem cartItem = cartItemRepository.findByCartItemId(req.getCartItemId());
        if(cartItem == null)
        {
            throw  new BadRequestException("Not found cartItem");
        }
        if(req.getQuantity() <= 0)
        {
            throw  new BadRequestException("quantity must larger 0");
        }
        ProductVariants productVariants = productVariantsRepository.findByVarId(cartItem.getProductVariants().getVarId());
        int Present_Quantity = cartItem.getQuantity() - 1 ;
        System.out.println(productVariants.getStock());

        if((req.getQuantity() - 1 ) > productVariants.getStock())
        {
            throw  new BadRequestException("hết hàng rồi hmm");
        }
        cartItem.setQuantity((Present_Quantity));
        cartItemRepository.save(cartItem);
        Cart cart = cartRepository.findByCartId(cartItem.getCart().getCartId());
        if(cart == null)
        {
            throw  new BadRequestException("Not found cart");
        }
        List<CartItem> cartItemList = cartItemRepository.findByCart_CartId(cartItem.getCart().getCartId());
        Double Price = 0.0;
        Integer Quantity=0;
        for(CartItem cartItem2: cartItemList)
        {
            Price = Price + Double.valueOf(cartItem2.getTotalPrice());
            Quantity = Quantity + cartItem2.getQuantity();
        }
        cart.setTotalProduct(Quantity);
        cart.setTotalPrice(Price);
        cartRepository.save(cart);
        return new IncreaseDecreaseItemQuantityResponse(
                Present_Quantity
        );
    }

    public DeleteCartItemResponse deleteOneItem(DeleteOneCartItemReq req)
    {
        CartItem cartItem = cartItemRepository.findByCartItemId(req.getCartItemId());
        if(cartItem == null)
        {
            throw  new BadRequestException("Not found cartItem");
        }
        cartItemRepository.delete(cartItem);
        List<CartItem> cartItemList = cartItemRepository.findByCart_CartId(cartItem.getCart().getCartId());
        Double Price = 0.0;
        Integer Quantity=0;
        for(CartItem cartItem2: cartItemList)
        {
            Price = Price + Double.valueOf(cartItem2.getTotalPrice());
            Quantity = Quantity + cartItem2.getQuantity();
        }
        Cart cart = cartRepository.findByCartId(cartItem.getCart().getCartId());
        cart.setTotalProduct(Quantity);
        cart.setTotalPrice(Price);
        cartRepository.save(cart);
        return new DeleteCartItemResponse(
                "Delete item success"
        );
    }

    public DeleteCartItemResponse deleteAllCartItem(DeleteAllCartItemReq req)
    {
        Cart cart = cartRepository.findByCartIdAndStatus(req.getCartId(),Status_Cart.NEW);
        if(cart == null)
        {
            throw  new BadRequestException("Not found cart");
        }
        List<CartItem> cartItemList = cartItemRepository.findByCart_CartId(req.getCartId());
        for(CartItem cartItem2: cartItemList)
        {
            cartItemRepository.delete(cartItem2);
        }
        cart.setTotalProduct(0);
        cart.setTotalPrice(0);
        cartRepository.save(cart);
        return new DeleteCartItemResponse(
                "Delete all item success"
        );
    }
}