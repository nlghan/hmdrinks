import React, { useState } from "react";
import Footer from "../../components/Footer/Footer.jsx";
import './Register.css';
import { assets } from '../../assets/assets.js';
import { useNavigate } from "react-router-dom";
import axios from "axios";

const Register = () => {
    const navigate = useNavigate();

    const [fullName, setFullName] = useState("");
    const [userName, setUserName] = useState("");
    const [email, setEmail] = useState(""); // State cho email
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const handleLogin = () => {
        navigate('/login');
    };

    const handleRegister = async () => {
        const data = {
            fullName,
            userName,
            password,
            email, // Gửi email trong request
        };

        try {
            const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/v1/auth/register`, data, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            console.log('Đăng ký thành công:', response.data);
            setMessage('Đăng ký thành công');
            setTimeout(() => {
                navigate('/login');
            }, 2000);
        } catch (error) {
            console.error('Lỗi đăng ký:', error);
            if (error.response) {
                const { status, data } = error.response;
                if (status === 409) {
                    if (data.message.includes("email")) {
                        setError("Email đã được sử dụng");
                    } else if (data.message.includes("username")) {
                        setError("Tài khoản đã tồn tại");
                    }
                    setMessage("");
                }
            }
        }
    };

    const handleBack = () => {
        navigate('/home');
    };

    const handleInputChange = (setter) => (event) => {
        setter(event.target.value);
        setError("");
        setMessage("");
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
                    {!message && !error && <p className="small-text">Nhập thông tin cá nhân bên dưới</p>}
                    {message && <p className="message">{message}</p>}
                    {error && <p className="message">{error}</p>}

                    <div className="register-input-group">
                        <input
                            type="text"
                            placeholder="Họ và tên"
                            className="register-input"
                            value={fullName}
                            onChange={handleInputChange(setFullName)}
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
                            className="register-input"
                            value={userName}
                            onChange={handleInputChange(setUserName)}
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
                            type="email"
                            placeholder="Email"
                            className="register-input"
                            value={email} // Giá trị email
                            onChange={handleInputChange(setEmail)} // Cập nhật state email
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
                            className="register-input"
                            value={password}
                            onChange={handleInputChange(setPassword)}
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
