import React, { useEffect, useState } from 'react';
import './ProductRecommend.css'; // Tạo CSS riêng cho giao diện
import { useNavigate } from 'react-router-dom';

const ProductRecommend = ({ product }) => {
    const navigate = useNavigate();
    const [averageRating, setAverageRating] = useState(0); // State để lưu trữ rating trung bình

    // Format giá với dấu chấm ngăn cách hàng nghìn
    const formattedPrice = new Intl.NumberFormat('vi-VN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
    }).format(parseFloat(product.price || 0));

    // Hàm fetchRating để lấy số rating trung bình cho sản phẩm
    // const fetchRating = async () => {
    //     try {
    //         const ratingResponse = await fetch(`http://localhost:1010/api/product/list-rating`, {
    //             method: 'GET',
    //             headers: {
    //                 'Accept': '*/*'
    //             }
    //         });

    //         if (!ratingResponse.ok) {
    //             throw new Error('Network response was not ok');
    //         }

    //         const ratingData = await ratingResponse.json();
    //         const productRating = ratingData.list.find(avgRating => avgRating.proId === product.proId);
    //         if (productRating) {
    //             setAverageRating(productRating.avgRating); // Cập nhật rating trung bình
    //             console.log(`Rating for product recommend ${product.proId}: ${productRating.avgRating}`); // Debugging
    //         } else {
    //             setAverageRating(0); // Nếu không tìm thấy rating, đặt về 0
    //         }
    //     } catch (error) {
    //         console.error('Failed to fetch product rating:', error);
    //     }
    // };

    // // Gọi fetchRating khi component được mount
    // useEffect(() => {
    //     fetchRating();
    // }, [product.proId]);

    // Render số sao đánh giá
    const renderStars = (rating) => {
        const stars = [];
        for (let i = 1; i <= 5; i++) {
            stars.push(
                <span key={i} className={`star ${i <= rating ? 'filled' : ''}`}>
                    ★
                </span>
            );
        }
        return stars;
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
            {/* <p>{renderStars(averageRating)}</p> */}
        </div>
    );
};

export default ProductRecommend;
