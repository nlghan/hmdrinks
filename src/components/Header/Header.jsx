import React from 'react';
import './Header.css'; // Tạo một file CSS riêng nếu cần, hoặc giữ CSS trong file tổng
import Menu from '../Menu/Menu';
const Header = ({ isMenuOpen, toggleMenu, title }) => {
    return (
        <div className="header">
            <Menu isMenuOpen={isMenuOpen} toggleMenu={toggleMenu} />
            <div className='flex-header'>
            <i className="ti-menu header-menu-toggle" onClick={toggleMenu}></i>
                <h1>{title}</h1> {/* Title sẽ là dynamic */}
               
            </div>
        </div>
    );
};

export default Header;
