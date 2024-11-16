import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './ProductCard.css';
import { useFavorite } from '../../context/FavoriteContext';
import { useAuth } from '../../context/AuthProvider'; // Import useAuth

function ProductCard({ product, onClick, onAddToCart, className, style, onFavoriteChange }) {
    const [isFavorited, setIsFavorited] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const navigate = useNavigate();
    const { favoriteItems, addFavorite, removeFavorite, errorMessage: contextError } = useFavorite();
    const { isLoggedIn } = useAuth(); // Access the authentication status
    const [isLoading, setIsLoading] = useState(false);
    const [showSuccess, setShowSuccess] = useState(false);
    const [showError, setShowError] = useState(false);
    const [message, setMessage] = useState(''); // Để hiển thị message động

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
    const handleAddToCartClick = async (event) => {
        event.stopPropagation();
        setIsLoading(true);
        try {
            await onAddToCart();
            setIsLoading(false);
            setMessage(`${product.name} đã được thêm vào giỏ hàng!`);
            setShowSuccess(true);
            setTimeout(() => {
                setShowSuccess(false);
            }, 2000);
        } catch (error) {
            setIsLoading(false);
            setMessage(error.message || "Đã đạt giới hạn số lượng cho sản phẩm này!");
            setShowError(true);
            setTimeout(() => {
                setShowError(false);
            }, 2000);
        }
    };
    

    return (
        <>
            <div 
                className={`product-card ${className}`} 
                onClick={onClick} 
                style={style}
            > 
                <div className="product-card-image-container zoomIn">
                    <img src={product.image} alt={product.name} />
                    {isLoggedIn && ( // Render the favorite icon only if logged in
                        <button className="favorite-icon" onClick={handleFavorite}>
                            <i className="fa fa-heart" style={{ color: isFavorited ? 'red' : 'grey' }} aria-hidden="true"></i>
                        </button>
                    )}
                </div>
                <div className="info-product-card">
                    <h3>{product.name} ({product.size})</h3>
                    <div className="product-card-price">
                        <p className="product-card-p">Giá: {formattedPrice} VND</p>
                    </div>
                    <button className="add-cart" onClick={handleAddToCartClick}>
                        <i className="ti-shopping-cart" /> Đặt mua
                    </button>
                </div>
                {(errorMessage || contextError) && <p className="error-message">{errorMessage || contextError}</p>}
            </div>

            {isLoading && (
                <div className="product-card-loading-animation">
                    <div className="product-card-loading-modal">
                        <div className="product-card-loading-spinner">
                            <div className="product-card-spinner"></div>
                        </div>
                        <h3>Đang xử lý...</h3>
                        <p>Vui lòng đợi trong giây lát</p>
                    </div>
                </div>
            )}

            {showSuccess && (
                <div className="product-card-success-animation">
                    <div className="product-card-success-modal">
                        <div className="product-card-success-icon">
                            <div className="product-card-success-icon-circle">
                                <svg className="product-card-checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                    <circle className="product-card-checkmark-circle" cx="26" cy="26" r="25" fill="none"/>
                                    <path className="product-card-checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
                                </svg>
                            </div>
                        </div>
                        <h3>Thêm vào giỏ hàng thành công!</h3>
                        <p>{message}</p>
                    </div>
                </div>
            )}

            {showError && (
                <div className="product-card-error-animation">
                    <div className="product-card-error-modal">
                        <div className="product-card-error-icon">
                            <div className="product-card-error-icon-circle">
                                <svg className="product-card-cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                    <circle className="product-card-cross-circle" cx="26" cy="26" r="25" fill="none"/>
                                    <path className="product-card-cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36"/>
                                </svg>
                            </div>
                        </div>
                        <h3>Thêm vào giỏ hàng thất bại!</h3>
                        <p>{message}</p>
                    </div>
                </div>
            )}
        </>
    );
}

export default ProductCard;
