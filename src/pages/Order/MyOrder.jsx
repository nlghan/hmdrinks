import React, { useState, useEffect } from 'react';
import './MyOrder.css';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useLocation } from "react-router-dom";

const MyOrder = () => {
    const [selectedTab, setSelectedTab] = useState('delivering');
    const [cancelledOrders, setCancelledOrders] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [currentDeliveringPage, setCurrentDeliveringPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [itemsPerPage, setItemsPerPage] = useState(5);

    const navigate = useNavigate(); // Hook dùng để chuyển hướng
    const location = useLocation();


    const handleCheckout = async (order) => {
        const token = getCookie('access_token');
        try {
            // Gọi API để lấy chi tiết đơn hàng
            const response = await axios.get(
                `http://localhost:1010/api/orders/detail-item/${order.orderId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            // Kết hợp dữ liệu chi tiết đơn hàng vào orderData và truyền qua state
            const orderData = {
                ...response.data,  // Dữ liệu chi tiết từ API
                orderId: order.orderId,
                address: order.address,
                phone: order.phone,
                discountPrice: order.discountPrice,
                deliveryFee: order.deliveryFee,
                totalPrice: order.totalPrice,
                dateOders: order.dateOders
            };

            // Truyền toàn bộ dữ liệu vào state khi điều hướng đến trang Order
            navigate('/order', {
                state: { orderData }
            });
        } catch (error) {
            console.error('Không thể lấy thông tin chi tiết đơn hàng:', error);
        }
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', {
            currency: 'VND',   // Đơn vị tiền tệ Việt Nam Đồng
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
        }).format(parseFloat(price));
    };




    const getUserIdFromToken = (token) => {
        try {
            // Tách payload từ token
            const payload = token.split('.')[1];
            // Giải mã payload từ base64
            const decodedPayload = JSON.parse(atob(payload));

            // Ép kiểu UserId thành int (số nguyên)
            return parseInt(decodedPayload.UserId, 10); // 10 là hệ cơ số thập phân
        } catch (error) {
            console.error("Cannot decode token:", error);
            return null;
        }
    };


    // Get Cookie by name
    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };


    const fetchCancelledOrders = async () => {
        setLoading(true);
        setError('');
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        try {

            // Gọi API lấy danh sách đơn hàng đã hủy nhưng đã thanh toán
            const responsePaid = await axios.get(
                `http://localhost:1010/api/orders/view/order-cancel/payment-have/${userId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        accept: '*/*',
                    },
                }
            );
            const paidOrders = responsePaid.data.list || [];

            // Hợp nhất hai danh sách đơn hàng
            const combinedOrders = [...paidOrders];
            setCancelledOrders(combinedOrders);

        } catch (err) {
            setError('Không thể tải danh sách đơn hàng đã hủy.');
            console.error('Error fetching cancelled orders:', err);
        } finally {
            setLoading(false);
        }
    };




    const [waitingOrders, setWaitingOrders] = useState([]);

    const fetchWaitingOrders = async () => {
        setLoading(true);
        setError('');
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        try {
            // Gọi API để lấy danh sách đơn hàng đang chờ
            const response = await axios.get(
                `http://localhost:1010/api/orders/view/fetchOrdersAwaiting/${userId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            // Tách dữ liệu từ response
            const { listOrderWaiting, listAllOrderConfirmAndNotPayment } = response.data;

            // Lấy danh sách đơn hàng đang chờ
            const waitingOrders = listOrderWaiting?.list || [];
            const confirmedNotPaidOrders = listAllOrderConfirmAndNotPayment?.list || [];

            // Gộp hai danh sách đơn hàng vào một nếu cần hiển thị cả hai hoặc xử lý riêng
            const combinedOrders = [...waitingOrders, ...confirmedNotPaidOrders];

            // Cập nhật danh sách đơn hàng đang chờ
            setWaitingOrders(combinedOrders);

        } catch (err) {
            setError('Không thể tải danh sách đơn hàng đang chờ thanh toán.');
            console.error('Error fetching waiting orders:', err);
        } finally {
            setLoading(false);
        }
    };




    const [confirmedOrders, setConfirmedOrders] = useState([]);
    const fetchConfirmedOrders = async () => {
        setLoading(true);
        setError('');
        const token = getCookie('access_token'); // Lấy token từ cookie
        const userId = getUserIdFromToken(token); // Lấy userId từ token

        try {
            // Gọi API để lấy danh sách đơn hàng đã xác nhận
            const response = await axios.get(
                `http://localhost:1010/api/orders/view/confirmed/${userId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const { data: list = [] } = response;

            // Chuẩn bị danh sách dữ liệu kết hợp giữa order và shipment
            const orders = list.map((item) => ({
                orderId: item.order.orderId,
                address: item.order.address,
                deliveryFee: item.order.deliveryFee,
                totalPrice: item.order.totalPrice,
                status: item.order.status,
                dateCreated: item.order.dateCreated,
                dateOders: item.order.dateOders,
                dateDelivered: item.order.dateDelivered,
                discountPrice: item.order.discountPrice,
                note: item.order.note,
                phone: item.order.phone,
                shipment: {
                    shipmentId: item.shipment.shipmentId,
                    customerName: item.shipment.customerName,
                    phoneNumber: item.shipment.phoneNumber,
                    email: item.shipment.email,
                    status: item.shipment.status,
                    dateDeliver: item.shipment.dateDeliver,
                    dateShipped: item.shipment.dateShipped,
                    shipperName: item.shipment.nameShipper,
                },
            }));

            // Lưu danh sách đơn hàng vào state
            setConfirmedOrders(orders);
            console.log('Danh sách đơn hàng đã xác nhận:', orders);
        } catch (err) {
            setError('Không thể tải danh sách đơn hàng đã xác nhận.');
            console.error('Lỗi khi gọi API:', err);
        } finally {
            setLoading(false);
        }
    };



    useEffect(() => {
        if (selectedTab === 'cancelled') {
            fetchCancelledOrders();
            window.scrollTo(0, 0);
        } else if (selectedTab === 'pending') {
            fetchWaitingOrders();
            window.scrollTo(0, 0);
        } else if (selectedTab === 'delivering') {
            fetchConfirmedOrders();
            window.scrollTo(0, 0);
        }
        else if (selectedTab === 'waiting') {
            fetchWaitingList();
            window.scrollTo(0, 0);
        }
        else if (selectedTab === 'refund') {
            fetchRefundOrders();
            window.scrollTo(0, 0);
        }
        else if (selectedTab === 'history') {
            fetchHistoryOrders();
            window.scrollTo(0, 0);
        }
    }, [selectedTab]);


    const [currentCancelledPage, setCurrentCancelledPage] = useState(1);
    const [currentPendingPage, setCurrentPendingPage] = useState(1);
    const [currentWaitingPage, setCurrentWaitingPage] = useState(1);

    const [historyOrders, setHistoryOrders] = useState([]);

    const fetchHistoryOrders = async () => {
        setLoading(true);
        setError('');
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        try {
            // Gọi API để lấy danh sách lịch sử đơn hàng
            const response = await axios.get(
                `http://localhost:1010/api/orders/history/${userId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const { list = [] } = response.data;

            // Chuẩn bị danh sách dữ liệu kết hợp giữa order và shipment
            const orders = list.map((item) => ({
                orderId: item.order.orderId,
                address: item.order.address,
                deliveryFee: item.order.deliveryFee,
                discountPrice: item.order.discountPrice,
                totalPrice: item.order.totalPrice,
                status: item.order.status,
                dateCreated: item.order.dateCreated,
                dateDelivered: item.order.dateDelivered,
                dateOders: item.order.dateOders,
                shipment: {
                    shipmentId: item.shipment.shipmentId,
                    customerName: item.shipment.customerName,
                    phoneNumber: item.shipment.phoneNumber,
                    email: item.shipment.email,
                    status: item.shipment.status,
                    dateDeliver: item.shipment.dateDeliver,
                    dateShipped: item.shipment.dateShipped,
                    shipperName: item.shipment.nameShipper
                },
            }));

            // Lưu danh sách đơn hàng vào state
            setHistoryOrders(orders);
            console.log('Lịch sử đặt hàng:', orders);
        } catch (err) {
            setError('Không thể tải danh sách lịch sử đơn hàng.');
            console.error('Lỗi khi gọi API lịch sử đơn hàng:', err);
        } finally {
            setLoading(false);
        }
    };

    const [waitingList, setWaitingList] = useState([]);


    const fetchWaitingList = async () => {
        setLoading(true);
        setError(null);
    
        const token = getCookie('access_token');
        if (!token) {
            setError('Vui lòng đăng nhập lại.');
            setLoading(false);
            return;
        }
        const userId = getUserIdFromToken(token);
    
        try {
            // Gọi API danh sách shipment có trạng thái WAITING
            const responseShipment = await axios.get(
                `http://localhost:1010/api/shipment/view/list-waiting/${userId}`,
                {
                    headers: {
                        Accept: '*/*',
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
    
            const listShipment = responseShipment.data.listShipment || [];
    
            // Lấy chi tiết info-payment của từng orderId
            const promises = listShipment.map(async (shipment) => {
                const responseOrder = await axios.get(
                    `http://localhost:1010/api/orders/info-payment?orderId=${shipment.orderId}`,
                    {
                        headers: {
                            Accept: '*/*',
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                return { ...shipment, infoPayment: responseOrder.data };
            });
    
            const listWithPaymentInfo = await Promise.all(promises);
            setWaitingList(listWithPaymentInfo);  // Sử dụng setWaitingList thay cho setData
            window.scrollTo(0, 0);
        } catch (error) {
            console.error('Error fetching waiting list:', error);
            setError('Không thể tải dữ liệu danh sách chờ.');
        } finally {
            setLoading(false);
        }
    };
    


    const [currentHistoryPage, setCurrentHistoryPage] = useState(1);

    const handleHistoryPageChange = (pageNumber) => {
        setCurrentHistoryPage(pageNumber);
        window.scrollTo(0, 0);
    };

    const handleDeliveringPageChange = (pageNumber) => {
        setCurrentDeliveringPage(pageNumber);
        window.scrollTo(0, 0);
    };

    const handleCancelledPageChange = (pageNumber) => {
        setCurrentCancelledPage(pageNumber);
        window.scrollTo(0, 0);
    };

    const handlePendingPageChange = (pageNumber) => {
        setCurrentPendingPage(pageNumber);
        window.scrollTo(0, 0);
    };

    const handlePrint = async (orderId) => {
        const token = getCookie('access_token');
        try {
            const response = await fetch(`http://localhost:1010/api/orders/pdf/invoice?orderId=${orderId}`, {
                method: 'GET',
                headers: {
                    'accept': '*/*',
                    Authorization: `Bearer ${token}`,
                }
            });

            // Kiểm tra nếu response trả về thành công
            if (response.ok) {
                const blob = await response.blob(); // Lấy dữ liệu dưới dạng blob (tệp PDF)
                const link = document.createElement('a'); // Tạo một liên kết ảo
                link.href = URL.createObjectURL(blob); // Tạo URL tạm thời cho tệp blob
                link.download = `Invoice_${orderId}.pdf`; // Đặt tên tệp PDF
                link.click(); // Mô phỏng nhấp vào liên kết để tải tệp xuống
            } else {
                console.error('Có lỗi khi tải hóa đơn.');
            }
        } catch (error) {
            console.error('Lỗi kết nối hoặc tải hóa đơn:', error);
        }
    };

    const [refundOrders, setRefundOrders] = useState([]);
    const [currentRefundPage, setCurrentRefundPage] = useState(1);

    const fetchRefundOrders = async () => {
        setLoading(true);
        setError('');
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        try {
            // Gọi API để lấy danh sách đơn hàng hoàn tiền
            const response = await axios.get(
                `http://localhost:1010/api/orders/view/order-cancel/payment-refund-user/${userId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        'Accept': '*/*'
                    },
                }
            );

            const { list = [] } = response.data;

            // Chuẩn bị danh sách dữ liệu kết hợp giữa order và payment
            const refunds = list.map((item) => ({
                orderId: item.order.orderId,
                address: item.order.address,
                phoneNumber: item.order.phone,
                deliveryFee: item.order.deliveryFee,
                discountPrice: item.order.discountPrice,
                totalPrice: item.order.totalPrice,
                status: item.order.status,
                dateCreated: item.order.dateCreated,
                dateDelivered: item.order.dateDelivered,
                dateOders: item.order.dateOders,
                note: item.order.note,

                payment: {
                    paymentId: item.payment.paymentId,
                    amount: item.payment.amount,
                    dateCreated: item.payment.dateCreated,
                    dateRefund: item.payment.dateRefund,
                    paymentMethod: item.payment.paymentMethod,
                    statusPayment: item.payment.statusPayment,
                    refunded: item.payment.refunded ? 'ĐÃ HOÀN' : 'CHƯA HOÀN',
                }
            }));

            // Lưu danh sách đơn hàng hoàn tiền vào state
            setRefundOrders(refunds);
            console.log('Danh sách hoàn tiền:', refunds);
        } catch (err) {
            setError('Không thể tải danh sách hoàn tiền.');
            console.error('Lỗi khi gọi API hoàn tiền:', err);
        } finally {
            setLoading(false);
        }
    };


    const handleRefundPageChange = (pageNumber) => {
        setCurrentRefundPage(pageNumber);
        window.scrollTo(0, 0);
    };

    const handleCancelOrder = async (orderId) => {
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);
        try {
            const response = await axios.put(
                'http://localhost:1010/api/orders/cancel-order',
                { orderId, userId },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                }
            );
            alert('Order cancelled successfully');
            console.log(response.data);
            fetchWaitingList();
        } catch (error) {
            console.error('Error:', error);
            alert('Failed to cancel order');
        }
    };



    const renderContent = () => {
        const itemsPerPage = 5;  // Số lượng đơn hàng hiển thị mỗi trang

        const paginate = (orders, currentPage) => {
            const startIndex = (currentPage - 1) * itemsPerPage;
            return orders.slice(startIndex, startIndex + itemsPerPage);
        };

        switch (selectedTab) {
            case 'delivering':
                return (
                    <div>
                        {loading ? (
                            <div>Đang tải...</div>
                        ) : error ? (
                            <div>{error}</div>
                        ) : (
                            <>
                                <ul className="my-orders-list">
                                    {paginate(confirmedOrders, currentDeliveringPage).map((order) => (
                                        <li key={order.orderId} className="my-orders-item">
                                            <div
                                                className="my-orders-item-header"
                                                style={{ background: '#7cc58d' }}
                                            >
                                                <p><strong>Mã đơn hàng:</strong> {order.orderId}</p>
                                                <button
                                                    className="btn-view-details-ship"
                                                    onClick={() =>
                                                        navigate(`/my-order-detail/${order.orderId}`, {
                                                            state: { dateDelivered: order.dateOders },
                                                        })
                                                    }
                                                >
                                                    Chi tiết
                                                </button>
                                            </div>
                                            <div className="my-orders-item-content">
                                                <p><strong>Địa chỉ: &nbsp;</strong> {order.address}</p>
                                                <p><strong>Số điện thoại: &nbsp;</strong> {order.phone}</p>
                                                <p><strong>Giảm giá:&nbsp;</strong> {formatPrice(order.discountPrice)} VND</p>
                                                <p><strong>Phí vận chuyển: &nbsp;</strong> {formatPrice(order.deliveryFee)} VND</p>
                                                <p>
                                                    <strong>Tổng tiền:&nbsp;</strong>
                                                    {formatPrice(Math.max(order.totalPrice + order.deliveryFee - order.discountPrice, 0))} VND
                                                </p>
                                                <p><strong>Ngày đặt hàng: &nbsp;</strong> {order.dateOders}</p>
                                                <p><strong>Mã đơn giao: &nbsp;</strong> {order.shipment?.shipmentId}</p>
                                                <p><strong>Tên shipper:&nbsp; </strong> {order.shipment?.shipperName}</p>

                                            </div>
                                            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                                                <button
                                                    className="shipping-status-button"
                                                    style={{
                                                        background: 'aliceblue',
                                                        padding: '10px',
                                                        borderRadius: '4px',
                                                        color: '#000',
                                                    }}

                                                >
                                                    ĐANG GIAO HÀNG
                                                </button>
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                                {/* Pagination */}
                                <div className="menu-category-pagination" style={{ width: '100%' }}>
                                    {Array.from({ length: Math.ceil(confirmedOrders.length / itemsPerPage) }, (_, index) => (
                                        <span
                                            key={index + 1}
                                            className={`pagination-cate-dot ${currentDeliveringPage === index + 1 ? 'active' : ''}`}
                                            onClick={() => handleDeliveringPageChange(index + 1)}
                                        >
                                            •
                                        </span>
                                    ))}
                                </div>

                            </>
                        )}
                    </div>
                );
                case 'waiting':
                    return (
                        <div>
                            {loading ? (
                                <div>Đang tải...</div>
                            ) : error ? (
                                <div>{error}</div>
                            ) : (
                                <>
                                    <ul className="my-orders-list">
                                        {paginate(waitingList, currentWaitingPage).map((order) => (
                                            <li key={order.orderId} className="my-orders-item">
                                                <div
                                                    className="my-orders-item-header"
                                                    style={{ background: 'rgb(194 229 182)' }}
                                                >
                                                    <p><strong>Mã đơn hàng:</strong> {order.orderId}</p>
                                                    <button
                                                        className="btn-view-details-ship"
                                                        onClick={() =>
                                                            navigate(`/my-order-detail/${order.orderId}`, {
                                                                state: { dateDelivered: order.dateOders },
                                                            })
                                                        }
                                                    >
                                                        Chi tiết
                                                    </button>
                                                </div>
                                                <div className="my-orders-item-content">
                                                    <p><strong>Địa chỉ: &nbsp;</strong> {order.address}</p>
                                                    <p><strong>Số điện thoại: &nbsp;</strong> {order.phoneNumber}</p>
                                                    <p><strong>Giảm giá:&nbsp;</strong> {formatPrice(order.infoPayment?.discountPrice)} VND</p>
                                                    <p><strong>Phí vận chuyển: &nbsp;</strong> {formatPrice(order.infoPayment?.deliveryFee)} VND</p>
                                                    <p>
                                                        <strong>Tổng tiền:&nbsp;</strong>
                                                        {formatPrice(Math.max(order.infoPayment?.totalPrice + order.infoPayment?.deliveryFee - order.infoPayment?.discountPrice, 0))} VND
                                                    </p>
                                                    <p><strong>Ngày đặt hàng: &nbsp;</strong> {order.infoPayment?.dateOders}</p>
                                                    
                                                </div>
                                                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                                                    <button
                                                        className="shipping-status-button"
                                                        style={{
                                                            background: 'red',
                                                            padding: '10px',
                                                            borderRadius: '4px',
                                                            color: 'white',
                                                            marginRight:'5px',
                                                            marginBottom:'5px'
                                                        }}   
                                                        onClick={() => handleCancelOrder(order.orderId)} 
                                                    >
                                                        HỦY ĐƠN
                                                    </button>
                                                </div>
                                            </li>
                                        ))}
                                    </ul>
                                    {/* Pagination */}
                                    <div className="menu-category-pagination" style={{ width: '100%' }}>
                                        {Array.from({ length: Math.ceil(confirmedOrders.length / itemsPerPage) }, (_, index) => (
                                            <span
                                                key={index + 1}
                                                className={`pagination-cate-dot ${currentDeliveringPage === index + 1 ? 'active' : ''}`}
                                                onClick={() => handleDeliveringPageChange(index + 1)}
                                            >
                                                •
                                            </span>
                                        ))}
                                    </div>
    
                                </>
                            )}
                        </div>
                    );

            case 'cancelled':
                return (
                    <div>
                        {loading ? (
                            <div>Đang tải...</div>
                        ) : error ? (
                            <div>{error}</div>
                        ) : (
                            <>
                                <ul className="my-orders-list">
                                    {paginate(cancelledOrders, currentCancelledPage).map((order) => (
                                        <li key={order.orderId} className="my-orders-item">
                                            <div className="my-orders-item-header" style={{ background: '#d67474' }}><p><strong>Mã đơn hàng:</strong> {order.orderId}</p>
                                            <button
                                                        className="btn-view-details-ship"
                                                        onClick={() =>
                                                            navigate(`/my-order-detail/${order.orderId}`, {
                                                                state: { dateDelivered: order.dateOders },
                                                            })
                                                        }
                                                    >
                                                        Chi tiết
                                                    </button>
                                            </div>
                                            <div className="my-orders-item-content">
                                                <p><strong>Địa chỉ: &nbsp;</strong> {order.address}</p>
                                                <p><strong>Số điện thoại: &nbsp;</strong> {order.phone}</p>
                                                <p><strong>Giảm giá:&nbsp;</strong> {formatPrice(order.discountPrice)} VND</p>
                                                <p><strong>Phí vận chuyển: &nbsp;</strong> {formatPrice(order.deliveryFee)} VND</p>
                                                <p>
                                                    <strong>Tổng tiền:&nbsp;</strong>
                                                    {formatPrice(Math.max(order.totalPrice + order.deliveryFee - order.discountPrice, 0))} VND
                                                </p>
                                                <p><strong>Ngày đặt hàng:&nbsp; </strong> {order.dateOders}</p>
                                                <p><strong>Ngày hủy đơn: &nbsp;</strong> {order.dateCanceled}</p>
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                                {/* Pagination */}
                                <div className="menu-category-pagination" style={{ width: '100%' }}>
                                    {Array.from({ length: Math.ceil(cancelledOrders.length / itemsPerPage) }, (_, index) => (
                                        <span
                                            key={index + 1}
                                            className={`pagination-cate-dot ${currentCancelledPage === index + 1 ? 'active' : ''}`}
                                            onClick={() => handleCancelledPageChange(index + 1)}
                                        >
                                            •
                                        </span>
                                    ))}
                                </div>
                            </>
                        )}
                    </div>
                );
            case 'pending':
                return (
                    <div>
                        {loading ? (
                            <div>Đang tải...</div>
                        ) : error ? (
                            <div>{error}</div>
                        ) : (
                            <>
                                <ul className="my-orders-list" >
                                    {paginate(waitingOrders, currentPendingPage).map((order) => (
                                        <li key={order.orderId} className="my-orders-item">
                                            <div className="my-orders-item-header">
                                                <p><strong>Mã đơn hàng:</strong> {order.orderId}</p>

                                            </div>
                                            <div className="my-orders-item-content">
                                                <p><strong>Địa chỉ:&nbsp;</strong> {order.address}</p>
                                                <p><strong>Số điện thoại:&nbsp;</strong> {order.phone}</p>
                                                <p><strong>Giảm giá:&nbsp;</strong> {formatPrice(order.discountPrice)} VND</p>
                                                <p><strong>Phí vận chuyển: &nbsp;</strong> {formatPrice(order.deliveryFee)} VND</p>
                                                <p>
                                                    <strong>Tổng tiền:&nbsp;</strong>
                                                    {formatPrice(Math.max(order.totalPrice + order.deliveryFee - order.discountPrice, 0))} VND
                                                </p>
                                                <p><strong>Ngày đặt hàng:&nbsp;</strong> {order.dateOders}</p>
                                            </div>
                                            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                                                <button
                                                    className="checkout-button-order"
                                                    onClick={() => handleCheckout(order)}
                                                    style={{ background: '##ffa07a' }}
                                                >
                                                    Thanh toán <i className="ti-arrow-right" style={{ fontSize: '12px' }} />
                                                </button>
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                                {/* Pagination */}
                                <div className="menu-category-pagination" style={{ width: '100%' }}>
                                    {Array.from({ length: Math.ceil(waitingOrders.length / itemsPerPage) }, (_, index) => (
                                        <span
                                            key={index + 1}
                                            className={`pagination-cate-dot ${currentPendingPage === index + 1 ? 'active' : ''}`}
                                            onClick={() => handlePendingPageChange(index + 1)}
                                        >
                                            •
                                        </span>
                                    ))}
                                </div>
                            </>
                        )}
                    </div>
                );
            case 'refund':
                return (
                    <div>
                        {loading ? (
                            <div>Đang tải...</div>
                        ) : error ? (
                            <div>{error}</div>
                        ) : (
                            <>
                                <ul className="my-orders-list">
                                    {paginate(refundOrders, currentRefundPage).map((refund) => (
                                        <li key={refund.orderId} className="my-orders-item">
                                            <div className="my-orders-item-header" style={{ background: '#f3d2f1' }}>
                                                <p><strong>Mã đơn hàng:</strong> {refund.orderId}</p>
                                                <button
                                                    className="btn-view-details-ship"
                                                    onClick={() =>
                                                        navigate(`/my-order-detail/${refund.orderId}`, {
                                                            state: { dateRequested: refund.dateRequested },
                                                        })
                                                    }
                                                >
                                                    Chi tiết
                                                </button>
                                            </div>
                                            <div className="my-refunds-item-content">
                                                <p><strong>Địa chỉ:&nbsp;</strong> {refund.address}</p>
                                                <p><strong>Số điện thoại:&nbsp;</strong> {refund.phoneNumber}</p>
                                                <p><strong>Ngày đặt hàng:&nbsp;</strong> {refund.dateOders}</p>
                                                <p><strong>Số tiền đã thanh toán (gồm ship):&nbsp;</strong> {formatPrice(refund.payment?.amount)} VND</p>
                                                <p><strong>Phương thức thanh toán:&nbsp;</strong> {refund.payment?.paymentMethod}</p>
                                                <p><strong>Trạng thái hoàn tiền:&nbsp; </strong> {refund.payment?.refunded}</p>
                                                {/* <p><strong>Ngày hoàn tiền:&nbsp;</strong> {refund.payment?.dateRefund}</p> */}
                                            </div>

                                        </li>
                                    ))}
                                </ul>

                                {/* Pagination */}
                                <div className="menu-category-pagination" style={{ width: '100%' }}>
                                    {Array.from({ length: Math.ceil(refundOrders.length / itemsPerPage) }, (_, index) => (
                                        <span
                                            key={index + 1}
                                            className={`pagination-cate-dot ${currentRefundPage === index + 1 ? 'active' : ''}`}
                                            onClick={() => handleRefundPageChange(index + 1)}
                                        >
                                            •
                                        </span>
                                    ))}
                                </div>
                            </>
                        )}
                    </div>
                );

            case 'history':
                return (
                    <div>
                        {loading ? (
                            <div>Đang tải...</div>
                        ) : error ? (
                            <div>{error}</div>
                        ) : (
                            <>
                                <ul className="my-orders-list">
                                    {paginate(historyOrders, currentHistoryPage).map((order) => (
                                        <li key={order.orderId} className="my-orders-item">
                                            <div className="my-orders-item-header" style={{ background: '#a0ccdb' }}>
                                                <p><strong>Mã đơn hàng:</strong> {order.orderId}</p>
                                                <button
                                                    className="btn-view-details-ship"
                                                    onClick={() =>
                                                        navigate(`/my-order-detail/${order.shipment?.shipmentId}`, {
                                                            state: { dateDelivered: order.dateOders },
                                                        })
                                                    }
                                                >
                                                    Chi tiết
                                                </button>

                                            </div>
                                            <div className="my-orders-item-content">
                                                <p><strong>Địa chỉ:&nbsp;</strong> {order.address}</p>
                                                <p><strong>Số điện thoại:&nbsp;</strong> {order.shipment?.phoneNumber}</p>
                                                <p><strong>Giảm giá:&nbsp;</strong> {formatPrice(order.discountPrice)} VND</p>
                                                <p><strong>Phí vận chuyển: &nbsp;</strong> {formatPrice(order.deliveryFee)} VND</p>
                                                <p>
                                                    <strong>Tổng tiền:&nbsp;</strong>
                                                    {formatPrice(Math.max(order.totalPrice + order.deliveryFee - order.discountPrice, 0))} VND
                                                </p>

                                                <p><strong>Ngày đặt hàng:&nbsp;</strong> {order.dateOders}</p>
                                                <p><strong>Ngày nhận hàng:&nbsp;</strong> {order.shipment?.dateShipped}</p>
                                                <p><strong>Mã đơn giao:&nbsp; </strong> {order.shipment?.shipmentId}</p>
                                                <p><strong>Tên shipper:&nbsp; </strong> {order.shipment?.shipperName}</p>

                                            </div>
                                            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                                                <button
                                                    className="checkout-button-order"
                                                    onClick={() => handlePrint(order.orderId)}
                                                    style={{ background: '#45b6cc' }}
                                                >
                                                    Xuất hóa đơn <i className="ti-printer" style={{ fontSize: '15px' }} />
                                                </button>
                                            </div>
                                        </li>
                                    ))}
                                </ul>

                                {/* Pagination */}
                                <div className="menu-category-pagination" style={{ width: '100%' }}>
                                    {Array.from({ length: Math.ceil(historyOrders.length / itemsPerPage) }, (_, index) => (
                                        <span
                                            key={index + 1}
                                            className={`pagination-cate-dot ${currentHistoryPage === index + 1 ? 'active' : ''}`}
                                            onClick={() => handleHistoryPageChange(index + 1)}
                                        >
                                            •
                                        </span>
                                    ))}
                                </div>
                            </>
                        )}
                    </div>
                );

            default:
                return null;
        }
    };

    return (
        <>
            <Navbar currentPage={'Đơn hàng của tôi'} />
            <div className="my-orders-container">
                <div className="my-orders-sidebar">
                    <div
                        className={`my-orders-menu-item ${selectedTab === 'delivering' ? 'my-orders-active' : ''}`}
                        onClick={() => setSelectedTab('delivering')}
                    >
                        Đơn hàng đang giao
                    </div>
                    <div
                        className={`my-orders-menu-item ${selectedTab === 'waiting' ? 'my-orders-active' : ''}`}
                        onClick={() => setSelectedTab('waiting')}
                    >
                        Đơn hàng chờ giao
                    </div>
                    <div
                        className={`my-orders-menu-item ${selectedTab === 'cancelled' ? 'my-orders-active' : ''}`}
                        onClick={() => setSelectedTab('cancelled')}
                    >
                        Đơn hàng đã hủy
                    </div>
                    <div
                        className={`my-orders-menu-item ${selectedTab === 'pending' ? 'my-orders-active' : ''}`}
                        onClick={() => setSelectedTab('pending')}
                    >
                        Đơn hàng chờ thanh toán
                    </div>
                    <div
                        className={`my-orders-menu-item ${selectedTab === 'refund' ? 'my-orders-active' : ''}`}
                        onClick={() => setSelectedTab('refund')}
                    >
                        Đơn hàng hoàn tiền
                    </div>
                    <div
                        className={`my-orders-menu-item ${selectedTab === 'history' ? 'my-orders-active' : ''}`}
                        onClick={() => setSelectedTab('history')}
                    >
                        Lịch sử đặt hàng
                    </div>

                </div>
                <div className="my-orders-content">
                    {renderContent()}
                </div>
            </div>
            <Footer />
        </>
    );
};

export default MyOrder;
