import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import Footer from "../../components/Footer/Footer.jsx";
import Navbar from "../../components/Navbar/Navbar.jsx";
import './Info.css'; // Nhập CSS

const Info = () => {
    const navigate = useNavigate(); // Tạo đối tượng điều hướng
    const [formData, setFormData] = useState({
        email: '',
        fullName: '',
        phoneNumber: '',
        avatar: '',
        sex: '',
        birthDay: '',
        address: ''
    });

    const [loading, setLoading] = useState(true); // State để kiểm tra trạng thái tải
    const [error, setError] = useState(""); // State để lưu thông báo lỗi

    // Hàm để giải mã JWT và lấy userId
    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1]; // Lấy phần payload của token
            const decodedPayload = JSON.parse(atob(payload)); // Giải mã base64
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Không thể giải mã token:", error);
            return null; // Trả về null nếu không giải mã được
        }
    };

    // Hàm để lấy giá trị cookie theo tên
    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    // Hàm lấy thông tin người dùng từ API
    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const token = getCookie('access_token');
                if (!token) {
                    setError("Bạn cần đăng nhập để xem thông tin này.");
                    setLoading(false);
                    return;
                }

                const userId = getUserIdFromToken(token);
                if (!userId) {
                    setError("Không thể lấy userId từ token.");
                    setLoading(false);
                    return;
                }
                console.log("Decoded Token1:", userId);
                console.log("token:", token);

                const response = await axios.get(`http://localhost:1010/api/user/info/${userId}`, {
                    headers: {
                        'Accept': '*/*',
                        'Authorization': `Bearer ${token}`
                    }
                });
                console.log(response.data);

                const userInfo = response.data;

                setFormData({
                    email: userInfo.email,
                    fullName: userInfo.fullName,
                    phoneNumber: userInfo.phoneNumber,
                    avatar: userInfo.avatar,
                    sex: userInfo.sex,
                    birthDay: userInfo.birthDay.split('T')[0],
                    address: userInfo.address
                });

                setLoading(false);
            } catch (err) {
                setError("Không thể lấy thông tin người dùng.");
                setLoading(false);
            }
        };

        fetchUserInfo();
    }, []);

    const handleBack = () => {
        navigate('/home');
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log(formData);
    };

    if (loading) {
        return <p>Đang tải thông tin người dùng...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <>
            <Navbar />
            <div className="body-info">
            <div className="container">
                <form onSubmit={handleSubmit} className="user-info-form">
                    <h2>Thông tin cá nhân</h2>

                    {/* Avatar */}
                    <div className="avatar-container">
                        {formData.avatar ? (
                            <img src={formData.avatar} alt="Avatar" className="avatar-image" />
                        ) : (
                            <p>Chưa có ảnh đại diện</p>
                        )}
                    </div>

                    <div className="form-group">
                        <label>Email:</label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        />
                    </div>

                    <div className="form-group">
                        <label>Họ và tên:</label>
                        <input
                            type="text"
                            name="fullName"
                            value={formData.fullName}
                            onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                        />
                    </div>

                    <div className="form-group">
                        <label>Số điện thoại:</label>
                        <input
                            type="text"
                            name="phoneNumber"
                            value={formData.phoneNumber}
                            onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                        />
                    </div>

                    {/* <div className="form-group">
                        <label>Avatar URL:</label>
                        <input
                            type="text"
                            name="avatar"
                            value={formData.avatar}
                            onChange={(e) => setFormData({ ...formData, avatar: e.target.value })}
                        />
                    </div> */}

                    <div className="form-group">
                        <label>Giới tính:</label>
                        <select
                            name="sex"
                            value={formData.sex}
                            onChange={(e) => setFormData({ ...formData, sex: e.target.value })}
                        >
                            <option value="">Chọn giới tính</option>
                            <option value="Male">Nam</option>
                            <option value="Female">Nữ</option>
                            <option value="Other">Khác</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label>Ngày sinh:</label>
                        <input
                            type="date"
                            name="birthDay"
                            value={formData.birthDay}
                            onChange={(e) => setFormData({ ...formData, birthDay: e.target.value })}
                        />
                    </div>

                    <div className="form-group">
                        <label>Địa chỉ:</label>
                        <input
                            type="text"
                            name="address"
                            value={formData.address}
                            onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                        />
                    </div>

                    <div className="button-group">
                        <button type="submit" className="btn">Cập nhật thông tin</button>
                        <button type="button" className="btn btn-back" onClick={handleBack}>Trở lại</button>
                    </div>
                </form>

            </div>

            </div>
            
            <Footer />
        </>
    );
};

export default Info;
