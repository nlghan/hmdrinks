import React from 'react';
import './ProductRecommend.css'; // Tạo CSS riêng cho giao diện
import { useNavigate } from 'react-router-dom';

const ProductRecommend = ({ product }) => {
    const navigate = useNavigate();

    // Format giá với dấu chấm ngăn cách hàng nghìn
    const formattedPrice = new Intl.NumberFormat('vi-VN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
    }).format(parseFloat(product.price || 0));

    // Render số sao đánh giá
    const renderStars = (rate) => {
        return (
            <div className="recommend-star-rating">
                {[...Array(5)].map((_, index) => (
                    <span key={index} className={`recommend-star ${index < rate ? 'filled' : ''}`}>★</span>
                ))}
            </div>
        );
    };

    const handleProductRecommendClick = () => {
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
