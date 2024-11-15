import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import PostCard from "../../components/Card/PostCard.jsx";
import NavbarShipper from '../../components/Navbar/NavbarShipper';
import Footer from '../../components/Footer/Footer';
import Map from "../../components/Card/MapComponent.jsx";
import { assets } from "../../assets/assets.js";
import LoadingAnimation from "../../components/Animation/LoadingAnimation";
import ErrorMessage from "../../components/Animation/ErrorMessage";
import "./HomeShipper.css";

const HomeShipper = () => {
    const [posts, setPosts] = useState([]);
    const [visiblePosts, setVisiblePosts] = useState(3);
    const [showLoginPrompt, setShowLoginPrompt] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const postRefs = useRef([]);
    const navigate = useNavigate();
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPage, setTotalPage] = useState(1);
    const [limit, setLimit] = useState(9); // Hiển thị 9 items mỗi trang
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            console.log("Decoded UserId from token:", decodedPayload.UserId); // Log UserId
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Unable to decode token:", error);
            return null;
        }
    };
    
    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        const cookieValue = parts.length === 2 ? parts.pop().split(';').shift() : null;
        console.log(`Cookie ${name}:`, cookieValue); // Log giá trị cookie
        return cookieValue;
    };
    
    const fetchData = async (page, pageLimit) => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                console.error("Token không tồn tại, cần kiểm tra cookie.");
                setError("Token không hợp lệ, vui lòng đăng nhập lại.");
                return;
            }
            
            console.log("Đang gửi request với token:", token);
    
            const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/view/list-All`, {
                params: {
                    page: page,
                    limit: pageLimit,
                },
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
    
            console.log("Dữ liệu phản hồi từ server:", response.data);
    
            // Mapping response để hiển thị dữ liệu đúng format
            const { content, totalPages, currentPage, limit } = response.data;
    
            setData(content); // Danh sách shipments
            setTotalPage(totalPages);
            setCurrentPage(currentPage);
            setLimit(limit);
            setLoading(false);
        } catch (error) {
            console.error('Error fetching data:', error);
            if (error.response) {
                console.error("Thông tin lỗi từ server:", error.response.data);
                console.error("Mã lỗi HTTP:", error.response.status);
            }
            setError("Không thể tải dữ liệu");
            setLoading(false);
        }
    };
    

    useEffect(() => {
        fetchData(currentPage, limit);
    }, [currentPage]);

    const handlePageChange = (newPage) => {
        if (newPage > 0 && newPage <= totalPage) {
            setCurrentPage(newPage);
        }
    };

    const getPaginationNumbers = () => {
        const paginationNumbers = [];
        const maxButtons = 5;

        if (totalPage <= maxButtons) {
            for (let i = 1; i <= totalPage; i++) {
                paginationNumbers.push(i);
            }
        } else {
            paginationNumbers.push(1);

            if (currentPage > 3) {
                paginationNumbers.push('...');
            }

            const startPage = Math.max(2, currentPage - 1);
            const endPage = Math.min(totalPage - 1, currentPage + 1);

            for (let i = startPage; i <= endPage; i++) {
                paginationNumbers.push(i);
            }

            if (currentPage < totalPage - 2) {
                paginationNumbers.push('...');
            }

            paginationNumbers.push(totalPage);
        }

        return paginationNumbers;
    };

    return (
        <>
            <NavbarShipper currentPage="Trang Chủ" />
            <div className="shipper-home-container">
                <div className="shipper-home-content">
                    {loading ? (
                        <LoadingAnimation />
                    ) : error ? (
                        <ErrorMessage message={error} />
                    ) : (
                        <div className="shipper-post-grid">
                            {data.map((shipment, index) => (
                                <div key={shipment.shipmentId} className="shipper-post-card-wrapper">
                                    <div className="shipment-details">
                                        <h3>Shipment ID: {shipment.shipmentId}</h3>
                                        <p><strong>Customer:</strong> {shipment.customerName}</p>
                                        <p><strong>Address:</strong> {shipment.customerAddress}</p>
                                        <p><strong>Phone:</strong> {shipment.customerPhoneNumber}</p>
                                        <p><strong>Status:</strong> {shipment.status}</p>
                                        <p><strong>Created At:</strong> {shipment.dateCreated}</p>
                                    </div>
                                </div>
                            ))}
                        </div>

                    )}
                </div>

                <div className="shipper-pagination">
                    <button
                        className="btn btn-pre"
                        onClick={() => handlePageChange(currentPage - 1)}
                        disabled={currentPage === 1}
                    >
                        &lt;
                    </button>
                    {getPaginationNumbers().map((number, index) => (
                        <button
                            key={index}
                            className={`btn ${number === currentPage ? 'btn-page' : 'btn-light'}`}
                            onClick={() => {
                                if (number !== '...') {
                                    handlePageChange(number);
                                }
                            }}
                            disabled={number === '...'}
                        >
                            {number}
                        </button>
                    ))}
                    <button
                        className="btn btn-next"
                        onClick={() => handlePageChange(currentPage + 1)}
                        disabled={currentPage === totalPage}
                    >
                        &gt;
                    </button>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default HomeShipper;
