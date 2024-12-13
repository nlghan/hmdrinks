import React, { useEffect, useState } from 'react';
import './Navbar.css';
import { assets } from '../../assets/assets.js';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import axiosInstance from '../../utils/axiosConfig';
import Cookies from 'js-cookie';
import { useAuth } from '../../context/AuthProvider.jsx';
import { useCart } from '../../context/CartContext.jsx';
import { useFavorite } from '../../context/FavoriteContext.jsx'

const Navbar = ({ currentPage }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isLoggedIn, logout } = useAuth();


  const { cartItems } = useCart();
  const { favoriteCount, clearFavorites } = useFavorite();
  const totalProducts = cartItems.reduce((acc, item) => acc + item.quantity, 0);
  const favCount = favoriteCount
  // Hide breadcrumb if on homepage, product detail page, or root URL
  const [showBreadcrumb, setShowBreadcrumb] = useState(location.pathname !== '/home' && location.pathname !== '/' && location.pathname !== '');
  // State to manage box shadow visibility
  const [showBoxShadow, setShowBoxShadow] = useState(location.pathname === '/home' || location.pathname.startsWith('/product/'));
  // State to manage the visibility of the navbar menu
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  // State to determine if the screen is small
  const [isSmallScreen, setIsSmallScreen] = useState(window.innerWidth < 1400); // Adjust threshold as needed
  const [isDropdownVisible, setIsDropdownVisible] = useState(false);

  const handleMouseEnter = () => {
    setIsDropdownVisible(true);
  };

  const handleMouseLeave = () => {
    setIsDropdownVisible(false);
  };

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
      const response = await axiosInstance.post(`${import.meta.env.VITE_API_BASE_URL}/v1/auth/logout`, {}, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      console.log("Logged out:", response.data);
      Cookies.remove('access_token');
      Cookies.remove('refresh_token');
      clearFavorites();
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
    const handleResize = () => {
      setIsSmallScreen(window.innerWidth < 1571); // Adjust threshold as needed
    };

    // Listen for resize events
    window.addEventListener('resize', handleResize);

    // Clean up the event listener when the component unmounts
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

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
    setShowBreadcrumb(
      location.pathname !== '/home' &&
      location.pathname !== '/' &&
      location.pathname !== '' &&
      location.pathname !== '/change' && // Add this condition
      !isProductDetailPage
    );
    setShowBoxShadow(location.pathname === '/home' || isProductDetailPage);
  }, [location.pathname]);

  // Toggle menu visibility
  const toggleMenu = () => {
    setIsMenuOpen((prev) => !prev);
  };

  return (
    <>
      <div className={`navbar ${showBoxShadow ? 'box-shadow' : ''}`}>
        <img src={assets.logo} alt='' className="logo" />
        {/* Only show the toggle button on small screens */}
        {isSmallScreen && (
          <div className="navbar-toggle" onClick={toggleMenu}>
            ☰
          </div>
        )}
        <ul className={`navbar-menu ${isMenuOpen ? 'show' : ''}`}>
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
            onClick={() => navigate('/post')}
            className={location.pathname === '/post' ? 'active' : ''}
          >
            TIN TỨC
          </li>
          <li
            onClick={() => navigate('/contact')}
            className={location.pathname === '/contact' ? 'active' : ''}
          >
            LIÊN HỆ
          </li>

          {/* Group icons in a single div for better spacing control */}
          {isLoggedIn && (
            <div className="icon-group-nav">
              <li onClick={handleCartIconClick}>
                <i className="ti-shopping-cart"></i>
                {totalProducts > 0 && <span className="icon-badge-nav">{totalProducts}</span>}
                {isSmallScreen && <span> Giỏ Hàng</span>}
              </li>
              <li onClick={handleFavIconClick}>
                <i className="ti-heart"></i>
                {favCount > 0 && <span className="icon-badge-nav">{favCount}</span>}
                {isSmallScreen && <span> Yêu Thích</span>}
              </li>
              {/* User Icon with Dropdown */}
              <li className="user-menu">
                <i className="ti-user"></i>
                {isSmallScreen && <span> Thông Tin</span>}
                <div className="dropdown-menu">
                  <div onClick={() => navigate('/info')} className="dropdown-item">
                    Thông tin cá nhân
                  </div>
                  <div className="dropdown-divider"></div> {/* Dòng phân cách */}
                  <div onClick={() => navigate('/my-orders')} className="dropdown-item">
                    Đơn hàng của tôi
                  </div>
                </div>
              </li>


            </div>
          )}
          <li>
            {isLoggedIn ? (
              <button className='signup' style={{ marginRight: '80px' }} onClick={handleLogout}>Đăng Xuất</button>
            ) : (
              <button className='login1' onClick={handleLogin}>Đăng Nhập</button>
            )}
          </li>
          {!isLoggedIn && (
            <li style={{ marginRight: '10px' }}>
              <button className='signup' onClick={handleRegister}>Đăng Ký</button>
            </li>
          )}
        </ul>
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
