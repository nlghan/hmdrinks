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

            // Lưu token vào cookie nếu có trong phản hồi
            if (response.data.access_token) {
                Cookies.set('access_token', response.data.access_token, { expires: 7 }); // Hết hạn sau 7 ngày
            }
            if (response.data.refresh_token) {
                Cookies.set('refresh_token', response.data.refresh_token, { expires: 7 });
            }


            // Điều hướng đến trang home sau một khoảng thời gian
            setTimeout(() => {
                navigate('/login');
            }, 2000); // Thời gian đợi 2 giây trước khi điều hướng
        } catch (error) {
            console.error('Lỗi đăng ký:', error);
            // Kiểm tra lỗi cụ thể
            if (error.response) {
                console.log('Dữ liệu phản hồi:', error.response.data);
                console.log('Trạng thái:', error.response.status);
                console.log('Headers:', error.response.headers);
                // Cập nhật thông báo lỗi
                setMessage("Có lỗi xảy ra, vui lòng thử lại.");
            }
        }
    };

    const handleBack = () => {
        navigate('/home'); // Điều hướng đến trang Register
    };

    return (
        <div className="register-page">
            <div className="register-container">
                <div className="login-image">
                    <img src={assets.login} alt=''/>
                    <i className="ti-arrow-left" onClick={handleBack}></i>

                </div>
                <div className="register-form">
                    <h2>Tạo tài khoản mới</h2>
                    <p className="small-text">Nhập thông tin cá nhân bên dưới</p>
                    <div className="input-group">
                        <input 
                            type="text" 
                            placeholder="Họ và tên" 
                            className="input" 
                            value={fullName}
                            onChange={(e) => setFullName(e.target.value)} 
                        />
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
