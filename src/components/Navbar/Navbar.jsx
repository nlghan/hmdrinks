import React, { useEffect, useState } from 'react';
import './Navbar.css';
import { assets } from '../../assets/assets.js';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios'; // Nhập axios để gửi yêu cầu HTTP
import Cookies from 'js-cookie'; // Nhập thư viện js-cookie để quản lý cookies

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    const loggedIn = sessionStorage.getItem("isLoggedIn");
    setIsLoggedIn(loggedIn === "true");
  }, []);

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
      const response = await axios.post('http://localhost:1010/api/v1/auth/logout', {}, {
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
      navigate('/'); 
      window.location.reload();
    } catch (error) {
      console.error('Error during logout:', error);
    }
  };

  const handleUserIconClick = () => {
    // navigate('/user'); 
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
            <button className='login' onClick={handleLogin}>Đăng Nhập</button>
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
