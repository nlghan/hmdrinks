import React, { useEffect, useState } from 'react';
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

  // Hide breadcrumb if on homepage, product detail page, or root URL
  const [showBreadcrumb, setShowBreadcrumb] = useState(location.pathname !== '/home' && location.pathname !== '/' && location.pathname !== '');
  // State to manage box shadow visibility
  const [showBoxShadow, setShowBoxShadow] = useState(location.pathname === '/home' || location.pathname.startsWith('/product/'));

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

  useEffect(() => {
    const handleClick = (e) => {
      createBubble(e);
    };

    const createBubble = (e) => {
      const bubble = document.createElement('span');
      bubble.classList.add('bubble');
      document.body.appendChild(bubble);

      const x = e.pageX;
      const y = e.pageY;

      bubble.style.left = `${x}px`;
      bubble.style.top = `${y}px`;

      setTimeout(() => {
        bubble.remove(); // Remove the bubble after animation ends
      }, 1000); // Match the animation duration in CSS
    };

    // Add event listener to the entire document for click events
    document.addEventListener('click', handleClick);

    // Clean up the event listener when the component unmounts
    return () => {
      document.removeEventListener('click', handleClick);
    };
  }, []); // Run only once when the component mounts

  // Update breadcrumb and box shadow visibility when route changes
  useEffect(() => {
    const isProductDetailPage = location.pathname.startsWith('/product/');
    setShowBreadcrumb(location.pathname !== '/home' && location.pathname !== '/' && location.pathname !== '' && !isProductDetailPage);
    setShowBoxShadow(location.pathname === '/home' || isProductDetailPage);
  }, [location.pathname]);

  return (
    <>
      <div className={`navbar ${showBoxShadow ? 'box-shadow' : ''}`}>
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

      {/* Breadcrumb is hidden on homepage and product detail page */}
      {showBreadcrumb && (
        <div className="progress-bar">
          <div className="progress-navigation">
            <span className="nav-item">Trang chủ</span>
            <span className="separator"> &gt; </span>
            <span className="nav-item">{currentPage}</span>
          </div>
        </div>
      )}
    </>
  );
};

export default Navbar;
