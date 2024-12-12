import React, { useEffect, useState } from 'react';
import './Dashboard.css';
import { assets } from '../../assets/assets';
import Cookies from 'js-cookie';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Header from '../../components/Header/Header';
import LineChart from '../../components/Charts/LineChart';
import GaugeCard from '../../components/Card/GaugeCardDash';
import axiosInstance from '../../utils/axiosConfig';

const Dashboard = () => {
    const navigate = useNavigate();

    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [users, setUsers] = useState([]);
    const [error, setError] = useState("");
    const [pendingCount, setPendingCount] = useState(0);
    const [approvedCount, setApprovedCount] = useState(0);
    const [totalResponses, setTotalResponses] = useState(0);
    const [shipments, setShipments] = useState([]);

    useEffect(() => {
        const loggedIn = sessionStorage.getItem("isLoggedIn");
        setIsLoggedIn(loggedIn === "true");
    }, []);

    const fetchTotalCounts = async () => {
        try {
            const token = Cookies.get('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const firstPageUrl = `http://localhost:1010/api/contact/view/all?page=1&limit=100`;
            const firstPageResponse = await fetch(firstPageUrl, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!firstPageResponse.ok) {
                throw new Error('Network response was not ok');
            }

            const firstPageData = await firstPageResponse.json();
            const totalPages = firstPageData.totalPage;

            let allResponses = [];
            for (let page = 1; page <= totalPages; page++) {
                const url = `http://localhost:1010/api/contact/view/all?page=${page}&limit=100`;
                const response = await fetch(url, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok) {
                    throw new Error(`Failed to fetch page ${page}`);
                }

                const data = await response.json();
                allResponses = allResponses.concat(data.listContacts || []);
            }

            const waiting = allResponses.filter(r => r.status === 'WAITING').length;
            const completed = allResponses.filter(r => r.status === 'COMPLETED').length;
            const rejected = allResponses.filter(r => r.status === 'REJECTED').length;
            const total = allResponses.length;

            setPendingCount(waiting);
            setApprovedCount(completed);
            setTotalResponses(total);

        } catch (error) {
            console.error('Error fetching total counts:', error);
            setError("Không thể lấy thông tin tổng số lượng phản hồi.");
        }
    };

    useEffect(() => {
        fetchTotalCounts();
    }, []);

    // Tính toán phần trăm cho từng trạng thái
    const pendingPercentage = totalResponses ? ((pendingCount / totalResponses) * 100).toFixed(0) : 0;
    const approvedPercentage = totalResponses ? ((approvedCount / totalResponses) * 100).toFixed(0) : 0;

    const fetchUsers = async (page = 1, limit = 100, role = 'CUSTOMER') => {
        try {
            const token = Cookies.get('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const url = `http://localhost:1010/api/admin/listUser-role?page=${page}&limit=${limit}&role=${role}`;
            const response = await axiosInstance.get(url, {
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
        const inputDate = new Date(date);

        // Kiểm tra nếu ngày truyền vào là hôm nay
        if (now.toDateString() === inputDate.toDateString()) {
            return "Vừa tạo hôm nay";
        }

        const seconds = Math.floor((now - inputDate) / 1000);
        let interval = Math.floor(seconds / 31536000);
        if (interval > 0) return `${interval} năm trước`;
        interval = Math.floor(seconds / 2592000);
        if (interval > 0) return `${interval} tháng trước`;
        interval = Math.floor(seconds / 86400);
        if (interval > 0) return `${interval} ngày trước`;

        return "Vừa tạo hôm nay"; // fallback
    };

    const fetchShipments = async (page = 1) => {
        const token = Cookies.get('access_token');
        if (!token) {
            setError("Bạn cần đăng nhập để xem thông tin này.");
            return;
        }

        try {
            const response = await fetch(`http://localhost:1010/api/shipment/view/list-All?page=${page}&limit=10`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            setShipments(data.listShipment || []);
        } catch (error) {
            console.error('Error fetching shipments:', error);
            setError("Không thể lấy thông tin đơn hàng.");
        }
    };

    useEffect(() => {
        fetchShipments();
    }, []);

    return (
        <div className="dashboard">
            <Header isMenuOpen={isMenuOpen} toggleMenu={toggleMenu} title="Tổng quan" />
            <div className={`dashboard-row ${isMenuOpen ? 'dimmed' : ''}`}>
                <div className="main-section">
                    <div className="stats-section">
                        <GaugeCard
                            percentage={pendingPercentage} // Phần trăm cho trạng thái WAITING
                            width='510px'
                            height='180px'
                            data="📩 TỶ LỆ TIẾP NHẬN PHẢN HỒI"
                            description="Các phản hồi từ khách hàng đã tiếp nhận và chờ xử lý"
                            color="#ff5959"
                            colorline="#fae6e6" // Pastel Orange
                            backgroundColor="#fccaca" // Light Pastel Orange
                        />
                        <GaugeCard
                            percentage={approvedPercentage} // Phần trăm cho trạng thái SHIPPING
                            width='510px'
                            height='180px'
                            data="📬 TỶ LỆ GIẢI QUYẾT PHẢN HỒI"
                            description="Các phản hồi từ khách hàng đã được giải quyết xong"
                            color="#66e366"
                            colorline="#e8ffe8" // Pastel Blue
                            backgroundColor="#cafaca" // Light Pastel Blue
                        />
                    </div>

                    <div className="orders-box-dash">
                        <h2>Đơn Hàng Gần Đây</h2>
                        <div className="table-container">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Số thứ tự</th>
                                        <th>Tên Khách Hàng</th>
                                        <th>Số Điện Thoại</th>
                                        <th>Trạng Thái</th>
                                        <th>Ngày Đặt</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {shipments.length > 0 ? (
                                        shipments
                                            .sort((a, b) => new Date(b.dateCreated) - new Date(a.dateCreated))
                                            .map((shipment, index) => (
                                                <tr key={shipment.shipmentId}>
                                                    <td>{index + 1}</td>
                                                    <td>{shipment.customerName}</td>
                                                    <td>{shipment.phoneNumber}</td>
                                                    <td className={`dash-status-${shipment.status.toLowerCase()}`}>
                                                        {shipment.status === 'WAITING' ? 'Đang chờ' :
                                                            shipment.status === 'SHIPPING' ? 'Đang giao' :
                                                                shipment.status === 'SUCCESS' ? 'Thành công' :
                                                                    shipment.status === 'CANCELLED' ? 'Thất bại' :
                                                                        shipment.status}
                                                    </td>
                                                    <td>{shipment.dateCreated}</td>
                                                </tr>
                                            ))
                                    ) : (
                                        <tr>
                                            <td colSpan="5" style={{ textAlign: 'center' }}>
                                                Không có đơn hàng nào.
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div className="side-section">
                    <div className="updates-box">
                        <h2>Cập nhật gần đây</h2>
                        <ul>
                            {users.map(user => (
                                <li key={user.userId}>                                    
                                    {user.avatar && user.avatar !== "None" && user.avatar !== null && user.avatar !== "string" ? (
                                        <img src={user.avatar} className="update-image-dash" />
                                    ) : (
                                        <img src={assets.avtrang} className="update-image-dash" />
                                    )}
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
