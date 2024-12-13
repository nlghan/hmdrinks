import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
const PaymentOnlineStatusVnpay = () => {
    const [status, setStatus] = useState('Đang xử lý...');
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        const vnp_ResponseCode = queryParams.get('vnp_ResponseCode');
        const statusParam = queryParams.get('status'); // Assuming 'status' exists as per your request

        if (vnp_ResponseCode) {
            // Check vnp_ResponseCode to determine payment status
            if (vnp_ResponseCode === "00") {
                setStatus('Thanh toán thành công!');
                navigate('/payment-status', { state: { status: 'success' } });
            } else if (vnp_ResponseCode === "01") {
                setStatus('Giao dịch chưa hoàn tất.');
                navigate('/payment-status', { state: { status: 'failure' } });
            } else if (vnp_ResponseCode === "02") {
                setStatus('Giao dịch bị lỗi.');
                navigate('/payment-status', { state: { status: 'failure' } });
            } else {
                setStatus(`Trạng thái thanh toán không xác định: ${statusParam || 'Không rõ'}`);
                navigate('/payment-status', { state: { status: 'failure' } });
            }
        } else {
            setStatus('Không tìm thấy mã kết quả.');
        }
    }, [location.search, navigate]);

    return (
        <div>
            <h2>{status}</h2>
        </div>
    );
};

export default PaymentOnlineStatusVnpay;
