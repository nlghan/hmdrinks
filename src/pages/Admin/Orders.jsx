import React, { useEffect, useState } from 'react';
import './Orders.css';
import Header from '../../components/Header/Header';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import debounce from 'lodash/debounce';
import Swal from 'sweetalert2';
import FormDetailsOrder from '../../components/Form/FormDetailsOrder';
import axiosInstance from '../../utils/axiosConfig';
function Orders() {
    const [shipments, setShipments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [totalOrders, setTotalOrders] = useState(1);
    const [selectedStatus, setSelectedStatus] = useState('all'); // Trạng thái đã chọn
    const [refundOrders, setRefundOrders] = useState([]); // Danh sách đơn hàng hoàn tiền
    const [loadingRefunds, setLoadingRefunds] = useState(true);
    const [searchTerm, setSearchTerm] = useState("");
    const [showSuccess, setShowSuccess] = useState(false);
    const [showSuccessRe, setShowSuccessRe] = useState(false);
    const [showError, setShowError] = useState(false);

    const [cancleOrders, setCancleOrders] = useState([]); // Danh sách yêu cầu hủy đơn
    const [loadingCancle, setLoadingCancle] = useState(true); // Trạng thái tải dữ liệu

    const [showDetailsForm, setShowDetailsForm] = useState(false);
    const [selectedShipment, setSelectedShipment] = useState(null);

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);

        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const fetchRefundOrders = async (page = 1, limit = 100) => {
        const token = getCookie('access_token');
        setLoadingRefunds(true);

        try {
            const response = await axiosInstance.get(`http://localhost:1010/api/admin/list-payment-refund?page=${page}&limit=${limit}`, {
                headers: {
                    Accept: '*/*',
                    Authorization: `Bearer ${token}`,
                },
            });

            const refundOrders = response.data.listPayments.map((payment) => ({
                refundId: payment.paymentId,
                orderId: payment.orderId,
                paymentMethod: payment.paymentMethod,
                amount: payment.amount,
                refunded: payment.refunded ? "True" : "False",
            }));

            setRefundOrders(refundOrders);
        } catch (error) {
            console.error('Error fetching refund orders:', error);
        } finally {
            setLoadingRefunds(false);
        }
    };

    const fetchShipperName = async (shipperId, token) => {
        try {
            const response = await axiosInstance.get(`http://localhost:1010/api/user/info/${shipperId}`, {
                headers: {
                    Accept: '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });
            return response.data.fullName || 'Unknown';
        } catch (error) {
            console.error('Error fetching shipper name:', error);
            return 'Unknown';
        }
    };

    const fetchPaymentInfo = async (paymentId, token) => {
        try {
            const response = await axiosInstance.get(`http://localhost:1010/api/payment/view/${paymentId}`, {
                headers: {
                    Accept: '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });
            return {
                amount: response.data.amount,
                paymentMethod: response.data.paymentMethod || 'Unknown',
                statusPayment: response.data.statusPayment || 'Unknown',
            };
        } catch (error) {
            console.error('Error fetching payment info:', error);
            return {
                paymentMethod: 'Unknown',
                statusPayment: 'Unknown',
            };
        }
    };

    const fetchShipments = async (page, status = 'all', keyword = '') => {
        const token = getCookie('access_token');
        setLoading(true);

        let url = '';
        if (keyword) {
            url = `http://localhost:1010/api/shipment/search-shipment?keyword=${encodeURIComponent(keyword)}&page=${page}&limit=8`;
        } else if (status !== 'all') {
            url = `http://localhost:1010/api/shipment/view/listByStatus?page=${page}&limit=5&status=${status}`;
        } else {
            url = `http://localhost:1010/api/shipment/view/list-All?page=${page}&limit=8`;
        }

        try {
            const response = await axiosInstance.get(url, {
                headers: {
                    Accept: '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });

            const data = response.data;
            const shipmentsData = keyword ? data.listShipment : (data.listShipment || []);
            setTotalOrders(data.total || shipmentsData.length);

            const updatedShipments = await Promise.all(
                shipmentsData.map(async (shipment) => {
                    const shipperName = shipment.shipperId
                        ? await fetchShipperName(shipment.shipperId, token)
                        : 'Unknown';

                    const paymentInfo = shipment.paymentId
                        ? await fetchPaymentInfo(shipment.paymentId, token)
                        : { paymentMethod: 'Unknown', statusPayment: 'Unknown', amount: 0 };

                    return {
                        ...shipment,
                        shipperName,
                        amount: paymentInfo.amount,
                        paymentMethod: paymentInfo.paymentMethod,
                        statusPayment: paymentInfo.statusPayment,
                    };
                })
            );

            setShipments(updatedShipments);
            setTotalPages(data.totalPage || 1);
        } catch (error) {
            console.error('Error fetching shipments:', error);
        } finally {
            setLoading(false);
        }
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', {
            currency: 'VND',   // Đơn vị tiền tệ Việt Nam Đồng
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
        }).format(parseFloat(price));
    };

    useEffect(() => {
        fetchShipments(currentPage, selectedStatus, searchTerm.trim());
        fetchRefundOrders();
    }, [currentPage, selectedStatus, searchTerm]);

    const getPaginationNumbers = () => {
        let numbers = [];
        if (totalPages <= 5) {
            for (let i = 1; i <= totalPages; i++) {
                numbers.push(i);
            }
        } else {
            if (currentPage <= 3) {
                numbers = [1, 2, 3, '...', totalPages];
            } else if (currentPage >= totalPages - 2) {
                numbers = [1, '...', totalPages - 2, totalPages - 1, totalPages];
            } else {
                numbers = [1, '...', currentPage - 1, currentPage, currentPage + 1, '...', totalPages];
            }
        }
        return numbers;
    };

    const handlePageChange = (newPage) => {
        if (newPage >= 1 && newPage <= totalPages) {
            setCurrentPage(newPage);
        }
    };

    const handleStatusChange = async (shipmentId, newStatus) => {
        const token = getCookie('access_token');
        const payload = {
            shipmentId,
            status: newStatus,
        };

        try {
            await axiosInstance.post('http://localhost:1010/api/admin/shipment/activate', payload, {
                headers: {
                    Accept: '*/*',
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            console.log('Status updated successfully');
            fetchShipments(currentPage, selectedStatus);
        } catch (error) {
            console.error('Error updating status:', error);
        }
    };

    const handleFilterChange = (e) => {
        const status = e.target.value;
        setSelectedStatus(status);
        setCurrentPage(1); // Reset to first page when filtering
    };

    const getSelectBackgroundColor = (status) => {
        switch (status) {
            case 'WAITING':
                return '#f8edbe';
            case 'SHIPPING':
                return '#a8d5f3';
            case 'SUCCESS':
                return '#afeec6';
            case 'CANCELLED':
                return '#fcc0ba';
            default:
                return '#fff';
        }
    };

    const updateRefund = async (refundId) => {
        const token = getCookie('access_token');

        try {
            const result = await Swal.fire({
                title: 'Xác nhận hoàn tiền',
                text: 'Bạn có chắc chắn muốn hoàn tiền cho đơn hàng này?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Có',
                cancelButtonText: 'Không',
            });

            if (result.isConfirmed) {
                await axiosInstance.put('http://localhost:1010/api/admin/activate/refund', {
                    id: refundId,
                }, {
                    headers: {
                        'Accept': '*/*',
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                // Check if the response is JSON or plain text
                const contentType = response.headers.get('Content-Type');
                if (contentType && contentType.includes('application/json')) {
                    const data = await response.json();
                    setShowSuccessRe(true);
                    setTimeout(() => {
                        setShowSuccessRe(false);
                    }, 2000);
                    // Handle the JSON response if needed
                    fetchRefundOrders();
                    console.log('Refund activated:', data);
                } else {
                    // If it's plain text, just handle the message
                    fetchRefundOrders();
                    const message = await response.text();
                    console.log('Refund activated:', message);
                }
            }

        } catch (error) {
            console.error('Error activating refund:', error);
            fetchRefundOrders();
        }
    };

    const handleInputChange = debounce((event, newInputValue) => {
        setSearchTerm(newInputValue);
        const trimmedValue = newInputValue.trim();
        if (trimmedValue === '') {
            // If input is empty, fetch all shipments
            fetchShipments(currentPage, selectedStatus);
        } else {
            // If there's a search term, fetch filtered shipments
            fetchShipments(currentPage, selectedStatus, trimmedValue);
        }
    }, 500);

    const fetchCancelOrders = async () => {
        const token = getCookie('access_token');
        setLoadingCancle(true);

        try {
            const response = await axiosInstance.get('http://localhost:1010/api/orders/list-cancel-reason', {
                headers: {
                    Accept: '*/*',
                    Authorization: `Bearer ${token}`,
                },
            });

            if (response.data && response.data.list) {
                setCancleOrders(response.data.list);
            }
        } catch (error) {
            console.error('Lỗi khi lấy danh sách yêu cầu hủy đơn:', error);
        } finally {
            setLoadingCancle(false);
        }
    };

    // Sử dụng useEffect để gọi fetchCancelOrders khi component mount
    useEffect(() => {
        fetchCancelOrders(); // Gọi hàm fetch khi component mount
    }, []);

    const acceptCancel = async (orderId) => {
        try {
            const result = await Swal.fire({
                title: 'Xác nhận duyệt yêu cầu',
                text: 'Bạn có chắc chắn muốn duyệt yêu cầu hủy đơn này?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Có',
                cancelButtonText: 'Không',
            });

            if (result.isConfirmed) {
                const token = getCookie('access_token');

                const response = await axiosInstance.post('http://localhost:1010/api/orders/reason-cancel/accept', {
                    id: orderId,
                }, {
                    headers: {
                        'Accept': '*/*',
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                const data = await response.data;

                if (response.status === 200 && data === 'Success') {
                    setShowSuccess(true);
                    setTimeout(() => {
                        setShowSuccess(false);
                    }, 2000);
                    console.log('Yêu cầu hủy đơn đã được chấp nhận:', data);
                    fetchCancelOrders();
                } else {
                    console.error('Lỗi khi chấp nhận yêu cầu hủy đơn:', data.message || 'Không xác định');
                }
            }
        } catch (error) {
            console.error('Lỗi kết nối khi chấp nhận yêu cầu hủy đơn:', error);
        }
    };

    const rejectCancel = async (orderId) => {
        try {
            const result = await Swal.fire({
                title: 'Xác nhận từ chối yêu cầu',
                text: 'Bạn có chắc muốn từ chối yêu cầu hủy đơn này?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Có',
                cancelButtonText: 'Không',
            });

            if (result.isConfirmed) {
                const token = getCookie('access_token');

                const response = await axiosInstance.post('http://localhost:1010/api/orders/reason-cancel/reject', {
                    id: orderId,
                }, {
                    headers: {
                        'Accept': '*/*',
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                const data = await response.data;

                if (response.status === 200 && data === 'Success') {
                    setShowSuccess(true);
                    setTimeout(() => {
                        setShowSuccess(false);
                    }, 2000);
                    console.log('Yêu cầu hủy đơn đã bị từ chối:', data);
                    fetchCancelOrders();
                } else {
                    console.error('Lỗi khi từ chối yêu cầu hủy đơn:', data || 'Không xác định');
                }
            }
        } catch (error) {
            console.error('Lỗi kết nối khi từ chối yêu cầu hủy đơn:', error);
        }
    };

    const handleDetailsClick = (shipment) => {
        setSelectedShipment(shipment);
        setShowDetailsForm(true);
    };

    const getCancelReasonText = (reason) => {
        const reasonMap = {
            'CHANGED_MY_MIND': 'Đổi ý không muốn mua nữa',
            'FOUND_CHEAPER_ELSEWHERE': 'Tìm thấy giá rẻ hơn ở nơi khác',
            'ORDERED_BY_MISTAKE': 'Đặt nhầm sản phẩm',
            'DELIVERY_TOO_SLOW': 'Giao hàng quá chậm',
            'WRONG_PRODUCT_SELECTED': 'Chọn nhầm sản phẩm',
            'NOT_NEEDED_ANYMORE': 'Không cần sản phẩm nữa',
            'PAYMENT_ISSUES': 'Gặp vấn đề khi thanh toán',
            'PREFER_DIFFERENT_STORE': 'Thích mua ở cửa hàng khác hơn',
            'UNSATISFIED_WITH_SERVICE': 'Không hài lòng với dịch vụ',
            'OTHER_REASON': 'Lý do khác'
        };
        
        return reasonMap[reason] || reason;
    };
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    return (
        <div className="orders-table">
            <Header isMenuOpen={isMenuOpen} toggleMenu={toggleMenu} title="Đơn Hàng" />
            <div className="orders-table-row">
                <div className="orders-main-section">
                    <div className="orders-box">
                        <div className="header-orders-box">
                            <h2>Danh Sách Đơn Hàng ({totalOrders})</h2>
                            <Autocomplete
                                freeSolo
                                options={Array.isArray(shipments) ? shipments.map((shipment) => shipment.customerName) : []}
                                inputValue={searchTerm}
                                onInputChange={handleInputChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Tìm kiếm đơn hàng theo tên..."
                                        variant="outlined"
                                        className="search-orders-input"
                                    />
                                )}
                                style={{ width: '1000px', borderRadius: '20px', marginLeft: '200px', marginRight: '-1300px' }}
                            />
                            <select
                                className="type-select"
                                style={{ width: '200px', borderRadius: '50px' }}
                                onChange={handleFilterChange}
                            >
                                <option value="all">Tất cả</option>
                                <option value="WAITING">Chờ xử lý</option>
                                <option value="SHIPPING">Đang giao</option>
                                <option value="SUCCESS">Đã giao</option>
                                <option value="CANCELLED">Đã hủy</option>
                            </select>
                        </div>

                        <table className="table-orders">
                            <thead>
                                <tr>
                                    <th>Mã Đơn Giao</th>
                                    <th>Mã Đơn Hàng</th>
                                    <th>Khách Hàng</th>
                                    <th>Điện Thoại</th>
                                    <th>Đơn Giá (VND)</th>
                                    <th>Loại Thanh Toán</th>
                                    {/* <th>Trạng Thái</th> */}
                                    <th>Ngày Đặt</th>
                                    <th>Ngày Giao</th>
                                    <th>Ngày Hủy</th>
                                    <th>Người Giao</th>
                                    <th>Trạng Thái</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                {loading ? (
                                    <tr>
                                        <td colSpan="9" style={{ textAlign: 'center' }}>Đang tải dữ liệu...</td>
                                    </tr>
                                ) : shipments.length > 0 ? (
                                    shipments.map((shipment) => (
                                        <tr key={shipment.shipmentId}>
                                            <td>{shipment.shipmentId}</td>
                                            <td>{shipment.orderId}</td>
                                            <td>{shipment.customerName}</td>
                                            <td>{shipment.phoneNumber}</td>
                                            <td>{formatPrice(shipment.amount)}</td>
                                            <td>{shipment.paymentMethod}</td>
                                            {/* <td>{shipment.statusPayment}</td> */}
                                            <td>{shipment.dateCreated}</td>
                                            <td>{shipment.dateShipped}</td>
                                            <td>{shipment.dateCancelled}</td>
                                            <td>{shipment.shipperName}</td>
                                            <td>
                                                <select
                                                    id='select-status'
                                                    value={shipment.status}
                                                    disabled
                                                    // onChange={(e) =>
                                                    //     handleStatusChange(
                                                    //         shipment.shipmentId,
                                                    //         e.target.value
                                                    //     )
                                                    // }
                                                    style={{

                                                        backgroundColor: getSelectBackgroundColor(
                                                            shipment.status
                                                        ),

                                                    }}
                                                >
                                                    <option value="WAITING">Chờ xử lý</option>
                                                    <option value="SHIPPING">Đang giao</option>
                                                    <option value="SUCCESS">Đã giao</option>
                                                    <option value="CANCELLED">Đã hủy</option>
                                                </select>
                                            </td>
                                            <td>
                                                <button 
                                                    style={{ backgroundColor: 'white', size: '20px', color: 'green'}}
                                                    onClick={() => handleDetailsClick(shipment)}
                                                >
                                                    <i className="ti-info-alt"></i>
                                                </button>
                                            </td>

                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan="9" style={{ textAlign: 'center' }}>
                                            Không có kết quả tìm kiếm.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>

                        <div className="pagination" style={{ marginLeft: '45%' }}>
                            <button
                                className="btn btn-pre me-2"
                                onClick={() => handlePageChange(currentPage - 1)}
                                disabled={currentPage === 1}
                            >
                                &lt;
                            </button>
                            {getPaginationNumbers().map((number, index) => (
                                <button
                                    key={index}
                                    className={`btn ${number === currentPage ? 'btn-page' : 'btn-light'
                                        } me-2`}
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
                                disabled={currentPage === totalPages}
                            >
                                &gt;
                            </button>
                        </div>
                    </div>

                    <div className="refund-orders-box">
                        <div className="header-orders-box">
                            <h2>Danh Sách Yêu Cầu Hủy Đơn</h2>
                        </div>
                        <div className="table-container">
                            <table className="table-orders-refund">
                                <thead>
                                    <tr>
                                        <th>Mã Đơn Hàng</th>
                                        <th>Mã Người Dùng</th>
                                        <th>Lý Do Hủy Đơn</th>
                                        <th>Hành động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {loadingCancle ? (
                                        <tr>
                                            <td colSpan="4" style={{ textAlign: 'center' }}>Đang tải dữ liệu...</td>
                                        </tr>
                                    ) : cancleOrders.length > 0 ? (
                                        cancleOrders.map((order) => (
                                            <tr key={order.orderId}>
                                                <td>{order.orderId}</td>
                                                <td>{order.userId}</td>
                                                <td>{getCancelReasonText(order.cancelReason)}</td>
                                                <td>
                                                    <button onClick={() => acceptCancel(order.orderId)}>
                                                        <i className="ti-check" style={{ color: 'blue', fontSize: '20px' }}></i> {/* Edit icon */}
                                                    </button>
                                                    <button style={{ marginLeft: '10px' }} onClick={() => rejectCancel(order.orderId)}>
                                                        <i className="ti-close" style={{ color: 'red', fontSize: '20px' }}></i> {/* Edit icon */}
                                                    </button>
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan="4" style={{ textAlign: 'center' }}>Không có yêu cầu hủy đơn.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>


                    {/* Bảng Danh Sách Đơn Hoàn Tiền */}
                    <div className="refund-orders-box">
                        <div className="header-orders-box">
                            <h2>Danh Sách Đơn Hoàn Tiền</h2>
                        </div>
                        <div className="table-container">
                            <table className="table-orders-refund">
                                <thead>
                                    <tr>
                                        <th>Mã Đơn Thanh Toán</th>
                                        <th>Mã Đơn Hàng</th>
                                        <th>Hình thức Thanh Toán</th>
                                        <th>Số Tiền</th>
                                        <th>Trạng Thái Hoàn Tiền</th>
                                        <th>Hành động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {loadingRefunds ? (
                                        <tr>
                                            <td colSpan="6" style={{ textAlign: 'center' }}>Đang tải dữ liệu...</td>
                                        </tr>
                                    ) : refundOrders.length > 0 ? (
                                        refundOrders.map((refund) => (
                                            <tr key={refund.refundId}>
                                                <td>{refund.refundId}</td>
                                                <td>{refund.orderId}</td>
                                                <td>{refund.paymentMethod}</td>
                                                <td>{formatPrice(refund.amount)} VND</td>
                                                <td>
                                                    <span style={{ 
                                                        color: refund.refunded === 'true' ? 'green' : 'red',
                                                        fontWeight: '500'
                                                    }}>
                                                        {refund.refunded === 'True' ? 'Đã hoàn tiền' : 'Chưa hoàn tiền'}
                                                    </span>
                                                </td>
                                                <td>
                                                    {refund.refunded === 'True' ? (
                                                        <span style={{ 
                                                            color: 'green', 
                                                            fontWeight: '500',
                                                            padding: '5px 10px',
                                                            backgroundColor: '#e8f5e9',
                                                            borderRadius: '4px'
                                                        }}>
                                                            Đã xử lý
                                                        </span>
                                                    ) : (
                                                        <button onClick={() => updateRefund(refund.refundId)}>
                                                            <i className="ti-pencil" style={{ color: 'blue', fontSize: '18px' }}></i>
                                                        </button>
                                                    )}
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan="6" style={{ textAlign: 'center' }}>Không có đơn hoàn tiền.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                    {showSuccess && (
                        <div className="success-animation">
                            <div className="success-modal">
                                <div className="success-icon">
                                    <div className="success-icon-circle">
                                        <svg className="checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                            <circle className="checkmark-circle" cx="26" cy="26" r="25" fill="none" />
                                            <path className="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
                                        </svg>
                                    </div>
                                </div>
                                <h3>Xử lý yêu cầu hủy đơn thành công!</h3>
                            </div>
                        </div>
                    )}
                    {showSuccessRe && (
                        <div className="success-animation">
                            <div className="success-modal">
                                <div className="success-icon">
                                    <div className="success-icon-circle">
                                        <svg className="checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                            <circle className="checkmark-circle" cx="26" cy="26" r="25" fill="none" />
                                            <path className="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
                                        </svg>
                                    </div>
                                </div>
                                <h3>Xử lý yêu cầu hoàn tiền thành công!</h3>
                            </div>
                        </div>
                    )}

                    {showError && (
                        <div className="error-animation">
                            <div className="error-modal">
                                <div className="error-icon">
                                    <div className="error-icon-circle">
                                        <svg className="cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                            <circle className="cross-circle" cx="26" cy="26" r="25" fill="none" />
                                            <path className="cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36" />
                                        </svg>
                                    </div>
                                </div>
                                <h3>Số lượng phải lớn hơn 0</h3>
                                <p>Vui lòng nhập số lượng khác</p>
                            </div>
                        </div>
                    )}

                    {showDetailsForm && selectedShipment && (
                        <FormDetailsOrder 
                            shipment={selectedShipment}
                            onClose={() => setShowDetailsForm(false)}
                            formatPrice={formatPrice}
                        />
                    )}
                </div>


            </div>
        </div>


    );
}

export default Orders;
