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
    const [categoryImagePreview, setCategoryImagePreview] = useState(null); // State cho ảnh preview
    const [isActive, setIsActive] = useState(false);
    const [categories, setCategories] = useState([]);
    const [sortOrder, setSortOrder] = useState('asc'); // Trạng thái cho sắp xếp
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

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
                setCategoryImagePreview(reader.result); // Cập nhật ảnh preview
            };
            reader.readAsDataURL(file); // Đọc file dưới dạng URL
        } else {
            setCategoryImage(null);
            setCategoryImagePreview(null);
        }
    };

    const handleSwitchChange = (index) => {
        const updatedCategories = [...categories];
        updatedCategories[index].active = !updatedCategories[index].active; // Đảo ngược trạng thái
        setCategories(updatedCategories); // Cập nhật lại danh sách danh mục
    };

    const handleAddCategory = () => {
        if (categoryName && categoryImage) {
            const newCategory = {
                cateId: categories.length + 1, // Cung cấp cateId giả
                cateName: categoryName,
                cateImg: categoryImagePreview, // Sử dụng ảnh preview
                active: isActive,
            };
            setCategories([...categories, newCategory]);
            setCategoryName('');
            setCategoryImage(null);
            setCategoryImagePreview(null); // Reset preview
            setIsActive(false);
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
                return a.cateName.localeCompare(b.cateName); // Sử dụng cateName
            } else {
                return b.cateName.localeCompare(a.cateName);
            }
        });
        setCategories(sortedCategories);
    };

    return (
        <div className="category">
            <Header isMenuOpen={isMenuOpen} toggleMenu={toggleMenu} title="Danh mục" />
            <div className={`category-row ${isMenuOpen ? 'dimmed' : ''}`}>
                <div className="side-section-cate">
                    <div className="updates-box">
                        <h2>Thêm danh mục đồ uống</h2>
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
                                    onChange={handleImageChange} // Thay đổi ảnh
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
                                    onClick={handleAddCategory}
                                >
                                    Thêm
                                </button>
                                <button
                                    type="button"
                                    className="btn-clear"
                                    onClick={() => {
                                        setCategoryName('');
                                        setCategoryImage(null);
                                        setCategoryImagePreview(null);
                                        setIsActive(false);
                                    }}
                                >
                                    Xóa
                                </button>
                            </div>
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
                                {categories.length > 0 ? (
                                    categories.map((category, index) => (
                                        <tr key={index}>
                                            <td>{index + 1}</td>
                                            
                                            <td>
                                                <img src={category.cateImg} alt={category.cateName} className="cate-img" style={{ width: '80px', height: '80px' }} /> {/* Cập nhật thành cateImg */}
                                            </td>
                                            <td>{category.cateName}</td> {/* Cập nhật thành cateName */}
                                            <td>
                                                <label className="cate-switch">
                                                    <input
                                                        type="checkbox"
                                                        checked={category.active || false} // Đảm bảo checked không bị lỗi
                                                        onChange={() => handleSwitchChange(index)}
                                                    />
                                                    <span className="cate-slider round"></span>
                                                </label>
                                            </td>
                                            <td>
                                                <div className='gr-btn-cate'>
                                                    <button className="btn-add">Cập nhật</button>
                                                    <button className="btn-clear" onClick={() => handleDeleteCategory(index)}>Xóa</button>
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
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Category;
