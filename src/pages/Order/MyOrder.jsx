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
        const token = getCookie('access_token')
        const userId = getUserIdFromToken(token);
        try {
            const response = await axios.get(
                `http://localhost:1010/api/orders/view/${userId}/status?page=1&limit=100&status=CANCELLED`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            setCancelledOrders(response.data.listOrders);
        } catch (err) {
            setError('Không thể tải danh sách đơn hàng đã hủy.');
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
            const response = await axios.get(
                `http://localhost:1010/api/orders/view/${userId}/status?page=1&limit=10&status=WAITING`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            // Lấy danh sách đơn hàng từ API
            const fetchedOrders = response.data.listOrders;

            // Duyệt qua các đơn hàng đã CONFIRMED để kiểm tra payment
            const confirmedResponse = await axios.get(
                `http://localhost:1010/api/orders/view/${userId}/status?page=1&limit=20&status=CONFIRMED`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            const confirmedOrders = confirmedResponse.data.listOrders;

            // Kiểm tra từng đơn hàng CONFIRMED với API info-payment
            const waitingFromConfirmed = [];
            for (const order of confirmedOrders) {
                try {
                    await axios.get(
                        `http://localhost:1010/api/orders/info-payment?orderId=${order.orderId}`,
                        {
                            headers: {
                                Authorization: `Bearer ${token}`,
                            },
                        }
                    );
                } catch (err) {
                    if (err.response && err.response.status === 404) {
                        // Nếu không có thông tin thanh toán, thêm vào danh sách waiting
                        waitingFromConfirmed.push(order);
                    } else {
                        console.error(`Lỗi khi kiểm tra payment cho orderId ${order.orderId}:`, err);
                    }
                }
            }

            // Gộp danh sách đơn hàng chờ từ API và từ CONFIRMED không có payment
            setWaitingOrders([...fetchedOrders, ...waitingFromConfirmed]);
        } catch (err) {
            setError('Không thể tải danh sách đơn hàng đang chờ thanh toán.');
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
        else if (selectedTab === 'history') {
            fetchHistoryOrders();
            window.scrollTo(0, 0);
        }
    }, [selectedTab]);


    const [currentCancelledPage, setCurrentCancelledPage] = useState(1);
    const [currentPendingPage, setCurrentPendingPage] = useState(1);

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
                                                        navigate(`/my-order-detail/${order.shipment?.shipmentId}`, {
                                                            state: { dateDelivered: order.dateOders },
                                                        })
                                                    }
                                                >
                                                    Chi tiết
                                                </button>
                                            </div>
                                            <div className="my-orders-item-content">
                                                <p><strong>Địa chỉ:</strong> {order.address}</p>
                                                <p><strong>Số điện thoại:</strong> {order.phone}</p>
                                                <p><strong>Giảm giá:</strong> {order.discountPrice} VND</p>
                                                <p><strong>Phí vận chuyển:</strong> {order.deliveryFee} VND</p>
                                                <p><strong>Tổng tiền:</strong> {order.totalPrice} VND</p>
                                                <p><strong>Ngày đặt hàng:</strong> {order.dateOders}</p>
                                                <p><strong>Mã đơn giao: </strong> {order.shipment?.shipmentId}</p>
                                                <p><strong>Tên shipper: </strong> {order.shipment?.shipperName}</p>

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
                                                        navigate(`/my-order-detail/${order.shipment?.shipmentId}`, {
                                                            state: { dateDelivered: order.dateOders },
                                                        })
                                                    }
                                                >
                                                    Chi tiết
                                                </button>
                                            </div>
                                            <div className="my-orders-item-content">
                                                <p><strong>Địa chỉ: </strong> {order.address}</p>
                                                <p><strong>Số điện thoại: </strong> {order.phone}</p>
                                                <p><strong>Giảm giá: </strong> {order.discountPrice} VND</p>
                                                <p><strong>Phí vận chuyển: </strong> {order.deliveryFee} VND</p>
                                                <p><strong>Tổng tiền: </strong> {order.totalPrice} VND</p>
                                                <p><strong>Ngày đặt hàng: </strong> {order.dateOders}</p>
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
                                                <p><strong>Địa chỉ:</strong> {order.address}</p>
                                                <p><strong>Số điện thoại:</strong> {order.phone}</p>
                                                <p><strong>Giảm giá:</strong> {order.discountPrice} VND</p>
                                                <p><strong>Phí vận chuyển:</strong> {order.deliveryFee} VND</p>
                                                <p><strong>Tổng tiền:</strong> {order.totalPrice} VND</p>
                                                <p><strong>Ngày đặt hàng:</strong> {order.dateOders}</p>
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
                                                <p><strong>Địa chỉ:</strong> {order.address}</p>
                                                <p><strong>Số điện thoại:</strong> {order.shipment?.phoneNumber}</p>
                                                <p><strong>Giảm giá:</strong> {order.discountPrice} VND</p>
                                                <p><strong>Phí vận chuyển:</strong> {order.deliveryFee} VND</p>
                                                <p><strong>Tổng tiền:</strong> {order.totalPrice} VND</p>
                                                <p><strong>Ngày đặt hàng:</strong> {order.dateOders}</p>
                                                <p><strong>Ngày nhận hàng:</strong> {order.shipment?.dateShipped}</p>
                                                <p><strong>Mã đơn giao: </strong> {order.shipment?.shipmentId}</p>
                                                <p><strong>Tên shipper: </strong> {order.shipment?.shipperName}</p>

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
