import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../../context/AuthProvider';  // Import useAuth để sử dụng login
import Cookies from 'js-cookie';

const IntermediaryPage = () => {
    const location = useLocation();
    const { login } = useAuth();  // Sử dụng login từ AuthContext

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);

        // Google OAuth parameters
        const code = queryParams.get('code'); // Authorization code
        const state = queryParams.get('state'); // State parameter to prevent CSRF attacks

        // Payment system parameters
        const orderCode = queryParams.get('orderCode');
        const status = queryParams.get('status');
        const orderId = queryParams.get('orderId');
        const resultCode = queryParams.get('resultCode');
        const vnp_ResponseCode = queryParams.get('vnp_ResponseCode');
        const vnp_TxnRef = queryParams.get('vnp_TxnRef');
        const token = sessionStorage.getItem('access_token'); // Token from session storage

        const handleGoogleLogin = async (authCode) => {
            try {
                // Gọi API backend để xác thực mã code với Google OAuth
                const response = await axios.get(`http://localhost:1010/api/v1/auth/oauth2/callback`, {
                    params: {
                        code: authCode, // Gửi mã xác thực nhận được từ Google
                    },
                    headers: {
                        Accept: 'application/json', // Đảm bảo API trả về dữ liệu JSON
                    },
                });
        
                // Kiểm tra nếu API trả về thành công và chứa access_token và refresh_token
                const accessToken = response.data.access_token;
                const refreshToken = response.data.refresh_token;
        
                if (accessToken && refreshToken) {
                    // Lưu access_token và refresh_token vào Cookies (hoặc localStorage nếu cần)
                    Cookies.set('access_token', accessToken, { expires: 7 });
                    Cookies.set('refresh_token', refreshToken, { expires: 7 });
        
                    // Chuyển hướng đến trang localhost và truyền token qua query parameters
                    window.location.href = `http://localhost:5173/login-gg?access_token=${accessToken}&refresh_token=${refreshToken}`;
                } else {
                    // Nếu không có token, dùng token mặc định
                    const defaultToken = 'default_token';
                    Cookies.set('access_token', defaultToken, { expires: 7 });
        
                    const role = getRoleFromToken(defaultToken);
                    // redirectToPage(role); // Chuyển hướng theo role mặc định
                }
        
            } catch (error) {
                console.error('Error during Google OAuth API call:', error);
                console.log("Lỗi trong quá trình đăng nhập với Google.");
        
                // Nếu gặp lỗi, vẫn có thể chuyển hướng về trang localhost với token mặc định
                const defaultToken = 'default_token'; // Token mặc định khi có lỗi
                Cookies.set('access_token', defaultToken, { expires: 7 });
        
                const role = getRoleFromToken(defaultToken);
                redirectToPage(role); // Chuyển hướng theo role mặc định
            }
        };
        
       
        
         
        if (code && state) {
            // Gọi xử lý Google OAuth nếu có mã xác thực
            handleGoogleLogin(code);
        }
        // Handle PayOS
        else if (orderCode) {
            const redirectUrl = `http://localhost:5173/payment-online-status-payos?orderCode=${orderCode}&status=${status}&token=${token}`;
            window.location.href = redirectUrl;
        }
        // Handle MoMo
        else if (orderId && resultCode !== null) {
            const redirectUrl = `http://localhost:5173/payment-online-status-momo?orderId=${orderId}&resultCode=${resultCode}&token=${token}`;
            window.location.href = redirectUrl;
        }
        // Handle VNPay
        else if (vnp_ResponseCode) {
            let paymentStatus;
            if (vnp_ResponseCode === "00") {
                paymentStatus = "success";
            } else if (vnp_ResponseCode === "01") {
                paymentStatus = "pending";
            } else if (vnp_ResponseCode === "02") {
                paymentStatus = "error";
            } else {
                paymentStatus = "unknown";
            }
            const redirectUrl = `http://localhost:5173/payment-online-status-vnpay?vnp_ResponseCode=${vnp_ResponseCode}&status=${paymentStatus}&token=${token}`;
            window.location.href = redirectUrl;
        }
        // Handle error if no valid parameters
        else {
            console.error('No valid parameters found in the URL.');
        }
    }, [location, login]); // Đảm bảo login có thể được sử dụng trong useEffect

    return <div>Redirecting...</div>;
};

export default IntermediaryPage;
