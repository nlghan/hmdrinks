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

    // Lấy ảnh có id = 1 từ productImageResponseList
    const imageWithId1 = product.productImageResponseList?.find(img => img.id === 1);

    return (
        <div className="recommend-product-recommend" onClick={handleProductRecommendClick}>
            <img 
                src={imageWithId1?.linkImage || 'default-image-url.jpg'} 
                alt={product.proName} 
                className="recommend-product-image" 
            />
            <h3>{product.proName}</h3>
            <p className="recommend-product-description">{product.description}</p>
            {renderStars(product.rate)}
        </div>
    );
};

export default ProductRecommend;
