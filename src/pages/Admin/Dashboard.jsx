import React, { useEffect, useState } from 'react';
import './Dashboard.css';
import { assets } from '../../assets/assets';
import Cookies from 'js-cookie';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Header from '../../components/Header/Header';
import LineChart from '../../components/Charts/LineChart';
import GaugeCard from '../../components/Card/GaugeCardDash';

const Dashboard = () => {
    const navigate = useNavigate();

    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [users, setUsers] = useState([]);
    const [error, setError] = useState("");

    useEffect(() => {
        const loggedIn = sessionStorage.getItem("isLoggedIn");
        setIsLoggedIn(loggedIn === "true");
    }, []);

    const fetchUsers = async (page = 1, limit = 100, role = 'CUSTOMER') => {
        try {
            const token = Cookies.get('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const url = `http://localhost:1010/api/admin/listUser-role?page=${page}&limit=${limit}&role=${role}`;
            const response = await axios.get(url, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            const sortedUsers = response.data.detailUserResponseList || [];
            sortedUsers.sort((a, b) => new Date(b.dateCreated) - new Date(a.dateCreated));
            setUsers(sortedUsers.slice(0, 5));
        } catch (error) {
            console.error('Error fetching users:', error);
            setError("Không thể lấy thông tin người dùng.");
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    const timeAgo = (date) => {
        const now = new Date();
        const seconds = Math.floor((now - new Date(date)) / 1000);

        let interval = Math.floor(seconds / 31536000);
        if (interval > 1) return `${interval} năm trước`;
        interval = Math.floor(seconds / 2592000);
        if (interval > 1) return `${interval} tháng trước`;
        interval = Math.floor(seconds / 86400);
        if (interval > 1) return `${interval} ngày trước`;
        return "Vừa tạo hôm nay";
    };

    return (
        <div className="dashboard">
            <Header isMenuOpen={isMenuOpen} toggleMenu={toggleMenu} title="Dashboard" />
            <div className={`dashboard-row ${isMenuOpen ? 'dimmed' : ''}`}>
                <div className="main-section">
                    <div className="stats-section">
                        <GaugeCard
                            percentage={50} // Phần trăm cho trạng thái WAITING
                            width='350px'
                            height='180px'
                            data="⏳ĐANG CHỜ"
                            description="Các đơn hàng đang chờ"
                            color="#FFA07A" // Pastel Orange
                            backgroundColor="#FFF5E6" // Light Pastel Orange
                        />
                        <GaugeCard
                            percentage={65} // Phần trăm cho trạng thái SHIPPING
                            width='350px'
                            height='180px'
                            data="🚚 ĐANG GIAO"
                            description="Các đơn hàng đang giao"
                            color="#87CEFA" // Pastel Blue
                            backgroundColor="#E6F7FF" // Light Pastel Blue
                        />
                        <GaugeCard
                            percentage={79} // Phần trăm cho trạng thái SUCCESS
                            width='350px'
                            height='180px'
                            data="✅ ĐÃ GIAO"
                            description="Các đơn hàng thành công"
                            color="#90EE90" // Pastel Green
                            backgroundColor="#F0FFF0" // Light Pastel Green
                        />
                    </div>

                    <div className="orders-box">
                        <h2>Đơn Hàng Gần Đây</h2>
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
                    <div className="updates-box">
                        <h2>Cập nhật gần đây</h2>
                        <ul>
                            {users.map(user => (
                                <li key={user.userId}>
                                    <img src={user.avatar && user.avatar.trim() !== "" ? user.avatar : assets.avtrang} alt={user.userName} className="update-image-dash" />
                                    <span>{user.userName} đã đăng ký tài khoản</span>
                                    <span>{timeAgo(user.dateCreated)}</span>
                                </li>
                            ))}
                        </ul>
                    </div>

                    <div className="review-chart">
                        <h2>Đánh giá tổng quan từ khách hàng</h2>
                        <LineChart />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
