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

    useEffect(() => {
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

        fetchShipmentDetail();
    }, [shipmentId]);

    if (loading) {
        return <LoadingAnimation />;
    }

    if (error) {
        return <ErrorMessage message={error} />;
    }





    return (
        <>
            <NavbarShipper currentPage="Chi Tiết Đơn Hàng" />
            <div className="shipment-detail-container">
                <h1>Chi Tiết Đơn Hàng</h1>

                {loading ? (
                    <div>Đang tải...</div>
                ) : error ? (
                    <div className="error-message">{error}</div>
                ) : (
                    <>
                        <div className="shipment-detail-card">
                            <h2>Chi Tiết Đơn Hàng</h2>
                            <p><strong>Tên khách hàng:</strong> {shipment?.customerName}</p>
                            <p><strong>Địa chỉ giao hàng:</strong> {shipment?.address}</p>
                            <p><strong>Số điện thoại:</strong> {shipment?.phoneNumber}</p>
                            <p><strong>Trạng thái giao hàng:</strong> {shipment?.status}</p>
                            <p><strong>Ngày tạo:</strong> {shipment?.dateCreated}</p>
                            <p><strong>Ghi chú:</strong> {shipment?.notes || 'Không có ghi chú.'}</p>
                            <h2>Thông Tin Thanh Toán</h2>
                            <p><strong>Phương thức thanh toán:</strong> {payment?.paymentMethod}</p>
                            <p><strong>Trạng thái thanh toán:</strong> {payment?.statusPayment}</p>

                            <p><strong>Mã đơn hàng:</strong> {order?.orderId}</p>
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
                        </div>

                        <div className="shipment-actions" style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px' }}>
                            <button className="btn-back" onClick={() => navigate(-1)}>
                                Quay lại
                            </button>
                            <button
                                className="btn-complete"
                                onClick={() => console.log(`Hoàn thành đơn hàng: ${shipmentId}`)}
                            >
                                Hoàn thành
                            </button>
                        </div>
                    </>
                )}
            </div>
            <Footer />
        </>
    );
}

export default ShipmentDetail;
