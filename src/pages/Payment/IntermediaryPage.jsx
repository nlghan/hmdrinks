import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

const IntermediaryPage = () => {
    const location = useLocation();

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);

        // Extract parameters for PayOS
        const orderCode = queryParams.get('orderCode');
        const status = queryParams.get('status');

        // Extract parameters for MoMo
        const orderId = queryParams.get('orderId');
        const resultCode = queryParams.get('resultCode');

        // Extract parameters for VNPay
        const vnp_ResponseCode = queryParams.get('vnp_ResponseCode'); // Response code
        const vnp_TxnRef = queryParams.get('vnp_TxnRef'); // Transaction reference
        const token = sessionStorage.getItem('access_token'); // Token from session storage

        if (orderCode) {
            // Redirect to PayOS payment status page
            const redirectUrl = `http://localhost:5173/payment-online-status-payos?orderCode=${orderCode}&status=${status}&token=${token}`;
            window.location.href = redirectUrl;
        } else if (orderId && resultCode !== null) {
            // Redirect to MoMo payment status page
            const redirectUrl = `http://localhost:5173/payment-online-status-momo?orderId=${orderId}&resultCode=${resultCode}&token=${token}`;
            window.location.href = redirectUrl;
        } else if (vnp_ResponseCode) {
            // Determine VNPay payment status based on response code
            let status;
            if (vnp_ResponseCode === "00") {
                status = "success"; // Payment succeeded
            } else if (vnp_ResponseCode === "01") {
                status = "pending"; // Transaction not completed
            } else if (vnp_ResponseCode === "02") {
                status = "error"; // Transaction error
            } else {
                status = "unknown"; // Undefined status
            }

            // Redirect to VNPay payment status page
            const redirectUrl = `http://localhost:5173/payment-online-status-vnpay?vnp_ResponseCode=${vnp_ResponseCode}&status=${status}&token=${token}`;
            window.location.href = redirectUrl;
        } else {
            // Handle error if no parameters are available
            console.error('No order information found.');
        }
    }, [location]);

    return <div>Redirecting...</div>;
};

export default IntermediaryPage;
