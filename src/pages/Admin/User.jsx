import React, { useState } from 'react';
import './User.css';

const Dashboard = () => {
    const [switches, setSwitches] = useState([true, false]); // Mảng để lưu trạng thái cho các switch

    const handleSwitchChange = (index) => {
        const newSwitches = [...switches];
        newSwitches[index] = !newSwitches[index]; // Chuyển đổi trạng thái của switch
        setSwitches(newSwitches);
    };

    return (
        <div className="dashboard">
            <div className="dashboard-row">
                <div className="main-section">
                    <div className="orders-box">
                        <div className="header">
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
                                            <button className="update-btn1">Cập nhật</button>
                                            <button className="update-btn2">Xóa</button>                                           
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    <div className="stats-section">
                        <div className="stat-box1">
                            <div className="percentage-circle">
                                <div className="inner-circle"></div>
                                <span>70%</span>
                            </div>
                            <div className="icon"></div>
                            <div className="text1">
                                <h3>Admin</h3>
                            </div>
                            <div className="count">
                                <h4></h4>
                            </div>
                            <div className="details">
                                <h5>Last 24 Hours</h5>
                            </div>
                        </div>
                        <div className="stat-box2">
                            <div className="percentage-circle2">
                                <div className="inner-circle2"></div>
                                <span>80%</span>
                            </div>
                            <div className="icon"></div>
                            <div className="text1">
                                <h3>Customer</h3>
                            </div>
                            <div className="count">
                                <h4></h4>
                            </div>
                            <div className="details">
                                <h5>Last 24 Hours</h5>
                            </div>
                        </div>
                        <div className="stat-box3">
                            <div className="percentage-circle3">
                                <div className="inner-circle3"></div>
                                <span>60%</span>
                            </div>
                            <div className="icon"></div>
                            <div className="text1">
                                <h3>Shipper</h3>
                            </div>
                            <div className="count">
                                <h4></h4>
                            </div>
                            <div className="details">
                                <h5>Last 24 Hours</h5>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    );
};

export default Dashboard;
