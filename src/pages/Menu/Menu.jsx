import React, { useEffect, useState, useCallback } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import ProductCard from '../../components/Card/ProductCard';
import backgroundImage from '../../assets/img/5.jpg';
import { Autocomplete, TextField } from '@mui/material';
import "./Menu.css";
import { useNavigate } from 'react-router-dom';
import LoadingAnimation from "../../components/Animation/LoadingAnimation.jsx";
import axios from 'axios';
import Cookies from 'js-cookie';

const Menu = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [searchOptions, setSearchOptions] = useState([]); // Search options for Autocomplete
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [limit] = useState(8);
    const [categoryLimit] = useState(10);
    const [selectedCategoryId, setSelectedCategoryId] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearchTerm, setDebouncedSearchTerm] = useState(''); // Debounced search term for optimization
    const navigate = useNavigate();

    // Fetch search options (autocomplete suggestions)
    const fetchSearchOptions = useCallback(async (keyword) => {
        try {
            const token = Cookies.get('access_token');
            if (!token) {
                throw new Error("Bạn cần đăng nhập để xem thông tin này.");
            }

            const apiUrl = `http://localhost:1010/api/product/search?keyword=${encodeURIComponent(keyword)}&page=1&limit=10`;
            const response = await axios.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.data && response.data.productResponseList) {
                setSearchOptions(response.data.productResponseList);
            } else {
                setSearchOptions([]);
            }
        } catch (err) {
            console.error("Lỗi khi lấy gợi ý tìm kiếm:", err);
            setSearchOptions([]);
        }
    }, []);

    // Fetch products from API based on search term or category
    const fetchProducts = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const token = Cookies.get('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                setLoading(false);
                return;
            }

            let apiUrl;
            if (debouncedSearchTerm && debouncedSearchTerm.trim() !== '') {
                // Fetch products based on the search term
                apiUrl = `http://localhost:1010/api/product/search?keyword=${encodeURIComponent(debouncedSearchTerm)}&page=${currentPage}&limit=${limit}`;
            } else if (selectedCategoryId) {
                // Fetch products by category if one is selected
                apiUrl = `http://localhost:1010/api/cate/view/${selectedCategoryId}/product?page=${currentPage}&limit=${limit}`;
            } else {
                // Fetch all products if no search term or category
                apiUrl = `http://localhost:1010/api/product/list-product?page=${currentPage}&limit=${limit}`;
            }

            const response = await axios.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`
                }
            });

            let productData;
            if (response.data && response.data.productResponseList) {
                productData = response.data.productResponseList;
            } else if (response.data && response.data.productResponses) {
                productData = response.data.productResponses;
            } else if (response.data && response.data.responseList) {
                productData = response.data.responseList;
            } else {
                setProducts([]);
                return;
            }

            // Fetch price and size for each product and update the product list with this data
            const productsWithDetails = await Promise.all(productData.map(async (product) => {
                try {
                    const variantResponse = await fetch(`http://localhost:1010/api/product/variants/${product.proId}`);
                    const variantData = await variantResponse.json();
                    const variant = variantData.responseList[0]; // Get the first variant

                    // Get the price and size from the first variant or set defaults
                    const price = variant?.price || 'N/A';
                    const size = variant?.size || 'N/A'; // Assuming 'size' is available in the variant

                    return { ...product, price, size };
                } catch (variantError) {
                    console.error(`Error fetching variant for product ${product.proId}:`, variantError);
                    return { ...product, price: 'N/A', size: 'N/A' }; // Handle error by setting defaults
                }
            }));

            setProducts(productsWithDetails);
            setTotalPages(response.data.totalPage || 1);
        } catch (err) {
            console.error("Lỗi khi lấy danh sách sản phẩm:", err);
            setError("Không thể lấy danh sách sản phẩm.");
        } finally {
            setLoading(false);
        }
    }, [debouncedSearchTerm, selectedCategoryId, currentPage]);


    // Fetch product variants for additional product data
    const fetchProductVariants = async (productId) => {
        try {
            const response = await axios.get(`http://localhost:1010/api/product/variants/${productId}`);
            return response.data.responseList || [];
        } catch (error) {
            console.error(`Lỗi khi lấy biến thể sản phẩm ${productId}:`, error);
            return [];
        }
    };

    // Fetch product images
    const fetchProductImages = async (productId) => {
        try {
            const response = await axios.get(`http://localhost:1010/api/product/images/${productId}`);
            return response.data.responseList || [];
        } catch (error) {
            console.error(`Lỗi khi lấy hình ảnh sản phẩm ${productId}:`, error);
            return [];
        }
    };

    // Fetch categories
    const fetchCategories = useCallback(async () => {
        try {
            const response = await axios.get('http://localhost:1010/api/cate/list-category?page=1&limit=100');
            setCategories(response.data.categoryResponseList);
        } catch (error) {
            console.error('Lỗi khi lấy danh mục:', error);
        }
    }, []);

    // Debounce search term for optimization
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedSearchTerm(searchTerm);
        }, 500);

        return () => {
            clearTimeout(handler);
        };
    }, [searchTerm]);

    useEffect(() => {
        fetchCategories();
    }, [fetchCategories]);

    useEffect(() => {
        fetchProducts();
    }, [debouncedSearchTerm, fetchProducts]);

    useEffect(() => {
        if (debouncedSearchTerm) {
            fetchSearchOptions(debouncedSearchTerm);
        } else {
            setSearchOptions([]);
        }
    }, [debouncedSearchTerm, fetchSearchOptions]);

    const handleSearchChange = (event, value) => {
        setSearchTerm(value);
        setCurrentPage(1);
    };

    const handleCategorySelect = (categoryId) => {
        setSelectedCategoryId(categoryId);
        setCurrentPage(1);
        setSearchTerm(''); // Clear the search term when selecting a category
    };

    const handleProductPageChange = (page) => {
        setCurrentPage(page);
    };

    const handleProductCardClick = (product) => {
        navigate(`/product/${product.proId}`, { state: { product } });
    };

    const displayedCategories = categories.slice(
        (currentPage - 1) * categoryLimit,
        currentPage * categoryLimit
    );

    const hasProducts = products.length > 0;

    return (
        <>
            <Navbar />
            <div
                style={{
                    position: 'relative',
                    minHeight: '100vh',
                    display: 'flex'
                }}
            >
                <div
                    style={{
                        background: `url(${backgroundImage}) no-repeat center center fixed`,
                        backgroundSize: 'cover',
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        width: '100%',
                        height: '100%',
                        opacity: 0.25,
                        zIndex: -1,
                    }}
                ></div>

                <div className="container-menu">
                    <div className="overlay-menu">
                        <div className='menu-category'>

                            <Autocomplete
                                freeSolo
                                options={searchOptions.map((option) => option.proName)}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="🔍 Bạn muốn uống gì?"
                                        variant="outlined"
                                        margin="normal"
                                        sx={{
                                            width: '80%',
                                            marginLeft: '10px',
                                            backgroundColor: '#E15959',
                                            marginBottom: '20px',
                                            '& .MuiOutlinedInput-root': {
                                                '& fieldset': { border: 'none' },
                                                '& input': { color: 'white', padding: '10px 0' }
                                            },
                                            '& .MuiInputLabel-root': { color: 'white' }
                                        }}
                                        onChange={(event) => handleSearchChange(event, event.target.value)}
                                    />
                                )}
                            />
                            {/* Category listing */}
                            <ul className="menu-product-category">
                                <li onClick={() => handleCategorySelect(null)}>
                                    ❤️ Tất cả
                                </li>
                                {displayedCategories.map((category) => (
                                    <li key={category.cateId} onClick={() => handleCategorySelect(category.cateId)}>
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
                        <button>Lọc sản phẩm</button>
                        {hasProducts && (
                            <div className="menu-product-pagination">
                                {/* Previous Button */}
                                <span
                                    className={`pagination-arrow ${currentPage === 1 ? 'disabled' : ''}`}
                                    onClick={() => currentPage > 1 && handleProductPageChange(currentPage - 1)}
                                >
                                    {'<'}
                                </span>

                                {/* Pagination Dots */}
                                {Array.from({ length: totalPages }, (_, index) => (
                                    <span
                                        key={index + 1}
                                        className={`pagination-dot ${currentPage === index + 1 ? 'active' : ''}`}
                                        onClick={() => handleProductPageChange(index + 1)}
                                    >
                                        •
                                    </span>
                                ))}

                                {/* Next Button */}
                                <span
                                    className={`pagination-arrow ${currentPage === totalPages ? 'disabled' : ''}`}
                                    onClick={() => currentPage < totalPages && handleProductPageChange(currentPage + 1)}
                                >
                                    {'>'}
                                </span>
                            </div>
                        )}
                    </div>

                    <div className="products">
                        {loading ? (
                            <p>Đang tải sản phẩm...</p>
                        ) : hasProducts ? (
                            products.map((product) => (
                                <ProductCard
                                    key={product.proId}
                                    product={{
                                        name: product.proName,
                                        size: product.size,
                                        price: `${product.price} VND`,
                                        image: product.productImageResponseList.length > 0
                                            ? product.productImageResponseList[0].linkImage
                                            : backgroundImage
                                    }}
                                    onClick={() => handleProductCardClick(product)}
                                />
                            ))
                        ) : (
                            <p>Không có sản phẩm nào thuộc danh mục này.</p>
                        )}
                    </div>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default Menu;
