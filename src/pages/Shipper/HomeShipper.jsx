import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import axiosInstance from '../../utils/axiosConfig';
import NavbarShipper from '../../components/Navbar/NavbarShipper';
import Footer from '../../components/Footer/Footer';
import LoadingAnimation from '../../components/Animation/LoadingAnimation';
import ErrorMessage from '../../components/Animation/ErrorMessage';
import './HomeShipper.css';

const HomeShipper = () => {
    const [data, setData] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPage, setTotalPage] = useState(1);
    const [limit] = useState(5); // Items per page
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedStatus, setSelectedStatus] = useState('SHIPPING');
    const [filteredData, setFilteredData] = useState([]);
    const navigate = useNavigate();

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`); 
        return parts.length === 2 ? parts.pop().split(';').shift() : null;
    };

    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return parseInt(decodedPayload.UserId, 10);
        } catch (error) {
            console.error("Cannot decode token:", error);
            return null;
        }
    };

    const fetchData = async (page, status = selectedStatus) => {
        setLoading(true);
        setError(null);
        
        const token = getCookie('access_token');
        if (!token) {
            setError('Vui lòng đăng nhập lại.');
            setLoading(false);
            return;
        }
        
        let url = `http://localhost:1010/api/shipment/shipper/listShippment`;
        const params = { page, limit, status };
    
        // Nếu trạng thái không phải là 'WAITING', thêm userId vào params
        if (status !== 'WAITING') {
            const userId = getUserIdFromToken(token);
            if (!userId) {
                setError('Không thể xác định UserId.');
                setLoading(false);
                return;
            }
            params.userId = userId;
        } else {
            url = `http://localhost:1010/api/shipment/view/listByStatus`;
        }
        
        try {
            const response = await axiosInstance.get(url, {
                params,
                headers: {
                    Accept: '*/*',
                    Authorization: `Bearer ${token}`,
                },
            });
    
            const { listShipment, totalPage } = response.data;
            setData(listShipment || []);
            setTotalPage(totalPage || 1);
            setCurrentPage(page);
            setLoading(false);
        } catch (err) {
            setError('Không thể tải dữ liệu.');
            setLoading(false);
        }
    };
    
    
    
    const handleStatusChange = (status) => {
        setSelectedStatus(status);
        setCurrentPage(1); // Reset to page 1 on status change
        fetchData(1, status); // Fetch data with the new status
        window.scrollTo(0, 0); 
    };

    useEffect(() => {
        fetchData(currentPage);
    }, [currentPage]);

    useEffect(() => {
        setFilteredData(data.filter((item) => item.status === selectedStatus));
    }, [data, selectedStatus]);

    const handlePageChange = (newPage) => {
        if (newPage > 0 && newPage <= totalPage) {
            setCurrentPage(newPage);
        }
        window.scrollTo(0, 0); 
    };

    const getPaginationNumbers = () => {
        const pagination = [];
        const maxButtons = 5;

        if (totalPage <= maxButtons) {
            for (let i = 1; i <= totalPage; i++) {
                pagination.push(i);
            }
        } else {
            if (currentPage > 3) pagination.push('...');
            pagination.push(currentPage);
            if (currentPage < totalPage - 2) pagination.push('...');
            pagination.push(totalPage);
        }

        return pagination;
    };

    return (
        <>
            <NavbarShipper currentPage="Trang Chủ" />
            <div className="shipper-home-container">
                <div className="side-menu-shipper">
                <button
                        className={`side-menu-shipper ${selectedStatus === 'WAITING' ? 'active' : ''}`}
                        onClick={() => handleStatusChange('WAITING')}
                    >
                        Đơn hàng cần giao
                    </button>
                    <button
                        className={`side-menu-shipper ${selectedStatus === 'SHIPPING' ? 'active' : ''}`}
                        onClick={() => handleStatusChange('SHIPPING')}
                    >
                        Đơn hàng được phân công
                    </button>
                    <button
                        className={`side-menu-shipper ${selectedStatus === 'SUCCESS' ? 'active' : ''}`}
                        onClick={() => handleStatusChange('SUCCESS')}
                    >
                        Đơn hàng đã hoàn thành
                    </button>
                    <button
                        className={`side-menu-shipper ${selectedStatus === 'CANCELLED' ? 'active' : ''}`}
                        onClick={() => handleStatusChange('CANCELLED')}
                    >
                        Đơn hàng đã hủy
                    </button>
                </div>

                <div className="shipper-home-content">
                    {loading ? (
                        <LoadingAnimation />
                    ) : error ? (
                        <ErrorMessage message={error} />
                    ) : data.length === 0 ? (
                        <p>Không có đơn hàng nào để hiển thị.</p>
                    ) : (
                        <ul className="shipment-list">
                            <h2>DANH SÁCH ĐƠN HÀNG</h2>
                            {filteredData.map((shipment) => (
                                <li key={shipment.shipmentId} className="shipment-item">
                                    <div
                                        className="shipment-header"
                                        style={{
                                            background:
                                                shipment.status === 'SUCCESS' ? '#6fb380' :
                                                    shipment.status === 'SHIPPING' ? 'rgb(255, 169, 131)' :
                                                        shipment.status === 'WAITING' ? 'pink' :
                                                            shipment.status === 'CANCELLED' ? 'red' : '#FFA983',
                                            border:
                                                shipment.status === 'SUCCESS' ? '2px solid #4c8b5b' :
                                                    shipment.status === 'SHIPPING' ? '2px solid rgb(255, 169, 131)' :
                                                        shipment.status === 'CANCELLED' ? '2px solid red' : 'none',
                                        }}
                                    >
                                        <span className="shipment-id">
                                            Mã đơn hàng: {shipment?.orderId || "N/A"}
                                        </span>

                                        <button
                                            className="btn-view-details-ship"
                                            onClick={() =>
                                                navigate(`/shipment-detail/${shipment.shipmentId}`)
                                            }
                                        >
                                            Chi tiết
                                        </button>
                                    </div>
                                    <div className="shipment-body">
                                        <p><strong>Tên khách hàng:</strong> {shipment.customerName}</p>
                                        <p><strong>Địa chỉ:</strong> {shipment.address}</p>
                                        <p><strong>Số điện thoại:</strong> {shipment.phoneNumber}</p>
                                        <p><strong>Trạng thái đơn:</strong> {shipment.status}</p>
                                        <p><strong>Ngày nhận đơn:</strong> {shipment.dateCreated}</p>
                                        
                                    </div>

                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                {!loading && !error && (
                    <div className="shipper-pagination">
                        <button
                            className="btn btn-pre"
                            onClick={() => handlePageChange(currentPage - 1)}
                            disabled={currentPage === 1}
                        >
                            &lt;
                        </button>
                        {getPaginationNumbers().map((num, idx) => (
                            <button
                                key={idx}
                                className={`btn ${num === currentPage ? 'btn-page' : 'btn-light'}`}
                                onClick={() => num !== '...' && handlePageChange(num)}
                                disabled={num === '...'}
                            >
                                {num}
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
                )}
            </div>
            <Footer />
        </>
    );
};

export default HomeShipper;
