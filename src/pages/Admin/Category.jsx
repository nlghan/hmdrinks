import React, { useEffect, useState, useCallback } from 'react';
import './Category.css'; // CSS cho component
import Cookies from 'js-cookie';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Header from '../../components/Header/Header';
import LoadingAnimation from '../../components/Animation/LoadingAnimation';
import ErrorMessage from '../../components/Animation/ErrorMessage';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import axiosInstance from '../../utils/axiosConfig';

const Category = () => {
    const navigate = useNavigate();
    const [switches, setSwitches] = useState({});
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [categoryName, setCategoryName] = useState('');
    const [categoryImage, setCategoryImage] = useState(null);
    const [categoryImagePreview, setCategoryImagePreview] = useState(null);
    const [isActive, setIsActive] = useState(false);
    const [categories, setCategories] = useState([]);
    const [sortOrder, setSortOrder] = useState('asc'); // Mặc định sắp xếp mới nhất trước
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [loadingbtn, setLoadingbtn] = useState(false);

    const [isUpdateMode, setIsUpdateMode] = useState(false); // Track if we are in update mode
    const [updateCategory, setUpdateCategory] = useState(null); // The category to update
    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearchTerm, setDebouncedSearchTerm] = useState(searchTerm);
    const [limit, setLimit] = useState(5);
    const [currentPage, setCurrentPage] = useState(1); // Trang hiện tại
    const [totalPages, setTotalPages] = useState(1); // Tổng số trang
    const [isCreating, setIsCreating] = useState(false); // Trạng thái khi tạo mới danh mục
    const [total, setTotal] = useState(); // Tổng số trang


    // Cơ chế debounce cho searchTerm
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedSearchTerm(searchTerm);
            setCurrentPage(1); // Reset về trang đầu khi thay đổi từ khóa tìm kiếm
        }, 500); // Độ trễ 500ms

        return () => {
            clearTimeout(handler);
        };
    }, [searchTerm]);

    // Hàm lấy cookie
    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    // Hàm sắp xếp client-side
    const sortCategories = (categories, order) => {
        return categories.sort((a, b) => {
            if (order === 'asc') {
                return a.cateId - b.cateId;
            } else {
                return b.cateId - a.cateId;
            }
        });
    };

    // Hàm fetchCategories được định nghĩa bên ngoài useEffect
    const fetchCategories = useCallback(async (page, keyword, sortOrderParam) => {
        setLoading(true);
        setError(null);

        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                setLoading(false);
                return;
            }

            let apiUrl = `${import.meta.env.VITE_API_BASE_URL}/admin/list-category?page=${page}&limit=${limit}`;
            if (keyword) {
                apiUrl = `${import.meta.env.VITE_API_BASE_URL}/cate/search?keyword=${encodeURIComponent(keyword)}&page=${page}&limit=${limit}`;
            }

            // Gọi API để lấy danh sách danh mục
            const response = await axiosInstance.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`
                }
            });
            setTotal(response.data.total)

            // Kiểm tra dữ liệu trả về
            if (response.data && response.data.categoryResponseList) {
                let sortedCategories = response.data.categoryResponseList;
                // Thực hiện sắp xếp client-side
                sortedCategories = sortCategories(sortedCategories, sortOrderParam);

                setCategories(sortedCategories); // Cập nhật danh sách danh mục đã sắp xếp
                setCurrentPage(response.data.currentPage); // Cập nhật trang hiện tại
                setTotalPages(response.data.totalPage); // Cập nhật tổng số trang

                const initialSwitchStates = {};
                sortedCategories.forEach(category => {
                    // Set switch state based on isDeleted, ensure cateId is used as the key
                    initialSwitchStates[category.cateId] = (category.isDeleted === false || category.isDeleted === null);
                });
                setSwitches(initialSwitchStates);
            } else {
                setError("Không có dữ liệu danh mục.");
            }

        } catch (error) {
            console.error("Lỗi khi lấy danh sách danh mục:", error);
            setError("Không thể lấy danh sách danh mục.");
        } finally {
            setLoading(false);
        }
    }, [limit]);

    // Gọi fetchCategories khi currentPage, limit, debouncedSearchTerm, hoặc sortOrder thay đổi
    useEffect(() => {
        fetchCategories(currentPage, debouncedSearchTerm, sortOrder);
    }, [currentPage, limit, debouncedSearchTerm, sortOrder, fetchCategories]);

    const handlePageChange = (newPage) => {
        if (newPage >= 1 && newPage <= totalPages) {
            setCurrentPage(newPage); // Thay đổi trang
        }
    };
    if (loading) {
        return <div>Loading...</div>; // Hiển thị khi đang tải
    }

    if (error) {
        return <div>{error}</div>; // Hiển thị thông báo lỗi
    }

    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    // Hàm xử lý thay đổi file ảnh và tạo URL xem trước
    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setCategoryImage(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setCategoryImagePreview(reader.result);
            };
            reader.readAsDataURL(file);
        } else {
            setCategoryImage(null);
            setCategoryImagePreview(null);
        }
    };

    const handleSwitchChange = async (cateId) => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để thực hiện thao tác này.");
                return;
            }

            // Tìm category trong state hiện tại
            const categoryToUpdate = categories.find(category => category.cateId === cateId);
            if (!categoryToUpdate) {
                console.error("No category found with the given cateId:", cateId);
                return;
            }

            // Cập nhật UI ngay lập tức
            setCategories(prevCategories =>
                prevCategories.map(category =>
                    category.cateId === cateId
                        ? { ...category, isDeleted: !category.isDeleted }
                        : category
                )
            );

            // Xác định endpoint dựa trên trạng thái mới
            const apiUrl = categoryToUpdate.isDeleted
                ? `${import.meta.env.VITE_API_BASE_URL}/cate/enable`
                : `${import.meta.env.VITE_API_BASE_URL}/cate/disable`;

            // Gọi API
            const response = await axiosInstance.put(
                apiUrl,
                { id: cateId },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                }
            );

            // Nếu API call thất bại, hoàn tác thay đổi UI
            if (response.status !== 200) {
                setCategories(prevCategories =>
                    prevCategories.map(category =>
                        category.cateId === cateId
                            ? { ...category, isDeleted: categoryToUpdate.isDeleted }
                            : category
                    )
                );
                setError("Không thể thay đổi trạng thái danh mục. Vui lòng thử lại.");
            }

        } catch (error) {
            console.error("Error changing category status:", error);
            // Hoàn tác thay đổi UI nếu có lỗi
            setCategories(prevCategories =>
                prevCategories.map(category =>
                    category.cateId === cateId
                        ? { ...category, isDeleted: !category.isDeleted }
                        : category
                )
            );
            setError("Không thể thay đổi trạng thái danh mục. Vui lòng thử lại.");
        }
    };

    const handleAddCategory = async () => {
        if (categoryName && categoryImage) {
            const token = getCookie('access_token');
            const categoryData = {
                cateName: categoryName,
                cateImg: ''
            };
            setIsCreating(true);

            try {
                const response = await axiosInstance.post(`${import.meta.env.VITE_API_BASE_URL}/cate/create-category`, categoryData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                const newCategory = response.data;
                const cateId = newCategory.cateId;

                const formData = new FormData();
                formData.append('file', categoryImage);

                const uploadResponse = await axiosInstance.post(`${import.meta.env.VITE_API_BASE_URL}/image/cate/upload?cateId=${cateId}`, formData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'multipart/form-data'
                    }
                });

                const imageUrl = uploadResponse.data.url;
                newCategory.cateImg = imageUrl;

                setSuccessMessage("Thêm danh mục thành công!");
                setTimeout(() => setSuccessMessage(''), 2000);

                // Reset form
                resetForm();

                // Tải lại danh sách danh mục
                fetchCategories(currentPage, debouncedSearchTerm, sortOrder);
            } catch (error) {
                console.error("Lỗi khi thêm danh mục:", error);
                // Kiểm tra lỗi 409
                if (error.response && error.response.status === 409) {
                    setErrorMessage("Danh mục với tên này đã tồn tại. Vui lòng chọn tên khác.");
                } else {
                    setErrorMessage("Không thể thêm danh mục. Vui lòng thử lại.");
                }
                setTimeout(() => setErrorMessage(''), 2000);
            } finally {
                setIsCreating(false);
            }
        } else {
            setErrorMessage("Vui lòng nhập tên danh mục và chọn hình ảnh.");
            setTimeout(() => setErrorMessage(''), 2000);
        }
    };


    const handleSortChange = (e) => {
        const order = e.target.value;
        setSortOrder(order);
        setCurrentPage(1); // Reset về trang đầu khi thay đổi sắp xếp
    };

    const handleUpdateCategory = async () => {
        if (updateCategory && categoryName) {
            const token = getCookie('access_token');
            let updatedCategoryData = {
                cateId: updateCategory.cateId,  // Đảm bảo đây là ID đúng của danh mục
                cateName: categoryName,  // Sử dụng tên danh mục mới nhập vào
                cateImg: updateCategory.cateImg // Mặc định giữ lại hình ảnh cũ nếu không có hình ảnh mới
            };

            try {
                setLoadingbtn(true);

                // Gửi yêu cầu cập nhật danh mục trước
                const updateCategoryResponse = await axiosInstance.put(`${import.meta.env.VITE_API_BASE_URL}/cate/update`, updatedCategoryData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                // Kiểm tra nếu trả về thông điệp "category already exists" trong response.data
                if (updateCategoryResponse.data === "category already exists") {
                    setErrorMessage("Danh mục với tên này đã tồn tại. Vui lòng chọn tên khác.");
                    setTimeout(() => setErrorMessage(''), 2000);
                    return; // Dừng lại nếu tên danh mục đã tồn tại
                }

                // Nếu có hình ảnh mới, upload ảnh
                if (categoryImage) {
                    const formData = new FormData();
                    formData.append('file', categoryImage);

                    // Upload hình ảnh để lấy URL mới
                    const uploadResponse = await axiosInstance.post(`${import.meta.env.VITE_API_BASE_URL}/image/cate/upload?cateId=${updateCategory.cateId}`, formData, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'multipart/form-data'
                        }
                    });

                    // Cập nhật URL hình ảnh sau khi upload thành công
                    updatedCategoryData.cateImg = uploadResponse.data.url;

                    // Gửi yêu cầu cập nhật danh mục một lần nữa với hình ảnh mới
                    await axiosInstance.put(`${import.meta.env.VITE_API_BASE_URL}/cate/update`, updatedCategoryData, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        }
                    });
                }

                setSuccessMessage("Cập nhật danh mục thành công!");
                setTimeout(() => setSuccessMessage(''), 2000);

                // Reset form
                resetForm();

                // Tải lại danh sách danh mục
                fetchCategories(currentPage, debouncedSearchTerm, sortOrder);
            } catch (error) {
                console.error("Lỗi khi cập nhật danh mục:", error);

                // Kiểm tra nếu có lỗi trả về từ server
                if (error.response && error.response.data === "category already exists") {
                    setErrorMessage("Danh mục với tên này đã tồn tại. Vui lòng chọn tên khác.");
                } else {
                    setErrorMessage("Không thể cập nhật danh mục. Vui lòng thử lại.");
                }

                setTimeout(() => setErrorMessage(''), 2000);
            } finally {
                setLoadingbtn(false);
            }
        } else {
            setErrorMessage("Vui lòng nhập tên danh mục.");
            setTimeout(() => setErrorMessage(''), 2000);
        }
    };



    const resetForm = () => {
        setCategoryName('');
        setCategoryImage(null);
        setCategoryImagePreview(null);
        setIsActive(false);
        setIsUpdateMode(false);
        setUpdateCategory(null);
        const categoryImageInput = document.getElementById('categoryImage');
        if (categoryImageInput) {
            categoryImageInput.value = ''; // reset input file
        }
    };

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

    const handleViewProduct = (cateId, cateName) => {
        navigate('/product', { state: { cateId, cateName } });
    };

    return (
        <div className="category" style={{ paddingLeft: '70px' }}>
            {isCreating && (
                <div className="category-loading-overlay active">
                    <div className="category-loading-spinner"></div>
                </div>
            )}
            <Header isMenuOpen={isMenuOpen} toggleMenu={toggleMenu} title="Danh mục" />
            <div className={`category-row ${isMenuOpen ? 'dimmed' : ''}`}>
                <div className="side-section-cate">
                    <div className="updates-box">
                        <h2>{isUpdateMode ? 'Cập nhật danh mục đồ uống' : 'Thêm danh mục đồ uống'}</h2>

                        <form className="category-form">
                            <div className="form-group">
                                <label htmlFor="categoryName"><div className="name-label-cate">
                                    Tên danh mục
                                </div></label>
                                <input
                                    type="text"
                                    id="categoryName"
                                    value={categoryName}
                                    onChange={(e) => setCategoryName(e.target.value)}
                                    placeholder="Nhập tên danh mục"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="categoryImage"><div className="name-label-cate">
                                    Hình ảnh danh mục
                                </div></label>
                                {categoryImagePreview && (
                                    <div className="image-preview">
                                        <img src={categoryImagePreview} alt="Preview" />
                                    </div>
                                )}
                                <input
                                    type="file"
                                    id="categoryImage"
                                    onChange={handleImageChange}
                                />
                            </div>


                            <div className="cate-form-actions">
                                <button
                                    type="button"
                                    id="btn-add"
                                    disabled={loadingbtn}
                                    onClick={isUpdateMode ? handleUpdateCategory : handleAddCategory}
                                    style={{
                                        cursor: loadingbtn ? 'not-allowed' : 'pointer',
                                        borderRadius: "6px"
                                    }}
                                >
                                    {isUpdateMode ? 'Cập nhật' : 'Thêm danh mục'}
                                </button>
                                {isUpdateMode && (
                                    <button
                                        type="button"
                                        className="btn-clear"
                                        onClick={resetForm}
                                        disabled={loadingbtn}
                                        style={{
                                            cursor: loadingbtn ? 'not-allowed' : 'pointer',
                                            borderRadius: "6px"
                                        }}
                                    >
                                        Hủy
                                    </button>
                                )}
                            </div>
                            {successMessage && <div className="success-message">{successMessage}</div>}
                            {errorMessage && <p className="form-update-post-error">{errorMessage}</p>}
                        </form>
                    </div>
                </div>
                <div className="main-section-cate">
                    <div className="list-cate-box">
                        <div className="prodcut-table-header">
                            <h2>Danh mục các loại đồ uống ({total})</h2>

                            <Autocomplete
                                freeSolo
                                options={categories.map((option) => option.cateName)}
                                inputValue={searchTerm}
                                onInputChange={(event, newInputValue) => {
                                    setSearchTerm(newInputValue);
                                }}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Tìm kiếm danh mục..."
                                        variant="outlined"
                                        className="search-bar"
                                        sx={{
                                            width: '400px', // Set width to 400px
                                            marginRight: '16px', // Adjust margin if needed

                                            '& .css-19qnlrw-MuiFormLabel-root-MuiInputLabel-root': {
                                                lineHeight: '2.5em  !important', // Line height adjustment

                                            },
                                        }}
                                    />
                                )}
                                style={{ width: '400px', marginRight: '16px' }} // Style for the Autocomplete component
                            />


                            {/* <select
                                    value={sortOrder}
                                    onChange={handleSortChange}
                                    className="sort-select"
                                >
                                    <option value="desc">Sắp xếp theo mới nhất</option>
                                    <option value="asc">Sắp xếp theo cũ nhất</option>
                                </select> */}

                        </div>
                        <table>
                            <thead>
                                <tr>
                                    <th>STT</th>
                                    <th>Hình ảnh</th>
                                    <th>Tên danh mục</th>
                                    <th>Trạng thái</th>
                                    <th>Ngày tạo</th>
                                    <th>Ngày cập nhật</th>
                                    <th>Ngày xóa</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                {categories.length > 0 ? (
                                    categories.map((category, index) => (
                                        <tr key={category.cateId}>
                                            <td>{index + 1 + (currentPage - 1) * limit}</td>
                                            <td>
                                                <img src={category.cateImg} alt={category.cateName} className="cate-img" style={{ width: '80px', height: '80px' }} />
                                            </td>
                                            <td>{category.cateName}</td>
                                            <td>
                                                <label className="cate-switch">
                                                    <input
                                                        type="checkbox"
                                                        checked={category.isDeleted === false} // Use the state to check if it is active
                                                        onChange={() => handleSwitchChange(category.cateId)}
                                                    />
                                                    <span className="cate-slider round"></span>
                                                </label>
                                            </td>
                                            <td>
                                                {category.dateCreated
                                                    ? new Date(category.dateCreated).toLocaleDateString('vi-VN')
                                                    : ''
                                                }
                                            </td>
                                            <td>
                                                {category.dateUpdated
                                                    ? new Date(category.dateUpdated).toLocaleDateString('vi-VN')
                                                    : ''
                                                }
                                            </td>
                                            <td>
                                                {category.dateDeleted
                                                    ? new Date(category.dateDeleted).toLocaleDateString('vi-VN')
                                                    : ''
                                                }
                                            </td>
                                            <td>
                                                <div className='gr-btn-cate'>
                                                    <button
                                                        className="cate-btn"
                                                        onClick={() => {
                                                            setUpdateCategory(category);
                                                            setCategoryName(category.cateName);
                                                            setCategoryImagePreview(category.cateImg);
                                                            setIsUpdateMode(true);
                                                        }}
                                                    >
                                                        <i className="ti-pencil" style={{ color: "blue", fontSize: "20px" }}></i> {/* Edit icon */}
                                                    </button>

                                                    <button
                                                        className="cate-btn"
                                                        onClick={() => handleViewProduct(category.cateId, category.cateName)}
                                                    >
                                                        <i className="ti-zoom-in" style={{ color: "red", fontSize: "20px" }}></i> {/* View/eye icon */}
                                                    </button>

                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan="7">Không có danh mục nào.</td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                        {/* Phân trang */}
                        <div className="pagination">
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
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Category;
