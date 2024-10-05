import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import Footer from "../../components/Footer/Footer.jsx";
import Navbar from "../../components/Navbar/Navbar.jsx";
import './Info.css'; // Import CSS
import '../../assets/assets.js';
import { assets } from "../../assets/assets.js";
import LoadingAnimation from "../../components/Animation/LoadingAnimation.jsx";
import ErrorMessage from "../../components/Animation/ErrorMessage.jsx";

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
    const [isUploading, setIsUploading] = useState(false); // New state for upload loading

    // Function to decode JWT token and get userId
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

    // Function to retrieve token from cookies
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

                // Fetch user info from backend
                const response = await axios.get(`http://localhost:1010/api/user/info/${userId}`, {
                    headers: {
                        'Accept': '*/*',
                        'Authorization': `Bearer ${token}`
                    }
                });

                const userInfo = response.data;

                // Initialize formData with user data from server
                setFormData({
                    email: userInfo.email || '',
                    fullName: userInfo.fullName || '',
                    phoneNumber: userInfo.phoneNumber || '',
                    avatar: userInfo.avatar || '',
                    sex: userInfo.sex || '',
                    birthDay: userInfo.birthDay ? userInfo.birthDay.split('T')[0] : '',
                    address: userInfo.address || ''
                });

                setPreviewImage(userInfo.avatar || '');
                setLoading(false);
            } catch (err) {
                setError("Không thể lấy thông tin người dùng.");
                setLoading(false);
            }
        };

        fetchUserInfo();
    }, []);

    // Handle image file changes
    const handleFileChange = (e) => {
        const file = e.target.files[0];
        setSelectedFile(file);
        setPreviewImage(URL.createObjectURL(file)); 
    };

    // Handle back button click
    const handleBack = () => {
        navigate('/home');
    };

    // Handle password change button click
    const handleChangePass = () => {
        navigate('/change');
    };

    // Handle form submission to update user info
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (!token || !userId) {
            setError("Bạn cần phải đăng nhập.");
            return;
        }

        // Prepare data for updating user information
        const updatedData = {
            userId: userId,
            email: formData.email,
            fullName: formData.fullName,
            phoneNumber: formData.phoneNumber,
            avatar: formData.avatar, 
            sex: formData.sex,
            birthDay: formData.birthDay,
            address: formData.address
        };

        try {
            const response = await axios.put(`http://localhost:1010/api/user/info/${userId}`, updatedData, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            // If update is successful, show success message or navigate
            alert("Cập nhật thông tin thành công!");
        } catch (error) {
            console.error("Lỗi khi cập nhật thông tin người dùng:", error);
            setError("Không thể cập nhật thông tin.");
        }
    };

    if (loading) {
        return <LoadingAnimation animationPath="https://lottie.host/0c6e3916-8606-485d-a8e4-dcc5f06e896c/q9CCaIYNpb.json" />;
    }

    if (error) {
        return <ErrorMessage path={"https://lottie.host/66736d57-35f4-486f-9925-3195e8e1c67e/Zi6FIi6tGt.json"} message={error} />;
    }

    return (
        <>
            <Navbar />
            <div className="body-info">
                <div className="container">
                    <form className="user-info-form" onSubmit={handleSubmit}>
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

            {isUploading && (
                <div className="overlay">
                    <LoadingAnimation animationPath="https://lottie.host/bfcb91ed-f6e3-486d-b052-6c0705a6416c/yzsyGYxWhd.json" isVisible={isUploading}/>
                </div>
            )}

            <Footer />
        </>
    );
};

export default Info;
