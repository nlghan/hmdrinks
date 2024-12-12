import React, { useState, useEffect } from 'react';
import './FormDetailsOrder.css';
import axios from 'axios';
import axiosInstance from '../../utils/axiosConfig';

const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
};

const FormDetailsOrder = ({ shipment, onClose, formatPrice }) => {
    const [orderDetails, setOrderDetails] = useState(null);

    useEffect(() => {
        fetchOrderDetails();
    }, [shipment.orderId]);

    const fetchOrderDetails = async () => {
        try {
            const token = getCookie('access_token');

            if (!token) {
                console.error('No token found');
                return;
            }

            const response = await axiosInstance.get(
                `${import.meta.env.VITE_API_BASE_URL}/orders/detail-item/${shipment.orderId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            setOrderDetails(response.data);
        } catch (error) {
            console.error('Error fetching order details:', error);
        }
    };

    const renderShippingInfo = () => {
        switch (shipment.status) {
            case 'WAITING':
            case 'SHIPPING':
                return (
                    <>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Người Giao Hàng:</span>
                            <span className="fod-detail-value">{shipment.shipperName}</span>
                        </div>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Ngày Đặt:</span>
                            <span className="fod-detail-value">{shipment.dateCreated}</span>
                        </div>
                    </>
                );
            case 'SUCCESS':
                return (
                    <>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Người Giao Hàng:</span>
                            <span className="fod-detail-value">{shipment.shipperName}</span>
                        </div>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Ngày Đặt:</span>
                            <span className="fod-detail-value">{shipment.dateCreated}</span>
                        </div>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Ngày Giao:</span>
                            <span className="fod-detail-value">{shipment.dateShipped}</span>
                        </div>
                    </>
                );
            case 'CANCELLED':
                return (
                    <>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Người Giao Hàng:</span>
                            <span className="fod-detail-value">{shipment.shipperName}</span>
                        </div>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Ngày Đặt:</span>
                            <span className="fod-detail-value">{shipment.dateCreated}</span>
                        </div>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Ngày Hủy:</span>
                            <span className="fod-detail-value">{shipment.dateCancelled}</span>
                        </div>
                    </>
                );
            default:
                return null;
        }
    };

    const getSelectBackgroundColor = (status) => {
        switch (status) {
            case 'WAITING':
                return '#FFF3CD';
            case 'SHIPPING':
                return '#CCE5FF';
            case 'SUCCESS':
                return '#D4EDDA';
            case 'CANCELLED':
                return '#F8D7DA';
            default:
                return '#FFFFFF';
        }
    };

    return (
        <div className="fod-overlay">
            <div className="fod-container">
                <button
                    onClick={onClose}
                    style={{
                        height: '35px',
                        width: '35px',
                        borderRadius: '50%',
                        backgroundColor: '#e8e6e6',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        border: 'none'
                    }}
                    className="fod-close-button"
                >
                    <i className="ti-close" style={{ color: '#f21b1b', fontSize: '18px' }}></i>
                </button>

                <div className="fod-content">
                    <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Chi Tiết Đơn Hàng</h2>

                    <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '20px' }}>
                        <select
                            className="fod-status-select"
                            value={shipment.status}
                            disabled
                            style={{
                                backgroundColor: getSelectBackgroundColor(shipment.status),
                                padding: '8px 15px',
                                borderRadius: '4px',
                                border: '1px solid #ddd',
                                fontSize: '14px',
                                width: '150px',
                                textAlign: 'center'
                            }}
                        >
                            <option value="WAITING">Chờ xử lý</option>
                            <option value="SHIPPING">Đang giao</option>
                            <option value="SUCCESS">Đã giao</option>
                            <option value="CANCELLED">Đã hủy</option>
                        </select>
                    </div>

                    <div className="fod-section">
                        <h4>Thông Tin Đơn Hàng</h4>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Mã Đơn Giao:</span>
                            <span className="fod-detail-value">{shipment.shipmentId}</span>
                        </div>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Mã Đơn Hàng:</span>
                            <span className="fod-detail-value">{shipment.orderId}</span>
                        </div>

                        {orderDetails && (
                            <div className="fod-order-items">
                                <h5 className="fod-items-title">Chi tiết sản phẩm:</h5>
                                <ul className="fod-items-list">
                                    {orderDetails.listItemOrders.map(item => (
                                        <li key={item.cartItemId} className="fod-item">
                                            <div className="fod-detail-row">
                                                <span className="fod-detail-label">Sản phẩm:</span>
                                                <span className="fod-detail-value">{item.proName}</span>
                                            </div>
                                            <div className="fod-detail-row">
                                                <span className="fod-detail-label">Kích cỡ:</span>
                                                <span className="fod-detail-value">{item.size}</span>
                                            </div>
                                            <div className="fod-detail-row">
                                                <span className="fod-detail-label">Giá:</span>
                                                <span className="fod-detail-value">{formatPrice(item.priceItem)} VND</span>
                                            </div>
                                            <div className="fod-detail-row">
                                                <span className="fod-detail-label">Số lượng:</span>
                                                <span className="fod-detail-value">{item.quantity}</span>
                                            </div>
                                            <div className="fod-detail-row">
                                                <span className="fod-detail-label">Thành tiền:</span>
                                                <span className="fod-detail-value">{formatPrice(item.totalPrice)} VND</span>
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}
                    </div>

                    <div className="fod-section">
                        <h4>Thông Tin Khách Hàng</h4>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Tên Khách Hàng:</span>
                            <span className="fod-detail-value">{shipment.customerName}</span>
                        </div>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Số Điện Thoại:</span>
                            <span className="fod-detail-value">{shipment.phoneNumber}</span>
                        </div>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Địa Chỉ:</span>
                            <span className="fod-detail-value">{shipment.address}</span>
                        </div>
                    </div>

                    <div className="fod-section">
                        <h4>Thông Tin Thanh Toán</h4>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Tổng Tiền:</span>
                            <span className="fod-detail-value">{formatPrice(shipment.amount)} VND</span>
                        </div>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Phương Thức:</span>
                            <span className="fod-detail-value">{shipment.paymentMethod}</span>
                        </div>
                        <div className="fod-detail-row">
                            <span className="fod-detail-label">Trạng Thái:</span>
                            <span className="fod-detail-value">{shipment.statusPayment}</span>
                        </div>
                    </div>

                    <div className="fod-section">
                        <h4>Thông Tin Giao Hàng</h4>
                        {renderShippingInfo()}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FormDetailsOrder;
