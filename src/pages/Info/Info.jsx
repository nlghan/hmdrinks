import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import Footer from "../../components/Footer/Footer.jsx";
import Navbar from "../../components/Navbar/Navbar.jsx";
import './Info.css'; // Nhập CSS
import '../../assets/assets.js'
import { assets } from "../../assets/assets.js";

const Info = () => {
    const navigate = useNavigate(); 
    const [formData, setFormData] = useState({
        email: '',
        fullName: '',
        phoneNumber: '',
        avatar: '',
        sex: '',
        birthDay: '',
        address: ''
    });

    const [selectedFile, setSelectedFile] = useState(null); 
    const [previewImage, setPreviewImage] = useState(''); 
    const [loading, setLoading] = useState(true); 
    const [error, setError] = useState(""); 

    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1]; 
            const decodedPayload = JSON.parse(atob(payload)); 
            return decodedPayload.UserId;
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

                const response = await axios.get(`http://localhost:1010/api/user/info/${userId}`, {
                    headers: {
                        'Accept': '*/*',
                        'Authorization': `Bearer ${token}`
                    }
                });

                const userInfo = response.data;

                // Khởi tạo formData với giá trị mặc định nếu không có giá trị từ server
                setFormData({
                    email: userInfo.email || '',  // Nếu không có email, khởi tạo thành chuỗi rỗng
                    fullName: userInfo.fullName || '',
                    phoneNumber: userInfo.phoneNumber || '',
                    avatar: userInfo.avatar || '',
                    sex: userInfo.sex || '',
                    birthDay: userInfo.birthDay ? userInfo.birthDay.split('T')[0] : '',  // Nếu không có ngày sinh, khởi tạo thành chuỗi rỗng
                    address: userInfo.address || ''
                });

                setPreviewImage(userInfo.avatar || ''); // Cập nhật previewImage với avatar hoặc chuỗi rỗng
                setLoading(false);
            } catch (err) {
                setError("Không thể lấy thông tin người dùng.");
                setLoading(false);
            }
        };

        fetchUserInfo();
    }, []);

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        setSelectedFile(file);
        setPreviewImage(URL.createObjectURL(file)); 
    };

    const handleBack = () => {
        navigate('/home');
    };

    const handleChangePass = () => {
        navigate('/change');
    };

    const handleSubmitImg = async (e) => {
        e.preventDefault();

        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (!token || !userId) {
            setError("Bạn cần phải đăng nhập.");
            return;
        }

        if (selectedFile) {
            const formData = new FormData();
            formData.append("file", selectedFile);

            try {
                // Gửi yêu cầu POST để tải ảnh lên
                const response = await axios.post(`http://localhost:1010/api/image/user/upload?userId=${userId}`, formData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'multipart/form-data'
                    }
                });

                // Nếu thành công, cập nhật ảnh đại diện
                const imageUrl = response.data.url;
                setPreviewImage(imageUrl);

                alert("Đã cập nhật ảnh đại diện!");

            } catch (error) {
                console.error("Lỗi cập nhật:", error);
                setError("Không thể cập nhật ảnh đại diện.");
            }
        }
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
                    <form className="user-info-form">
                        <h2>Thông tin cá nhân</h2>

                        <div className="avatar-container">
                            {previewImage ? (
                                <img src={previewImage} alt="Avatar" className="avatar-image" />
                            ) : (
                                <img src={assets.avtrang} alt="" className="avatar-image" />
                            )}
                        </div>

                        <div className="up-img">
                            <input
                                type="file"
                                accept="image/*"
                                id="file-upload"
                                onChange={handleFileChange}
                                style={{ display: 'none' }}  
                            />
                            <div className="btn-gr-img">
                            <button
                                type="button"
                                className="btn-upload"
                                onClick={() => document.getElementById('file-upload').click()}
                            >
                                Tải ảnh lên
                            </button>

                            <button
                                type="button"
                                className="btn-save"
                                onClick={handleSubmitImg}
                            >
                                Lưu
                            </button>

                            </div>
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
                            <button type="button" className="btn btn-change" onClick={handleChangePass}>Đổi mật khẩu</button>
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
