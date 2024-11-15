import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

const IntermediaryPage = () => {
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        const orderCode = queryParams.get('orderCode');
        const status = queryParams.get('status');
        const token = sessionStorage.getItem('access_token');

        if (orderCode) {
            // Chuyển hướng đến localhost với các tham số cần thiết
            const redirectUrl = `http://localhost:5173/payment-online-status-payos?orderCode=${orderCode}&status=${status}&token=${token}`;
            window.location.href = redirectUrl;  // Dùng window.location.href để điều hướng đến URL đầy đủ
        } else {
            // Nếu không có orderCode, có thể thông báo lỗi hoặc xử lý gì đó
            console.error('Không có mã đơn hàng');
        }
    }, [location, navigate]);

    return <div>Đang chuyển hướng...</div>;
};

export default IntermediaryPage;
