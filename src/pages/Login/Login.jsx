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
    const [showPassword, setShowPassword] = useState(false);
    const [passwordError, setPasswordError] = useState("");

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

    const handleLoginGG = async () => {   
        try {
          // Gửi yêu cầu GET để lấy URL OAuth2 cho Google login
          const response = await axios.get('http://localhost:1010/api/v1/auth/social-login/google', {
            headers: { 'accept': '*/*' }
          });
      
          // Kiểm tra nếu API trả về URL cho Google login
          if (response.data) {
            // Điều hướng đến Google login (redirect URL)
            window.location.href = response.data;
          } else {
            console.error('Không nhận được URL đăng nhập từ API');
          }
        } catch (error) {
          console.error('Lỗi khi gửi yêu cầu Google login:', error);
        }
      };

    const handleLogin = async () => {
        const data = { userName, password };
        const maxAttempts = 5; // Số lần nhập sai tối đa
        const lockoutTime = 5 * 60 * 1000; // Thời gian khóa tính bằng ms (5 phút)

        // Lấy thông tin lần nhập sai từ localStorage
        const loginAttempts = JSON.parse(localStorage.getItem('loginAttempts')) || {};
        const userAttempts = loginAttempts[userName] || { count: 0, lockedUntil: null };

        // Kiểm tra nếu người dùng đang bị khóa
        if (userAttempts.lockedUntil && new Date() < new Date(userAttempts.lockedUntil)) {
            setMessage("Bạn đã nhập sai mật khẩu quá số lần quy định, vui lòng thử lại sau 5 phút.");
            setError("Tài khoản bị khóa tạm thời.");
            return;
        }

        try {
            const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/v1/auth/authenticate`, data, {
                headers: { 'Content-Type': 'application/json' }
            });

            console.log('Đăng nhập thành công:', response.data);

            // Xóa thông tin số lần sai nếu đăng nhập thành công
            delete loginAttempts[userName];
            localStorage.setItem('loginAttempts', JSON.stringify(loginAttempts));
            // Save tokens to cookies
            if (response.data.access_token) {
                Cookies.set('access_token', response.data.access_token, { expires: 7 });
                sessionStorage.setItem('access_token', response.data.access_token);
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
            } else if (role.includes("SHIPPER")) {
                login();
                navigate('/shipper-home');
            }

        } catch (error) {
            if (error.response) {
                const { status, data } = error.response;
                if (status === 404) {
                    setMessage("Không tìm thấy tên đăng nhập.");
                    setError("Tên đăng nhập không tồn tại.");
                } else if (status === 401 && data === "Invalid username or password") {
                    // Xử lý số lần nhập sai
                    userAttempts.count += 1;
                    if (userAttempts.count >= maxAttempts) {
                        userAttempts.lockedUntil = new Date(new Date().getTime() + lockoutTime); // Khóa trong 5 phút
                        setError("Bạn đã nhập sai mật khẩu quá số lần quy định, vui lòng thử lại sau 5 phút.");
                    } else {
                        setError(`Sai tên đăng nhập hoặc mật khẩu. Bạn còn ${maxAttempts - userAttempts.count} lần thử.`);
                    }
                    loginAttempts[userName] = userAttempts;
                    localStorage.setItem('loginAttempts', JSON.stringify(loginAttempts));
                } else if (status === 500) {
                    setMessage("Đã xảy ra lỗi máy chủ. Vui lòng thử lại sau.");
                    setError("Lỗi máy chủ.");
                }
            } else {
                setMessage("Không thể kết nối với máy chủ. Vui lòng thử lại sau.");
                setError("Lỗi kết nối.");
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
    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword); // Đảo ngược giá trị của showPassword
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

                    {error && <p className="error-message-login">{error}</p>}
                    <div className="input-group">
                        <input
                            list="usernames"
                            placeholder="Tên tài khoản"
                            className={`input ${error ? 'input-error' : ''}`} // Conditional class for error
                            value={userName}
                            onChange={handleInputChange(setUserName)}
                            onKeyPress={handleKeyPress}
                        />
                        <datalist id="usernames">
                            {savedUsernames.map((username, index) => (
                                <option key={index} value={username} />
                            ))}
                        </datalist>
                        <div className="password-input-container-login">
                            <input
                                type={showPassword ? 'text' : 'password'} // Kiểu của input thay đổi tùy vào trạng thái showPassword
                                placeholder="Mật khẩu"
                                className={`input ${passwordError ? 'input-error' : ''}`}
                                value={password}
                                onChange={handleInputChange(setPassword)}
                                onKeyPress={handleKeyPress}
                                style={{
                                    width: '100%',
                                    padding: '10px 0',
                                    border: 'none',
                                    borderBottom: '1px solid #666',
                                    outline: 'none',
                                    margin: '5px 0',
                                    fontSize: '16px'
                                }}
                            />
                            <i
                                className={`ti-eye ${showPassword ? 'show' : 'hide'}`}
                                onClick={togglePasswordVisibility}
                            >
                
                            </i>
                        </div>
                    </div>
                    <div className="button-group-login">
                        <button className="btn-login" onClick={handleLogin}>Đăng Nhập</button>
                        <span className="forgot-pass-link" onClick={handleForget}>Quên mật khẩu</span>
                    </div>
                    <button className="btn-google" onClick={handleLoginGG}>
                        <img src={assets.gg} alt='' className="google-icon"  />Đăng Nhập bằng Google
                    </button>
                    <p className="register-text">Bạn chưa có tài khoản? <span className="register-link" onClick={handleRegister}>Đăng ký</span></p>
                </div>
            </div>
            <Footer />
        </div>
    );
};

export default Login;
