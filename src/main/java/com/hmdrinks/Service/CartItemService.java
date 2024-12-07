package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Size;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.CRUDCartItemResponse;
import com.hmdrinks.Response.ChangeSizeItemResponse;
import com.hmdrinks.Response.DeleteCartItemResponse;
import com.hmdrinks.Response.IncreaseDecreaseItemQuantityResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.sql.DriverManager.println;

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

    public ResponseEntity<?> insertCartItem(InsertItemToCart req)
    {
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
        }
        Product product= productRepository.findByProIdAndIsDeletedFalse(req.getProId());
        if(product == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found");
        }
        ProductVariants productVariants = productVariantsRepository.findBySizeAndProduct_ProIdAndIsDeletedFalse(req.getSize(),req.getProId());
        if(productVariants == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("production size not exists");
        }
        Cart cart1 = cartRepository.findByUserUserIdAndStatus(user.getUserId(), Status_Cart.NEW);
        if(cart1 == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found");
        }
        Cart cart = cartRepository.findByCartId(req.getCartId());
        if(cart == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found");
        }
        if(req.getQuantity() < 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is less than 0");
        }
        CartItem cartItem1 = cartItemRepository.findByProductVariants_VarIdAndProductVariants_SizeAndCart_CartId(productVariants.getVarId(),req.getSize(),req.getCartId());
        CartItem cartItem = new CartItem();
        if(cartItem1 == null)
        {
            if(req.getQuantity() > productVariants.getStock())
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is greater than stock");
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
            return ResponseEntity.status(HttpStatus.OK).body(new CRUDCartItemResponse(
                    cartItem.getCartItemId(),
                    cartItem.getProductVariants().getProduct().getProId(),
                    cartItem.getCart().getCartId(),
                    cartItem.getProductVariants().getSize(),
                    cartItem.getTotalPrice(),
                    cartItem.getQuantity()
            ));
        }
        else
        {
            if((req.getQuantity()+cartItem1.getQuantity()) > productVariants.getStock())
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is greater than stock");
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
            return ResponseEntity.status(HttpStatus.OK).body(new CRUDCartItemResponse(
                    cartItem1.getCartItemId(),
                    cartItem1.getProductVariants().getProduct().getProId(),
                    cartItem1.getCart().getCartId(),
                    cartItem1.getProductVariants().getSize(),
                    cartItem1.getTotalPrice(),
                    cartItem1.getQuantity()
            ));
        }
    }

    public ResponseEntity<?> increaseCartItemQuantity(IncreaseDecreaseItemQuantityReq req)
    {
        CartItem cartItem = cartItemRepository.findByCartItemId(req.getCartItemId());
        if(cartItem == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CartItem Not Found");
        }
        if(req.getQuantity() <= 0)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is less than 0");
        }
        ProductVariants productVariants = productVariantsRepository.findByVarId(cartItem.getProductVariants().getVarId());
        int Present_Quantity = cartItem.getQuantity() + 1 ;
        double Present_TotalPrice = productVariants.getPrice() * Present_Quantity;
        System.out.println(productVariants.getStock());

        if((req.getQuantity() + 1 ) > productVariants.getStock())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is greater than stock");
        }
        cartItem.setQuantity((Present_Quantity));
        cartItem.setTotalPrice((Present_TotalPrice));

        cartItemRepository.save(cartItem);
        Cart cart = cartRepository.findByCartId(cartItem.getCart().getCartId());
        if(cart == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found");
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
        return ResponseEntity.status(HttpStatus.OK).body(new IncreaseDecreaseItemQuantityResponse(
                Present_Quantity,
                Present_TotalPrice
        ));
    }

    public ResponseEntity<?> updateCartItemQuantity(IncreaseDecreaseItemQuantityReq req)
    {
        CartItem cartItem = cartItemRepository.findByCartItemId(req.getCartItemId());
        if(cartItem == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CartItem Not Found");
        }
        if(req.getQuantity() <= 0)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is less than 0");
        }
        ProductVariants productVariants = productVariantsRepository.findByVarId(cartItem.getProductVariants().getVarId());
        int Present_Quantity = req.getQuantity();  ;
        double Present_TotalPrice = productVariants.getPrice() * Present_Quantity;
        System.out.println(productVariants.getStock());

        if((req.getQuantity() ) > productVariants.getStock())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is greater than stock");
        }
        cartItem.setQuantity((Present_Quantity));
        cartItem.setTotalPrice((Present_TotalPrice));

        cartItemRepository.save(cartItem);
        Cart cart = cartRepository.findByCartId(cartItem.getCart().getCartId());
        if(cart == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found");
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
        return ResponseEntity.status(HttpStatus.OK).body(new IncreaseDecreaseItemQuantityResponse(
                Present_Quantity,
                Present_TotalPrice
        ));
    }

    @Transactional
    public ResponseEntity<?> changeSizeCartItemQuantity(ChangeSizeItemReq req)
    {
        CartItem cartItem = cartItemRepository.findByCartItemId(req.getCartItemId());
        if(cartItem == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CartItem Not Found");
        }

        ProductVariants productVariants = productVariantsRepository.findByVarIdAndSize(cartItem.getProductVariants().getVarId(),req.getSize());
        if(productVariants == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ProductVariants Not Found with Size");
        }
        cartItem.setProductVariants(productVariants);
        double Present_TotalPrice = productVariants.getPrice() * cartItem.getQuantity();
        cartItem.setTotalPrice(Present_TotalPrice);
        cartItemRepository.save(cartItem);
        Cart cart = cartRepository.findByCartId(cartItem.getCart().getCartId());
        if(cart == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found");
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
        return ResponseEntity.status(HttpStatus.OK).body(new ChangeSizeItemResponse(
                 req.getSize(),
                 Quantity,
                 Present_TotalPrice
                )
        );
    }

    public ResponseEntity<?> decreaseCartItemQuantity(IncreaseDecreaseItemQuantityReq req)
    {
        CartItem cartItem = cartItemRepository.findByCartItemId(req.getCartItemId());
        if(cartItem == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CartItem Not Found");
        }
        if(req.getQuantity() <= 0)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is less than 0");
        }
        ProductVariants productVariants = productVariantsRepository.findByVarId(cartItem.getProductVariants().getVarId());
        int Present_Quantity = cartItem.getQuantity() - 1 ;
        double Present_TotalPrice = productVariants.getPrice() * Present_Quantity;
        System.out.println(productVariants.getStock());

        if((req.getQuantity() - 1 ) > productVariants.getStock())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity is greater than stock");
        }
        cartItem.setQuantity((Present_Quantity));
        cartItem.setTotalPrice((Present_TotalPrice));

        cartItemRepository.save(cartItem);
        Cart cart = cartRepository.findByCartId(cartItem.getCart().getCartId());
        if(cart == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found");
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
        return ResponseEntity.status(HttpStatus.OK).body(new IncreaseDecreaseItemQuantityResponse(
                Present_Quantity,
                Present_TotalPrice
        ));
    }

    public ResponseEntity<?> deleteOneItem(DeleteOneCartItemReq req)
    {
        CartItem cartItem = cartItemRepository.findByCartItemId(req.getCartItemId());
        if(cartItem == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CartItem Not Found");
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
        return ResponseEntity.status(HttpStatus.OK).body(new DeleteCartItemResponse(
                "Delete item success"
        ));
    }

    public ResponseEntity<?> deleteAllCartItem(DeleteAllCartItemReq req)
    {
        Cart cart = cartRepository.findByCartIdAndStatus(req.getCartId(),Status_Cart.NEW);
        if(cart == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart Not Found");
        }
        List<CartItem> cartItemList = cartItemRepository.findByCart_CartId(req.getCartId());
        for(CartItem cartItem2: cartItemList)
        {
            cartItemRepository.delete(cartItem2);
        }
        cart.setTotalProduct(0);
        cart.setTotalPrice(0);
        cartRepository.save(cart);
        return ResponseEntity.status(HttpStatus.OK).body(new DeleteCartItemResponse(
                "Delete all item success"
        ));
    }
}