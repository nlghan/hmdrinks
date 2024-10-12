import React, { useState, useEffect } from "react"; 
import Footer from "../../components/Footer/Footer.jsx";
import './ChangePassword.css'; 
import { assets } from '../../assets/assets.js';
import { useNavigate } from "react-router-dom";
import axios from "axios"; 
import Cookies from 'js-cookie'; 
import Navbar from "../../components/Navbar/Navbar.jsx";

const ChangePassword = () => {
    const navigate = useNavigate();    
    
    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [message, setMessage] = useState(""); 
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(""); 
    const [userId, setUserId] = useState(null); // State để lưu userId

    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1]; 
            const decodedPayload = JSON.parse(atob(payload)); 
            return decodedPayload.UserId; // Sửa lại tên trường cho chính xác
        } catch (error) {
            console.error("Không thể giải mã token:", error);
            return null; 
        }
    };

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    useEffect(() => {
        const token = getCookie('access_token');
        if (!token) {
            setError("Bạn cần đăng nhập để xem thông tin này.");
            setLoading(false);
            return;
        }

        const id = getUserIdFromToken(token);
        if (!id) {
            setError("Không thể lấy userId từ token.");
            setLoading(false);
            return;
        }

        setUserId(id); // Lưu userId vào state
        setLoading(false); // Đặt loading là false sau khi lấy userId
    }, []);

    const handleBack = () => {
        navigate('/home'); 
    };

    const handleChangePassword = async () => {
        // Kiểm tra xem mật khẩu mới và xác nhận mật khẩu có khớp hay không
        if (newPassword !== confirmPassword) {
            setMessage("Mật khẩu mới và xác nhận mật khẩu không khớp.");
            return;
        }

        const data = {
            userId, // Sử dụng userId đã lấy
            currentPassword,
            newPassword,
            confirmNewPassword: confirmPassword // Đặt tên trường đúng với backend
        };
    
        try {
            const response = await axios.put(`${import.meta.env.VITE_API_BASE_URL}/user/password/change`, data, {
                headers: {
                    'Content-Type': 'application/json', 
                    'Authorization': `Bearer ${Cookies.get('access_token')}` // Thêm Authorization Bearer token nếu có
                }
            });

            console.log('Thay đổi mật khẩu thành công:', response.data);
            setMessage("Thay đổi mật khẩu thành công!");

            // Điều hướng đến trang login sau một khoảng thời gian
            setTimeout(() => {
                navigate('/info');
            }, 2000); 
        } catch (error) {
            console.error('Lỗi thay đổi mật khẩu:', error);
            // Kiểm tra lỗi cụ thể
            if (error.response) {
                console.log('Dữ liệu phản hồi:', error.response.data);
                console.log('Trạng thái:', error.response.status);
                // Cập nhật thông báo lỗi dựa trên phản hồi từ server
                setMessage(error.response.data.message || "Có lỗi xảy ra, vui lòng thử lại.");
            } else {
                setMessage("Có lỗi xảy ra, vui lòng thử lại.");
            }
        }
    };

    if (loading) {
        return <div>Đang tải...</div>; // Có thể thay đổi để hiển thị loading spinner
    }

    return (
        <>
            <Navbar />
            <div className="change-password-page">
                <div className="change-password-container">
                    <div className="change-password-image">
                        <img src={assets.login} alt='' />
                        <i className="ti-arrow-left" onClick={handleBack}></i>
                    </div>
                    <div className="change-password-form">
                        <h2>Thay đổi mật khẩu</h2>
                        <p className="small-text">Nhập thông tin bên dưới</p>
                        <div className="input-group">
                            <input
                                type="password"
                                placeholder="Mật khẩu hiện tại"
                                className="input"
                                value={currentPassword}
                                onChange={(e) => setCurrentPassword(e.target.value)} />
                            <input
                                type="password"
                                placeholder="Mật khẩu mới"
                                className="input"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)} />
                            <input
                                type="password"
                                placeholder="Xác nhận mật khẩu mới"
                                className="input"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)} />
                        </div>
                        <div className="button-group-change-password">
                            <button className="btn-change-password" onClick={handleChangePassword}>Thay đổi mật khẩu</button>
                        </div>

                        {/* Hiển thị thông báo lỗi nếu có */}
                        {message && <p className="message">{message}</p>}
                        {error && <p className="error">{error}</p>} {/* Hiển thị lỗi nếu có */}
                    </div>
                </div>
                <Footer />
            </div>
        </>
    );
};

export default ChangePassword;
