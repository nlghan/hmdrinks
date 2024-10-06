import React from 'react';
import { assets } from '../../assets/assets'; 
import '../Menu/Menu.css';
import { useNavigate } from 'react-router-dom'; // Correct import for navigation

const Menu = ({ isMenuOpen, toggleMenu }) => {
    const navigate = useNavigate(); // Use the correct hook

    const handleUser = () => {
        navigate('/user'); // Navigate to the user page
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
                        <div className='menu-and-user'>
                            <i className='ti-package' />
                            <li>Sản phẩm</li>
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
                        <div className='menu-and-user'>
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
