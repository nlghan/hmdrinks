import React, { useState } from "react";
import Footer from "../../components/Footer/Footer.jsx"; 
import './Login.css'; 
import { assets } from '../../assets/assets.js';
import { useNavigate } from "react-router-dom";
import axios from "axios"; 
import Cookies from 'js-cookie'; 
import { useAuth } from "../../context/AuthProvider.jsx"; // Import useAuth từ AuthProvider

const Login = () => { 
    const navigate = useNavigate();
    const { login, isLoggedIn } = useAuth(); // Lấy hàm login và trạng thái isLoggedIn từ AuthProvider
    
    // State to store login information
    const [userName, setUserName] = useState("");
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState(""); 
    const [error, setError] = useState(""); 

   

    const getRoleFromToken = (token) => {
        try {
            // Split the token and get the payload part
            const payload = token.split('.')[1]; 
            
            // Decode the base64url encoded payload
            const decodedPayload = JSON.parse(atob(payload)); 
            console.log(decodedPayload.Roles);
            
            // Return the Roles value from the decoded payload
            return decodedPayload.Roles; // Ensure to use the exact key from the payload
        } catch (error) {
            console.error("Không thể giải mã token:", error);
            return null; 
        }
    };

    const handleLogin = async () => {
        const data = {
            userName,
            password
        };
    
        try {
            const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/v1/auth/authenticate`, data, {
                headers: {
                    'Content-Type': 'application/json' 
                }
            });
    
            console.log('Đăng nhập thành công:', response.data);
    
            // Save tokens to cookies if present in the response
            if (response.data.access_token) {
                Cookies.set('access_token', response.data.access_token, { expires: 7 }); 
            }
            if (response.data.refresh_token) {
                Cookies.set('refresh_token', response.data.refresh_token, { expires: 7 });
            }
    
            // Retrieve the token from cookies
            const token = Cookies.get('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }
    
            const role = getRoleFromToken(token);
            console.log("role:"+ role);
            if (!role) {
                setError("Không thể lấy role từ token.");
                return;
            }
    
            if (role.includes("ADMIN")) {
                navigate('/dashboard'); // Navigate to dashboard if ADMIN
            } else if (role.includes("CUSTOMER")){
                login();
                navigate('/home'); // Navigate to home for CUSTOMER or other roles
            }
        } catch (error) {
            console.error('Lỗi đăng nhập:', error);
            if (error.response) {
                setMessage("Có lỗi xảy ra, vui lòng kiểm tra lại thông tin đăng nhập.");
            } else {
                setMessage("Không thể kết nối với máy chủ. Vui lòng thử lại sau.");
            }
        }
    };
    

    const handleRegister = () => {
        navigate('/register'); 
    };

    const handleBack = () => {
        navigate('/home'); 
    };

    const handleKeyPress = (event) => {
        if (event.key === "Enter") {
            handleLogin(); // Call login function on Enter key press
        }
    };

    const handleForget = () => {
        navigate('/send-mail');
    };

    return (
        <div className="login-page">
            <div className="login-container">
                <div className="login-image">
                    <img src={assets.login} alt='' />

                    {/* Back Button */}
                    <i className="ti-arrow-left" onClick={handleBack}></i>
                </div>
                <div className="login-form">
                    <h2>Đăng Nhập</h2>
                    <p className="small-text">Nhập thông tin cá nhân bên dưới</p>
                    <div className="input-group">
                        <input 
                            type="text" 
                            placeholder="Tên tài khoản" 
                            className="input" 
                            value={userName} 
                            onChange={(e) => setUserName(e.target.value)} 
                        />
                        <input 
                            type="password" 
                            placeholder="Mật khẩu" 
                            className="input" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)} 
                            onKeyPress={handleKeyPress} 
                        />
                    </div>
                    <div className="button-group-login">
                        <button className="btn-login" onClick={handleLogin}>Đăng Nhập</button>
                        <span className="forgot-pass-link" onClick={handleForget}>Quên mật khẩu</span>
                    </div>
                    <button className="btn-google">
                        <img src={assets.gg} alt='' className="google-icon" />Đăng Nhập bằng Google
                    </button>
                    <p className="register-text">Bạn chưa có tài khoản? <span className="register-link" onClick={handleRegister}>Đăng ký</span></p>

                    {/* Display error message if any */}
                    {message && <p className="message">{message}</p>}
                    {error && <p className="error-message">{error}</p>} {/* Display error state */}
                </div>
            </div>
            <Footer />
        </div>
    );
};

export default Login;
