import React, { createContext, useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

// Create Context
const CartContext = createContext();

// Function to get cookie
const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);

    if (parts.length === 2) return parts.pop().split(';').shift();
};

// Create Provider
export const CartProvider = ({ children }) => {
    const [cartItems, setCartItems] = useState([]);
    const [cartId, setCartId] = useState(null); // Default cart to null

    // Ensure cart exists
    // Ensure cart exists
    const ensureCartExists = async (userId) => {
        // Check if the user is not logged in (userId is null or undefined)
        if (!userId) {
            console.log("User is not logged in, skipping cart fetch.");
            return; // Exit the function early
        }

        const token = getCookie('access_token');

        try {
            const response = await fetch(`http://localhost:1010/api/cart/list-cart/${userId}`, {
                method: 'GET',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });

            const data = await response.json();
            console.log("Cart List Response:", data); // Debugging response

            if (response.ok) {
                const cartList = data.listCart || [];
                console.log("Cart List:", cartList); // Checking cart list

                // Check if there is a cart with the status "NEW"
                const newCart = cartList.find(cart => cart.statusCart === 'NEW');

                if (newCart) {
                    // If there is already a "NEW" cart, just retrieve its cartId
                    if (cartId !== newCart.cartId) { // Check if cartId has been set
                        setCartId(newCart.cartId);
                        console.log("Existing NEW cart ID:", newCart.cartId); // Log cart ID
                        await fetchCartItemsByCartId(newCart.cartId); // Fetch items of the "NEW" cart
                    }
                } else {
                    // If there is no "NEW" cart, create a new cart and save cartId
                    console.log('No "NEW" cart found, creating a new cart...');
                    const newCartId = await createCart(userId); // Create a new cart with status "NEW"
                    setCartId(newCartId); // Save cartId
                }
            } else {
                console.error('Failed to fetch carts:', data);
            }
        } catch (error) {
            console.error('Error fetching cart:', error);
        }
    };


    useEffect(() => {
        const initializeCart = async () => {
            const userId = getUserIdFromToken(getCookie('access_token'));
            await ensureCartExists(userId);
        };
        initializeCart();
    }, []);



    // Create a new cart
    const createCart = async (userId) => {
        const token = getCookie('access_token');

        try {
            const response = await fetch('http://localhost:1010/api/cart/create', {
                method: 'POST',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ userId }),
            });

            const data = await response.json();

            if (response.ok) {
                console.log('New cart created:', data);
                return data.cartId; // Assuming API returns cartId
            } else {
                console.error('Failed to create cart:', data);
                throw new Error('Failed to create cart');
            }
        } catch (error) {
            console.error('Error creating cart:', error);
        }
    };

    // Fetch cart items for a specific user by userId
    const fetchCartItemsByCartId = async (cartId) => {
        const token = getCookie('access_token');

        try {
            // Fetch the cart items using the provided cartId
            const itemsResponse = await fetch(`http://localhost:1010/api/cart/list-cartItem/${cartId}`, {
                method: 'GET',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });

            const itemsData = await itemsResponse.json();

            if (itemsResponse.ok) {
                // Enrich each item with product details
                const itemsWithDetails = await Promise.all(
                    itemsData.listCartItemResponses.map(async (item) => {
                        const productDetails = await fetchProductDetails(item.proId, item.size);
                        return {
                            cartItemId: item.cartItemId,
                            productId: item.proId,
                            size: item.size,
                            quantity: item.quantity,
                            totalPrice: item.totalPrice,
                            name: productDetails?.name || 'Unknown Product',
                            image: productDetails?.image || '',
                        };
                    })
                );

                setCartItems(itemsWithDetails); // Set enriched cart items state
            } else {
                console.error('Failed to fetch cart items:', itemsData);
            }
        } catch (error) {
            console.error('Error fetching cart items:', error);
        }
    };


    // Fetch product details by product ID and size
    const fetchProductDetails = async (productId, size) => {
        const token = getCookie('access_token');
        let productDetails = null;
        let variantDetails = null;

        try {
            // Fetch product details (name and image)
            const productResponse = await fetch(`http://localhost:1010/api/product/view/${productId}`, {
                method: 'GET',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });

            const productData = await productResponse.json();

            if (productResponse.ok) {
                productDetails = {
                    name: productData.proName,
                    image: productData.productImageResponseList[0]?.linkImage || '', // Get the first image
                };
            } else {
                console.error('Failed to fetch product details:', productData);
                return null; // Exit if product details couldn't be fetched
            }

            // Fetch product variants to get the price for the specified size
            const variantResponse = await fetch(`http://localhost:1010/api/product/variants/${productId}`, {
                method: 'GET',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });

            const variantData = await variantResponse.json();

            if (variantResponse.ok) {
                // Find the price for the specified size
                variantDetails = variantData.responseList.find(item => item.size === size);
                if (variantDetails) {
                    return {
                        ...productDetails,
                        price: variantDetails.price || 0, // Get the price for the selected size
                    };
                } else {
                    console.error(`No variant found for size: ${size}`);
                    return null; // Exit if no variant is found
                }
            } else {
                console.error('Failed to fetch product variants:', variantData);
                return null; // Exit if variants couldn't be fetched
            }
        } catch (error) {
            console.error('Error fetching product details or variants:', error);
            return null; // Exit in case of error
        }
    };


    const addToCart = async (product) => {
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        console.log("CartID nè: ", cartId); // Log cartId để kiểm tra

        // Nếu cartId vẫn không có sau khi gọi ensureCartExists, không tiếp tục
        if (!cartId) {
            console.error('No cartId found!');

        }


        // Fetch product details including the price based on size
        const productDetails = await fetchProductDetails(product.productId, product.size);

        if (!productDetails) {
            console.error('Could not fetch product details');
            return; // Exit if product details couldn't be fetched
        }

        // Prepare the request body
        const requestBody = {
            userId: userId,
            cartId: cartId,
            proId: product.productId,
            size: product.size,
            quantity: product.quantity,
        };

        try {
            const response = await fetch('http://localhost:1010/api/cart-item/insert', {
                method: 'POST',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestBody),
            });

            const data = await response.json();

            if (response.ok) {
                console.log('Item added to cart successfully:', data);
                // Show alert for successful addition to cart
                alert(`${product.name} đã được thêm vào giỏ hàng!`);

                // Update cart items
                setCartItems((prevItems) => {
                    const itemExists = prevItems.find(item => item.productId === product.productId && item.size === product.size);
                    if (itemExists) {
                        return prevItems.map(item =>
                            item.productId === product.productId && item.size === product.size
                                ? { ...item, quantity: item.quantity + product.quantity }
                                : item
                        );
                    }

                    return [...prevItems, {
                        ...product,
                        cartItemId: data.cartItemId,
                        name: productDetails.name, // Use fetched product name
                        image: productDetails.image, // Use fetched product image
                        totalPrice: productDetails.price * product.quantity, // Calculate total price based on quantity
                    }];
                });
            } else {
                console.error('Failed to add item to cart:', data);
            }
        } catch (error) {
            console.error('Error while adding item to cart:', error);
        }
    };

    const [selectedVoucher, setSelectedVoucher] = useState(null); // Add state for selectedVoucher
    const [note, setNote] = useState(""); // Add state for note
    const navigate = useNavigate()



    const handleCheckout = async () => {
        const token = getCookie('access_token');
        const userId = parseInt(getUserIdFromToken(token), 10);

        if (!userId || !cartId) {
            console.error("User ID or Cart ID is missing");
            return;
        }

        const orderData = { userId, cartId, voucherId: selectedVoucher, note };

        console.log('Data to be sent to the server:', orderData);

        try {
            const response = await fetch('http://localhost:1010/api/orders/create', {
                method: 'POST',
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(orderData),
            });

            const data = await response.json();

            console.log('Response Status:', response.status);
            console.log('Response Body:', data.body);

            if (data.statusCodeValue === 400) {
                alert(`Error: ${data.body}`);
                navigate('/info');
            } else if (data.statusCodeValue === 200) {
                console.log('Order created successfully: ', data);

                // Pass the newly created order data to the Order page
                alert(`Đặt hàng thành công`);

                setCartItems([]);  // Clear cart after successful checkout
                await ensureCartExists(userId);

                // Navigate to the order page and pass the order data using state
                navigate('/order', { state: { orderData: data.body } });

            } else {
                console.error('Error creating order:', data);
            }
        } catch (error) {
            console.error('Error while making order request:', error);
        }
    };

    const increase = async (cartItemId) => {
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (!cartItemId || !cartId) {  // Check if both cartItemId and cartId are provided
            console.error('No cart item ID or cart ID provided.');
            return;
        }

        try {
            // Step 1: Fetch the cart item to get the productId, size, and current quantity
            const cartItemResponse = await fetch(`http://localhost:1010/api/cart/list-cartItem/${cartId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json',
                },
            });

            // Check if the response is OK
            if (!cartItemResponse.ok) {
                const errorText = await cartItemResponse.text(); // Read response as text
                console.error('Failed to fetch cart item data:', errorText);
                return; // Exit if the response is not OK
            }

            // Attempt to parse the response as JSON
            let cartItemData;
            try {
                cartItemData = await cartItemResponse.json();
            } catch (jsonError) {
                console.error('Error parsing JSON response:', jsonError);
                return; // Exit if JSON parsing fails
            }

            // Find the specific cart item using the cartItemId
            const cartItem = cartItemData.listCartItemResponses.find(item => item.cartItemId === cartItemId);

            if (!cartItem) {
                console.error(`Cart item with ID ${cartItemId} not found.`);
                return;
            }

            const { proId, size, quantity } = cartItem; // Ensure your API returns productId, size, and quantity fields

            // Step 2: Fetch stock information using the productId
            const variantResponse = await fetch(`http://localhost:1010/api/product/variants/${proId}`, {
                method: 'GET',
                headers: {
                    'accept': '*/*',
                },
            });

            // Check if the response for variants is OK
            if (!variantResponse.ok) {
                const variantErrorText = await variantResponse.text(); // Read response as text
                console.error('Failed to fetch variant data:', variantErrorText);
                return; // Exit if the response is not OK
            }

            // Attempt to parse the variant response as JSON
            let variantData;
            try {
                variantData = await variantResponse.json();
            } catch (jsonError) {
                console.error('Error parsing JSON variant response:', jsonError);
                return; // Exit if JSON parsing fails
            }

            // Step 3: Find the specific variant that matches the size
            const variant = variantData.responseList.find(v => v.size === size);

            if (!variant) {
                console.error(`No variant found for size: ${size}`);
                return;
            }

            // Step 4: Check stock information
            const { stock } = variant; // Ensure your API returns a stock field

            // Step 5: Check if adding one more exceeds stock
            if (quantity >= stock) {
                console.error(`Cannot increase quantity. Stock limit reached for cart item ID ${cartItemId}.`);
                alert(`Đã đạt giới hạn số lượng cho sản phẩm này!`); // Alert the user that stock limit is reached
                return; // Exit if limit is reached
            }

            // Step 6: Proceed to increase the item quantity
            const updateResponse = await fetch('http://localhost:1010/api/cart-item/increase', {
                method: 'PUT',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: userId,
                    cartItemId: cartItemId,
                    quantity: 1, // Increase quantity by 1
                }),
            });

            // Check if the update response is OK
            if (!updateResponse.ok) {
                const updateErrorText = await updateResponse.text(); // Read response as text
                console.error('Failed to increase item quantity:', updateErrorText);
                return; // Exit if the response is not OK
            }

            // Update the cart items in the state if the request is successful
            setCartItems(prevItems =>
                prevItems.map(item =>
                    item.cartItemId === cartItemId
                        ? { ...item, quantity: item.quantity + 1 }
                        : item
                )
            );

        } catch (error) {
            console.error('Error increasing item quantity:', error);
        }
    };


    // Decrease quantity of item in cart
    const decrease = async (cartItemId) => {
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (!cartItemId) {
            console.error('No cart item ID provided.');
            return;
        }

        // Find the item in the cart to check its current quantity
        const cartItem = cartItems.find(item => item.cartItemId === cartItemId);

        if (!cartItem) {
            console.error('Cart item not found.');
            return;
        }

        if (cartItem.quantity > 1) {
            // If the quantity is greater than 1, decrease it
            try {
                const response = await fetch('http://localhost:1010/api/cart-item/decrease', {
                    method: 'PUT',
                    headers: {
                        'accept': '*/*',
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        userId: userId,
                        cartItemId: cartItemId,
                        quantity: 1, // Decrease quantity by 1
                    }),
                });

                const data = await response.json();

                if (response.ok) {
                    // Update the cart items in the state
                    setCartItems(prevItems =>
                        prevItems.map(item =>
                            item.cartItemId === cartItemId
                                ? { ...item, quantity: item.quantity - 1 }
                                : item
                        )
                    );
                } else {
                    console.error('Failed to decrease item quantity:', data);
                }
            } catch (error) {
                console.error('Error decreasing item quantity:', error);
            }
        } else {
            // If the quantity is 1, remove the item from the cart
            try {
                const response = await fetch(`http://localhost:1010/api/cart-item/delete/${cartItemId}`, {
                    method: 'DELETE',
                    headers: {
                        'accept': '*/*',
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        userId: userId,
                        cartItemId: cartItemId,
                    }),
                });

                const data = await response.json();

                if (response.ok) {
                    console.log('Item deleted from cart:', data.message);

                    // Remove the item from the cart items state
                    setCartItems(prevItems =>
                        prevItems.filter(item => item.cartItemId !== cartItemId)
                    );
                } else {
                    console.error('Failed to delete item:', data);
                }
            } catch (error) {
                console.error('Error deleting item:', error);
            }
        }
    };

    // Get userId from token
    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Cannot decode token:", error);
            return null;
        }
    };



    // Handle user login and logout
    const handleAuthChange = async () => {
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (userId) {
            console.log('User logged in, fetching cart items for userId:', userId);
            ensureCartExists(userId)
            await fetchCartItemsByCartId(cartId);
        } else {
            console.log('User logged out, clearing cart items');
            setCartItems([]); // Clear cart items on logout
            setCartId(null); // Clear cart ID on logout
        }
    };

    // Call handleAuthChange on component mount and when the access token changes
    useEffect(() => {
        handleAuthChange();
    }, [getCookie('access_token')]); // Dependency to re-run the effect on token change

    const clearCart = async () => {
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (!cartId) {
            console.error('No cart ID provided.');
            return;
        }

        try {
            const response = await fetch(`http://localhost:1010/api/cart/delete-allItem/${cartId}`, {
                method: 'DELETE',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: userId,
                    cartId: cartId,
                }),
            });

            const data = await response.json();

            if (response.ok) {
                console.log('All items deleted from cart:', data.message);
                // Clear cart items in the state
                setCartItems([]);
            } else {
                console.error('Failed to clear cart items:', data);
            }
        } catch (error) {
            console.error('Error clearing cart items:', error);
        }
    };

    // Delete one item from cart
    const deleteOneItem = async (cartItemId) => {
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (!cartItemId) {
            console.error('No cart item ID provided.');
            return;
        }

        try {
            const response = await fetch(`http://localhost:1010/api/cart-item/delete/${cartItemId}`, {
                method: 'DELETE',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: userId,
                    cartItemId: cartItemId,
                }),
            });

            const data = await response.json();

            if (response.ok) {
                console.log('Item deleted successfully:', data.message);

                // Remove the item from the cart items state
                setCartItems(prevItems =>
                    prevItems.filter(item => item.cartItemId !== cartItemId)
                );
            } else {
                console.error('Failed to delete item:', data);
            }
        } catch (error) {
            console.error('Error deleting item:', error);
        }
    };



    return (
        <CartContext.Provider value={{ cartItems, cartId, addToCart, increase, decrease, clearCart, deleteOneItem, selectedVoucher, setSelectedVoucher, note, setNote, handleCheckout }}>
            {children}
        </CartContext.Provider>
    );
};

// Custom hook to use CartContext
export const useCart = () => {
    return useContext(CartContext);
};