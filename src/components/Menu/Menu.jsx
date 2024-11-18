import React, { useEffect, useState } from 'react';
import { assets } from '../../assets/assets'; 
import '../Menu/Menu.css';
import { useNavigate } from 'react-router-dom';
import Cookies from 'js-cookie'; 
import axios from 'axios';

const Menu = ({ isMenuOpen, toggleMenu }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const loggedIn = sessionStorage.getItem("isLoggedIn");
        setIsLoggedIn(loggedIn === "true");
    }, []);

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
            sessionStorage.removeItem("isLoggedIn");
            Cookies.remove('access_token');
            Cookies.remove('refresh_token');
    
            setIsLoggedIn(false);
            navigate('/home');
        } catch (error) {
            console.error('Error during logout:', error);
        }
    };

    return (
        <>
            {isMenuOpen && <div className="menu-dim-background" onClick={toggleMenu}></div>}
            <div className={`menu-side-menu ${isMenuOpen ? 'expanded' : ''}`}>
                <ul className="menu-items">
                    <img 
                        src={isMenuOpen ? assets.logo : assets.logomini} 
                        alt='' 
                        className="menu-logo" 
                    />
                    <div className='menu-and-user' onClick={() => navigate('/dashboard')}>
                        <i className='ti-home' />
                        <li>Trang quản trị</li>
                    </div>
                    <div className='menu-and-user' onClick={() => navigate('/user')}>
                        <i className='ti-user' />
                        <li>Tài khoản</li>
                    </div>
                    <div className='menu-and-user' onClick={() => navigate('/category')}>
                        <i className='ti-package' />
                        <li>Danh mục</li>
                    </div>
                    <div className='menu-and-user' onClick={() => navigate('/product')}>
                        <i className='ti-paint-bucket' />
                        <li>Sản phẩm</li>
                    </div>
                    <div className='menu-and-user'>
                        <i className='ti-pencil-alt' />
                        <li>Đơn hàng</li>
                    </div>
                    <div className='menu-and-user' onClick={() => navigate('/news')}>
                        <i className='ti-signal' />
                        <li>Tiếp thị</li>
                    </div>
                    <div className='menu-and-user'>
                        <i className='ti-share-alt' onClick={() => navigate('/response')}/>
                        <li>Phản hồi</li>
                    </div>
                    <div className='menu-and-user'>
                        <i className='ti-image' onClick={() => navigate('/analytics')}/>
                        <li>Thống kê</li>
                    </div>
                    <div className='menu-and-user' onClick={handleLogout}>
                        <i className='ti-back-left' />
                        <li>Đăng xuất</li>
                    </div>
                </ul>
            </div>
        </>
    );
};

export default Menu;
