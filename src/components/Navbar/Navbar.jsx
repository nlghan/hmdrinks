import React, { useState } from 'react';
import './Navbar.css';
import { assets } from '../../assets/assets.js';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import Cookies from 'js-cookie';
import { useAuth } from '../../context/AuthProvider.jsx';

const Navbar = ({ currentPage }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isLoggedIn, logout } = useAuth();
  
  const [showBreadcrumb, setShowBreadcrumb] = useState(false); // State to manage breadcrumb visibility

  const handleLogin = () => {
    navigate('/login');
  };

  const handleRegister = () => {
    navigate('/register');
  };

  const handleLogout = async () => {
    const accessToken = Cookies.get('access_token');

    if (!accessToken) {
      console.error('No access token found. Unable to logout.');
      return;
    }

    try {
      const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/v1/auth/logout`, {}, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      console.log("Logged out:", response.data);
      Cookies.remove('access_token');
      Cookies.remove('refresh_token');
      logout();
      navigate('/home');
    } catch (error) {
      console.error('Error during logout:', error);
    }
  };

  const handleUserIconClick = () => {
    navigate('/info');
  };

  const handleCartIconClick = () => {
    navigate('/cart');
  };

  const handleFavIconClick = () => {
    navigate('/favorite');
  };

  const handleMouseEnter = () => {
    setShowBreadcrumb(true); // Show breadcrumb on hover
  };

  const handleMouseLeave = () => {
    setShowBreadcrumb(false); // Hide breadcrumb when not hovering
  };

  return (
    <>
      <div style={{
        backgroundColor: 'rgb(243, 208, 208)',
        height: "30px",
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontWeight: 'bold',
        fontSize: '16px'
      }}>
        Summer Sale For All Swim Suits And Free Express Delivery - OFF 50%!
      </div>

      <div 
        className='navbar' 
        onMouseEnter={handleMouseEnter} 
        onMouseLeave={handleMouseLeave} // Apply hover events to the entire navbar
      >
        <img src={assets.logo} alt='' className="logo" />
        <ul className="navbar-menu">
          <li 
            onClick={() => navigate('/home')} 
            className={location.pathname === '/home' ? 'active' : ''}
          >
            TRANG CHỦ
          </li>
          <li 
            onClick={() => navigate('/about')} 
            className={location.pathname === '/about' ? 'active' : ''}
          >
            GIỚI THIỆU
          </li>
          <li 
            onClick={() => navigate('/menu')} 
            className={location.pathname === '/menu' ? 'active' : ''}
          >
            THỰC ĐƠN
          </li>
          <li 
            onClick={() => navigate('/news')} 
            className={location.pathname === '/news' ? 'active' : ''}
          >
            TIN TỨC
          </li>
          <li 
            onClick={() => navigate('/contact')} 
            className={location.pathname === '/contact' ? 'active' : ''}
          >
            LIÊN HỆ
          </li>
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
              <i className="ti-heart" onClick={handleFavIconClick}></i>
              <i className="ti-user" onClick={handleUserIconClick}></i>
              <button className='signup' onClick={handleLogout}>Đăng Xuất</button>
            </>
          )}
        </div>
      </div>

      {/* Conditionally render the breadcrumb based on state */}
      <div className={`breadcrumb ${showBreadcrumb ? 'show' : ''}`}>
        <div className="breadcrumb-title">
          {currentPage}
        </div>
        <div className="breadcrumb-navigation">
          {`Trang chủ > ${currentPage}`}
        </div>
      </div>
    </>
  );
};

export default Navbar;
