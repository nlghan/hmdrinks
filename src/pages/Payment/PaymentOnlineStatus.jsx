import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

const PaymentOnlineStatus = () => {
    const [status, setStatus] = useState('Đang xử lý...');
    const location = useLocation();
    const navigate = useNavigate();
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

    // Function to send app_trans_id to the backend for status verification
    const verifyPaymentStatus = async (appTransId) => {
        const token = getCookie('access_token');
        if (!token) {
            console.error("Không tìm thấy token");
            return;
        }
    
        try {
            // Thực hiện GET request với appTransId dưới dạng query parameter
            const response = await axios.get(`http://localhost:1010/api/payment/zalo/callback?app_trans_id=${appTransId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            });
    
            // Cập nhật trạng thái dựa trên phản hồi từ backend
            if (response.data.status === 1) {
                setStatus('Thanh toán thành công!');
                navigate('/payment-status', { state: { status: 'success' } });
            } else {
                setStatus('Thanh toán thất bại');
                navigate('/payment-status', { state: { status: 'failure' } });
            }
        } catch (error) {
            console.error("Có lỗi xảy ra khi xác minh thanh toán:", error);
            setStatus('Có lỗi xảy ra khi xác minh thanh toán');
            navigate('/payment-status', { state: { status: 'failure' } });
        }
    };
    

    useEffect(() => {
        // Parse the query parameters from the URL to get apptransid
        const queryParams = new URLSearchParams(location.search);
        const appTransId = queryParams.get('apptransid'); // Correct parameter name

        // If apptransid exists, send it to the backend for verification
        if (appTransId) {
            verifyPaymentStatus(appTransId);
        } else {
            setStatus('Không tìm thấy dữ liệu thanh toán.');
        }
    }, [location.search]);

    return (
        <div>
            <h2>{status}</h2>
        </div>
    );
};

export default PaymentOnlineStatus;
