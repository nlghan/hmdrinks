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
    const [switches, setSwitches] = useState({});
    const [activeProducts, setActiveProducts] = useState([]);


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

    const LIMIT = 4; // Số lượng sn phẩm mỗi trang
    const [total, setTotal] = useState(); // Tổng số trang

    const [selectedVariant, setSelectedVariant] = useState(null); // State để lưu biến thể được chọn
    const [priceHistory, setPriceHistory] = useState({ oldPrice: null, newPrice: null }); // State để lưu giá lịch sử
    const [showPriceHistoryModal, setShowPriceHistoryModal] = useState(false); // State để kiểm soát hiển thị modal

    const [modalPosition, setModalPosition] = useState({ top: 0, left: 0 });

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

            const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/admin/product/variants/${productId}`;
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

            const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/product/search?keyword=${encodeURIComponent(keyword)}&page=1&limit=4`;
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

            // Xác định URL API dựa trên các điều kiện
            const apiUrl = debouncedSearchTerm?.trim()
                ? `${import.meta.env.VITE_API_BASE_URL}/admin/search-product?keyword=${encodeURIComponent(debouncedSearchTerm)}&page=${currentPage}&limit=${LIMIT}`
                : selectedCateId
                    ? `${import.meta.env.VITE_API_BASE_URL}/admin/cate/view/${selectedCateId}/product?page=${currentPage}&limit=${LIMIT}`
                    : `${import.meta.env.VITE_API_BASE_URL}/admin/list-product?page=${currentPage}&limit=${LIMIT}`;

            console.log("Fetching data from URL:", apiUrl);  // Log URL để xác nhận

            // Gọi API để lấy danh sách sản phẩm
            const response = await axios.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`
                }
            });

            console.log("Response data:", response.data);  // Log toàn bộ dữ liệu phản hồi

            let productData;

            console.log("Checking responseList:", response.data?.responseList);

            if (response.data?.productResponseList && response.data.productResponseList.length > 0) {
                productData = response.data.productResponseList;
            } else if (response.data?.productResponses && response.data.productResponses.length > 0) {
                productData = response.data.productResponses;
            } else if (response.data?.body?.responseList && response.data.body.responseList.length > 0) {
                productData = response.data.body.responseList;  // Lấy dữ liệu từ body nếu tồn tại responseList
                console.log("Product data for category:", productData); // Log để kiểm tra dữ liệu của responseList
            } else {
                setProducts([]);
                console.log("No product data found in response");  // Log nếu không tìm thấy dữ liệu sản phẩm
                return;
            }
            


            console.log("Parsed product data:", productData);  // Log dữ liệu sản phẩm đã được phân tích

            // Xử lý dữ liệu sản phẩm và lấy thêm biến thể, hình ảnh
            if (productData?.length > 0) {
                const updatedProducts = await Promise.all(productData.map(async (product) => {
                    console.log("Processing product:", product.proId);  // Log mỗi sản phẩm trước khi xử lý

                    const variants = await fetchProductVariants(product.proId);
                    const images = await fetchProductImages(product.proId);

                    console.log("Fetched variants for product:", product.proId, variants);  // Log biến thể của sản phẩm
                    console.log("Fetched images for product:", product.proId, images);  // Log hình ảnh của sản phm

                    return { ...product, variants, images };
                }));

                setProducts(updatedProducts);
                console.log("Updated products with details:", updatedProducts);  // Log danh sách sản phẩm sau khi xử lý

                // Cập nhật danh sách sản phẩm đang hoạt động
                const activeIds = updatedProducts.filter(product => !product.deleted).map(product => product.proId);
                setActiveProducts("delete: " + activeIds);

                setTotalPages(response.data.totalPage||response.data.body.totalPage || 1);
                setTotal(response.data.total)
            } else {
                setProducts([]);
                console.log("No products found in parsed product data");  // Log nếu không có sản phẩm trong dữ liệu đã phân tích
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

            const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/admin/product/view/${productId}`;
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

    const handleSwitchChange = async (productId) => {
        try {
            const token = Cookies.get('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để thực hiện thao tác này.");
                return;
            }

            const productToUpdate = products.find(product => product.proId === productId);
            if (!productToUpdate) {
                console.error("No product found with the given productId:", productId);
                return;
            }

            const newIsDeletedStatus = !productToUpdate.deleted;

            // Cập nhật UI ngay lập tức
            setProducts((prevProducts) =>
                prevProducts.map((product) =>
                    product.proId === productId
                        ? {
                            ...product,
                            deleted: newIsDeletedStatus,
                            dateDeleted: newIsDeletedStatus ? new Date().toISOString() : null,
                        }
                        : product
                )
            );

            // Sau đó mới gọi API
            const apiUrl = newIsDeletedStatus
                ? `${import.meta.env.VITE_API_BASE_URL}/product/disable`
                : `${import.meta.env.VITE_API_BASE_URL}/product/enable`;

            const response = await axios.put(
                apiUrl,
                { id: productId },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                }
            );

            // Nếu API thất bại thì rollback state
            if (response.status !== 200) {
                setError("Không thể thay đổi trạng thái sản phẩm. Vui lòng thử lại.");
                // Rollback state
                setProducts((prevProducts) =>
                    prevProducts.map((product) =>
                        product.proId === productId
                            ? {
                                ...product,
                                deleted: !newIsDeletedStatus,
                                dateDeleted: !newIsDeletedStatus ? new Date().toISOString() : null,
                            }
                            : product
                    )
                );
            }
        } catch (error) {
            console.error("Error changing product status:", error);

        }
    };

    // Hàm fetchPriceHistory để lấy oldPrice và newPrice cho varId
    const fetchPriceHistory = async (varId) => {
        try {
            const response = await fetch(`http://localhost:1010/api/price-history/view/productVar?page=1&limit=1&proVarId=${varId}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            if (data.priceHistoryResponses && data.priceHistoryResponses.length > 0) {
                const { oldPrice, newPrice } = data.priceHistoryResponses[0]; // Lấy giá từ phản hồi
                setPriceHistory({ oldPrice, newPrice });
            } else {
                setPriceHistory({ oldPrice: null, newPrice: null });
            }
        } catch (error) {
            console.error('Failed to fetch price history:', error);
        }
    };

    // Hàm để xử lý khi người dùng hover vào biến thể
    const handleVariantHover = (variant, event) => {
        setSelectedVariant(variant); // Lưu biến thể được chọn
        fetchPriceHistory(variant.varId); // Gọi API để lấy giá lịch sử

        // Lấy vị trí của phần tử size
        const sizeElement = event.currentTarget.querySelector('.size');
        const rect = sizeElement.getBoundingClientRect();

        // Cập nhật vị trí của modal
        setModalPosition({
            top: rect.top + window.scrollY,
            left: rect.left + window.scrollX + rect.width
        });

        setShowPriceHistoryModal(true); // Hiển thị modal
    };

    // Hàm để ẩn modal khi hover ra ngoài
    const handleMouseLeave = () => {
        setShowPriceHistoryModal(false); // Ẩn modal
        setSelectedVariant(null); // Đặt lại biến thể đã chọn
    };

    if (loading) {
        return <LoadingAnimation />;
    }

    if (error) {
        return <ErrorMessage message={error} />;
    }

    const getPaginationNumbers = () => {
        const paginationNumbers = [];
        const maxButtons = 5; // Max page buttons to display

        // Show ellipsis khi có nhiều hơn maxButtons trang
        if (totalPages <= maxButtons) {
            for (let i = 1; i <= totalPages; i++) {
                paginationNumbers.push(i);
            }
        } else {
            // Luôn hiển thị trang đầu tiên
            paginationNumbers.push(1);

            if (currentPage > 3) {
                paginationNumbers.push('...'); // Ellipsis nếu trang hiện tại lớn hơn 3
            }

            const startPage = Math.max(2, currentPage - 1); // Bắt đầu từ trang thứ 2 hoặc trang hiện tại -1
            const endPage = Math.min(totalPages - 1, currentPage + 1); // Kết thúc ở trang trước cuối hoặc trang hiện tại +1

            for (let i = startPage; i <= endPage; i++) {
                paginationNumbers.push(i);
            }

            if (currentPage < totalPages - 2) {
                paginationNumbers.push('...'); // Ellipsis nếu trang hiện tại nhỏ hơn tổng trang -2
            }

            // Luôn hiển thị trang cuối
            paginationNumbers.push(totalPages);
        }

        return paginationNumbers;
    };

    const handlePageChange = (newPage) => {
        if (newPage >= 1 && newPage <= totalPages) {
            setCurrentPage(newPage); // Thay đổi trang
        }
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', {
            currency: 'VND',   // Đơn vị tiền tệ Việt Nam Đồng
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
        }).format(parseFloat(price));
    };

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
                        <div>
                            <h3 className="product-title-table">
                                {selectedCateId ? `Sản phẩm của danh mục -  ${selectedCateName}` : `Tất Cả Sản Phẩm (${total})`}
                            </h3>

                        </div>

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
                                <i className="ti-plus"></i>
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
                                        <th>ID</th> {/* Số thứ tự */}
                                        <th>Tên Sản Phẩm</th>
                                        <th>Hình Ảnh</th>
                                        <th>Mô Tả</th>
                                        <th>Trạng Thái</th>
                                        <th>Giá Sản Phẩm</th>
                                        <th>Thao Tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {products.map((product, index) => (
                                        <tr key={product.proId}>
                                            {/* Số thứ tự */}
                                            <td>{product.proId}</td>
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
                                                <label className="pro-switch">
                                                    <input
                                                        type="checkbox"
                                                        checked={product.deleted === false} // Checkbox sẽ checked nếu isDeleted là false
                                                        onChange={() => handleSwitchChange(product.proId)}                                
                                                    />
                                                    <span className="pro-slider round"></span>
                                                </label>
                                            </td>


                                            <td>
                                                {product.variants && product.variants.length > 0 ? (
                                                    <ul >
                                                        {product.variants.map(variant => (
                                                            <li 
                                                                key={variant.varId} 
                                                                onMouseEnter={(event) => handleVariantHover(variant, event)} // Pass event to get position
                                                                onMouseLeave={handleMouseLeave} // Ẩn modal khi không hover
                                                            >
                                                                <span className="size">{variant.size}</span>
                                                                <span className="price">{formatPrice(variant.price)} VND</span>
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
                                                    <div id="btn-pro-add" onClick={() => handleUpdateClick(product.proId)}>
                                                        <i className="ti-pencil"></i> {/* Themify icon for updating */}
                                                    </div>
                                                    {/* <div id="btn-pro-clear" onClick={() => {  }}>
                                                        <i className="ti-trash"></i> {}
                                                    </div> */}
                                                </div>

                                            </td>
                                        </tr>
                                    ))}

                                </tbody>
                            </table>

                            <div className="pro-pagination">
                                <button
                                    className="btn btn-pre me-2"
                                    onClick={() => handlePageChange(currentPage - 1)}
                                    disabled={currentPage === 1}
                                >
                                    &lt;
                                </button>
                                {getPaginationNumbers().map((number, index) => (
                                    <button
                                        key={index}
                                        className={`btn ${number === currentPage ? 'btn-page' : 'btn-light'} me-2`}
                                        onClick={() => {
                                            if (number !== '...') {
                                                handlePageChange(number);
                                            }
                                        }}
                                        disabled={number === '...'} // Disable button for ellipsis
                                    >
                                        {number}
                                    </button>
                                ))}
                                <button
                                    className="btn btn-next"
                                    onClick={() => handlePageChange(currentPage + 1)}
                                    disabled={currentPage === totalPages}
                                >
                                    &gt;
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

                {/* Hiển thị form nhỏ khi có biến thể được chọn */}
                {showPriceHistoryModal && selectedVariant && (
                    <div 
                        className="price-history-modal" 
                        style={{ 
                            position: 'absolute', 
                            top: `${modalPosition.top}px`, 
                            left: `${modalPosition.left}px`, 
                            zIndex: 1000 
                        }}
                    >
                        <h4>Bảng giá cập nhật cho size {selectedVariant.size}</h4>
                        {priceHistory.oldPrice === null && priceHistory.newPrice === null ? (
                            <p>Chưa có cập nhật giá</p>
                        ) : (
                            <>
                                <p>Giá cũ: {priceHistory.oldPrice !== null ? `${priceHistory.oldPrice} VND` : 'Không có dữ liệu'}</p>
                                <p>Giá mới: {priceHistory.newPrice !== null ? `${priceHistory.newPrice} VND` : 'Không có dữ liệu'}</p>
                            </>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Product;
