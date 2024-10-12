import React, { useEffect, useState } from 'react';
import './Navbar.css';
import { assets } from '../../assets/assets.js';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import Cookies from 'js-cookie';
import { useAuth } from '../../context/AuthProvider.jsx'; // Import useAuth để lấy context

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isLoggedIn, logout } = useAuth(); // Lấy trạng thái và hàm logout từ context

  const handleLogin = () => {
    navigate('/login');
  };

  const handleRegister = () => {
    navigate('/register');
  };

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
      
      // Xóa token trong cookies
      Cookies.remove('access_token');
      Cookies.remove('refresh_token'); 

      // Gọi hàm logout từ AuthContext để cập nhật trạng thái
      logout();

      // Điều hướng về trang chủ sau khi đăng xuất
      navigate('/home');
    } catch (error) {
      console.error('Error during logout:', error);
    }
  };

  const handleUserIconClick = () => {
    navigate('/info'); 
  };

  const handleCartIconClick = () => {
    // navigate('/cart'); 
  };

  const handleMenuIconClick = () => {
    // console.log("Menu clicked");
  };

  return (
    <div className='navbar'>
      <img src={assets.logo} alt='' className="logo" />
      <ul className="navbar-menu">
        <li onClick={() => navigate('/home')} className={location.pathname === '/home' ? 'active' : ''}>TRANG CHỦ</li>
        <li onClick={() => navigate('/about')} className={location.pathname === '/about' ? 'active' : ''}>GIỚI THIỆU</li>
        <li onClick={() => navigate('/menu')} className={location.pathname === '/menu' ? 'active' : ''}>THỰC ĐƠN</li>
        <li onClick={() => navigate('/news')} className={location.pathname === '/news' ? 'active' : ''}>TIN TỨC</li>
        <li onClick={() => navigate('/contact')} className={location.pathname === '/contact' ? 'active' : ''}>LIÊN HỆ</li>
      </ul>
      <div className="navbar-right">
        {!isLoggedIn ? (
          <>
            <button className='login1' onClick={handleLogin}>Đăng Nhập</button>
            <button className='signup' onClick={handleRegister}>Đăng Ký</button>
          </>
        ) : (
          <>
            <i className="ti-shopping-cart" onClick={handleCartIconClick}></i>
            <i className="ti-user" onClick={handleUserIconClick}></i>
            <button className='signup' onClick={handleLogout}>Đăng Xuất</button>
          </>
        )}
      </div>
    </div>
  );
};

export default Navbar;
