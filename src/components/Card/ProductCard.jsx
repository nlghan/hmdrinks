import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './ProductCard.css';
import { useFavorite } from '../../context/FavoriteContext';

function ProductCard({ product, onClick, onAddToCart, className, style, onFavoriteChange }) {
    const [isFavorited, setIsFavorited] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const navigate = useNavigate();
    const { favoriteItems, addFavorite, removeFavorite, errorMessage: contextError } = useFavorite();

    // Check if product is in favorites
    useEffect(() => {
        setIsFavorited(favoriteItems.some(fav => fav.proId === product.proId));
    }, [favoriteItems, product.proId]);

    // Add to favorites handler
    const handleFavorite = async (event) => {
        event.stopPropagation(); // Prevent event from bubbling to parent card click
        setErrorMessage(null);
    
        try {
            if (isFavorited) {
                await removeFavorite(product, 'productCard'); // Specify source as 'productCard'
                setIsFavorited(false);
                onFavoriteChange && onFavoriteChange(false);
            } else {
                await addFavorite(product); // Add product to favorites
                setIsFavorited(true);
                onFavoriteChange && onFavoriteChange(true);
            }
        } catch (error) {
            setErrorMessage("An error occurred while updating the favorite status. Please try again.");
        }
    };
    

    // Format price to have dots as thousands separators
    const formattedPrice = new Intl.NumberFormat('vi-VN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
    }).format(parseFloat(product.price));

    // Handle click on the "Đặt mua" button
    const handleAddToCartClick = (event) => {
        event.stopPropagation();
        onAddToCart();
    };

    return (
        <div 
            className={`product-card ${className}`} 
            onClick={onClick} 
            style={style}
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
            {(errorMessage || contextError) && <p className="error-message">{errorMessage || contextError}</p>}
        </div>
    );
}

export default ProductCard;
