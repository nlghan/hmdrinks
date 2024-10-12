import React, { useEffect, useState } from 'react';
import { assets } from '../../assets/assets'; 
import '../Menu/Menu.css';
import { useNavigate } from 'react-router-dom'; // Correct import for navigation
import Cookies from 'js-cookie'; 
import axios from 'axios';

const Menu = ({ isMenuOpen, toggleMenu }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    useEffect(() => {
        const loggedIn = sessionStorage.getItem("isLoggedIn");
        setIsLoggedIn(loggedIn === "true");
      }, []);

      const handleLogout = async () => {
        const accessToken = Cookies.get('access_token'); // Lấy accessToken từ cookies
    
        if (!accessToken) {
          console.error('No access token found. Unable to logout.');
          return;
        }
    
        try {
          // Gửi yêu cầu đăng xuất đến backend
          const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/v1/auth/logout`, {}, {
            headers: {
              'Authorization': `Bearer ${accessToken}`, // Gửi token trong header
            },
          });
    
          console.log("Logged out:", response.data);
          
          // Xóa token và cập nhật trạng thái đăng nhập
          sessionStorage.removeItem("isLoggedIn");
          Cookies.remove('access_token'); // Xóa token trong cookies
          Cookies.remove('refresh_token'); // Xóa refresh token nếu cần
    
          setIsLoggedIn(false); // Cập nhật trạng thái đăng nhập
          navigate('/home'); 
        } catch (error) {
          console.error('Error during logout:', error);
        }
      };
    const navigate = useNavigate(); // Use the correct hook

    const handleUser = () => {
        navigate('/user'); // Navigate to the user page
    };

    const handleCate = () => {
        navigate('/category'); // Navigate to the user page
    };

    const handleDashboard = () => {
        navigate('/dashboard'); // Navigate to the user page
    };

    return (
        <>
            {isMenuOpen && <div className="menu-dim-background" onClick={toggleMenu}></div>}
            {isMenuOpen && (
                <div className="menu-side-menu">
                    <ul className="menu-items">
                        <img src={assets.logo} alt='' className="menu-logo" />
                        <div className='menu-and-user' onClick={handleDashboard}>
                            <i className='ti-home' />
                            <li>Trang quản trị</li>
                        </div>
                        <div className='menu-and-user' onClick={handleUser}>
                            <i className='ti-user' />
                            <li>Tài khoản</li>
                        </div>
                        <div className='menu-and-user' onClick={handleCate}>
                            <i className='ti-package' />
                            <li>Danh mục</li>
                        </div>
                        <div className='menu-and-user'>
                            <i className='ti-pencil-alt' />
                            <li>Đơn hàng</li>
                        </div>
                        <div className='menu-and-user'>
                            <i className='ti-signal' />
                            <li>Tiếp thị</li>
                        </div>
                        <div className='menu-and-user'>
                            <i className='ti-share-alt' />
                            <li>Phản hồi</li>
                        </div>
                        <div className='menu-and-user'>
                            <i className='ti-image' />
                            <li>Analytics</li>
                        </div>
                        <div className='menu-and-user' onClick={handleLogout}>
                            <i className='ti-back-left' />
                            <li>Logout</li>
                        </div>
                    </ul>
                </div>
            )}
        </>
    );
};

export default Menu;
