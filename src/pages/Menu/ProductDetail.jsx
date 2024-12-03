import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './ProductDetail.css';
import axios from 'axios';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthProvider';
import ProductCard from '../../components/Card/ProductCard'; // Import ProductCard
import ProductRecommend from '../../components/Card/ProductRecommend'; // Import ProductRecommend

const ProductDetail = () => {
    const location = useLocation();
    const product = location.state?.product;
    const navigate = useNavigate();
    const { addToCart } = useCart(); // Destructure addToCart from useCart context
    const [selectedSize, setSelectedSize] = useState(product?.size || 'L'); // Default size selection
    const [quantity, setQuantity] = useState(1); // Default quantity selection
    const [currentImageIndex, setCurrentImageIndex] = useState(0); // Track current image index
    const [categoryName, setCategoryName] = useState(''); // State to hold category name
    const [price, setPrice] = useState(product?.price); // State to hold current price based on size
    const [availableSizes, setAvailableSizes] = useState([]); // State to hold available sizes
    const productDetailRef = useRef(null); // Ref to observe scroll
    const [stock, setStock] = useState(0);
    const [reviews, setReviews] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPage, setTotalPage] = useState(0);
    const [limit] = useState(4);
    const [editingReviewId, setEditingReviewId] = useState(null);
    const [newContent, setNewContent] = useState('');
    const [newRating, setNewRating] = useState(0);
    const { isLoggedIn, logout } = useAuth();
    const [total, setTotal] = useState(0);
    const [showLoginPrompt, setShowLoginPrompt] = useState(false);
    const [showSuccess, setShowSuccess] = useState(false);
    const [showError, setShowError] = useState(false);
    const [showErrorLogin, setShowErrorLogin] = useState(false);
    const [recommendedProducts, setRecommendedProducts] = useState([]); // State to hold recommended products
    const [averageRating, setAverageRating] = useState(0); // State để lưu trữ rating trung bình
    const [productRatings, setProductRatings] = useState({}); // State để lưu trữ rating cho từng sản phẩm


    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Không thể giải mã token:", error);
            return null;
        }
    };

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);

        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const userId = getUserIdFromToken(getCookie('access_token'));

    const [newReview, setNewReview] = useState({
        userId: userId, // Nếu userId là null, bạn có thể điều chỉnh giao diện để không cho phép gửi đánh giá
        proId: product.proId,
        content: '',
        ratingStart: 0
    });
    useEffect(() => {
        // Lấy userId từ cookie hoặc token
        const token = getCookie('access_token');
        const id = getUserIdFromToken(token);
        setNewReview((prev) => ({ ...prev, userId: id })); // Cập nhật userId trong state
    }, [product.proId]);



    useEffect(() => {
        if (product && product.productImageResponseList.length > 0) {
            setCurrentImageIndex(0); // Start with the first image
        }
    }, [product]);

    if (!product) {
        navigate('/'); // Redirect to home if no product data is available
        return null; // Return null to prevent rendering without product data
    }

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
            setCategoryName(categoryData.cateName);
        } catch (error) {
            console.error('Failed to fetch category name:', error);
        }
    };

    // Hàm fetchRating để lấy số rating trung bình cho sản phẩm
    const fetchRating = async () => {
        try {
            const ratingResponse = await fetch(`http://localhost:1010/api/product/list-rating`, {
                method: 'GET',
                headers: {
                    'Accept': '*/*'
                }
            });

            if (!ratingResponse.ok) {
                throw new Error('Network response was not ok');
            }

            const ratingData = await ratingResponse.json();
            const ratings = {};
            ratingData.list.forEach(avgRating => {
                ratings[avgRating.proId] = avgRating.avgRating;
            });
            setProductRatings(ratings);
            const productRating = ratingData.list.find(avgRating => avgRating.proId === product.proId);
            if (productRating) {
                setAverageRating(productRating.avgRating); // Cập nhật rating trung bình
                console.log(`Rating for product prodetails ${product.proId}: ${productRating.avgRating}`); // Debugging
            } else {
                setAverageRating(0); // Nếu không tìm thấy rating, đặt về 0
            }
        } catch (error) {
            console.error('Failed to fetch product rating:', error);
        }
    };

    useEffect(() => {
        fetchCategoryName();
        fetchRating(); // Gọi hàm fetchRating khi component được mount
    }, [product.cateId, product.proId]); // Thêm product.proId vào dependency array

    useEffect(() => {
        const fetchProductVariants = async () => {
            try {
                const variantResponse = await fetch(`http://localhost:1010/api/product/variants/${product.proId}`, {
                    method: 'GET',
                    headers: {
                        'Accept': '*/*'
                    }
                });

                if (!variantResponse.ok) {
                    throw new Error('Network response was not ok');
                }

                const variantData = await variantResponse.json();
                setAvailableSizes(variantData.responseList.map(v => v.size)); // Get available sizes

                // Find the variant based on selected size
                const variant = variantData.responseList.find(v => v.size === selectedSize);
                if (variant) {
                    setPrice(variant.price); // Set the price based on selected size
                    setStock(variant.stock); // Set the stock based on selected size
                }
            } catch (error) {
                console.error('Failed to fetch product variants:', error);
            }
        };

        fetchProductVariants();
    }, [selectedSize, product.proId]);


    const handleSizeChange = (size) => {
        setSelectedSize(size);
    };

    const handleQuantityChange = (e) => {
        const value = Math.max(1, e.target.value);
        setQuantity(value);
    };

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

    const handleDotClick = (index) => {
        setCurrentImageIndex(index);
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
        navigate('/menu');
    };

    const handleAddToCart = async () => {
        const token = getCookie('access_token')
        const userId = getUserIdFromToken(token); // Hàm lấy userId từ token
        if (!userId) {
            setShowLoginPrompt(true); // Hiện thông báo yêu cầu đăng nhập
            return;
        }

        if (quantity > stock) {
            setShowError(true);
            setTimeout(() => {
                setShowError(false);
            }, 2000);
            return;
        }

        const { status, message } = await addToCart({
            productId: product.proId,
            name: product.proName,
            price: price,
            size: selectedSize,
            quantity: quantity,
            image: product.productImageResponseList[currentImageIndex].linkImage,
        });

        // Kiểm tra nếu API trả về lỗi 400
        if (status === 400) {
            setShowError(true);
            setMessage(message || "Đã đạt giới hạn số lượng cho sản phẩm này!");
            setTimeout(() => {
                setShowError(false);
            }, 2000);
            return; // Dừng lại nếu có lỗi 400
        }
        // Nếu thành công
        setShowSuccess(true);
        setMessage(message);
        setTimeout(() => {
            setShowSuccess(false);
        }, 2000);

    };

    const formattedPrice = new Intl.NumberFormat('vi-VN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
    }).format(price);


    const handleSubmitReview = async () => {
        const token = getCookie('access_token'); // Lấy token từ cookie
        if (!token) {
            setShowErrorLogin(true);
            setTimeout(() => {
                setShowErrorLogin(false);
            }, 2000);
            return;
        }

        const userId = getUserIdFromToken(token); // Lấy userId từ token
        if (!userId) {
            setShowErrorLogin(true);
            setTimeout(() => {
                setShowErrorLogin(false);
            }, 2000);
            return;
        }

        console.log("Attempting to submit review with token:", token);

        // Tạo review data với userId
        const reviewData = {
            userId: userId,
            proId: newReview.proId,
            content: newReview.content,
            ratingStart: newReview.ratingStart
        };

        console.log("Review data to be submitted:", reviewData);

        try {
            const response = await fetch('http://localhost:1010/api/review/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}` // Gửi token trong header
                },
                body: JSON.stringify(reviewData), // Gửi reviewData
            });

            console.log("Response status:", response.status);

            if (!response.ok) {
                console.error("Response error details:", await response.json());
                throw new Error(`Failed to submit review with status code: ${response.status}`);
            }

            const result = await response.json();
            console.log("Review submission result:", result);

            // Reset form
            setNewReview({ content: '', ratingStart: 0, proId: product.proId }); // Reset với proId
            fetchReviews(newReview.proId); // Gọi fetchReviews với proId từ newReview
        } catch (error) {
            console.error('Error submitting review:', error);            
        }
    };

    // Hàm để xử lý khi người dùng bấm vào input review
    const handleInputFocus = () => {
        if (newReview.ratingStart === 0) { // Nếu số sao hiện tại chưa được thiết lập
            setNewReview({ ...newReview, ratingStart: 5 });
        }
    };

    const handleStarClick = (rating) => {
        setNewReview({ ...newReview, ratingStart: rating, proId: product.proId }); // Gán proId vào newReview
    };


    const fetchReviews = async (proId) => {
        try {
            const response = await axios.get(`http://localhost:1010/api/product/list-review?proId=${proId}&page=${currentPage}&limit=${limit}`);

            // Kiểm tra dữ liệu trước khi cập nhật state
            if (response.data && Array.isArray(response.data.listReviews)) {
                setTotal(response.data.total)
                setReviews(response.data.listReviews);
                setTotalPage(response.data.totalPage);
            } else {
                console.warn('Unexpected data format:', response.data);
                setReviews([]); // Thiết lập lại reviews nếu không có dữ liệu
                setTotalPage(0); // Cập nhật totalPage
            }
        } catch (error) {
            console.error('Error fetching reviews:', error);
        }
    };



    useEffect(() => {
        if (product) {
            fetchReviews(product.proId); // Truyền proId vào fetchReviews
        }
    }, [currentPage, product]);

    const handlePageChange = (newPage) => {
        console.log(`Attempting to change to page: ${newPage}`);
        if (newPage > 0 && newPage <= totalPage) {
            setCurrentPage(newPage);
        }
    };

    const getPaginationNumbers = () => {
        const paginationNumbers = [];
        const maxButtons = 5;

        if (totalPage <= maxButtons) {
            for (let i = 1; i <= totalPage; i++) {
                paginationNumbers.push(i);
            }
        } else {
            paginationNumbers.push(1);

            if (currentPage > 3) {
                paginationNumbers.push('...');
            }

            const startPage = Math.max(2, currentPage - 1);
            const endPage = Math.min(totalPage - 1, currentPage + 1);

            for (let i = startPage; i <= endPage; i++) {
                paginationNumbers.push(i);
            }

            if (currentPage < totalPage - 2) {
                paginationNumbers.push('...');
            }

            paginationNumbers.push(totalPage);
        }

        return paginationNumbers;
    };
    const handleEdit = (reviewId, content, ratingStart) => {
        setEditingReviewId(reviewId);
        setNewContent(content);
        setNewRating(ratingStart);
    };

    const handleUpdate = async (reviewId, userId, proId) => {
        try {
            const token = getCookie('access_token');
            const req = {
                reviewId: reviewId,
                userId: userId, // Đảm bảo bạn trnuyền userId chính xác ở đây
                proId: proId,   // Đảm bảo bạn truyền proId chính xác ở đây
                content: newContent,
                ratingStart: newRating
            };

            const response = await axios.put('http://localhost:1010/api/review/update', req, {
                headers: {
                    'Authorization': `Bearer ${token}` // Thay thế `yourToken` bằng token thực tế
                }
            });

            console.log(response.data); // Xử lý phản hồi nếu cần            
            await fetchReviews(proId);
            setEditingReviewId(null);
        } catch (error) {
            console.error("Error updating review:", error);
            console.log(error.response.data); // In ra chi tiết lỗi
        }
    };
    const userIdFromToken = getUserIdFromToken(getCookie('access_token'));

    const handleDelete = async (reviewId, proId) => {
        try {
            const token = getCookie('access_token');
            if (!token) throw new Error("Token không tồn tại. Người dùng cần đăng nhập.");

            const userIdFromToken = getUserIdFromToken(token);
            const reqData = { reviewId: reviewId, userId: userIdFromToken };

            console.log("Deleting review with data:", reqData); // Log dữ liệu trước khi gửi

            const response = await axios.delete('http://localhost:1010/api/review/delete', {
                data: reqData,
                headers: { 'Authorization': `Bearer ${token}` },
            });

            console.log(response.data); // Xác nhận xóa thành công
            await fetchReviews(proId); // Truyền proId vào fetchReviews
        } catch (error) {
            console.error("Error deleting review:", error);
            if (error.response) {
                console.error('Error response:', error.response.data); // Log thêm phản hồi lỗi từ server
            }
        }
    };
    const [activeTab, setActiveTab] = useState('description');

    useEffect(() => {
        const fetchRecommendedProducts = async () => {
            const token = getCookie('access_token');
            const userId = getUserIdFromToken(token);
            if (userId) {
                try {
                    console.log(`Fetching recommended products for userId: ${userId}`);
                    const response = await axios.get(`http://localhost:1010/api/product/recommended/${userId}`);
                    console.log('API Response:', response.data); // Log the entire response

                    if (response.data && response.data.listRecommend) {
                        const currentProductName = product.proName; // Get the current product's name

                        // Đảo ngược danh sách `listRecommend`
                        const reversedList = [...response.data.listRecommend].reverse();

                        const uniqueRecommendedProducts = [];
                        const seenProductNames = new Set(); // To track unique product names

                        // Lặp qua danh sách đã đảo ngược để lọc sản phẩm
                        for (const recommendation of reversedList) {
                            if (recommendation.proName !== currentProductName && !seenProductNames.has(recommendation.proName)) {
                                uniqueRecommendedProducts.push(recommendation); // Add to unique products
                                seenProductNames.add(recommendation.proName); // Mark this product name as seen
                            }

                            // Dừng lại khi đã có đủ 3 sản phẩm
                            if (uniqueRecommendedProducts.length === 4) {
                                break; // Exit the loop if we have 3 unique products
                            }
                        }

                        // Đảm bảo danh sách có đủ 3 sản phẩm
                        if (uniqueRecommendedProducts.length < 4) {
                            for (const recommendation of reversedList) {
                                if (recommendation.proName !== currentProductName && !seenProductNames.has(recommendation.proName)) {
                                    uniqueRecommendedProducts.push(recommendation); // Add to unique products
                                    seenProductNames.add(recommendation.proName); // Mark this product name as seen
                                }

                                // Stop if we have collected 3 unique products
                                if (uniqueRecommendedProducts.length === 4) {
                                    break; // Exit the loop if we have 3 unique products
                                }
                            }
                        }

                        // Cập nhật state với danh sách sản phẩm đề xuất
                        setRecommendedProducts(uniqueRecommendedProducts);
                    } else {
                        console.warn('Unexpected response structure:', response.data);
                        setRecommendedProducts([]); // Reset to empty if structure is unexpected
                    }
                } catch (error) {
                    console.error('Error fetching recommended products:', error);
                }
            } else {
                console.warn('User ID is not available.');
            }
        };

        fetchRecommendedProducts();
    }, [product]); // Fetch recommended products when the product changes

    const handleProductRecommendClick = (product) => {
        console.log("Navigating to product with ID:", product.proId); // Debugging line
        navigate(`/product/${product.proId}`, { state: { product } });
    };

    // Hàm để render ngôi sao dựa trên rating
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

    return (
        <>
            <Navbar />
            <div className='proCon'>
                <svg xmlns="http://www.w3.org/2000/svg" width="1007" height="806" viewBox="0 0 1007 806" fill="rgba(221, 45, 80, 0.9)" strokeWidth="1px">
                    <path d="M895.13 -200.09C993.128 -123.54 1027.19 34.8807 995.227 155.053C961.104 283.328 791.957 326.814 724.549 441.159C672.668 529.164 712.072 652.411 652.921 735.705C593.052 820.012 497.272 882.109 396.133 903.623C296.755 924.763 200.417 851.204 98.8429 853.569C-8.75754 856.075 -107.398 932.771 -213.986 917.83C-326.749 902.023 -431.512 843.126 -516.473 767.316C-605.308 688.049 -699.869 591.657 -712.236 473.242C-724.872 352.252 -614.412 253.81 -582.356 136.461C-554.839 35.726 -606.302 -92.2929 -536.527 -169.986C-463.121 -251.721 -322.471 -213.92 -223.978 -262.585C-141.699 -303.238 -94.4871 -395.739 -9.50265 -430.385C81.1919 -467.358 186.919 -501.635 278.729 -467.524C371.889 -432.911 386.134 -289.071 477.305 -249.515C607.385 -193.078 783.386 -287.378 895.13 -200.09Z" fill="#DD2D50" fillOpacity="0.05" />
                </svg>
                <div className="product-detail-container" ref={productDetailRef}>
                    <div className="product-detail-main">
                        <div className="product-image-container">
                            {product.productImageResponseList.length > 0 && (
                                <>
                                    <div id='img1-container'>
                                        <img
                                            src={product.productImageResponseList[currentImageIndex].linkImage}
                                            alt={product.name}
                                            className="product-image"
                                        />
                                        <div className="navigation-buttons">
                                            <button className="arrow-button" onClick={handlePreviousImage}>
                                                <i className='ti-arrow-circle-left' />
                                            </button>
                                            <button className="arrow-button" onClick={handleNextImage}>
                                                <i className='ti-arrow-circle-right' />
                                            </button>
                                        </div>
                                    </div>

                                    <div style={{ display: 'flex', gap: '15px' }}>
                                        {product.productImageResponseList.map((image, index) => (
                                            <img
                                                key={index}
                                                src={image.linkImage}
                                                alt={product.name}
                                                className={`product-image-detail ${currentImageIndex === index ? 'active' : ''}`}
                                                onClick={() => handleDotClick(index)}
                                            />
                                        ))}
                                    </div>
                                </>
                            )}
                        </div>

                        <div className="product-info-container">
                            <h1>{product.proName}</h1>
                            <span className="product-rating">
                                {renderStars(averageRating)} {/* Hiển thị ngôi sao */}
                            </span>
                            <span className="product-category">Danh mục: {categoryName}</span>
                            <span className="product-price">Giá: {formattedPrice} VND</span>
                            <span className="product-stock">Còn lại: {stock > 0 ? stock : '0'} sản phẩm</span>

                            <div className="product-size">
                                <span>Chọn size:</span>
                                <div className="size-options">
                                    {['S', 'M', 'L'].map(size => (
                                        <button
                                            key={size}
                                            className={selectedSize === size ? 'size-option active' : 'size-option'}
                                            style={{ borderRadius: '6px' }}
                                            onClick={() => handleSizeChange(size)}
                                            disabled={!availableSizes.includes(size)} // Disable button if size is not available
                                        >
                                            {size}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div className="product-quantity">
                                <span>Số lượng sản phẩm:</span>
                                <input
                                    type="number"
                                    value={quantity}
                                    min="1"
                                    max={stock}
                                    onChange={handleQuantityChange}
                                    className="quantity-input"
                                />
                            </div>

                            <button className="add-to-cart-button" style={{ borderRadius: '6px' }} onClick={handleAddToCart}>Thêm vào giỏ hàng</button>
                            <button className="add-to-cart-button" style={{ marginBottom: '10px', borderRadius: '6px', backgroundColor: '#099494' }} onClick={handleBack}>Xem đồ uống khác</button>
                        </div>
                    </div>
                    {showSuccess && (
                        <div className="success-animation">
                            <div className="success-modal">
                                {/* <div className="success-icon">
                                    <div className="success-icon-circle">
                                        <svg className="checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                            <circle className="checkmark-circle" cx="26" cy="26" r="25" fill="none" />
                                            <path className="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
                                        </svg>
                                    </div>
                                </div> */}
                                <h3>Thêm vào giỏ hàng thành công!</h3>
                                <p>Đồ uống đã được thêm vào giỏ hàng của bạn.</p>
                            </div>
                        </div>
                    )}
                    {showLoginPrompt && (
                        <div className="login-modal">
                            <div className="login-modal-content">
                                <p>Bạn cần đăng nhập để có thể mua hàng.</p>
                                <a href="/login">Đăng nhập</a>
                                <button onClick={() => setShowLoginPrompt(false)}>Đóng</button>
                            </div>
                        </div>
                    )}
                    {showError && (
                        <div className="error-animation">
                            <div className="error-modal">
                                {/* <div className="error-icon">
                                    <div className="error-icon-circle">
                                        <svg className="cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                            <circle className="cross-circle" cx="26" cy="26" r="25" fill="none" />
                                            <path className="cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36" />
                                        </svg>
                                    </div>
                                </div> */}
                                <h3>Không thể thêm vào giỏ hàng!</h3>
                                <p>Số lượng trong kho chỉ còn {stock} sản phẩm.</p>
                            </div>
                        </div>
                    )}
                    {showErrorLogin && (
                        <div className="error-animation">
                            <div className="error-modal">
                                {/* <div className="error-icon">
                                    <div className="error-icon-circle">
                                        <svg className="cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                            <circle className="cross-circle" cx="26" cy="26" r="25" fill="none" />
                                            <path className="cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36" />
                                        </svg>
                                    </div>
                                </div> */}
                                <h3>Bạn cần phải đăng nhập!</h3>
                                <p>Vui lòng đăng nhập để đánh giá sản phẩm.</p>
                            </div>
                        </div>
                    )}

                    <svg id="img-phai" xmlns="http://www.w3.org/2000/svg" width="394" height="360" viewBox="0 0 394 360" fill="pink" stroke-width="1px" stroke="rgba(35, 48, 0, 0.40)">
                        <path d="M202.354 183.632C202.354 183.632 201.671 149.014 218.664 137.798C235.656 126.581 238.377 127.593 238.377 127.593C238.377 127.593 241.772 137.798 239.733 141.691C237.695 145.594 236.376 140.657 236.376 139.744C236.376 138.831 240.454 129.693 250.9 129.693C261.347 129.693 266.827 137.193 266.827 137.193C266.827 137.193 266.697 144.869 263.769 148.344C263.769 148.344 267.977 140.844 274.729 141.394C274.729 141.394 284.157 152.544 287.345 160.77C287.345 160.77 279.19 177.045 264.658 178.871C264.658 178.871 272.047 180.883 273.963 183.621C273.963 183.621 248.348 196.971 243.251 190.747C238.154 184.523 245.543 184.71 247.712 187.822C249.881 190.934 254.212 199.886 254.212 199.886C254.212 199.886 233.441 207.748 229.363 193.848L230.513 204.823C230.513 204.823 210.884 199.523 205.151 190.56C199.418 181.598 218.786 168.248 228.727 164.96C238.668 161.672 258.166 159.296 264.03 160.935M197.257 179.97C197.257 179.97 187.316 153.644 166.675 151.082C166.675 151.082 163.617 160.22 167.695 165.532C167.695 165.532 160.306 145.968 135.067 155.107C135.067 155.107 139.658 170.832 142.969 170.469C146.28 170.106 146.663 163.519 143.1 163.343C139.528 163.156 124.429 163.255 116.213 175.957C116.213 175.957 122.582 182.907 127.679 181.807C127.679 181.807 118.251 182.907 110.349 211.795C110.349 211.795 127.679 222.396 137.114 217.645C137.114 217.645 133.802 231.545 129.978 232.271C129.978 232.271 151.385 231.172 153.807 220.571C156.229 209.97 148.327 213.984 148.84 217.283C149.354 220.582 154.704 232.271 154.704 232.271C154.704 232.271 168.722 233.371 175.091 217.645C175.091 217.645 176.877 227.521 173.819 230.809C173.819 230.809 199.817 206.308 199.05 187.294C198.284 168.281 142.448 189.505 136.439 197.994M149.844 170.106C149.844 170.106 165.901 176.319 165.396 188.02M179.414 188.02C179.414 188.02 176.356 203.745 162.337 207.77M250.011 146.573C250.011 146.573 234.529 153.897 233.226 166.202M220.955 174.857C220.955 174.857 241.856 178.519 252.303 173.394M216.288 131.947C216.288 131.947 228.183 107.084 221.722 87.0915C221.722 87.0915 213.567 91.4792 213.567 95.3831C213.567 95.3831 220.365 55.8935 210.509 37.8478C210.509 37.8478 205.412 40.7729 204.73 47.1071C204.73 47.1071 190.113 16.393 184.342 12.973C184.342 12.973 181.115 21.2646 182.303 31.2607C182.303 31.2607 173.298 4.43952 162.935 1.03052C162.935 1.03052 159.026 11.0266 160.383 17.8556C160.383 17.8556 143.391 1.52537 137.612 7.37567C137.612 7.37567 132.515 27.1259 137.612 37.3639C137.612 37.3639 134.04 35.4175 131.158 38.1007C131.158 38.1007 128.437 63.9432 145.092 79.3057C145.092 79.3057 137.106 77.8432 136.424 81.2522C135.742 84.6612 139.145 102.465 157.156 113.682C157.156 113.682 148.833 114.166 148.319 116.123C148.319 116.123 159.532 142.944 165.817 146.595M199.97 165.103C199.97 165.103 163.947 61.7328 160.889 47.1071C157.831 32.4814 171.765 32.2394 175.329 43.94C181.744 64.9989 175.329 101.53 175.329 101.53M203.542 95.625C203.542 95.625 203.028 130.98 192.834 140.481M192.834 210.695C192.834 210.695 180.173 230.072 181.706 247.26C181.706 247.26 193.172 249.459 195.977 244.698C195.977 244.698 184.511 254.21 184.764 285.65C184.764 285.65 191.899 286.749 195.977 279.063C195.977 279.063 184.764 294.051 194.191 322.214C194.191 322.214 202.085 314.538 202.346 306.489C202.346 306.489 198.268 325.139 211.781 345.252C211.781 345.252 216.111 342.69 217.644 333.552C217.644 333.552 225.293 351.465 236.76 358.415C236.76 358.415 248.226 345.978 247.206 319.289C247.206 319.289 252.817 324.413 258.166 325.876C258.166 325.876 265.302 307.962 259.699 284.55C259.699 284.55 266.068 291.137 268.874 288.938C268.874 288.938 264.796 250.174 251.544 242.861C251.544 242.861 259.193 243.961 260.719 241.036C260.719 241.036 252.533 207.759 237.273 204.834M203.542 196.07C203.542 196.07 215.115 209.596 223.646 254.21C232.176 298.824 235.234 312.35 231.923 318.574C228.612 324.798 219.944 324.424 220.457 314.912C220.971 305.4 221.477 269.198 231.164 261.16M220.955 219.108C220.955 219.108 211.014 228.983 208.723 247.997C206.431 267.01 207.19 276.159 207.19 276.159M227.332 104.642C227.332 104.642 233.962 83.1876 248.739 73.8073C248.739 73.8073 252.817 76.0067 253.33 89.2248C253.33 89.2248 260.979 60.7651 278.815 58.5658C296.657 56.3664 299.716 62.9535 299.716 62.9535C299.716 62.9535 308.637 85.6729 299.716 81.3841C290.794 77.0954 305.326 65.1528 312.967 65.8786C320.609 66.6044 343.043 75.6328 354.256 95.625L349.159 111.955C349.159 111.955 362.925 104.642 367.002 106.105C371.08 107.568 391.467 144.385 393 165.103C393 165.103 377.709 190.956 356.295 191.682C356.295 191.682 353.743 201.909 358.334 209.222C358.334 209.222 313.052 216.777 322.993 203.614C332.933 190.45 324.526 224.826 327.752 232.282C327.752 232.282 298.949 240.673 287.353 224.584L282.892 239.21C282.892 239.21 267.402 236.538 257.408 219.097M369.547 151.082C333.355 134.631 292.572 148.663 292.572 148.663C292.572 148.663 296.144 178.497 326.219 185.095M335.394 110.493C335.394 110.493 319.084 120.775 317.045 147.09M287.353 92.205C287.353 92.205 276.262 100.771 271.165 127.571M152.397 134.51C152.397 134.51 127.165 99.034 103.62 115.111L107.667 125.558C107.667 125.558 93.0045 112.274 70.2563 119.576C47.5081 126.878 47.0022 142.713 47.0022 142.713C47.0022 142.713 54.9273 162.617 58.1234 152.28C61.3195 141.944 40.932 151.291 35.3752 155.25C29.8108 159.208 15.6622 175.726 12.627 188.911C12.627 188.911 29.3126 190.219 29.8185 192.859C30.3243 195.498 4.54099 204.735 1 239.386C1 239.386 9.59188 252.252 23.7482 257.531C23.7482 257.531 35.3752 255.683 36.3869 250.009C36.3869 250.009 35.7124 261.93 33.1832 265.119C33.1832 265.119 39.2458 274.026 58.6293 277.765C58.6293 277.765 82.6267 269.616 75.4221 264.063C68.2175 258.51 73.7206 282.834 69.7504 289.641C69.7504 289.641 81.3774 301.518 104.969 296.24C104.969 296.24 116.09 291.621 120.137 280.624C120.137 280.624 121.654 294.26 120.305 298.879C120.305 298.879 158.222 289.202 161.088 262.798C163.955 236.395 164.629 267.197 164.629 270.936C164.629 270.936 182.487 257.96 184.173 244.753M106.517 198.742C106.517 198.742 55.8854 199.226 19.8623 234.339M34.4708 185.095C34.4708 185.095 68.1179 190.857 78.3116 210.893M104.141 210.893C104.141 210.893 101.765 228.488 83.7534 252.384M80.3504 155.349C80.3504 155.349 89.862 152.907 104.141 155.349C118.412 157.79 121.47 164.091 121.47 164.091" stroke="black" stroke-miterlimit="10" />
                    </svg>

                    <div className="product-detail-container fade-in">
                        {/* Thanh điều hướng */}
                        <div className="tab-buttons">
                            <button
                                className={`tab-button ${activeTab === 'description' ? 'active' : ''}`}
                                onClick={() => setActiveTab('description')}
                            >
                                Mô Tả
                            </button>
                            <button
                                className={`tab-button ${activeTab === 'reviews' ? 'active' : ''}`}
                                onClick={() => setActiveTab('reviews')}
                            >
                                Đánh Giá ({total})
                            </button>
                        </div>

                        {/* Hiển thị Mô Tả */}
                        {activeTab === 'description' && (
                            <div className="product-description-container fade-in">
                                {/* <h2>Mô tả</h2> */}
                                <p className='des-product' >
                                    <div style={{ marginTop: '20px' }}>{product.description}</div>
                                </p>
                            </div>
                        )}


                        {/* Hiển thị Đánh Giá */}
                        {activeTab === 'reviews' && (
                            <div className="product-list-reviews fade-in">
                                {/* <h2 className='h2-maybe'>Đánh giá sản phẩm:</h2> */}
                                <div className="reviews-container">
                                    {reviews.length > 0 ? reviews.map((review) => (
                                        <div className="review-item" key={review.reviewId}>
                                            <div className="review-header">
                                                <span className="review-fullname">{review.fullName}</span>
                                                <span className="review-date">
                                                    {editingReviewId === review.reviewId ? new Date().toLocaleDateString() : review.dateCreated}
                                                </span>
                                                {userIdFromToken === String(review.userId) && (
                                                    <>
                                                        <span className="edit-icon" onClick={() => handleEdit(review.reviewId, review.content, review.ratingStart)}>
                                                            <i className="ti-pencil"></i>
                                                        </span>
                                                        <span className="delete-icon" onClick={() => handleDelete(review.reviewId, review.proId)}>
                                                            <i className="ti-trash"></i>
                                                        </span>
                                                    </>
                                                )}
                                            </div>
                                            <div className="review-stars">
                                                {editingReviewId === review.reviewId ? (
                                                    <div className="rating-stars">
                                                        {[1, 2, 3, 4, 5].map((star) => (
                                                            <span
                                                                key={star}
                                                                className={`star ${newRating >= star ? 'filled' : ''}`}
                                                                onClick={() => setNewRating(star)}
                                                                style={{ cursor: 'pointer' }}
                                                            >
                                                                ★
                                                            </span>
                                                        ))}
                                                    </div>
                                                ) : (
                                                    Array.from({ length: review.ratingStart }, (_, index) => (
                                                        <span key={index} className="star filled">★</span>
                                                    ))
                                                )}
                                            </div>
                                            {editingReviewId === review.reviewId ? (
                                                <div>
                                                    <input
                                                        type="text"
                                                        value={newContent}
                                                        onChange={(e) => setNewContent(e.target.value)}
                                                    />
                                                    <button className="review-update-btn" onClick={() => handleUpdate(review.reviewId, userId, review.proId)}>Cập nhật</button>
                                                </div>
                                            ) : (
                                                <div className="review-content">{review.content}</div>
                                            )}
                                        </div>
                                    )) : (
                                        <div style={{ textAlign: 'center' }}>Không có đánh giá nào.</div>
                                    )}
                                </div>

                                <div className="menu-product-pagination" style={{ marginBottom: '20px' }}>
                                    {/* <span className={`pagination-arrow ${currentPage === 1 ? 'disabled' : ''}`} onClick={() => handlePageChange(currentPage - 1)}>
              <i className='ti-arrow-left' id='arrow' />
            </span> */}
                                    {Array.from({ length: totalPage }, (_, index) => (
                                        <span key={index + 1} className={`pagination-dot ${currentPage === index + 1 ? 'active' : ''}`} onClick={() => handlePageChange(index + 1)}>
                                            •
                                        </span>
                                    ))}
                                    {/* <span className={`pagination-arrow ${currentPage === totalPages ? 'disabled' : ''}`} onClick={() => handlePageChange(currentPage + 1)}>
              <i className='ti-arrow-right' id='arrow' />
            </span> */}
                                </div>
                            </div>
                        )}

                        {/* Phần đánh giá sản phẩm */}
                        {activeTab === 'reviews' && (
                            <div className="product-rating-container fade-in">
                                <span className="product-rating">
                                    {"★".repeat(product.rate)}{"☆".repeat(5 - product.rate)}
                                </span>

                                {isLoggedIn ? (
                                    <div className="review-container">
                                        <div className="rating-stars">
                                            {[1, 2, 3, 4, 5].map((star) => (
                                                <span
                                                    key={star}
                                                    className={`star ${newReview.ratingStart >= star ? 'filled' : ''}`}
                                                    onClick={() => handleStarClick(star)}
                                                >
                                                    ★
                                                </span>
                                            ))}
                                        </div>
                                        <input
                                            type="text"
                                            placeholder="Hãy cho chúng tôi biết cảm nhận của bạn về sản phẩm"
                                            value={newReview.content}
                                            onFocus={handleInputFocus}
                                            onChange={(e) => setNewReview({ ...newReview, content: e.target.value })}
                                            id="input-text-review"
                                        />
                                        <div className="send-button" onClick={() => handleSubmitReview(newReview.proId)}>
                                            <i className="far fa-paper-plane"></i>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="login-prompt">
                                        <p>Vui lòng <a href="/login">đăng nhập</a> để viết đánh giá.</p>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>


                    <h2 className="fade-in h2-maybe">Có thể bạn sẽ thích</h2>
                    <div className="related-products fade-in" style={{ display: 'flex', flexDirection: 'row' }}>
                        {recommendedProducts.length > 0 ? (
                            recommendedProducts.map((recommendedProduct, index) => (
                                <ProductRecommend
                                    key={index}
                                    product={recommendedProduct}
                                    averageRating={productRatings[recommendedProduct.proId] || 0}
                                    onClick={() => handleProductRecommendClick(recommendedProduct)}
                                />
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
