import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import axiosInstance from '../../utils/axiosConfig';
const PaymentOnlineStatusMomo = () => {
    const [status, setStatus] = useState('Đang xử lý...');
    const location = useLocation();
    const navigate = useNavigate();

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const verifyPaymentStatus = async (orderId, resultCode) => {
        const token = getCookie('access_token');
        if (!token) {
            console.error("Không tìm thấy token");
            setStatus("Không tìm thấy token");
            return;
        }

        try {
            const response = await axiosInstance.get(`http://localhost:1010/api/payment/momo/callback`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': '*/*'
                },
                params: { orderId, resultCode }
            });

            const { message, status: apiStatus } = response.data;

            if (apiStatus === 200 && message === "Payment completed successfully") {
                setStatus('Thanh toán thành công!');
                navigate('/payment-status', { state: { status: 'success' } });
            } else {
                setStatus(`Thanh toán thất bại. Lý do: ${message}`);
                navigate('/payment-status', { state: { status: 'failure' } });
            }
        } catch (error) {
            console.error("Có lỗi xảy ra khi xác minh thanh toán:", error);
            setStatus('Có lỗi xảy ra khi xác minh thanh toán');
            navigate('/payment-status', { state: { status: 'failure' } });
        }
    };

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        const orderId = queryParams.get('orderId');
        const resultCode = queryParams.get('resultCode');

        if (orderId && resultCode) {
            verifyPaymentStatus(orderId, resultCode);
        } else {
            setStatus('Không tìm thấy mã đơn hàng hoặc mã kết quả.');
        }
    }, [location.search]);

    return (
        <div>
            <h2>{status}</h2>
        </div>
    );
};

export default PaymentOnlineStatusMomo;
