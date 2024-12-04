import React, { useEffect, useState } from 'react';
import './Orders.css';
import Header from '../../components/Header/Header';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import debounce from 'lodash/debounce';

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


    const [cancleOrders, setCancleOrders] = useState([]); // Danh sách yêu cầu hủy đơn
    const [loadingCancle, setLoadingCancle] = useState(true); // Trạng thái tải dữ liệu

   


    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);

        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const fetchRefundOrders = async (page = 1, limit = 100) => {
        const token = getCookie('access_token');
        setLoadingRefunds(true);

        try {
            const response = await fetch(`http://localhost:1010/api/admin/list-payment-refund?page=${page}&limit=${limit}`, {
                method: 'GET',
                headers: {
                    Accept: '*/*',
                    Authorization: `Bearer ${token}`,
                },
            });

            const data = await response.json();

            const refundOrders = data.listPayments.map((payment) => ({
                refundId: payment.paymentId,
                orderId: payment.orderId,
                paymentMethod: payment.paymentMethod,
                amount: payment.amount,
                refunded: payment.refunded ? "True" : "False", // Convert boolean to string
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
            const response = await fetch(`http://localhost:1010/api/user/info/${shipperId}`, {
                method: 'GET',
                headers: {
                    Accept: '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });
            const data = await response.json();
            return data.fullName || 'Unknown';
        } catch (error) {
            console.error('Error fetching shipper name:', error);
            return 'Unknown';
        }
    };

    const fetchPaymentInfo = async (paymentId, token) => {
        try {
            const response = await fetch(`http://localhost:1010/api/payment/view/${paymentId}`, {
                method: 'GET',
                headers: {
                    Accept: '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });
            const data = await response.json();
            return {
                amount: data.amount,
                paymentMethod: data.paymentMethod || 'Unknown',
                statusPayment: data.statusPayment || 'Unknown',
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
            url = `http://localhost:1010/api/shipment/search-shipment?keyword=${encodeURIComponent(keyword)}&page=${page}&limit=5`;
        } else if (status !== 'all') {
            url = `http://localhost:1010/api/shipment/view/listByStatus?page=${page}&limit=5&status=${status}`;
        } else {
            url = `http://localhost:1010/api/shipment/view/list-All?page=${page}&limit=5`;
        }

        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    Accept: '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });
            const data = await response.json();

            // Handle the response data based on whether it's a search result or normal list
            const shipmentsData = keyword ? data.listShipment : (data.listShipment || []);
            setTotalOrders(data.total || shipmentsData.length);

            const updatedShipments = await Promise.all(
                shipmentsData.map(async (shipment) => {
                    const shipperName = shipment.shipperId
                        ? await fetchShipperName(shipment.shipperId, token)
                        : 'Unknown';

                    // Fetch payment info and amount
                    const paymentInfo = shipment.paymentId
                        ? await fetchPaymentInfo(shipment.paymentId, token)
                        : { paymentMethod: 'Unknown', statusPayment: 'Unknown', amount: 0 }; // Default amount if not found

                    return {
                        ...shipment,
                        shipperName,
                        amount: paymentInfo.amount, // Lấy giá trị amount
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
            const response = await fetch('http://localhost:1010/api/admin/shipment/activate', {
                method: 'POST',
                headers: {
                    Accept: '*/*',
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
            });

            const data = await response.json();
            console.log('Status updated successfully:', data);

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
        const token = getCookie('access_token');  // Get token from the cookie

        try {
            const response = await fetch('http://localhost:1010/api/admin/activate/refund', {
                method: 'PUT',
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    id: refundId,  // Send the refund ID to activate it
                }),
            });

            // Check if the response is JSON or plain text
            const contentType = response.headers.get('Content-Type');
            if (contentType && contentType.includes('application/json')) {
                const data = await response.json();
                // Handle the JSON response if needed
                fetchRefundOrders();
                console.log('Refund activated:', data);
            } else {
                // If it's plain text, just handle the message
                fetchRefundOrders();
                const message = await response.text();
                console.log('Refund activated:', message);
            }

        } catch (error) {
            fetchRefundOrders();  // Refresh the refund list after activation
            console.error('Error activating refund:', error);
            alert('Error activating refund');
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
        setLoadingCancle(true); // Bắt đầu tải dữ liệu
        const token = getCookie('access_token'); // Lấy token từ cookie
    
        try {
            // Gửi yêu cầu GET để lấy danh sách hủy đơn
            const response = await fetch('http://localhost:1010/api/orders/list-cancel-reason', {
                method: 'GET',
                headers: {
                    Accept: '*/*',
                    Authorization: `Bearer ${token}`,
                },
            });
    
            // Chuyển đổi phản hồi thành JSON
            const data = await response.json();
    
            // Kiểm tra nếu có dữ liệu trả về và cập nhật danh sách đơn hủy
            if (data && data.list) {
                setCancleOrders(data.list);
            }
        } catch (error) {
            console.error('Lỗi khi lấy danh sách yêu cầu hủy đơn:', error);
        } finally {
            setLoadingCancle(false); // Kết thúc tải dữ liệu
        }
    };
    
    // Sử dụng useEffect để gọi fetchCancelOrders khi component mount
    useEffect(() => {
        fetchCancelOrders(); // Gọi hàm fetch khi component mount
    }, []);


    const acceptCancel = async (orderId) => {
        const token = getCookie('access_token');  // Lấy token từ cookie
    
        try {
            const response = await fetch('http://localhost:1010/api/orders/reason-cancel/accept', {
                method: 'POST',
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ id: orderId }),
            });
    
             // Kiểm tra xem phản hồi có phải là chuỗi "Success" hay không
             const data = await response.text(); // Đọc phản hồi như văn bản (text)
            
             // Kiểm tra nếu phản hồi là "Success"
             if (response.ok && data === 'Success') {
                console.log('Yêu cầu hủy đơn đã được chấp nhận:', data);
                // Gọi lại fetchCancelOrders để cập nhật danh sách sau khi thao tác thành công
                fetchCancelOrders();
            } else {
                console.error('Lỗi khi chấp nhận yêu cầu hủy đơn:', data.message || 'Không xác định');
            }
        } catch (error) {
            console.error('Lỗi kết nối khi chấp nhận yêu cầu hủy đơn:', error);
        }
    };
    
    // Hàm xử lý từ chối yêu cầu hủy đơn
    const rejectCancel = async (orderId) => {
        const token = getCookie('access_token');  // Lấy token từ cookie
    
        try {
            const response = await fetch('http://localhost:1010/api/orders/reason-cancel/reject', {
                method: 'POST',
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ id: orderId }),
            });
    
            // Kiểm tra xem phản hồi có phải là chuỗi "Success" hay không
            const data = await response.text(); // Đọc phản hồi như văn bản (text)
            
            // Kiểm tra nếu phản hồi là "Success"
            if (response.ok && data === 'Success') {
                console.log('Yêu cầu hủy đơn đã bị từ chối:', data);
                fetchCancelOrders(); // Gọi lại fetchCancelOrders để cập nhật danh sách yêu cầu hủy đơn
            } else {
                console.error('Lỗi khi từ chối yêu cầu hủy đơn:', data || 'Không xác định');
            }
        } catch (error) {
            console.error('Lỗi kết nối khi từ chối yêu cầu hủy đơn:', error);
        }
    };
    
    
    


    return (
        <div className="orders-table">
            <Header title="Đơn Hàng" />

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
                                    <th>Địa Chỉ</th>
                                    <th>Đơn Giá (VND)</th>
                                    <th>Loại Thanh Toán</th>
                                    <th>Trạng Thái</th>
                                    <th>Ngày Đặt</th>
                                    <th>Ngày Giao</th>
                                    <th>Ngày Hủy</th>
                                    <th>Người Giao</th>
                                    <th>Trạng Thái</th>
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
                                            <td>{shipment.address}</td>
                                            <td>{formatPrice(shipment.amount)}</td>
                                            <td>{shipment.paymentMethod}</td>
                                            <td>{shipment.statusPayment}</td>
                                            <td>{shipment.dateCreated}</td>
                                            <td>{shipment.dateShipped}</td>
                                            <td>{shipment.dateCancelled}</td>
                                            <td>{shipment.shipperName}</td>
                                            <td>
                                                <select
                                                    id='select-status'
                                                    value={shipment.status}
                                                    onChange={(e) =>
                                                        handleStatusChange(
                                                            shipment.shipmentId,
                                                            e.target.value
                                                        )
                                                    }
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
                                                <td>{order.cancelReason}</td>
                                                <td>
                                                <button onClick={() => acceptCancel(order.orderId)}>
                                                        <i className="ti-check" style={{ color: 'blue', fontSize: '20px' }}></i> {/* Edit icon */}
                                                    </button>
                                                    <button style={{marginLeft:'10px'}} onClick={() => rejectCancel(order.orderId)}>
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
                                                <td>{refund.refunded}</td>
                                                <td>
                                                    <button onClick={() => updateRefund(refund.refundId)}>
                                                        <i className="ti-pencil" style={{ color: 'blue', fontSize: '20px' }}></i> {/* Edit icon */}
                                                    </button>
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

                </div>


            </div>
        </div>


    );
}

export default Orders;
