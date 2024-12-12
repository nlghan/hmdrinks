import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import axiosInstance from '../../utils/axiosConfig';
const PaymentOnlineStatusPayos = () => {
    const [status, setStatus] = useState('Đang xử lý...');
    const location = useLocation();
    const navigate = useNavigate();

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const verifyPaymentStatus = async (orderCode) => {
        const token = getCookie('access_token');
        if (!token) {
            console.error("Không tìm thấy token");
            setStatus("Không tìm thấy token");
            return;
        }

        try {
            const response = await axiosInstance.get(`http://localhost:1010/api/payment/payOS/callback`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': '*/*'
                },
                params: { orderCode }
            });

            const { statusPayment, amount, paymentMethod, dateCreated } = response.data;

            if (statusPayment === 'PAID' || statusPayment === 'COMPLETED') {
                setStatus(`Thanh toán thành công! Số tiền: ${amount} VND, Phương thức: ${paymentMethod}, Ngày: ${dateCreated}`);
                navigate('/payment-status', { state: { status: 'success' } });
            } else if (statusPayment === 'FAILED') {
                setStatus(`Thanh toán thất bại. Số tiền: ${amount} VND, Phương thức: ${paymentMethod}, Ngày: ${dateCreated}`);
                navigate('/payment-status', { state: { status: 'failure' } });
            } else {
                setStatus(`Trạng thái thanh toán không xác định: ${statusPayment}`);
                navigate('/payment-status', { state: { status: 'unknown' } });
            }
        } catch (error) {
            console.error("Có lỗi xảy ra khi xác minh thanh toán:", error);
            setStatus('Có lỗi xảy ra khi xác minh thanh toán');
            navigate('/payment-status', { state: { status: 'failure' } });
        }
    };

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        const orderCode = queryParams.get('orderCode');

        if (orderCode) {
            verifyPaymentStatus(orderCode);
        } else {
            setStatus('Không tìm thấy mã đơn hàng.');
        }
    }, [location.search]);

    return (
        <div>
            <h2>{status}</h2>
        </div>
    );
};

export default PaymentOnlineStatusPayos;
