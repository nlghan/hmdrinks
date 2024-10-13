import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import Cookies from 'js-cookie';
import Header from '../../components/Header/Header';
import LoadingAnimation from '../../components/Animation/LoadingAnimation';
import ErrorMessage from '../../components/Animation/ErrorMessage';
import FormAddProduct from '../../components/Form/FormAddProduct'; 
import FormUpdateProduct from '../../components/Form/FormUpdateProduct'; 
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import './Product.css';

// Hook để debounce giá trị
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

const Product = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { cateId, cateName } = location.state || {};

    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [selectedCateId, setSelectedCateId] = useState(cateId || null);
    const [selectedCateName, setSelectedCateName] = useState(cateName || null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [categoryPage, setCategoryPage] = useState(1);
    const [categoryTotalPages, setCategoryTotalPages] = useState(1);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isFormVisible, setIsFormVisible] = useState(false); // State for add form visibility
    const [isUpdateFormVisible, setIsUpdateFormVisible] = useState(false); // State for update form visibility
    const [selectedProduct, setSelectedProduct] = useState(null); // State for the product to update

    const [searchTerm, setSearchTerm] = useState('');
    const [searchOptions, setSearchOptions] = useState([]);

    const debouncedSearchTerm = useDebounce(searchTerm, 500);

    const LIMIT = 5; // Số lượng sản phẩm mỗi trang

    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    // Fetch variants for products
    const fetchProductVariants = useCallback(async (productId) => {
        try {
            const token = Cookies.get('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return [];
            }

            const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/product/variants/${productId}`;
            const response = await axios.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.data && response.data.responseList) {
                return response.data.responseList;
            } else {
                return [];
            }
        } catch (err) {
            console.error(`Lỗi khi lấy danh sách biến thể cho sản phẩm ID ${productId}:`, err);
            setError("Không thể lấy danh sách biến thể.");
            return [];
        }
    }, []);

    // Fetch images for products
    const fetchProductImages = useCallback(async (productId) => {
        try {
            const token = Cookies.get('access_token');
            if (!token) {
                throw new Error("Bạn cần đăng nhập để xem thông tin này.");
            }

            const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/product/list-image/${productId}`;
            const response = await axios.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.data && response.data.productImageResponseList) {
                return response.data.productImageResponseList.map(img => img.linkImage);
            } else {
                return [];
            }
        } catch (err) {
            console.error(`Lỗi khi lấy danh sách hình ảnh cho sản phẩm ID ${productId}:`, err);
            return [];
        }
    }, []);

    // Fetch categories
    const fetchCategories = useCallback(async () => {
        try {
            const token = Cookies.get('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const categoryApiUrl = `${import.meta.env.VITE_API_BASE_URL}/cate/list-category?page=${categoryPage}&limit=12`;
            const response = await axios.get(categoryApiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`
                }
            });

            console.log('API Categories response:', response.data); // Kiểm tra dữ liệu trả về

            if (response.data && response.data.categoryResponseList) {
                const categoryData = response.data.categoryResponseList.map(category => ({
                    cateId: category.cateId,
                    cateName: category.cateName
                }));
                setCategories(categoryData);
                setCategoryTotalPages(response.data.totalPage);
            } else {
                setError("Không có danh mục nào.");
            }
        } catch (err) {
            console.error("Lỗi khi lấy danh sách danh mục:", err);
            setError("Không thể lấy danh sách danh mục.");
        }
    }, [categoryPage]);

    const fetchSearchOptions = useCallback(async (keyword) => {
        try {
            const token = Cookies.get('access_token');
            if (!token) {
                throw new Error("Bạn cần đăng nhập để xem thông tin này.");
            }

            const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/product/search?keyword=${encodeURIComponent(keyword)}&page=1&limit=10`; 
            const response = await axios.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`
                }
            });

            console.log('API Search Options response:', response.data); 

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

            // Determine the API URL based on search term or category
            if (debouncedSearchTerm && debouncedSearchTerm.trim() !== '') {
                // Fetch products based on the search term
                apiUrl = `${import.meta.env.VITE_API_BASE_URL}/product/search?keyword=${encodeURIComponent(debouncedSearchTerm)}&page=${currentPage}&limit=${LIMIT}`;
            } else if (selectedCateId) {
                // Fetch products by category if one is selected
                apiUrl = `${import.meta.env.VITE_API_BASE_URL}/cate/view/${selectedCateId}/product?page=${currentPage}&limit=${LIMIT}`;
            } else {
                // If no search term or category, fetch all products
                apiUrl = `${import.meta.env.VITE_API_BASE_URL}/product/list-product?page=${currentPage}&limit=${LIMIT}`;
            }

            console.log('Fetching products from:', apiUrl);

            const response = await axios.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`
                }
            });

            console.log('API Products Response:', response.data);

            // Check the structure of the response and process accordingly
            let productData;
            if (response.data && response.data.productResponseList) {
                // Handle the search response
                productData = response.data.productResponseList;
            } else if (response.data && response.data.productResponses) {
                // Handle the list product response
                productData = response.data.productResponses;
            } else if (response.data && response.data.responseList) {
                // Handle the category view response
                productData = response.data.responseList;
            } else {
                setProducts([]); // Clear products if no valid data found
                return;
            }

            // If there are products, process them
            if (productData && productData.length > 0) {
                const updatedProducts = await Promise.all(productData.map(async (product) => {
                    const variants = await fetchProductVariants(product.proId);
                    const images = await fetchProductImages(product.proId);
                    return { ...product, variants, images };
                }));

                setProducts(updatedProducts);
                setTotalPages(response.data.totalPage || 1); // Handle total pages if provided
            } else {
                setProducts([]); // Clear products if no data is returned
            }
        } catch (err) {
            console.error("Lỗi khi lấy danh sách sản phẩm:", err);
            setError("Không thể lấy danh sách sản phẩm.");
        } finally {
            setLoading(false);
        }
    }, [debouncedSearchTerm, selectedCateId, currentPage, fetchProductVariants, fetchProductImages]);

    // Fetch categories when component mounts or when categoryPage changes
    useEffect(() => {
        fetchCategories();
    }, [fetchCategories]);

    // Fetch products when selectedCateId, currentPage, or debouncedSearchTerm changes
    useEffect(() => {
        fetchProducts();
    }, [debouncedSearchTerm, fetchProducts]);

    // Fetch search options when debouncedSearchTerm changes
    useEffect(() => {
        if (debouncedSearchTerm) {
            fetchSearchOptions(debouncedSearchTerm);
        } else {
            setSearchOptions([]);
        }
    }, [debouncedSearchTerm, fetchSearchOptions]);

    // Handle pagination for products
    const handleNextPage = () => {
        if (currentPage < totalPages) {
            setCurrentPage(prevPage => prevPage + 1);
        }
    };

    const handlePrevPage = () => {
        if (currentPage > 1) {
            setCurrentPage(prevPage => prevPage - 1);
        }
    };

    // Handle category pagination
    const handleNextCategoryPage = () => {
        if (categoryPage < categoryTotalPages) {
            setCategoryPage(prevPage => prevPage + 1);
        }
    };

    const handlePrevCategoryPage = () => {
        if (categoryPage > 1) {
            setCategoryPage(prevPage => prevPage - 1);
        }
    };

    // Handle category selection
    const handleCategoryClick = (id, name) => {
        setSelectedCateId(id);
        setSelectedCateName(name);
        setCurrentPage(1);
        setSearchTerm(''); // Reset tìm kiếm khi chọn danh mục
    };

    // Handle reload (reset category and search)
    const handleReload = () => {
        setSelectedCateId(null);
        setSelectedCateName(null);
        setCurrentPage(1);
        setSearchTerm(''); // Reset tìm kiếm khi reload
    };

    // Handle add form close and refresh products list
    const handleFormClose = () => {
        setIsFormVisible(false); // Close the add form
        fetchProducts(); // Refresh products list after adding new product
    };

    // Handle update form close and refresh products list
    const handleFormCloseUpdate = () => {
        setIsUpdateFormVisible(false); // Close the update form
        setSelectedProduct(null); // Clear selected product
        fetchProducts(); // Refresh products list after updating product
    };

    // Handle clicking the update button
    const handleUpdateClick = async (productId) => {
        try {
            const token = Cookies.get('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/product/view/${productId}`;
            const response = await axios.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.data) {
                // Fetch images separately if needed
                const images = await fetchProductImages(productId);
                const productData = { ...response.data, images };
                setSelectedProduct(productData);
                setIsUpdateFormVisible(true);
            } else {
                setError("Không thể lấy thông tin sản phẩm.");
            }
        } catch (err) {
            console.error(`Lỗi khi lấy thông tin sản phẩm ID ${productId}:`, err);
            setError("Không thể lấy thông tin sản phẩm.");
        }
    };

    if (loading) {
        return <LoadingAnimation />;
    }

    if (error) {
        return <ErrorMessage message={error} />;
    }

    return (
        <div className="product-page">
            <Header isMenuOpen={isMenuOpen} toggleMenu={toggleMenu} title="Sản phẩm" />
            <div className="product-content">
                <div className="product-categories">
                    <h3 className="product-title" onClick={handleReload}>Danh Mục</h3>
                    <ul className="product-category-list">
                        {categories.map(category => (
                            <li
                                key={category.cateId}
                                className={`product-category-item ${selectedCateId === category.cateId ? 'active' : ''}`}
                                onClick={() => handleCategoryClick(category.cateId, category.cateName)}
                            >
                                {category.cateName}
                            </li>
                        ))}
                    </ul>
                    <div className="product-pagination-controls">
                        <button onClick={handlePrevCategoryPage} disabled={categoryPage === 1}>
                            Trang Trước
                        </button>
                        <span>Trang {categoryPage} / {categoryTotalPages}</span>
                        <button onClick={handleNextCategoryPage} disabled={categoryPage === categoryTotalPages}>
                            Trang Sau
                        </button>
                    </div>
                </div>

                <div className="product-list">
                    <div className='prodcut-table-header'>
                        <h3 className="product-title-table">
                            {selectedCateId ? `Sản Phẩm của Danh Mục ${selectedCateName}` : 'Tất Cả Sản Phẩm'}
                        </h3>
                        <div className="search-add-container">
                            {/* Thanh tìm kiếm bằng Autocomplete */}
                            <Autocomplete
                                freeSolo
                                options={searchOptions.map(option => option.proName)}
                                inputValue={searchTerm}
                                onInputChange={(event, newInputValue) => {
                                    setSearchTerm(newInputValue);
                                }}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Tìm kiếm sản phẩm..."
                                        variant="outlined"
                                        size="small"
                                    />
                                )}
                                style={{ width: 300, marginRight: '16px' }} // Adjust width and spacing as needed
                            />
                            <button className="btn-pro-add1" onClick={() => setIsFormVisible(true)}>
                                Thêm sản phẩm
                            </button>
                        </div>
                    </div>

                    {products.length === 0 ? (
                        <p>Không có sản phẩm nào phù hợp với từ khóa tìm kiếm của bạn.</p>
                    ) : (
                        <>
                            <table className="product-table">
                                <thead>
                                    <tr>
                                        <th>STT</th> {/* Số thứ tự */}
                                        <th>Tên Sản Phẩm</th>
                                        <th>Hình Ảnh</th>
                                        <th>Mô Tả</th>
                                        <th>Giá</th>
                                        <th>Thao Tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {products.map((product, index) => (
                                        <tr key={product.proId}>
                                            {/* Số thứ tự */}
                                            <td>{(currentPage - 1) * LIMIT + index + 1}</td>
                                            <td>{product.proName}</td>
                                            <td>
                                                {product.images && product.images.length > 0 ? (
                                                    <div className="product-images">
                                                        {product.images.map((imgSrc, imgIndex) => (
                                                            <img
                                                                key={imgIndex}
                                                                src={imgSrc}
                                                                alt={`${product.proName} ${imgIndex + 1}`}
                                                                className="product-img"
                                                                loading="lazy"
                                                            />
                                                        ))}
                                                    </div>
                                                ) : (
                                                    <p>Đang tải hình ảnh...</p>
                                                )}
                                            </td>
                                            <td>{product.description}</td>
                                            <td>
                                                {product.variants && product.variants.length > 0 ? (
                                                    <ul>
                                                        {product.variants.map(variant => (
                                                            <li key={variant.varId}>
                                                                <span className="size">{variant.size}</span>
                                                                <span className="price">{variant.price} VND</span>
                                                                <span className="stock">({variant.stock} sản phẩm)</span>
                                                            </li>
                                                        ))}
                                                    </ul>
                                                ) : (
                                                    <p>Đang tải biến thể...</p>
                                                )}
                                            </td>

                                            <td className='pro-action'>
                                                <div className='gr-btn-pro'>
                                                    <button
                                                        className="btn-pro-add"
                                                        onClick={() => handleUpdateClick(product.proId)}
                                                    >
                                                        Cập nhật
                                                    </button>
                                                    <button className="btn-pro-clear" onClick={() => { /* Delete logic here */ }}>
                                                        Xóa
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}

                                </tbody>
                            </table>

                            <div className="product-pagination-controls">
                                <button onClick={handlePrevPage} disabled={currentPage === 1}>
                                    Trang Trước
                                </button>
                                <span>Trang {currentPage} / {totalPages}</span>
                                <button onClick={handleNextPage} disabled={currentPage === totalPages}>
                                    Trang Sau
                                </button>
                            </div>
                        </>
                    )}
                </div>

                {isFormVisible && (
                    <FormAddProduct onClose={handleFormClose} />
                )}

                {isUpdateFormVisible && selectedProduct && (
                    <FormUpdateProduct
                        product={selectedProduct}
                        onClose={handleFormCloseUpdate}
                        onUpdate={fetchProducts} // Optionally, you can pass fetchProducts to handle update logic
                    />
                )}
            </div>
        </div>
    );
};

export default Product;
