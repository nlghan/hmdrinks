import React from "react";
import Footer from "../components/Footer/Footer"; 
import './Login.css'; 
import { assets } from '../assets/assets.js';

const Login = () => {
    return (
        <div className="login-page">
            <div className="login-container">
                <div className="login-image">
                    <img src={assets.login} alt=''/>
                </div>
                <div className="login-form">
                    <h2>Đăng Nhập</h2>
                    <p className="small-text">Nhập thông tin cá nhân bên dưới</p>
                    <div className="input-group">
                        <input type="text" placeholder="Tên tài khoản" className="input" />
                        <input type="password" placeholder="Mật khẩu" className="input" />
                    </div>
                    <div className="button-group">
                        <button className="btn">Đăng Nhập</button>
                        <span className="forgot-pass-link">Quên mật khẩu</span>
                    </div>
                    <button className="btn btn-google">
                    <img src={assets.gg} alt='' className="google-icon"/>Đăng Nhập bằng Google</button>
                    <p className="register-text">Bạn chưa có tài khoản? <span className="register-link">Đăng ký</span></p>
                </div>
            </div>
            <Footer />
        </div>
    );
};

export default Login;
