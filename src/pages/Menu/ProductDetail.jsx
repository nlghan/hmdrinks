import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './ProductDetail.css';

const ProductDetail = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [selectedSize, setSelectedSize] = useState('M'); // Default size selection
    const [quantity, setQuantity] = useState(1); // Default quantity selection
    const [currentImageIndex, setCurrentImageIndex] = useState(0); // Track current image index
    const [categoryName, setCategoryName] = useState(''); // State to hold category name
    const productDetailRef = useRef(null); // Ref to observe scroll

    // Retrieve product data from location state
    const product = location.state?.product;

    // Set the initial main image to the first product image
    useEffect(() => {
        if (product && product.productImageResponseList.length > 0) {
            setCurrentImageIndex(0); // Start with the first image
        }
    }, [product]);

    // If no product data is passed, redirect back to home or product list
    if (!product) {
        navigate('/'); // Redirect to home if no product data is available
        return null; // Return null to prevent rendering without product data
    }

    // Fetch category name based on cateId
    useEffect(() => {
        const fetchCategoryName = async () => {
            try {
                const categoryResponse = await fetch(`http://localhost:1010/api/cate/view/${product.cateId}`, {
                    method: 'GET',
                    headers: {
                        'Accept': '*/*'
                    }
                });

                if (!categoryResponse.ok) {
                    throw new Error('Network response was not ok');
                }

                const categoryData = await categoryResponse.json();
                setCategoryName(categoryData.cateName); // Set the category name from the response
            } catch (error) {
                console.error('Failed to fetch category name:', error);
            }
        };

        fetchCategoryName();
    }, [product.cateId]);

    const handleSizeChange = (size) => {
        setSelectedSize(size);
    };

    const handleQuantityChange = (e) => {
        const value = Math.max(1, e.target.value); // Ensure the minimum quantity is 1
        setQuantity(value);
    };

    // Scroll Animation
    useEffect(() => {
        const handleScroll = () => {
            const elements = document.querySelectorAll('.fade-in');
            elements.forEach((el) => {
                const rect = el.getBoundingClientRect();
                if (rect.top < window.innerHeight - 100) {
                    el.classList.add('show');
                }
                if (rect.top >= window.innerHeight || rect.bottom < 0) {
                    el.classList.remove('show');
                }
            });
        };

        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    // Handle clicking a dot to update the main image
    const handleDotClick = (index) => {
        setCurrentImageIndex(index); // Update current image index based on clicked dot
    };

    const handlePreviousImage = () => {
        setCurrentImageIndex((prevIndex) => 
            prevIndex === 0 ? product.productImageResponseList.length - 1 : prevIndex - 1
        );
    };
    
    const handleNextImage = () => {
        setCurrentImageIndex((prevIndex) => 
            prevIndex === product.productImageResponseList.length - 1 ? 0 : prevIndex + 1
        );
    };

    const handleBack = () => {
        navigate('/menu'); // Navigate to the menu page
    };

    return (
        <>
            <Navbar />
            <div className='proCon'>
                <div className="product-detail-container" ref={productDetailRef}>
                    <div className="product-detail-main">
                        <div className="product-image-container">
                            {product.productImageResponseList.length > 0 && (
                                <>
                                    <div id='img1-container'>
                                        <img
                                            src={product.productImageResponseList[currentImageIndex].linkImage} // Main image based on current index
                                            alt={product.name}
                                            className="product-image"
                                        />
                                        <div className="navigation-buttons">
                                            <button className="arrow-button" onClick={handlePreviousImage}>
                                                &lt; {/* Left arrow */}
                                            </button>
                                            <button className="arrow-button" onClick={handleNextImage}>
                                                &gt; {/* Right arrow */}
                                            </button>
                                        </div>
                                    </div>

                                    <div style={{ display: 'flex', gap: '15px' }}>
                                        {product.productImageResponseList.map((image, index) => (
                                            <img
                                                key={index}
                                                src={image.linkImage}
                                                alt={product.name}
                                                className={`product-image-detail ${currentImageIndex === index ? 'active' : ''}`} // Highlight active thumbnail
                                                onClick={() => handleDotClick(index)} // Click handler to change main image
                                            />
                                        ))}
                                    </div>
                                </>
                            )}
                        </div>

                        <div className="product-info-container">
                            <h1>{product.proName}</h1>
                            <span className="product-category">Danh mục: {categoryName}</span> {/* Display category name */}
                            <span className="product-price">Giá: {product.price}</span>

                            <div className="product-size">
                                <span>Chọn size:</span>
                                <div className="size-options">
                                    {['S', 'M', 'L'].map(size => (
                                        <button
                                            key={size}
                                            className={selectedSize === size ? 'size-option active' : 'size-option'}
                                            onClick={() => handleSizeChange(size)}
                                        >
                                            {size}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div className="product-quantity">
                                <span>Số lượng:</span>
                                <input
                                    id='soluong'
                                    type="number"
                                    value={quantity}
                                    min="1"
                                    onChange={handleQuantityChange}
                                    className="quantity-input"
                                />
                            </div>

                            <button className="add-to-cart-button">Thêm vào giỏ hàng</button>
                            <button className="add-to-cart-button" style={{marginBottom:'10px', backgroundColor:'#099494'}} onClick={handleBack}>Xem đồ uống khác</button>
                        </div>
                    </div>

                    <div className="product-description-container fade-in">
                        <h2>Mô tả</h2>
                        <p>{product.description}</p>
                    </div>

                    <div className="product-rating-container fade-in">
                        <span>Đánh giá sản phẩm:</span>
                        <span className="product-rating">
                            {"★".repeat(product.rating)}{"☆".repeat(5 - product.rating)}
                        </span>
                        <input type="text" placeholder="Hãy cho chúng tôi biết cảm nhận của bạn về sản phẩm" />
                        <button>Gửi</button>
                    </div>

                    <h2 className="fade-in">Có thể bạn sẽ thích</h2>
                    <div className="related-products fade-in">
                        {product.relatedProducts?.length > 0 ? (
                            product.relatedProducts.map((relatedProduct, index) => (
                                <div key={index} className="related-product-card">
                                    <img src={relatedProduct.imageUrl} alt={relatedProduct.name} />
                                    <h3>{relatedProduct.name}</h3>
                                    <span>{relatedProduct.price}</span>
                                    <button>Mua ngay</button>
                                </div>
                            ))
                        ) : (
                            <p>Không có sản phẩm liên quan.</p>
                        )}
                    </div>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default ProductDetail;
