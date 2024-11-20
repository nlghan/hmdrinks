import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
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
    const navigate = useNavigate();

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        return parts.length === 2 ? parts.pop().split(';').shift() : null;
    };

    const getUserIdFromToken = (token) => {
        try {
            // Decode payload from token
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return parseInt(decodedPayload.UserId, 10); // Convert UserId to integer
        } catch (error) {
            console.error("Cannot decode token:", error);
            return null;
        }
    };

    const fetchData = async (page) => {
        setLoading(true);
        setError(null);

        const token = getCookie('access_token');
        if (!token) {
            setError('Vui lòng đăng nhập lại.');
            setLoading(false);
            return;
        }

        const userId = getUserIdFromToken(token);
        if (!userId) {
            setError('Không thể xác định UserId.');
            setLoading(false);
            return;
        }

        try {
            const response = await axios.get(
                `${import.meta.env.VITE_API_BASE_URL}/shipment/view/list-All`,
                {
                    params: { page, limit },
                    headers: {
                        Accept: '*/*',
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const { listShipment, totalPage } = response.data;
            console.log("API response data:", response.data);

            // Filter shipments by userId (shipperId)
            const filteredShipments = listShipment.filter(
                (shipment) => shipment.shipperId === userId
            );
            console.log("Filtered Shipments:", filteredShipments);

            setData(filteredShipments || []);
            setTotalPage(totalPage || 1);
            setCurrentPage(page);
            setLoading(false);
        } catch (err) {
            console.error('Error fetching data:', err);
            setError('Không thể tải dữ liệu.');
            setLoading(false);
        }
    };

    const handleStatusChange = async (shipmentId, newStatus) => {
        const token = getCookie('access_token');
        if (!token) {
            setError('Vui lòng đăng nhập lại.');
            return;
        }

        // Nếu trạng thái được chọn là "SUCCESS", gọi API
        if (newStatus === 'SUCCESS') {
            const userId = getUserIdFromToken(token);
            if (!userId) {
                setError('Không thể xác định UserId.');
                return;
            }

            try {
                const response = await axios.post(
                    'http://localhost:1010/api/shipment/activate/success',
                    {
                        userId,
                        shipmentId, // Đảm bảo shipmentId được truyền vào
                    },
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                            'Content-Type': 'application/json',
                        },
                    }
                );
                console.log('API response:', response.data);
                // Sau khi cập nhật, refresh lại dữ liệu
                fetchData(currentPage);
            } catch (error) {
                console.error('Error calling success API:', error);
                setError('Không thể cập nhật trạng thái thành công.');
            }
        } else {
            // Xử lý các trạng thái khác như "SHIPPING" hay "CANCELLED" nếu cần
            try {
                const response = await axios.put(
                    `${import.meta.env.VITE_API_BASE_URL}/shipment/update-status/${shipmentId}`,
                    { status: newStatus },
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                console.log('Status updated:', response.data);
                // Refresh the data after status change
                fetchData(currentPage);
            } catch (error) {
                console.error('Error updating status:', error);
                setError('Không thể cập nhật trạng thái.');
            }
        }
    };

    useEffect(() => {
        fetchData(currentPage);
    }, [currentPage]);

    const handlePageChange = (newPage) => {
        if (newPage > 0 && newPage <= totalPage) {
            setCurrentPage(newPage);
        }
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
                <div className="shipper-home-content">
                    {loading ? (
                        <LoadingAnimation />
                    ) : error ? (
                        <ErrorMessage message={error} />
                    ) : data.length === 0 ? (
                        <p>Không có đơn hàng nào để hiển thị.</p>
                    ) : (
                        <ul className="shipment-list">
                            {data.map((shipment) => (
                                <li key={shipment.shipmentId} className="shipment-item">
                                <div
                                    className="shipment-header"
                                    style={{
                                        background: 
                                            shipment.status === 'SUCCESS' ? '#6fb380' : 
                                            shipment.status === 'SHIPPING' ? 'rgb(255, 169, 131)' : 
                                            shipment.status === 'CANCELLED' ? 'red' : '#FFA983', // Màu nền tùy thuộc vào trạng thái
                                        border: 
                                            shipment.status === 'SUCCESS' ? '2px solid #4c8b5b' : 
                                            shipment.status === 'SHIPPING' ? '2px solid rgb(255, 169, 131)' : 
                                            shipment.status === 'CANCELLED' ? '2px solid red' : 'none', // Border tùy thuộc vào trạng thái
                                    }}
                                >
                                    <span className="shipment-id">
                                        Mã đơn giao: {shipment?.shipmentId || "N/A"}
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
                                    <p><strong>Customer:</strong> {shipment.customerName}</p>
                                    <p><strong>Address:</strong> {shipment.address}</p>
                                    <p><strong>Phone:</strong> {shipment.phoneNumber}</p>
                                    <p><strong>Status:</strong> {shipment.status}</p>
                                    <p><strong>Created At:</strong> {shipment.dateCreated}</p>
                                </div>
                                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                                    <select
                                        className="btn-deliver"
                                        value={shipment.status}  // Đảm bảo trạng thái hiện tại được chọn
                                        onChange={(e) => {
                                            const selectedStatus = e.target.value;
                                            handleStatusChange(shipment.shipmentId, selectedStatus); // Gọi hàm xử lý khi chọn trạng thái
                                        }}
                                        style={{
                                            backgroundColor: 
                                                shipment.status === 'SUCCESS' ? '#6fb380' :
                                                shipment.status === 'SHIPPING' ? 'rgb(255, 169, 131)' :
                                                shipment.status === 'CANCELLED' ? 'red' : 'initial', // Màu nền tùy thuộc vào trạng thái
                                            color: 'white',  // Đảm bảo chữ màu trắng
                                            border: 
                                                shipment.status === 'SUCCESS' ? '2px solid #4c8b5b' :
                                                shipment.status === 'SHIPPING' ? '2px solid rgb(255, 169, 131)' :
                                                shipment.status === 'CANCELLED' ? '2px solid red' : 'none', // Border tùy thuộc vào trạng thái
                                        }}
                                    >
                                        <option value="CANCELLED">Đã hủy</option>
                                        <option value="SHIPPING">Đang giao</option>
                                        <option value="SUCCESS">Hoàn thành</option>
                                    </select>
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
