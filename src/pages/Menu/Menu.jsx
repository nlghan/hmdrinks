import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import ProductCard from '../../components/Card/ProductCard';
import backgroundImage from '../../assets/img/5.jpg'; // Path to your background image
import { Autocomplete, TextField } from '@mui/material'; // Import Autocomplete and TextField
import "./Menu.css";
import { useNavigate } from 'react-router-dom';
import LoadingAnimation from "../../components/Animation/LoadingAnimation.jsx";
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthProvider'; // Import useAuth
import { useLocation } from "react-router-dom";


const useDebounce = (value, delay) => {
    const [debouncedValue, setDebouncedValue] = useState(value);

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedValue(value);
        }, delay);

        return () => {
            clearTimeout(handler);
        };
    }, [value, delay]);

    return debouncedValue;
};

const Menu = () => {
    const location = useLocation();
    const [showError, setShowError] = useState(false);
    const { selectedCategoryId: selectedCategoryIdFromHome } = location.state || {}; // Get selectedCategoryId from Home
    const { isLoggedIn } = useAuth(); // Get login status from useAuth
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(1);
    const [currentCategoryPage, setCurrentCategoryPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [limit] = useState(8);
    const [categoryLimit] = useState(10);
    const [selectedCategoryId, setSelectedCategoryId] = useState(selectedCategoryIdFromHome || null); // Set initial value of selectedCategoryId
    const [searchTerm, setSearchTerm] = useState('');
    const [favoritedProIds, setFavoritedProIds] = useState([]); // State for favorited product IDs
    const navigate = useNavigate();
    const { addToCart } = useCart();
    const [showLoginPrompt, setShowLoginPrompt] = useState(false);
    const [showSuccess, setShowSuccess] = useState(false);

    const [productRatings, setProductRatings] = useState({}); // State để lưu trữ rating cho từng sản phẩm

    const [sortOption, setSortOption] = useState("");
    const [currentFilterType, setCurrentFilterType] = useState('');


    useEffect(() => {
        window.scrollTo(0, 0); // Scroll to the top of the page
    }, []); // Only when the component is mounted


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

    const userId = isLoggedIn ? getUserIdFromToken(getCookie('access_token')) : null; // Get userId from token if logged in

    // Fetch products and their prices and sizes from API
    useEffect(() => {
        const fetchProducts = async () => {
            setLoading(true);
            try {
                const url = searchTerm
                    ? `http://localhost:1010/api/product/search?keyword=${encodeURIComponent(searchTerm)}&page=${currentPage}&limit=${limit}`
                    : selectedCategoryId
                        ? `http://localhost:1010/api/cate/view/${selectedCategoryId}/product?page=${currentPage}&limit=${limit}`
                        : `http://localhost:1010/api/product/list-product?page=${currentPage}&limit=${limit}`;

                const response = await fetch(url);
                const data = await response.json();
                const productList = searchTerm ? data.productResponseList : (selectedCategoryId ? data.responseList : data.productResponses);

                // Lấy chi tiết từ listProductVariants ở vị trí 0 và log ra console
                const productsWithDetails = productList.map((product) => {
                    const firstVariant = product.listProductVariants?.[0] || {};
                    const variantDetails = {
                        size: firstVariant.size,
                        price: firstVariant.price,
                        stock: firstVariant.stock
                    };

                    console.log("Chi tiết sản phẩm:", {
                        id: product.id,
                        name: product.name,
                        price: variantDetails.price || 'Không có giá'
                    });

                    return { ...product, variantDetails };
                });

                setProducts(productsWithDetails);
                setTotalPages(data.totalPage);
            } catch (error) {
                console.error('Error fetching products:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchProducts();
    }, [currentPage, selectedCategoryId, searchTerm, currentFilterType]); // Cập nhật khi bộ lọc thay đổi





    // Fetch categories from API
    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const response = await fetch('http://localhost:1010/api/cate/list-category?page=1&limit=100'); // Fetch all categories
                const data = await response.json();
                setCategories(data.categoryResponseList); // Set the categories from the API response
            } catch (error) {
                console.error('Error fetching categories:', error);
            }
        };

        fetchCategories();
    }, []); // Fetch categories only once when the component mounts

    // Handle product page change
    const handleProductPageChange = (page) => {
        setCurrentPage(page);
        handleFilterChange(currentFilterType, page); // Gọi lại API với bộ lọc hiện tại
    };


    // Handle category page change
    const handleCategoryPageChange = (page) => {
        setCurrentCategoryPage(page);
    };

    // Handle category selection
    const handleCategorySelect = (categoryId) => {
        setSelectedCategoryId(categoryId); // Set the selected category
        setCurrentPage(1); // Reset to the first page when a category is selected
    };

    // Handle search input change from Autocomplete
    const handleSearchChange = (event, value) => {
        setSearchTerm(value);
    };


    // Calculate the categories to display for the current category page
    const displayedCategories = categories.slice((currentCategoryPage - 1) * categoryLimit, currentCategoryPage * categoryLimit);

    // Debounce the search term for better performance
    const debouncedSearchTerm = useDebounce(searchTerm, 300);

    // Filtered products based on the search term
    const filteredProducts = debouncedSearchTerm
        ? products.filter(product => product.proName.toLowerCase().includes(debouncedSearchTerm.toLowerCase()))
        : products;

    // Check if there are any products available for the selected category
    const hasProducts = filteredProducts.length > 0;

    // Handle adding the product to the cart with size and stock
    const handleAddToCart = (product) => {
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token); // Hàm lấy userId từ token
        const quantity = 1; // Đặt số lượng mặc định là 1
        const { proId, proName, variantDetails, productImageResponseList } = product;

        // Kiểm tra nếu không có variant hoặc không có giá
        if (!variantDetails || !variantDetails.price) {
            console.error('Không thể thêm sản phẩm do thiếu thông tin chi tiết.');
            return;
        }

        const { size, price, stock } = variantDetails;

        // Kiểm tra xem userId có tồn tại hay không
        if (!userId) {
            setShowLoginPrompt(true); // Hiện thông báo yêu cầu đăng nhập
            return;
        } else {
            // Nếu số lượng vượt quá hàng tồn kho, ngăn việc thêm vào giỏ hàng
            if (quantity > stock) {
                setShowError(true);
                setTimeout(() => {
                    setShowError(false);
                }, 2000);
                return;
            }

            // Thêm sản phẩm vào giỏ hàng
            addToCart({
                productId: proId,
                name: proName,
                price: price,
                size: size,
                quantity: quantity,
                image: productImageResponseList?.[0]?.linkImage || backgroundImage,
            });
        }
    };


    const handleProductCardClick = (product) => {
        navigate(`/product/${product.proId}`, { state: { product } });
    };

    const [isFirstLoad, setIsFirstLoad] = useState(true);
    useEffect(() => {
        // Set `isFirstLoad` to false after the first render
        setIsFirstLoad(false);
    }, []);

    // Hàm fetchRating để lấy số rating trung bình cho từng sản phẩm
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

            // Lưu trữ rating cho từng proId
            ratingData.list.forEach(avgRating => {
                ratings[avgRating.proId] = avgRating.avgRating;
            });

            setProductRatings(ratings); // Cập nhật state với rating
        } catch (error) {
            console.error('Failed to fetch product ratings:', error);
        }
    };

    useEffect(() => {
        fetchRating(); // Gọi fetchRating khi component được mount
    }, []);


    const [sortOrder, setSortOrder] = useState(() => {
        // Lấy giá trị từ localStorage nếu có, nếu không thì gán giá trị mặc định là 0
        const savedSortOrder = localStorage.getItem('sortOrder');
        return savedSortOrder ? parseInt(savedSortOrder) : 0;
    });

    const handleFilterChange = async (filterType, page = 1, limit = 8) => {
        setCurrentFilterType(filterType); // Cập nhật bộ lọc hiện tại

        let newSortOrder;
        let filterCode;

        switch (filterType) {
            case 'priceAsc':
                filterCode = [];
                newSortOrder = 1; // Sắp xếp tăng dần
                break;
            case 'priceDesc':
                filterCode = [];
                newSortOrder = 2; // Sắp xếp giảm dần
                break;
            case 'newest':
                filterCode = [];
                newSortOrder = 3; // Sắp xếp theo ngày tạo mới nhất
                break;
            case 'ratingAsc':
                filterCode = [];
                newSortOrder = 5; // Sắp xếp đánh giá tăng dần
                break;
            case 'ratingDesc':
                filterCode = [];
                newSortOrder = 4; // Sắp xếp đánh giá giảm dần
                break;
            case 'hot':
                filterCode = [];
                newSortOrder = 6;
                break;
            default:
                break;
        }

        // Chỉ lưu lại sortOrder vào localStorage nếu nó thay đổi
        if (newSortOrder !== sortOrder) {
            localStorage.setItem('sortOrder', newSortOrder);
            setSortOrder(newSortOrder);
        }

        // Gọi lại API khi bộ lọc thay đổi hoặc khi người dùng chuyển trang
        try {
            const response = await fetch('http://localhost:1010/api/product/filter-product', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    c: selectedCategoryId || -1,
                    p: filterCode,
                    o: newSortOrder,
                    page: page,
                    limit: limit
                }),
            });

            if (response.ok) {
                const data = await response.json();

                let products = [];
                if (newSortOrder === 6) {
                    // Trường hợp "hot" response có "varId", "size", "price", ...
                    products = data.productResponseList.map(product => {
                        // Truyền ảnh cho product detail trong trường hợp "hot"
                        const images = product.productImageResponseList && product.productImageResponseList.length > 0
                            ? product.productImageResponseList.map(img => img.linkImage)
                            : [backgroundImage]; // Fallback if no images

                        // Log link ảnh để kiểm tra
                        console.log(`Product ID: ${product.proId}, Images:, ${images} , ProSize: ${product.size}`);

                        // Đưa size, price, quantity vào variantDetails
                        const variantDetails = {
                            size: product.size || 'Không có kích thước',
                            price: product.price || 'Không có giá',
                            stock: product.quantity || 0
                        };

                        return {
                            proId: product.proId,
                            proName: product.proName,
                            variantDetails, // Thêm variantDetails vào sản phẩm
                            avgRating: product.avgRating,
                            totalSell: product.totalSell,
                            productImageResponseList: images, // Gán mảng link ảnh vào đối tượng sản phẩm
                        };
                    });
                } else {
                    // Trường hợp các filter khác
                    products = await Promise.all(data.productResponseList.map(async (product) => {
                        let variants = product.listProductVariants;
                        if (newSortOrder === 1) {
                            variants = [...variants].reverse(); // Đảo ngược phiên bản sản phẩm nếu cần
                        }

                        // Lấy thông tin của phiên bản đầu tiên sau khi đảo ngược (nếu có)
                        const firstVariant = variants[0] || {}; // Nếu không có variant thì tạo đối tượng rỗng

                        const images = product.productImageResponseList && product.productImageResponseList.length > 0
                            ? product.productImageResponseList.map(img => img.linkImage)
                            : [backgroundImage]; // Fallback if no images

                        return {
                            ...product,
                            variantDetails: {
                                size: firstVariant.size || 'Không có kích thước',
                                price: firstVariant.price || 'Không có giá',
                                stock: firstVariant.stock || 0
                            },
                            productImageResponseList: images, // Gán mảng link ảnh vào đối tượng sản phẩm
                        };
                    }));
                }

                // Cập nhật danh sách sản phẩm với thông tin ảnh
                setProducts(products);
                setTotalPages(data.totalPages);
                setCurrentPage(data.currentPage);


            } else {
                console.error("Error fetching filtered products");
            }
        } catch (error) {
            console.error("Error:", error);
        }
    };


    useEffect(() => {
        // Khi component load, gọi lại dữ liệu với sortOrder đã lưu từ localStorage
        if (sortOrder !== null) {
            handleFilterChange(currentFilterType, currentPage);
        }
    }, [sortOrder, selectedCategoryId, currentPage]);






    return (
        <>
            <Navbar currentPage={"Thực đơn"} />
            <div
                style={{
                    position: 'relative', // Position relative for the container
                    minHeight: '100vh',
                    display: 'flex'
                }}
            >
                {/* Background with overlay */}
                <div
                    style={{
                        background: `url(${backgroundImage}) no-repeat center center fixed`,
                        backgroundSize: 'cover',
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        width: '100%',
                        height: '100%',
                        opacity: 0.25, // Background opacity
                        zIndex: -1,
                    }}
                ></div>

                {/* Menu content */}
                <div className={`container-menu ${isFirstLoad ? 'slide-left' : ''}`}>
                    <div className="overlay-menu">
                        <div className='menu-category'>

                            {/* MUI Autocomplete for product names */}
                            <Autocomplete
                                freeSolo
                                options={products.map((product) => product.proName)}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="🔍 Bạn muốn uống gì?"
                                        variant="outlined"
                                        margin="normal"
                                        sx={{
                                            alignItems: 'center',
                                            width: '80%',
                                            marginLeft: '10px',
                                            borderRadius: '10px',
                                            backgroundColor: '#E15959', // Set background color to red
                                            marginBottom: '20px', // Spacing below the input
                                            '& .MuiOutlinedInput-root': {
                                                padding: 0, // Remove padding from the root
                                                '& fieldset': {
                                                    border: 'none', // Remove default fieldset border
                                                },
                                                '&:hover fieldset': {
                                                    border: 'none', // No border on hover
                                                },
                                                '&.Mui-focused fieldset': {
                                                    border: 'none', // No border when focused
                                                },
                                                '& input': {
                                                    color: 'white', // Change text color to white
                                                    padding: '10px 0', // Add vertical padding for better alignment
                                                    height: '1.5em', // Adjust height as needed to fit the design
                                                },
                                            },
                                            '& .MuiInputLabel-root': {
                                                color: 'white', // Change label color to white
                                                paddingBottom: '10px', // Remove padding from the label
                                                lineHeight: '0.8', // Center the text vertically
                                                opacity: 1, // Fully opaque by default
                                                transition: 'opacity 0.2s ease', // Add a smooth transition
                                            },
                                            '& .MuiInputLabel-root.Mui-focused': {
                                                opacity: 0, // Make label disappear when focused
                                                color: 'white', // Change label color when focused
                                            },
                                        }}
                                    />
                                )}
                                onChange={(event, newValue) => {
                                    setSearchTerm(newValue);
                                }}
                            />

                            <h2 className='menu-product-h2'>DANH MỤC SẢN PHẨM</h2>
                            <div className="menu-category-pagination">
                                {/* Pagination Dots */}
                                {Array.from({ length: Math.ceil(categories.length / categoryLimit) }, (_, index) => (
                                    <span
                                        key={index + 1}
                                        className={`pagination-cate-dot ${currentCategoryPage === index + 1 ? 'active' : ''}`}
                                        onClick={() => handleCategoryPageChange(index + 1)}
                                    >
                                        •
                                    </span>
                                ))}
                            </div>
                            <ul className="menu-product-category" style={{ listStyle: 'none', paddingLeft: '0px' }}>
                                {currentCategoryPage === 1 && (
                                    <li
                                        onClick={() => handleCategorySelect(null)}
                                        className={!selectedCategoryId ? 'active-category' : ''}
                                    >
                                        ❤️ Tất cả
                                    </li>
                                )}
                                {displayedCategories.map((category) => (
                                    <li
                                        key={category.cateId}
                                        onClick={() => handleCategorySelect(category.cateId)}
                                        className={selectedCategoryId === category.cateId ? 'active-category' : ''}
                                    >
                                        ❤️ {category.cateName}
                                    </li>
                                ))}
                            </ul>

                        </div>
                    </div>
                </div>

                {/* Product listing */}
                <div className="product-listing">
                    <div className="filter">
                        <div className="filter">
                            <select
                                onChange={(e) => handleFilterChange(e.target.value)}
                                value={currentFilterType} // Controlled component using value prop
                                style={{
                                    padding: '8px',
                                    borderRadius: '5px',
                                    backgroundColor: '#E15959',
                                    color: 'white',
                                    border: 'none',
                                    outline: 'none',
                                    cursor: 'pointer',
                                    marginBottom: '20px'
                                }}
                            >
                                <option value="" disabled>Chọn bộ lọc</option> {/* Removed selected */}
                                <option value="hot">Bán chạy nhất</option>
                                <option value="priceAsc">Giá thấp đến cao</option>
                                <option value="priceDesc">Giá cao đến thấp</option>
                                <option value="newest">Ngày mới nhất</option>
                                <option value="ratingAsc">Rating thấp đến cao</option>
                                <option value="ratingDesc">Rating cao đến thấp</option>
                            </select>

                        </div>
                        {hasProducts && (
                            <div className="menu-product-pagination">
                                <span
                                    className={`pagination-arrow ${currentPage === 1 ? 'disabled' : ''}`}
                                    onClick={() => currentPage > 1 && handleProductPageChange(currentPage - 1)}
                                >
                                    {'<'}
                                </span>

                                {Array.from({ length: totalPages }, (_, index) => (
                                    <span
                                        key={index + 1}
                                        className={`pagination-dot ${currentPage === index + 1 ? 'active' : ''}`}
                                        onClick={() => handleProductPageChange(index + 1)}
                                    >
                                        •
                                    </span>
                                ))}

                                <span
                                    className={`pagination-arrow ${currentPage === totalPages ? 'disabled' : ''}`}
                                    onClick={() => currentPage < totalPages && handleProductPageChange(currentPage + 1)}
                                >
                                    {'>'}
                                </span>
                            </div>


                        )}


                    </div>

                    {loading ? (
                        <LoadingAnimation />
                    ) : (
                        <div className="products zoomIn">
                            {hasProducts ? (
                                filteredProducts.map((product) => {
                                    // Lấy biến thể đầu tiên từ listProductVariants nếu có
                                    const firstVariant = product.listProductVariants?.[0] || {};

                                    return (
                                        <ProductCard
                                            key={product.varId}
                                            className={"zoom-in"}
                                            product={{
                                                proId: product.proId,
                                                name: product.proName,
                                                size: product.variantDetails?.size || firstVariant.size || product.size,
                                                price: product.variantDetails?.price || firstVariant.price || product.price || 'N/A',
                                                image: product.productImageResponseList?.[0]?.linkImage || product.productImageResponseList?.[0] || backgroundImage,
                                                averageRating: productRatings[product.proId] || 0,
                                                totalSell: product.totalSell || ''
                                            }}
                                            isFavorited={favoritedProIds.includes(product.proId)}
                                            onClick={() => handleProductCardClick(product)}
                                            onAddToCart={() => handleAddToCart(product)}
                                        />
                                    );
                                })
                            ) : (
                                <p>No products found matching your search.</p>
                            )}
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
                                <p>Số lượng trong kho đã hết.</p>
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



                </div>
            </div>
            <Footer />
        </>
    );
};

export default Menu;
