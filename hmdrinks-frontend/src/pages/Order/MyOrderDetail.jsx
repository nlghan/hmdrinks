import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import LoadingAnimation from '../../components/Animation/LoadingAnimation';
import ErrorMessage from '../../components/Animation/ErrorMessage';
import './MyOrderDetail.css';
import { useLocation } from 'react-router-dom';
import Swal from 'sweetalert2';

const MyOrderDetail = () => {
    const { shipmentId } = useParams(); // Nhận shipmentId từ URL
    const [shipment, setShipment] = useState(null);
    const [name, setName] = useState(null);
    const [items, setItems] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [payment, setPayment] = useState(null);
    const [order, setOrder] = useState(null);
    const location = useLocation();
    const { dateDelivered, phone  } = location.state || {};
    console.log("ngày đặt hàng", dateDelivered)
    const [cancelError, setCancelError] = useState(null);
    const [cancelReason, setCancelReason] = useState(''); // Trạng thái lưu lý do hủy đơn
    const [isReasonSelected, setIsReasonSelected] = useState(false);
    const [isRequestSent, setIsRequestSent] = useState(false);

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
            // Gọi API lấy toàn bộ thông tin
            const response = await axios.get(
                `${import.meta.env.VITE_API_BASE_URL}/orders/detail/${shipmentId}`, // Thay `shipmentId` bằng `orderId` nếu cần
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = response.data;

            // Cập nhật state với dữ liệu nhận được
            setName(data.customerName);
            setOrder(data.order);
            setPayment(data.payment);
            setShipment(data.shipment);
            setItems(data.listItem); // Nếu bạn muốn lưu danh sách item

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



    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', {
            currency: 'VND',   // Đơn vị tiền tệ Việt Nam Đồng
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
        }).format(parseFloat(price));
    };



    const handleCancelOrder = async (reason) => {
        if (!reason) {
            setCancelError('Vui lòng chọn lý do hủy đơn.');
            return;
        }

        try {
            const result = await Swal.fire({
                title: 'Xác nhận hủy đơn',
                text: 'Bạn có chắc chắn muốn hủy đơn hàng này?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Có',
                cancelButtonText: 'Không',
            });

            if (result.isConfirmed) {
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

                const response = await axios.post(
                    'http://localhost:1010/api/orders/reason-cancel',
                    {
                        orderId: order.orderId,
                        userId,
                        cancelReason: reason
                    },
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                            'Content-Type': 'application/json',
                        },
                    }
                );
                console.log('Order canceled:', response.data);
                setCancelError(null);
                setIsRequestSent(true);
                fetchShipmentDetail();
            } else {
                // Reset trạng thái khi người dùng chọn "Không"
                setCancelReason('');
                setIsReasonSelected(false);
            }

        } catch (error) {
            console.error('Error canceling order:', error);
            setCancelError('Chỉ được gửi yêu cầu hủy đơn một lần');
        }
    };




    return (
        <>
            <Navbar currentPage="Chi Tiết Đơn Hàng" />
            <div className="shipment-detail-container">
                <div className="shipment-actions" style={{ display: 'flex', justifyContent: 'space-between' }}>
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
                            <p><strong>Tên khách hàng:</strong> {shipment?.customerName || name}</p>
                            <p><strong>Địa chỉ giao hàng:</strong> {order?.address}</p>
                            <p><strong>Số điện thoại:</strong> {shipment?.phoneNumber || phone}</p>
                            <p><strong>Mã đơn giao:</strong> {shipment?.shipmentId || 'Không có'}</p>
                            <p><strong>Ngày đặt hàng:</strong> {order?.dateCreated}</p>
                            <p><strong>Trạng thái giao hàng:</strong> {shipment?.status || 'Không có'}</p>
                            {/* <p><strong>Ngày hủy đơn:</strong> {shipment?.dateCancelled}</p> */}
                            <p><strong>Ghi chú:</strong> {shipment?.notes || 'Không có ghi chú.'}</p>
                            <h2>Thông Tin Thanh Toán</h2>
                            <p><strong>Phương thức thanh toán:</strong> {payment?.paymentMethod}</p>
                            <p><strong>Trạng thái thanh toán:</strong> {payment?.statusPayment}</p>


                            <p><strong>Tổng tiền:</strong> {formatPrice(order.totalPrice + order.deliveryFee - order.discountPrice)} VND (Bao gồm phí ship)</p>
                            <ul className="order-items-list">
                                {items?.map(item => (
                                    <li key={item.cartItemId} className="order-item">
                                        <p><strong>Sản phẩm:</strong> {item.proName}</p> {/* Nếu cần tên sản phẩm, cần bổ sung dữ liệu từ API */}
                                        <p><strong>Kích cỡ:</strong> {item.size}</p>
                                        <p><strong>Số lượng:</strong> {item.quantity}</p>
                                        <p><strong>Thành tiền:</strong> {formatPrice(item.totalPrice)} VND</p>
                                    </li>
                                ))}
                            </ul>


                            {cancelError && <div className="error-message">{cancelError}</div>}
                            {payment?.statusPayment !== 'FAILED' && payment?.statusPayment !== 'REFUND' && order?.status != "CANCELLED" && shipment?.status != "SUCCESS" && shipment?.status != null? (
                                <div style={{ display: 'flex', justifyContent: 'flex-end', flexDirection: 'column', gap: '10px' }}>
                                    {!isRequestSent ? (
                                        <>
                                            <select
                                                id="cancelReason"
                                                style={{
                                                    width: '100%',
                                                    padding: '10px',
                                                    borderRadius: '4px',
                                                    border: '1px solid #ccc',
                                                    backgroundColor: '#fff',
                                                }}
                                                value={cancelReason}
                                                onChange={(e) => {
                                                    setCancelReason(e.target.value);
                                                    setIsReasonSelected(true);
                                                }}
                                            >
                                                <option value="" disabled>Chọn lý do hủy đơn</option>
                                                <option value="CHANGED_MY_MIND">Đổi ý không muốn mua nữa</option>
                                                <option value="FOUND_CHEAPER_ELSEWHERE">Tìm thấy giá rẻ hơn ở nơi khác</option>
                                                <option value="ORDERED_BY_MISTAKE">Đặt nhầm sản phẩm</option>
                                                <option value="DELIVERY_TOO_SLOW">Giao hàng quá chậm</option>
                                                <option value="WRONG_PRODUCT_SELECTED">Chọn nhầm sản phẩm</option>
                                                <option value="NOT_NEEDED_ANYMORE">Không cần sản phẩm nữa</option>
                                                <option value="PAYMENT_ISSUES">Gặp vấn đề khi thanh toán</option>
                                                <option value="PREFER_DIFFERENT_STORE">Thích mua ở cửa hàng khác hơn</option>
                                                <option value="UNSATISFIED_WITH_SERVICE">Không hài lòng với dịch vụ</option>
                                                <option value="OTHER_REASON">Lý do khác</option>
                                            </select>

                                            <button
                                                onClick={() => handleCancelOrder(cancelReason)}
                                                disabled={!isReasonSelected}
                                                style={{
                                                    padding: '10px',
                                                    borderRadius: '4px',
                                                    border: 'none',
                                                    backgroundColor: isReasonSelected ? '#dc3545' : '#ccc',
                                                    color: 'white',
                                                    cursor: isReasonSelected ? 'pointer' : 'not-allowed',
                                                    marginTop: '10px'
                                                }}
                                            >
                                                Xác nhận hủy đơn
                                            </button>
                                        </>
                                    ) : (
                                        <div style={{
                                            padding: '10px',
                                            backgroundColor: '#f8f9fa',
                                            border: '1px solid #dee2e6',
                                            borderRadius: '4px',
                                            textAlign: 'center',
                                            color: '#28a745'
                                        }}>
                                            Yêu cầu hủy đơn đã được gửi đi
                                        </div>
                                    )}
                                </div>
                            ) : (
                                <p style={{ color: 'red', fontWeight: 'bold' }}></p>
                            )}


                        </div>


                    </>
                )}
            </div>
            <Footer />
        </>
    );
}

export default MyOrderDetail;
