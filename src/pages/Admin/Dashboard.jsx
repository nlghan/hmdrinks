import React, { useState } from 'react';
import './Dashboard.css';
import { assets } from '../../assets/assets';


const Dashboard = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false); // State to toggle menu visibility

    // Function to toggle the menu
    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    return (
        <div className="dashboard">
            {/* Background dimming effect */}
            {isMenuOpen && <div className="dim-background" onClick={toggleMenu}></div>}

            <div className='menu-flex'>
                {isMenuOpen && (
                    <div className="side-menu">

                        <ul className="menu-items">
                            <img src={assets.logo} alt='' className="logo-menu" />
                            <div className='menu-and-user'>
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
                            </div><div className='menu-and-user'>
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
            </div>

            <div className={`dashboard-row ${isMenuOpen ? 'dimmed' : ''}`}>
                <div className="main-section">
                    <div className='flex'>
                        <h1>Dashboard</h1>
                    </div>

                    <div className="stats-section">
                        <div className="stat-box1">
                            <div className="percentage-circle">
                                <div className="inner-circle"></div>
                                <span>70%</span>
                            </div>
                            <div className="icon">📊</div>
                            <div className="text1">
                                <h3>Sales</h3>
                            </div>
                            <div className="count">
                                <h4>$200,000</h4>
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
                            <div className="icon">💰</div>
                            <div className="text1">
                                <h3>Revenue</h3>
                            </div>
                            <div className="count">
                                <h4>$200,000</h4>
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
                            <div className="icon">💸</div>
                            <div className="text1">
                                <h3>Expenses</h3>
                            </div>
                            <div className="count">
                                <h4>$200,000</h4>
                            </div>
                            <div className="details">
                                <h5>Last 24 Hours</h5>
                            </div>
                        </div>
                    </div>

                    <div className="orders-box">
                        <h2>Recent Orders</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>Tracking ID</th>
                                    <th>Date</th>
                                    <th>Status</th>
                                    <th>Additional Details</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Milk Tea</td>
                                    <td>123456</td>
                                    <td>2024-09-28</td>
                                    <td><span className="status-approved">Approved</span></td>
                                    <td><span className="text-details">Details</span></td>
                                </tr>
                                <tr>
                                    <td>Coffee</td>
                                    <td>654321</td>
                                    <td>2024-09-29</td>
                                    <td><span className="status-pending">Pending</span></td>
                                    <td><span className="text-details">Details</span></td>
                                </tr>
                                <tr>
                                    <td>Milk Tea</td>
                                    <td>123456</td>
                                    <td>2024-09-28</td>
                                    <td><span className="status-approved">Approved</span></td>
                                    <td><span className="text-details">Details</span></td>
                                </tr>
                                <tr>
                                    <td>Coffee</td>
                                    <td>654321</td>
                                    <td>2024-09-29</td>
                                    <td><span className="status-delivered">Delivered</span></td>
                                    <td><span className="text-details">Details</span></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div className="side-section">
                    {/* Replaced button with TiMenu icon */}
                    <i className=" ti-menu menu-toggle" onClick={toggleMenu}>

                    </i>
                    <div className="updates-box">
                        <h2>Recent Updates</h2>
                        <ul>
                            <li>
                                <img src="path/to/imageA.jpg" alt="User A" className="update-image" />
                                <span>User A updated profile</span>
                                <span>1 min ago</span>
                            </li>
                            <li>
                                <img src="path/to/imageB.jpg" alt="User B" className="update-image" />
                                <span>User B updated payment method</span>
                                <span>1 min ago</span>
                            </li>
                            <li>
                                <img src="path/to/imageC.jpg" alt="User C" className="update-image" />
                                <span>User C updated shipping address</span>
                                <span>1 min ago</span>
                            </li>
                        </ul>
                    </div>

                    <div className="review-chart">
                        <h2>Custom Review</h2>
                        <div className="chart-placeholder">Chart goes here</div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
