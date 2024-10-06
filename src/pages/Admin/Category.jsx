import React, { useEffect, useState } from 'react';
import './Category.css'; // CSS cho component
import Cookies from 'js-cookie';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Header from '../../components/Header/Header';


const Category = () => {
    const navigate = useNavigate();
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [categoryName, setCategoryName] = useState('');
    const [categoryImage, setCategoryImage] = useState(null);
    const [categoryImagePreview, setCategoryImagePreview] = useState(null);
    const [isActive, setIsActive] = useState(false);
    const [categories, setCategories] = useState([]);
    const [sortOrder, setSortOrder] = useState('asc');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState('');

    const [isUpdateMode, setIsUpdateMode] = useState(false); // Track if we are in update mode
    const [updateCategory, setUpdateCategory] = useState(null); // The category to update
    const [searchTerm, setSearchTerm] = useState('');



    // Thêm biến trạng thái phân trang
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 5; // Số danh mục mỗi trang
    const totalPages = Math.ceil(categories.length / itemsPerPage); // Tổng số trang

    // Hàm lấy userId từ token
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

    // Hàm lấy cookie
    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);

        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    useEffect(() => {
        const fetchCategories = async () => {
            setLoading(true);
            try {
                const token = getCookie('access_token');
                if (!token) {
                    setError("Bạn cần đăng nhập để xem thông tin này.");
                    setLoading(false);
                    return;
                }

                // Gọi API để lấy danh sách danh mục
                const response = await axios.get('http://localhost:1010/api/cate/list-category', {
                    headers: {
                        'Accept': '*/*',
                        'Authorization': `Bearer ${token}`
                    }
                });

                // Kiểm tra dữ liệu trả về
                if (response.data && response.data.categoryResponseList) {
                    setCategories(response.data.categoryResponseList); // Cập nhật danh sách danh mục
                } else {
                    setError("Không có dữ liệu danh mục.");
                }
            } catch (error) {
                console.error("Lỗi khi lấy danh sách danh mục:", error);
                setError("Không thể lấy danh sách danh mục.");
            } finally {
                setLoading(false);
            }
        };

        fetchCategories();
    }, []);

    if (loading) {
        return <div>Loading...</div>; // Hiển thị khi đang tải
    }

    if (error) {
        return <div>{error}</div>; // Hiển thị thông báo lỗi
    }

    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    const handleLogout = async () => {
        const accessToken = Cookies.get('access_token');
        if (!accessToken) {
            console.error('No access token found. Unable to logout.');
            return;
        }
        try {
            await axios.post('http://localhost:1010/api/v1/auth/logout', {}, {
                headers: {
                    'Authorization': `Bearer ${accessToken}`,
                },
            });
            sessionStorage.removeItem("isLoggedIn");
            Cookies.remove('access_token');
            Cookies.remove('refresh_token');
            setIsLoggedIn(false);
            navigate('/home');
            window.location.reload();
        } catch (error) {
            console.error('Error during logout:', error);
        }
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

    const handleSwitchChange = (index) => {
        const updatedCategories = [...categories];
        updatedCategories[index].active = !updatedCategories[index].active;
        setCategories(updatedCategories);
    };

    const handleAddCategory = async () => {
        if (categoryName && categoryImage) {
            const token = getCookie('access_token');
            const categoryData = {
                cateName: categoryName,
                cateImg: ''
            };

            try {
                const response = await axios.post('http://localhost:1010/api/cate/create-category', categoryData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                const newCategory = response.data;
                const cateId = newCategory.cateId;

                const formData = new FormData();
                formData.append('file', categoryImage);

                const uploadResponse = await axios.post(`http://localhost:1010/api/image/cate/upload?cateId=${cateId}`, formData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'multipart/form-data'
                    }
                });

                const imageUrl = uploadResponse.data.url;
                newCategory.cateImg = imageUrl;

                setCategories([...categories, newCategory]);
                setSuccessMessage("Thêm danh mục thành công!");
                setTimeout(() => setSuccessMessage(''), 1000);

                setCategoryName('');
                setCategoryImage(null);
                setCategoryImagePreview(null);
                setIsActive(false);
                document.getElementById('categoryImage').value = '';

            } catch (error) {
                console.error("Lỗi khi thêm danh mục:", error);
                alert("Không thể thêm danh mục. Vui lòng thử lại.");
            }
        } else {
            alert("Vui lòng nhập tên danh mục và chọn hình ảnh.");
        }
    };

    const handleDeleteCategory = (index) => {
        const updatedCategories = [...categories];
        updatedCategories.splice(index, 1);
        setCategories(updatedCategories);
    };

    const handleSortChange = (e) => {
        const order = e.target.value;
        setSortOrder(order);
        const sortedCategories = [...categories].sort((a, b) => {
            if (order === 'asc') {
                return a.cateName.localeCompare(b.cateName);
            } else {
                return b.cateName.localeCompare(a.cateName);
            }
        });
        setCategories(sortedCategories);
    };

    // Tính toán danh sách các danh mục để hiển thị dựa trên phân trang
    const indexOfLastCategory = currentPage * itemsPerPage;
    const indexOfFirstCategory = indexOfLastCategory - itemsPerPage;
    const currentCategories = categories.slice(indexOfFirstCategory, indexOfLastCategory);

    const changePage = (pageNumber) => {
        if (pageNumber < 1 || pageNumber > totalPages) return; // Ngăn không cho chuyển đến trang không hợp lệ
        setCurrentPage(pageNumber);
    };

    const getPaginationNumbers = () => {
        const paginationNumbers = [];
        const maxButtons = 5; // Max page buttons to display

        // Show ellipsis when there are more than maxButtons pages
        if (totalPages <= maxButtons) {
            for (let i = 1; i <= totalPages; i++) {
                paginationNumbers.push(i);
            }
        } else {
            // Always show the first page
            paginationNumbers.push(1);

            if (currentPage > 1) {
                paginationNumbers.push('...'); // Ellipsis if the current page is more than 3
            }

            const startPage = Math.max(2, currentPage - 1); // Start showing pages from the second page
            const endPage = Math.min(totalPages - 1, currentPage + 1); // End showing pages one before last page

            for (let i = startPage; i <= endPage; i++) {
                paginationNumbers.push(i);
            }

            if (currentPage < totalPages - 2) {
                paginationNumbers.push('...'); // Ellipsis if current page is less than total pages - 2
            }

            paginationNumbers.push(totalPages); // Always show the last page
        }

        return paginationNumbers;
    };

    const handleUpdateCategory = async () => {
        if (updateCategory && categoryName) {
            const token = getCookie('access_token');
            let updatedCategoryData = {
                cateId: updateCategory.cateId,  // Ensure this is the correct category ID
                cateName: categoryName,
                cateImg: updateCategory.cateImg // By default, keep the old image if no new image is uploaded
            };

            try {
                // If a new image is selected, upload it first
                if (categoryImage) {
                    const formData = new FormData();
                    formData.append('file', categoryImage);

                    // Upload the image to get the new URL
                    const uploadResponse = await axios.post(`http://localhost:1010/api/image/cate/upload?cateId=${updateCategory.cateId}`, formData, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'multipart/form-data'
                        }
                    });

                    // Update the cateImg URL after successful upload
                    updatedCategoryData.cateImg = uploadResponse.data.url;
                }

                // Make the PUT request to update the category with the new data
                await axios.put('http://localhost:1010/api/cate/update', updatedCategoryData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                // Update the categories in the state with the updated category
                const updatedCategories = categories.map((category) =>
                    category.cateId === updateCategory.cateId ? updatedCategoryData : category
                );
                setCategories(updatedCategories);
                setSuccessMessage("Cập nhật danh mục thành công!");
                setTimeout(() => setSuccessMessage(''), 1000);

                // Reset the form after successful update
                resetForm();

            } catch (error) {
                console.error("Lỗi khi cập nhật danh mục:", error);
                alert("Không thể cập nhật danh mục. Vui lòng thử lại.");
            }
        } else {
            alert("Vui lòng nhập tên danh mục.");
        }
    };
    const filteredCategories = categories.filter((category) =>
        category.cateName.toLowerCase().includes(searchTerm.toLowerCase())
    );



    const resetForm = () => {
        setCategoryName('');
        setCategoryImage(null);
        setCategoryImagePreview(null);
        setIsActive(false);
        setIsUpdateMode(false);
        setUpdateCategory(null);
        document.getElementById('categoryImage').value = ''; // reset input file
    };




    return (
        <div className="category">
            <Header isMenuOpen={isMenuOpen} toggleMenu={toggleMenu} title="Danh mục" />
            <div className={`category-row ${isMenuOpen ? 'dimmed' : ''}`}>
                <div className="side-section-cate">
                    <div className="updates-box">
                        <h2>{isUpdateMode ? 'Cập nhật danh mục đồ uống' : 'Thêm danh mục đồ uống'}</h2>

                        <form className="category-form">
                            <div className="form-group">
                                <label htmlFor="categoryName">Tên danh mục</label>
                                <input
                                    type="text"
                                    id="categoryName"
                                    value={categoryName}
                                    onChange={(e) => setCategoryName(e.target.value)}
                                    placeholder="Nhập tên danh mục"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="categoryImage">Hình ảnh danh mục</label>
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
                            <div className="form-group">
                                <label>
                                    <input
                                        className='check-box-active'
                                        type="checkbox"
                                        checked={isActive}
                                        onChange={(e) => setIsActive(e.target.checked)}
                                    />
                                    Kích hoạt
                                </label>
                            </div>
                            <div className="form-actions">
                                <button
                                    type="button"
                                    className="btn-add"
                                    onClick={isUpdateMode ? handleUpdateCategory : handleAddCategory}
                                >
                                    {isUpdateMode ? 'Cập nhật' : 'Thêm danh mục'}
                                </button>

                            </div>
                            {successMessage && <div className="success-message">{successMessage}</div>}
                        </form>
                    </div>
                </div>
                <div className="main-section-cate">
                    <div className="list-cate-box">
                        <div className="list-header">
                            <h2>Danh mục các loại đồ uống</h2>
                            <div className="search-sort-container">
                                <input
                                    type="search"
                                    placeholder="Tìm kiếm danh mục..."
                                    className="search-bar"
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                />
                                <select
                                    value={sortOrder}
                                    onChange={handleSortChange}
                                    className="sort-select"
                                >
                                    <option value="asc">Sắp xếp A-Z</option>
                                    <option value="desc">Sắp xếp Z-A</option>
                                </select>
                            </div>
                        </div>
                        <table>
                            <thead>
                                <tr>
                                    <th>STT</th>
                                    <th>Hình ảnh</th>
                                    <th>Tên danh mục</th>
                                    <th>Trạng thái</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                {currentCategories.length > 0 ? (
                                    currentCategories.map((category, index) => (
                                        <tr key={index}>
                                            <td>{index + 1 + indexOfFirstCategory}</td>
                                            <td>
                                                <img src={category.cateImg} alt={category.cateName} className="cate-img" style={{ width: '80px', height: '80px' }} />
                                            </td>
                                            <td>{category.cateName}</td>
                                            <td>
                                                <label className="cate-switch">
                                                    <input
                                                        type="checkbox"
                                                        checked={category.active || false}
                                                        onChange={() => handleSwitchChange(index + indexOfFirstCategory)}
                                                    />
                                                    <span className="cate-slider round"></span>
                                                </label>
                                            </td>
                                            <td>
                                                <div className='gr-btn-cate'>
                                                    <button
                                                        className="btn-add"
                                                        onClick={() => {
                                                            setUpdateCategory(category);
                                                            setCategoryName(category.cateName);
                                                            setCategoryImagePreview(category.cateImg);
                                                            setIsUpdateMode(true);
                                                        }}
                                                    >
                                                        Cập nhật
                                                    </button>
                                                    <button className="btn-clear" onClick={() => handleDeleteCategory(index + indexOfFirstCategory)}>Xóa</button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan="5">Không có danh mục nào.</td>
                                    </tr>
                                )}
                            </tbody>

                        </table>
                        {/* Phân trang */}
                        <div className="main-section-cate">
                            <div className="list-cate-box">
                                {/* Header and table code here... */}
                                <div className="pagination">
                                    <button
                                        className="btn btn-pre me-2"
                                        onClick={() => changePage(currentPage - 1)}
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
                                                    changePage(number);
                                                }
                                            }}
                                            disabled={number === '...'} // Disable button for ellipsis
                                        >
                                            {number}
                                        </button>
                                    ))}
                                    <button
                                        className="btn btn-next"
                                        onClick={() => changePage(currentPage + 1)}
                                        disabled={currentPage === totalPages}
                                    >
                                        &gt;
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Category;
