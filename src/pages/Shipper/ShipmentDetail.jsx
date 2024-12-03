import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import NavbarShipper from '../../components/Navbar/NavbarShipper';
import Footer from '../../components/Footer/Footer';
import LoadingAnimation from '../../components/Animation/LoadingAnimation';
import ErrorMessage from '../../components/Animation/ErrorMessage';
import './ShipmentDetail.css';

const ShipmentDetail = () => {
    const { shipmentId } = useParams(); // Nhận shipmentId từ URL
    const [shipment, setShipment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [payment, setPayment] = useState(null);
    const [order, setOrder] = useState(null);

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

   
        const fetchShipmentDetail = async () => {
            setLoading(true);
            setError(null);

            const token = document.cookie.split('; ').find(row => row.startsWith('access_token='))?.split('=')[1];
            if (!token) {
                setError('Vui lòng đăng nhập lại.');
                setLoading(false);
                return;
            }

            try {
                // Lấy chi tiết shipment
                const shipmentResponse = await axios.get(
                    `${import.meta.env.VITE_API_BASE_URL}/shipment/view/${shipmentId}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                const shipmentData = shipmentResponse.data;
                setShipment(shipmentData);

                // Lấy thông tin thanh toán
                const paymentResponse = await axios.get(
                    `${import.meta.env.VITE_API_BASE_URL}/payment/view/${shipmentData.paymentId}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                const paymentData = paymentResponse.data;
                setPayment(paymentData);

                // Lấy chi tiết đơn hàng
                const orderResponse = await axios.get(
                    `${import.meta.env.VITE_API_BASE_URL}/orders/detail-item/${paymentData.orderId}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                const orderData = orderResponse.data;
                setOrder(orderData);

                setLoading(false);
            } catch (err) {
                console.error('Error fetching data:', err);
                setError('Không thể tải thông tin.');
                setLoading(false);
            }
        };

        useEffect(() => {
            fetchShipmentDetail();
        }, [shipmentId]);

    if (loading) {
        return <LoadingAnimation />;
    }

    if (error) {
        return <ErrorMessage message={error} />;
    }

    const handleStatusChange = async (shipmentId, newStatus) => {
        const token = getCookie('access_token');
        if (!token) {
            setError('Vui lòng đăng nhập lại.');
            return;
        }
    
        const userId = getUserIdFromToken(token);
        if (!userId) {
            setError('Không thể xác định UserId.');
            return;
        }
    
        try {
            if (newStatus === 'SUCCESS') {
                const response = await axios.post(
                    'http://localhost:1010/api/shipment/activate/success',
                    {
                        userId,
                        shipmentId,
                    },
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                            'Content-Type': 'application/json',
                        },
                    }
                );
                console.log('API response (SUCCESS):', response.data);
                fetchShipmentDetail(); // Làm mới dữ liệu
            } else if (newStatus === 'CANCELLED') {
                const response = await axios.post(
                    'http://localhost:1010/api/shipment/activate/cancel',
                    {
                        userId,
                        shipmentId,
                    },
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                            'Content-Type': 'application/json',
                        },
                    }
                );
                console.log('API response (CANCELLED):', response.data);
                fetchShipmentDetail(); // Làm mới dữ liệu
            } else {
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
                fetchData(currentPage); // Làm mới dữ liệu
            }
        } catch (error) {
            console.error(`Error updating status (${newStatus}):`, error);
            setError('Không thể cập nhật trạng thái.');
        }
    };
    



    return (
        <>
            <NavbarShipper currentPage="Chi Tiết Đơn Hàng" />
            <div className="shipment-detail-container" >
            <div className="shipment-actions" style={{ display: 'flex', justifyContent: 'space-between'}}>
                            <button className="btn-back" onClick={() => navigate(-1)}>
                                Quay lại
                            </button>

                        </div>


                {loading ? (
                    <div>Đang tải...</div>
                ) : error ? (
                    <div className="error-message">{error}</div>
                ) : (
                    <>
                        <div className="shipment-detail-card">
                            <h2>Chi Tiết Đơn Hàng</h2>
                            <p><strong>Mã đơn hàng:</strong> {order?.orderId}</p>
                            <p><strong>Tên khách hàng:</strong> {shipment?.customerName}</p>
                            <p><strong>Địa chỉ giao hàng:</strong> {shipment?.address}</p>
                            <p><strong>Số điện thoại:</strong> {shipment?.phoneNumber}</p>
                            <p><strong>Mã đơn giao:</strong> {shipment?.shipmentId}</p>
                            <p><strong>Trạng thái giao hàng:</strong> {shipment?.status}</p>
                            <p><strong>Ngày tạo:</strong> {shipment?.dateCreated}</p>
                            <p><strong>Ghi chú:</strong> {shipment?.notes || 'Không có ghi chú.'}</p>
                            <h2>Thông Tin Thanh Toán</h2>
                            <p><strong>Phương thức thanh toán:</strong> {payment?.paymentMethod}</p>
                            <p><strong>Trạng thái thanh toán:</strong> {payment?.statusPayment}</p>

                            
                            <p><strong>Tổng tiền:</strong> {payment?.amount} VND (Bao gồm phí ship)</p>
                            <ul className="order-items-list">
                                {order?.listItemOrders.map(item => (
                                    <li key={item.cartItemId} className="order-item">
                                        <p><strong>Sản phẩm:</strong> {item.proName}</p>
                                        <p><strong>Kích cỡ:</strong> {item.size}</p>
                                        <p><strong>Giá:</strong> {item.priceItem} VND</p>
                                        <p><strong>Số lượng:</strong> {item.quantity}</p>
                                        <p><strong>Thành tiền:</strong> {item.totalPrice} VND</p>
                                    </li>
                                ))}
                            </ul>
                            <div>
                                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                                    <select
                                        className="btn-deliver"
                                        value={shipment.status}  // Đảm bảo trạng thái hiện tại được chọn
                                        onChange={(e) => {
                                            const selectedStatus = e.target.value;
                                            handleStatusChange(shipment.shipmentId, selectedStatus); // Gọi hàm xử lý khi chọn trạng thái
                                        }}
                                        disabled={shipment.status === 'SUCCESS'} // Vô hiệu hóa nếu trạng thái là SUCCESS
                                        style={{
                                            width: '100%',
                                            backgroundColor:
                                                shipment.status === 'SUCCESS' ? '#6fb380' :
                                                    shipment.status === 'SHIPPING' ? 'rgb(255, 169, 131)' :
                                                        shipment.status === 'CANCELLED' ? 'red' : 'pink', // Màu nền tùy thuộc vào trạng thái
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
                            </div>

                        </div>

                        
                    </>
                )}
            </div>
            <Footer />
        </>
    );
}

export default ShipmentDetail;
