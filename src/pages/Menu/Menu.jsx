import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import ProductCard from '../../components/Card/ProductCard';
import backgroundImage from '../../assets/img/5.jpg'; // Path to your background image
import { Autocomplete, TextField } from '@mui/material'; // Import Autocomplete and TextField
import "./Menu.css";

const Menu = () => {
    const [products, setProducts] = useState([]); // Products state
    const [categories, setCategories] = useState([]); // Categories state
    const [loading, setLoading] = useState(true); // To handle loading state
    const [currentPage, setCurrentPage] = useState(1); // Track the current product page
    const [currentCategoryPage, setCurrentCategoryPage] = useState(1); // Track the current category page
    const [totalPages, setTotalPages] = useState(1); // Track the total number of product pages
    const [limit] = useState(8); // Set the number of products per page
    const [categoryLimit] = useState(10); // Set the number of categories per page
    const [selectedCategoryId, setSelectedCategoryId] = useState(null); // Track selected category
    const [searchTerm, setSearchTerm] = useState(''); // Search term state

    // Fetch products and their prices from API
    useEffect(() => {
        const fetchProducts = async () => {
            setLoading(true);
            try {
                const url = selectedCategoryId
                    ? `http://localhost:1010/api/cate/view/${selectedCategoryId}/product`
                    : `http://localhost:1010/api/product/list-product?page=${currentPage}&limit=${limit}`;

                const response = await fetch(url);
                const data = await response.json();

                const productList = selectedCategoryId ? data.responseList : data.productResponses;

                // Fetch price for each product and update the product list with price data
                const productsWithPrices = await Promise.all(productList.map(async (product) => {
                    try {
                        const priceResponse = await fetch(`http://localhost:1010/api/product/variants/${product.proId}`);
                        const priceData = await priceResponse.json();
                        const price = priceData.responseList[0]?.price || 'N/A'; // Get the price from the first variant or default to 'N/A'
                        return { ...product, price };
                    } catch (priceError) {
                        console.error(`Error fetching price for product ${product.proId}:`, priceError);
                        return { ...product, price: 'N/A' }; // Handle error by setting price to 'N/A'
                    }
                }));

                setProducts(productsWithPrices); // Set the products with price information
                setTotalPages(data.totalPage); // Set the total number of pages if applicable
            } catch (error) {
                console.error('Error fetching products:', error);
            } finally {
                setLoading(false); // Turn off loading state after data is fetched
            }
        };

        fetchProducts();
    }, [currentPage, selectedCategoryId]); // Refetch products when the page changes or a category is selected

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

    // Filtered products based on the search term
    const filteredProducts = searchTerm ? products.filter(product => product.proName.toLowerCase().includes(searchTerm.toLowerCase())) : products;

    // Check if there are any products available for the selected category
    const hasProducts = filteredProducts.length > 0;

    return (
        <>
            <Navbar />
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
                        opacity: 0.4, // Background opacity
                        zIndex: -1,
                    }}
                ></div>

                {/* Menu content */}
                <div className="container-menu">
                    <div className="overlay-menu">
                        <div className='menu-category'>

                            {/* MUI Autocomplete for product names */}
                            <Autocomplete
                                freeSolo
                                options={products.map((product) => product.proName)}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="🔍     Bạn muốn uống gì?"
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
                            <ul className="menu-product-category">
                                {currentCategoryPage === 1 && (
                                    <li onClick={() => handleCategorySelect(null)}>
                                        ❤️ Tất cả
                                    </li>
                                )}
                                {/* Map through displayed categories */}
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
                            <p>Đang tải sản phẩm...</p> // Display loading message
                        ) : hasProducts ? (
                            filteredProducts.map((product) => (
                                <ProductCard
                                    key={product.proId}
                                    product={{
                                        name: product.proName,
                                        description: product.description,
                                        price: `${product.price} VND`, // Price fetched from the product variant
                                        image: product.productImageResponseList.length > 0
                                            ? product.productImageResponseList[0].linkImage
                                            : backgroundImage
                                    }}
                                />
                            ))
                        ) : (
                            <p>Không có sản phẩm nào thuộc danh mục này.</p> // Message when no products available
                        )}
                    </div>

                    {/* Show pagination only if there are products */}
                    
                </div>
            </div>
            <Footer />
        </>
    );
};

export default Menu;
