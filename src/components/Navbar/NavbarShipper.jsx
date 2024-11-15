import React, { useEffect, useState } from 'react';
import './NavbarShipper.css';
import { assets } from '../../assets/assets.js';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import Cookies from 'js-cookie';
import { useAuth } from '../../context/AuthProvider.jsx';
import { useCart } from '../../context/CartContext.jsx';
import { useFavorite } from '../../context/FavoriteContext.jsx'

const NavbarShipper = ({ currentPage }) => {
    const navigate = useNavigate();
    const location = useLocation();
    const { isLoggedIn, logout } = useAuth();


    const { cartItems } = useCart();
    const { favoriteCount, clearFavorites } = useFavorite();
    const totalProducts = cartItems.reduce((acc, item) => acc + item.quantity, 0);
    const favCount = favoriteCount
    // Hide breadcrumb if on homepage, product detail page, or root URL
    const [showBreadcrumb, setShowBreadcrumb] = useState(location.pathname !== '/shipper-home' && location.pathname !== '/' && location.pathname !== '');
    // State to manage box shadow visibility
    const [showBoxShadow, setShowBoxShadow] = useState(location.pathname === '/shipper-home' || location.pathname.startsWith('/product/'));
    // State to manage the visibility of the navbar menu
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    // State to determine if the screen is small
    const [isSmallScreen, setIsSmallScreen] = useState(window.innerWidth < 768); // Adjust threshold as needed
    const [userAvatar, setUserAvatar] = useState('');
    const [userName, setUserName] = useState('');

    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Cannot decode token:", error);
            return null;
        }
    };

    // Add useEffect to fetch user info
    useEffect(() => {
        const fetchUserInfo = async () => {
            const token = Cookies.get('access_token');
            if (!token) return;

            const userId = getUserIdFromToken(token);
            if (!userId) return;

            try {
                const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/user/info/${userId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                setUserAvatar(response.data.avatar || '');
                setUserName(response.data.fullName || '');
            } catch (error) {
                console.error("Error fetching user info:", error);
            }
        };

        fetchUserInfo();
    }, []);

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
            clearFavorites();
            logout();
            navigate('/home');
        } catch (error) {
            console.error('Error during logout:', error);
        }
    };

    const handleUserIconClick = () => {
        navigate('/shipper-info');
    };

    const handleCartIconClick = () => {
        navigate('/cart');
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
            location.pathname !== '/shipper-home' &&
            location.pathname !== '/' &&
            location.pathname !== '' &&
            location.pathname !== '/change' && // Add this condition
            !isProductDetailPage
        );
        setShowBoxShadow(location.pathname === '/shipper-home' || isProductDetailPage);
    }, [location.pathname]);

    // Toggle menu visibility
    const toggleMenu = () => {
        setIsMenuOpen((prev) => !prev);
    };

    return (
        <>
            <div className={`shipper-navbar ${showBoxShadow ? 'shipper-box-shadow' : ''}`}>
                <img src={assets.logo} alt='' className="shipper-logo" />
                {isSmallScreen && (
                    <div className="shipper-navbar-toggle" onClick={toggleMenu}>
                        ☰
                    </div>
                )}
                <ul className={`shipper-navbar-menu ${isMenuOpen ? 'show' : ''}`}>
                    <li
                        onClick={() => {
                            console.log('Navigating to /shipper-home');
                            navigate('/shipper-home');
                        }}
                        className={location.pathname === '/shipper-home' ? 'shipper-active' : ''}
                    >
                        TRANG CHỦ
                    </li>
                    <li
                        onClick={() => navigate('/shipper-about')}
                        className={location.pathname === '/shipper-about' ? 'shipper-active' : ''}
                    >
                        GIỚI THIỆU
                    </li>
                    <li
                        onClick={() => navigate('/shipper-contact')}
                        className={location.pathname === '/shipper-contact' ? 'shipper-active' : ''}
                    >
                        LIÊN HỆ
                    </li>

                    {/* Group icons in a single div for better spacing control */}
                    {isLoggedIn && (
                        <div className="shipper-icon-group-nav">
                            <span className="welcome-text">Xin chào {userName}!</span>
                            {/* <li onClick={handleCartIconClick}>
                                <i className="ti-clipboard"></i>
                                {totalProducts > 0 && <span className="shipper-icon-badge-nav">{totalProducts}</span>}
                                {isSmallScreen && <span> Đơn Hàng</span>}
                            </li> */}
                            <li>
                                <div className="user-avatar-container">
                                    {userAvatar ? (
                                        <img
                                            src={userAvatar}
                                            alt="User Avatar"
                                            className="user-avatar"
                                            onClick={handleUserIconClick}
                                        />
                                    ) : (
                                        <img
                                            src={assets.avtrang}
                                            alt="Default Avatar"
                                            className="user-avatar"
                                            onClick={handleUserIconClick}
                                        />
                                    )}
                                    <span className="status-dot"></span>
                                </div>
                            </li>
                        </div>
                    )}
                    <li>
                        {isLoggedIn ? (
                            <button className='shipper-signup' style={{ marginRight: '80px' }} onClick={handleLogout}>Đăng Xuất</button>
                        ) : (
                            <button className='shipper-login1' onClick={handleLogin}>Đăng Nhập</button>
                        )}
                    </li>
                    {!isLoggedIn && (
                        <li style={{ marginRight: '10px' }}>
                            <button className='shipper-signup' onClick={handleRegister}>Đăng Ký</button>
                        </li>
                    )}
                </ul>
            </div>

            {/* {showBreadcrumb && (
        <div className="shipper-progress-bar">
          <div className="shipper-progress-navigation">
            <span className="shipper-nav-item">Trang chủ</span>
            <span className="shipper-separator"> &gt; </span>
            <span className="shipper-nav-item">{currentPage}</span>
          </div>
        </div>
      )} */}
        </>
    );
};

export default NavbarShipper;
