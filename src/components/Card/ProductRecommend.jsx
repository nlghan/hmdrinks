import React from 'react';
import './ProductRecommend.css'; // Create a CSS file for styling
import { useNavigate } from 'react-router-dom';

const ProductRecommend = ({ product, onClick }) => {
    const navigate = useNavigate();

    // Format price to have dots as thousands separators
    const formattedPrice = new Intl.NumberFormat('vi-VN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
    }).format(parseFloat(product.price));

    // Render star rating
    const renderStars = (rate) => {
        return (
            <div className="recommend-star-rating">
                {[...Array(5)].map((_, index) => (
                    <span key={index} className={`recommend-star ${index < rate ? 'filled' : ''}`}>★</span>
                ))}
            </div>
        );
    };

    const handleProductRecommendClick = (product) => {
        console.log("Navigating to product with ID:", product.proId); // Debugging line
        navigate(`/product/${product.proId}`, { state: { product } });
    };

    return (
        <div className="recommend-product-recommend" onClick={() => handleProductRecommendClick(product)}>
            {product.productImageResponseList.length > 0 && (
                <div id='img1-container'>
                    {product.productImageResponseList.map((image, index) => (
                        <img
                            key={index}
                            src={image.linkImage} // Lấy linkImage từ productImageResponseList
                            alt={product.proName}
                            className="product-image"
                        />
                    ))}
                </div>
            )}
            <h3>{product.proName}</h3>
            <p className="recommend-product-description">{product.description}</p>
            {renderStars(product.rate)}
        </div>
    );
};

export default ProductRecommend;