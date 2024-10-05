import React, { useState } from 'react';
import './User.css';
import { assets } from '../../assets/assets';

const User = () => {
    const [switches, setSwitches] = useState([true, false]); // Mảng để lưu trạng thái cho các switch

    const handleSwitchChange = (index) => {
        const newSwitches = [...switches];
        newSwitches[index] = !newSwitches[index]; // Chuyển đổi trạng thái của switch
        setSwitches(newSwitches);
    };

    const [isMenuOpen, setIsMenuOpen] = useState(false); // State to toggle menu visibility

    // Function to toggle the menu
    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    return (
        <div className="user-table">
            {isMenuOpen && <div className="user-dim-background" onClick={toggleMenu}></div>}

            <div className='user-menu-flex'>
                {isMenuOpen && (
                    <div className="user-side-menu">

                        <ul className="user-menu-items">
                            <img src={assets.logo} alt='' className="user-logo-menu" />
                            <div className='user-menu-and-user'>
                                <i className='ti-user' />
                                <li>Tài khoản</li>
                            </div>

                            <div className='user-menu-and-user'>
                                <i className='ti-package' />
                                <li>Sản phẩm</li>
                            </div>
                            <div className='user-menu-and-user'>
                                <i className='ti-pencil-alt' />
                                <li>Đơn hàng</li>
                            </div>
                            <div className='user-menu-and-user'>
                                <i className='ti-signal' />
                                <li>Tiếp thị</li>
                            </div><div className='user-menu-and-user'>
                                <i className='ti-share-alt' />
                                <li>Phản hồi</li>
                            </div>
                            <div className='user-menu-and-user'>
                                <i className='ti-image' />
                                <li>Analytics</li>
                            </div>

                            <div className='user-menu-and-user'>
                                <i className='ti-back-left' />
                                <li>Logout</li>
                            </div>


                        </ul>
                    </div>
                )}
            </div>
            <div className={`user-table-row ${isMenuOpen ? 'user-dimmed' : ''}`}>
                <div className="user-main-section">

                    <div className="user-box">
                        <div className="header-user-box">
                            <h2>Users</h2>
                            <button className="add-user-btn">Thêm người dùng +</button>
                        </div>
                        <table>
                            <thead>
                                <tr>
                                    <th>Tên đăng nhập</th>
                                    <th>Họ và tên</th>
                                    <th>Vai trò</th>
                                    <th>Trạng thái hoạt động</th>
                                    <th>Cập nhật</th>
                                </tr>
                            </thead>
                            <tbody>
                                {switches.map((isChecked, index) => (
                                    <tr key={index}>
                                        <td>User{index + 1}</td>
                                        <td>Nguyễn Văn {String.fromCharCode(65 + index)}</td>
                                        <td>{<td>{index === 0 ? 'Admin' : index === 1 ? 'User' : 'Shipper'}</td>}</td>
                                        <td>
                                            <label className="switch">
                                                <input
                                                    type="checkbox"
                                                    checked={isChecked}
                                                    onChange={() => handleSwitchChange(index)} // Gọi hàm khi có sự thay đổi
                                                />
                                                <span className="slider round"></span>
                                            </label>
                                        </td>
                                        <td>
                                            <button className="user-update-btn1">Cập nhật</button>
                                            <button className="user-update-btn2">Xóa</button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    <div className="user-stats-section">
                        <div className="user-stat-box">
                            <i className="ti-menu menu-toggle" onClick={toggleMenu}>
                            </i>
                        </div>

                        <div className="user-stat-box1">
                            <div className="user-percentage-circle">
                                <div className="user-inner-circle"></div>
                                <span>70%</span>
                            </div>
                            <div className="user-stat-boxicon"></div>
                            <div className="user-stat-boxtext1">
                                <h3>Admin</h3>
                            </div>
                            <div className="user-stat-boxcount">
                                <h4></h4>
                            </div>
                            <div className="user-stat-boxdetails">
                                <h5>Last 24 Hours</h5>
                            </div>
                        </div>
                        <div className="user-stat-box2">
                            <div className="user-percentage-circle2">
                                <div className="user-inner-circle2"></div>
                                <span>80%</span>
                            </div>
                            <div className="user-stat-boxicon"></div>
                            <div className="user-stat-boxtext1">
                                <h3>Customer</h3>
                            </div>
                            <div className="user-stat-boxcount">
                                <h4></h4>
                            </div>
                            <div className="user-stat-boxdetails">
                                <h5>Last 24 Hours</h5>
                            </div>
                        </div>
                        <div className="user-stat-box3">
                            <div className="user-percentage-circle3">
                                <div className="user-inner-circle3"></div>
                                <span>60%</span>
                            </div>
                            <div className="user-stat-boxicon"></div>
                            <div className="user-stat-boxtext1">
                                <h3>Shipper</h3>
                            </div>
                            <div className="user-stat-boxcount">
                                <h4></h4>
                            </div>
                            <div className="user-stat-boxdetails">
                                <h5>Last 24 Hours</h5>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    );
};

export default User;
