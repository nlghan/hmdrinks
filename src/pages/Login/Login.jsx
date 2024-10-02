import React, { useState } from "react"; // Nhập useState
import Footer from "../../components/Footer/Footer.jsx"; 
import './Login.css'; 
import { assets } from '../../assets/assets.js';
import { useNavigate } from "react-router-dom";
import axios from "axios"; // Nhập axios
import Cookies from 'js-cookie'; // Nhập thư viện js-cookie

const Login = ({ setIsLoggedIn }) => { // Nhận props setIsLoggedIn
    const navigate = useNavigate();
    
    // State để lưu thông tin đăng nhập
    const [userName, setUserName] = useState("");
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState(""); // State để lưu thông báo

    const handleLogin = async () => {
        const data = {
            userName,
            password
        };

        try {
            const response = await axios.post('http://localhost:1010/api/v1/auth/authenticate', data, {
                headers: {
                    'Content-Type': 'application/json' // Đảm bảo rằng content type là JSON
                }
            });

            console.log('Đăng nhập thành công:', response.data);

            // Lưu token vào cookie nếu có trong phản hồi
            if (response.data.access_token) {
                Cookies.set('access_token', response.data.access_token, { expires: 7 }); // Hết hạn sau 7 ngày
            }
            if (response.data.refresh_token) {
                Cookies.set('refresh_token', response.data.refresh_token, { expires: 7 });
            }

            sessionStorage.setItem("isLoggedIn", "true"); // Sử dụng sessionStorage
            setIsLoggedIn(true); // Cập nhật trạng thái đăng nhập trong component cha
            navigate('/home'); // Điều hướng đến trang home sau khi đăng nhập thành công
        } catch (error) {
            console.error('Lỗi đăng nhập:', error);
            if (error.response) {
                setMessage("Có lỗi xảy ra, vui lòng kiểm tra lại thông tin đăng nhập.");
            }
        }
    };

    const handleRegister = () => {
        navigate('/register'); // Điều hướng đến trang Register
    };

    const handleBack = () => {
        navigate('/home'); // Điều hướng đến trang Register
    };

    return (
        <div className="login-page">
            <div className="login-container">
                <div className="login-image">
                    <img src={assets.login} alt=''/>
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
                            onChange={(e) => setUserName(e.target.value)} // Cập nhật giá trị
                        />
                        <input 
                            type="password" 
                            placeholder="Mật khẩu" 
                            className="input" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)} // Cập nhật giá trị
                        />
                    </div>
                    <div className="button-group-login">
                        <button className="btn-login" onClick={handleLogin}>Đăng Nhập</button>
                        <span className="forgot-pass-link">Quên mật khẩu</span>
                    </div>
                    <button className="btn-google">
                        <img src={assets.gg} alt='' className="google-icon"/>Đăng Nhập bằng Google
                    </button>
                    <p className="register-text">Bạn chưa có tài khoản? <span className="register-link" onClick={handleRegister}>Đăng ký</span></p>

                    {/* Hiển thị thông báo lỗi nếu có */}
                    {message && <p className="message">{message}</p>}
                </div>
            </div>
            <Footer />
        </div>
    );
};

export default Login;