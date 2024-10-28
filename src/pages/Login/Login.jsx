import React, { useState, useEffect } from "react";
import Footer from "../../components/Footer/Footer.jsx";
import './Login.css';
import { assets } from '../../assets/assets.js';
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Cookies from 'js-cookie';
import { useAuth } from "../../context/AuthProvider.jsx"; // Import useAuth từ AuthProvider

const Login = () => {
    const navigate = useNavigate();
    const { login, isLoggedIn } = useAuth();

    // State to store login information
    const [userName, setUserName] = useState("");
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    // State to store usernames for autocomplete
    const [savedUsernames, setSavedUsernames] = useState([]);

    // Fetch saved usernames from localStorage when component mounts
    useEffect(() => {
        const usernames = JSON.parse(localStorage.getItem('usernames')) || [];
        setSavedUsernames(usernames);
    }, []);

    const getRoleFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.Roles;
        } catch (error) {
            console.error("Không thể giải mã token:", error);
            return null;
        }
    };

    const handleLogin = async () => {
        const data = { userName, password };

        try {
            const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/v1/auth/authenticate`, data, {
                headers: { 'Content-Type': 'application/json' }
            });

            console.log('Đăng nhập thành công:', response.data);

            // Save tokens to cookies
            if (response.data.access_token) {
                Cookies.set('access_token', response.data.access_token, { expires: 7 });
            }
            if (response.data.refresh_token) {
                Cookies.set('refresh_token', response.data.refresh_token, { expires: 7 });
            }

            const token = Cookies.get('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const role = getRoleFromToken(token);
            if (!role) {
                setError("Không thể lấy role từ token.");
                return;
            }

            // Save the username to localStorage
            const usernames = JSON.parse(localStorage.getItem('usernames')) || [];
            if (!usernames.includes(userName)) {
                usernames.push(userName);
                localStorage.setItem('usernames', JSON.stringify(usernames));
            }

            // Redirect based on role
            if (role.includes("ADMIN")) {
                navigate('/dashboard');
            } else if (role.includes("CUSTOMER")) {
                login();
                navigate('/home');
            }

        } catch (error) {
            if (error.response) {
                const { status, data } = error.response;
                if (status === 404) {
                    setMessage("Không tìm thấy tên đăng nhập.");
                    setError("Tên đăng nhập không tồn tại."); // Set error state here
                } else if (status === 401 && data === "Invalid username or password") {
                    setMessage("Sai tên đăng nhập hoặc mật khẩu. Vui lòng thử lại.");
                    setError("Sai tên đăng nhập hoặc mật khẩu."); // Set error state here
                } else if (status === 500) {
                    setMessage("Đã xảy ra lỗi máy chủ. Vui lòng thử lại sau.");
                    setError("Lỗi máy chủ."); // Set error state here
                }
            } else {
                setMessage("Không thể kết nối với máy chủ. Vui lòng thử lại sau.");
                setError("Lỗi kết nối."); // Set error state here
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
            handleLogin();
        }
    };

    const handleForget = () => {
        navigate('/send-mail');
    };

    const handleInputChange = (setter) => (event) => {
        setter(event.target.value);
        setError(""); // Clear error message on input change
        setMessage(""); // Clear message on input change
    };

    return (
        <div className="login-page">
            <div className="login-container">
                <div className="login-image">
                    <img src={assets.login} alt='' />
                    <i className="ti-arrow-left" onClick={handleBack}></i>
                </div>
                <div className="login-form">
                    <h2 id="h2-login">Đăng Nhập</h2>
                    {(!message && !error) && <p className="small-text">Nhập thông tin cá nhân bên dưới</p>}

                    {error && <p className="message">{error}</p>}
                    <div className="input-group">
                        <input
                            list="usernames"
                            placeholder="Tên tài khoản"
                            className={`input ${error ? 'input-error' : ''}`} // Conditional class for error
                            value={userName}
                            onChange={handleInputChange(setUserName)}
                        />
                        <datalist id="usernames">
                            {savedUsernames.map((username, index) => (
                                <option key={index} value={username} />
                            ))}
                        </datalist>

                        <input
                            type="password"
                            placeholder="Mật khẩu"
                            className={`input ${error ? 'input-error' : ''}`} // Conditional class for error
                            value={password}
                            onChange={handleInputChange(setPassword)}
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
                </div>
            </div>
            <Footer />
        </div>
    );
};

export default Login;
