import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom'; // Import useNavigate for navigation
import './ProductCard.css';

function ProductCard({ product, onClick, onAddToCart, className, style, onFavoriteChange }) {
    const [isFavorited, setIsFavorited] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const navigate = useNavigate(); // Initialize navigate

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

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);

        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    // Check if product is favorited when component mounts
    // Check if product is favorited when component mounts
    useEffect(() => {
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);
    
        const checkIfFavorited = async () => {
            try {
                // Lấy danh sách yêu thích của người dùng
                const favResponse = await axios.get(`http://localhost:1010/api/fav/list-fav/${userId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'accept': '*/*'
                    }
                });
    
                const favorites = favResponse.data; // Lấy danh sách các mục yêu thích
                if (favorites && favorites.length > 0) {
                    const favId = favorites.favId;
                    console.log(`Favorite ID: ${favId}`);
    
                    // Kiểm tra sản phẩm có trong danh sách yêu thích
                    const favItemResponse = await axios.get(`http://localhost:1010/api/fav/list-favItem/${favId}`, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'accept': '*/*'
                        }
                    });
    
                    const favoriteItems = favItemResponse.data.favouriteItemResponseList || [];
                    console.log("Favorite Items: ", favoriteItems);
    
                    // Kiểm tra ID của sản phẩm hiện tại
                    const isFav = favoriteItems.some(fav => fav.proId === product.proId);
                    setIsFavorited(isFav); // Cập nhật trạng thái yêu thích của sản phẩm
                }
            } catch (error) {
                console.error("Error fetching favorites:", error);
            }
        };
    
        if (userId && token) {
            checkIfFavorited();
        }
    }, [product.proId]); // Bỏ onFavoriteChange khỏi dependency
    

    const handleFavorite = async (event) => {
        event.stopPropagation(); // Prevent event from bubbling to parent card click
        setErrorMessage(null); // Clear any previous error messages
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);
        let favId = null; // Variable to hold the favorite ID

        try {
            // Check if the favorite already exists for the user
            const response = await axios.get(`http://localhost:1010/api/fav/list-fav/${userId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'accept': '*/*'
                }
            });

            const existingFavorite = response.data; // Adjust this based on the actual response structure

            if (existingFavorite) {
                favId = existingFavorite.favId; // Store the favorite ID
                console.log("Favorite already exists with favId:", favId);
            } else {
                console.log("No existing favorite found.");
                return; // Exit if no favorite exists
            }
        } catch (error) {
            if (error.response) {
                if (error.response.status === 404) {
                    // Create a new favorite
                    try {
                        const createResponse = await axios.post(
                            'http://localhost:1010/api/fav/create',
                            { userId: userId }, // Use the dynamic user ID
                            {
                                headers: {
                                    'Authorization': `Bearer ${token}`,
                                    'Content-Type': 'application/json',
                                    'accept': '*/*'
                                }
                            }
                        );

                        favId = createResponse.data.favId; // Store the new favorite ID
                        console.log("Created new favorite with favId:", favId);
                    } catch (postError) {
                        setErrorMessage("An error occurred while creating the favorite. Please try again.");
                        return; // Exit if creating the favorite fails
                    }
                } else {
                    setErrorMessage("An error occurred. Please try again.");
                    return; // Exit on other errors
                }
            } else {
                setErrorMessage("An error occurred. Please try again.");
                return; // Exit on network or other errors
            }
        }

        // Now that we have the favId, we can add the product to the favorites
        try {
            const addItemResponse = await axios.post(
                'http://localhost:1010/api/fav-item/insert',
                {
                    userId: userId,
                    favId: favId,
                    proId: product.proId, // Use the product ID
                    size: product.size // Assuming you have product size available
                },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                        'accept': '*/*'
                    }
                }
            );

            console.log("Product added to favorites:", addItemResponse.data);
            setIsFavorited(true); // Update state to reflect that the product is favorited

            if (onFavoriteChange) {
                onFavoriteChange(true); // Notify parent about the change
            }
        } catch (addItemError) {
            setErrorMessage("An error occurred while adding the product to favorites. Please try again.");
        }
    };

    const handleDeleteFavorite = async () => {
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        try {
            const response = await axios.delete(`http://localhost:1010/api/fav-item/delete/${product.favItemId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'accept': '*/*',
                    'Content-Type': 'application/json'
                },
                data: {
                    userId: userId,
                    favItemId: product.favItemId // Send the id of the favorite item
                }
            });

            console.log("Deleted favorite item:", response.data);
            setIsFavorited(false); // Update local state
            if (onFavoriteChange) {
                onFavoriteChange(false); // Notify parent about the change
            }
        } catch (error) {
            setErrorMessage("An error occurred while deleting the favorite. Please try again.");
            console.error("Delete error:", error);
        }
    };

    // Format price to have dots as thousands separators
    const formattedPrice = new Intl.NumberFormat('vi-VN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
    }).format(parseFloat(product.price));

    // Handle click on the "Đặt mua" button
    const handleAddToCartClick = (event) => {
        event.stopPropagation(); // Prevent the event from bubbling up and triggering the card click
        onAddToCart(); // Trigger the function passed in as a prop
    };

    return (
        <div 
            className={`product-card ${className}`} 
            onClick={onClick} 
            style={style} // Apply the style passed in for animation delay
        > 
            <div className="product-card-image-container">
                <img src={product.image} alt={product.name} />
                <button className="favorite-icon" onClick={handleFavorite}>
                    <i className="fa fa-heart" style={{ color: isFavorited ? 'red' : 'grey' }} aria-hidden="true"></i>
                </button>
            </div>
            <h3>{product.name} ({product.size})</h3>
            <div className='product-card-price'>
                <p className='product-card-p card-product-price'>Giá: {formattedPrice} VND</p>
                <button className="add-cart" onClick={handleAddToCartClick}>
                    <i className="ti-shopping-cart" /> Đặt mua
                </button>
            </div>
            {errorMessage && <p className="error-message">{errorMessage}</p>}
        </div>
    );
}

export default ProductCard;
