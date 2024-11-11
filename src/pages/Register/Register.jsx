import React, { useState } from "react";
import Footer from "../../components/Footer/Footer.jsx";
import './Register.css';
import { assets } from '../../assets/assets.js';
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Cookies from 'js-cookie'; // Nhập thư viện js-cookie

const Register = () => {
    const navigate = useNavigate();

    // State để lưu thông tin đăng ký
    const [fullName, setFullName] = useState("");
    const [userName, setUserName] = useState("");
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState(""); // Added error state

    const handleLogin = () => {
        navigate('/login');
    };

    const handleRegister = async () => {
        const data = {
            fullName,
            userName,
            password
        };

        try {
            const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/v1/auth/register`, data, {
                headers: {
                    'Content-Type': 'application/json' // Đảm bảo rằng content type là JSON
                }
            });

            console.log('Đăng ký thành công:', response.data);
            setMessage('Đăng ký thành công')

            // // Lưu token vào cookie nếu có trong phản hồi
            // if (response.data.access_token) {
            //     Cookies.set('access_token', response.data.access_token, { expires: 7 }); // Hết hạn sau 7 ngày
            // }
            // if (response.data.refresh_token) {
            //     Cookies.set('refresh_token', response.data.refresh_token, { expires: 7 });
            // }

            // Điều hướng đến trang home sau một khoảng thời gian
            setTimeout(() => {
                navigate('/login');
            }, 2000); // Thời gian đợi 2 giây trước khi điều hướng
        } catch (error) {
            console.error('Lỗi đăng ký:', error);
            // Kiểm tra lỗi cụ thể
            if (error.response) {
                const { status, data } = error.response;
                if (status === 409) {
                    console.log(data)
                    setError("Tài khoản đã tồn tại"); // Set error message
                    setMessage(""); // Clear any previous messages
                } 
            }
        }
    };

    const handleBack = () => {
        navigate('/home'); // Điều hướng đến trang Register
    };

    // Handler for input change
    const handleInputChange = (setter) => (event) => {
        setter(event.target.value);
        setError(""); // Clear error message on input change
        setMessage(""); // Optionally clear message if you want
    };

    return (
        <div className="register-page">
            <div className="register-container">
                <div className="login-image">
                    <img src={assets.login} alt='' />
                    <i className="ti-arrow-left" onClick={handleBack}></i>
                </div>
                <div className="register-form">
                    <h2>Tạo tài khoản mới</h2>
                    {/* Conditional rendering for prompt message */}
                    {!message && !error && <p className="small-text">Nhập thông tin cá nhân bên dưới</p>}

                    {/* Display error if it exists */}
                    {message && <p className="message">{message}</p>}
                    {error && <p className="message">{error}</p>}

                    <div className="register-input-group">
                        <input
                            type="text"
                            placeholder="Họ và tên"
                            className="register-input"
                            value={fullName}
                            onChange={handleInputChange(setFullName)} // Use the new handler
                            style={{
                                width: '80%',
                                padding: '10px 0',
                                border: 'none',
                                borderBottom: '1px solid #666',
                                outline: 'none',
                                margin: '5px 0',
                                fontSize: '16px'
                            }}
                        />
                        <input
                            type="text"
                            placeholder="Tên tài khoản"
                            className={`input ${error ? 'input-error' : ''}`}
                            value={userName}
                            onChange={handleInputChange(setUserName)} // Use the new handler
                            style={{
                                width: '80%',
                                padding: '10px 0',
                                border: 'none',
                                borderBottom: '1px solid #666',
                                outline: 'none',
                                margin: '5px 0',
                                fontSize: '16px'
                            }}
                        />
                        <input
                            type="password"
                            placeholder="Mật khẩu"
                            className="input"
                            value={password}
                            onChange={handleInputChange(setPassword)} // Use the new handler
                        />
                    </div>
                    <div className="button-group-register">
                        <button className="btn-register" onClick={handleRegister}>Tạo tài khoản</button>
                    </div>
                    <button className="btn-google">
                        <img src={assets.gg} alt='' className="google-icon" />Đăng Nhập bằng Google
                    </button>
                    <p className="login-text">Bạn đã có tài khoản? <span className="login-link" onClick={handleLogin}>Đăng nhập</span></p>
                </div>
            </div>
            <Footer />
        </div>
    );
};

export default Register;
