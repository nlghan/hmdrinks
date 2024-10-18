import React, { useState } from 'react';
import './ProductCard.css';

function ProductCard({ product, onClick }) { // Accept onClick as a prop
    const [isFavorited, setIsFavorited] = useState(false);

    const handleFavorite = () => {
        setIsFavorited(!isFavorited);
    };

    // Format price to have dots as thousands separators
    const formattedPrice = new Intl.NumberFormat('vi-VN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
    }).format(parseFloat(product.price));

    return (
        <div className="product-card" onClick={onClick}> {/* Trigger onClick when card is clicked */}
            <div className="product-card-image-container">
                <img src={product.image} alt={product.name} />
                <button className="favorite-icon" onClick={handleFavorite}>
                    <i className="fa fa-heart" style={{ color: isFavorited ? 'red' : 'grey' }} aria-hidden="true"></i>
                </button>
            </div>
            <h3>{product.name} ({product.size})</h3>
            <div className='product-card-price'>
                <p className='product-card-p card-product-price'>Giá: {formattedPrice} VND</p>
                <button className="add-cart">
                    <i className="ti-shopping-cart" /> Đặt mua
                </button>
            </div>
        </div>
    );
}

export default ProductCard;
