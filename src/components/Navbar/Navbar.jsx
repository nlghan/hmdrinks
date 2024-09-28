import React, { useState } from 'react';
import './Navbar.css';
import { assets } from '../../assets/assets.js';

const Navbar = () => {
  const [menu, setMenu] = useState("TRANG CHỦ");

  return (
    <div className='navbar'>
      <img src={assets.logo} alt='' className="logo" />
      <ul className="navbar-menu">
        <li 
          className={menu === "TRANG CHỦ" ? "active" : ""} 
          onClick={() => setMenu("TRANG CHỦ")}
        >
          TRANG CHỦ
        </li>
        <li 
          className={menu === "GIỚI THIỆU" ? "active" : ""} 
          onClick={() => setMenu("GIỚI THIỆU")}
        >
          GIỚI THIỆU
        </li>
        <li 
          className={menu === "THỰC ĐƠN" ? "active" : ""} 
          onClick={() => setMenu("THỰC ĐƠN")}
        >
          THỰC ĐƠN
        </li>
        <li 
          className={menu === "TIN TỨC" ? "active" : ""} 
          onClick={() => setMenu("TIN TỨC")}
        >
          TIN TỨC
        </li>
        <li 
          className={menu === "LIÊN HỆ" ? "active" : ""} 
          onClick={() => setMenu("LIÊN HỆ")}
        >
          LIÊN HỆ
        </li>
      </ul>
      <div className="navbar-right">
        <button className='login'>Login</button>
        <button className='signup'>Create free account</button>
      </div>
    </div>
  );
}

export default Navbar;
