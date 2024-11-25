import React, { useEffect, useState } from 'react';
import './Orders.css';
import Header from '../../components/Header/Header';

function Orders() {
    const [shipments, setShipments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [totalOrders, setTotalOrders] = useState(1);
    const [selectedStatus, setSelectedStatus] = useState('all'); // Trạng thái đã chọn

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);

        if (parts.length === 2) return parts.pop().split(';').shift();
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

    const fetchShipments = async (page, status = 'all') => {
        const token = getCookie('access_token');
        setLoading(true);

        let url = `http://localhost:1010/api/shipment/view/list-All?page=${page}&limit=5`;
        if (status !== 'all') {
            url = `http://localhost:1010/api/shipment/view/listByStatus?page=${page}&limit=5&status=${status}`;
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
            setTotalOrders(data.total)
            const shipmentsData = data.listShipment || [];
            const total = data.totalPage || 1;

            const updatedShipments = await Promise.all(
                shipmentsData.map(async (shipment) => {
                    const shipperName = shipment.shipperId
                        ? await fetchShipperName(shipment.shipperId, token)
                        : 'Unknown';
                    const paymentInfo = shipment.paymentId
                        ? await fetchPaymentInfo(shipment.paymentId, token)
                        : { paymentMethod: 'Unknown', statusPayment: 'Unknown' };

                    return {
                        ...shipment,
                        shipperName,
                        paymentMethod: paymentInfo.paymentMethod,
                        statusPayment: paymentInfo.statusPayment,
                    };
                })
            );

            setShipments(updatedShipments);
            setTotalPages(total);
        } catch (error) {
            console.error('Error fetching shipments:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchShipments(currentPage, selectedStatus);
    }, [currentPage, selectedStatus]);

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

    return (
        <div className="orders-table">
            <Header title="Đơn Hàng" />

            <div className="orders-table-row">
                <div className="orders-main-section">
                    <div className="orders-box">
                        <div className="header-orders-box">
                            <h2>Danh Sách Đơn Hàng ({totalOrders})</h2>
                            <input
                                type="text"
                                placeholder="Tìm kiếm đơn hàng..."
                                className="search-orders-input"
                                id="search-orders"
                            />
                            <select
                                className="type-select"
                                style={{ width: '200px', borderRadius: '50px' }}
                                onChange={handleFilterChange} // Kích hoạt lọc khi chọn
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
                                    <th>Khách Hàng</th>
                                    <th>Điện Thoại</th>
                                    <th>Địa Chỉ</th>
                                    <th>Loại Thanh Toán</th>
                                    <th>Trạng Thái</th>
                                    <th>Ngày Đặt</th>
                                    <th>Ngày Giao</th>
                                    <th>Người Giao</th>
                                    <th>Trạng Thái Đơn</th>
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
                                            <td>{shipment.customerName}</td>
                                            <td>{shipment.phoneNumber}</td>
                                            <td>{shipment.address}</td>
                                            <td>{shipment.paymentMethod}</td>
                                            <td>{shipment.statusPayment}</td>
                                            <td>{shipment.dateCreated}</td>
                                            <td>{shipment.dateDeliver}</td>
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

                        <div className="pagination" style={{marginLeft:'45%'}}>
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
                                    className={`btn ${
                                        number === currentPage ? 'btn-page' : 'btn-light'
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
                </div>
            </div>
        </div>
    );
}

export default Orders;