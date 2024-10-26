import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import Footer from "../../components/Footer/Footer.jsx";
import Navbar from "../../components/Navbar/Navbar.jsx";
import './Info.css'; // Nhập CSS
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
    const [isUploading, setIsUploading] = useState(false);
    const [formErrors, setFormErrors] = useState({
        email: '',
        phoneNumber: '',
        birthDay: ''
    });
    const [isEditing, setIsEditing] = useState(false); // New state for editing mode

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

                const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/user/info/${userId}`, {
                    headers: {
                        'Accept': '*/*',
                        'Authorization': `Bearer ${token}`
                    }
                });

                const userInfo = response.data;

                const formatBirthDayForInput = (isoDate) => {
                    const date = new Date(isoDate);
                    const year = date.getFullYear();
                    const month = String(date.getMonth() + 1).padStart(2, '0');
                    const day = String(date.getDate()).padStart(2, '0');
                    return `${year}-${month}-${day}`;
                };

                setFormData({
                    email: userInfo.email || '',
                    fullName: userInfo.fullName || '',
                    phoneNumber: userInfo.phone || '',
                    avatar: userInfo.avatar || '',
                    sex: userInfo.sex || '',
                    birthDay: userInfo.birth_date ? formatBirthDayForInput(userInfo.birth_date) : '',
                    address: userInfo.address || ''
                });

                setPreviewImage(userInfo.avatar || '');
                setLoading(false);
            } catch (err) {
                console.error("Lỗi khi lấy thông tin người dùng:", err);
                setError("Không thể lấy thông tin người dùng.");
                setLoading(false);
            }
        };

        fetchUserInfo();
    }, []);

    const validateForm = () => {
        const errors = {
            email: '',
            phoneNumber: '',
            birthDay: ''
        };

        const phoneRegex = /^[0-9]{10}$/; // Phone number must be exactly 10 digits

        if (!formData.email.includes('@')) {
            errors.email = 'Email không hợp lệ!';
        }

        if (!phoneRegex.test(formData.phoneNumber)) {
            errors.phoneNumber = 'Số điện thoại phải có đúng 10 số và không chứa kí tự đặc biệt!';
        }

        const today = new Date();
        const birthDate = new Date(formData.birthDay);
        if (birthDate > today) {
            errors.birthDay = 'Ngày sinh không hợp lệ!';
        }

        setFormErrors(errors);

        // Return true if there are no errors
        return Object.values(errors).every(error => error === '');
    };

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

            setIsUploading(true);

            try {
                const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/image/user/upload?userId=${userId}`, formData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'multipart/form-data'
                    }
                });

                const imageUrl = response.data.url;
                setPreviewImage(imageUrl);

                alert("Đã cập nhật ảnh đại diện!");
            } catch (error) {
                console.error("Lỗi cập nhật:", error);
                setError("Không thể cập nhật ảnh đại diện.");
            } finally {
                setIsUploading(false);
            }
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (!token || !userId) {
            setError("Bạn cần phải đăng nhập.");
            return;
        }

        // Validate form data before submitting
        if (!validateForm()) {
            return; // Stop submission if validation fails
        }

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
            const response = await axios.put(`${import.meta.env.VITE_API_BASE_URL}/user/info-update`, updatedData, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            alert("Cập nhật thông tin thành công!");
            setIsEditing(!isEditing);
        } catch (error) {
            console.error("Lỗi khi cập nhật thông tin người dùng:", error);
            setError("Không thể cập nhật thông tin.");
        }
    };

    if (loading) {
        return (
            <LoadingAnimation
                animationPath="https://lottie.host/0c6e3916-8606-485d-a8e4-dcc5f06e896c/q9CCaIYNpb.json"
                isVisible={loading}
            />
        );
    }

    if (error) {
        return (
            <ErrorMessage
                path={"https://lottie.host/66736d57-35f4-486f-9925-3195e8e1c67e/Zi6FIi6tGt.json"}
                message={error}
            />
        );
    }

    return (
        <>
            <Navbar currentPage={'Thông tin cá nhân'} />

            <div className="body-info">
                <div className="container">
                    <h2>
                        Thông tin cá nhân 
                        <i
                            className="ti-pencil"
                            style={{ fontSize: '20px', color: 'green', cursor: 'pointer' , marginLeft:'5px'}}
                            onClick={() => setIsEditing(!isEditing)}
                        />
                    </h2>

                    <form className="user-info-form">
                        <div className="avatar-container">
                            <div className="avatar-image-wrapper">
                                {previewImage ? (
                                    <>
                                        <img src={previewImage} alt="Avatar" className="avatar-image" />
                                        <div className="image-overlay"></div> {/* Overlay for the blur effect */}
                                        <div className="btn-gr-img slideUp">
                                            <button
                                                type="button"
                                                id="btn-upload"
                                                onClick={() => document.getElementById('file-upload').click()}
                                            >
                                                < i className="ti-cloud-up"/>
                                            </button>
                                            <button
                                                type="button"
                                                id="btn-save"
                                                onClick={handleSubmitImg}
                                            >
                                                < i className="ti-save"/>
                                            </button>
                                        </div>
                                    </>
                                ) : (
                                    <img src={assets.avtrang} alt="" className="avatar-image" />
                                )}
                            </div>


                            <input
                                type="file"
                                accept="image/*"
                                id="file-upload"
                                onChange={handleFileChange}
                                style={{ display: 'none' }}
                            />
                        </div>


                        <div className="form-grid">
                            <div className="form-column">
                                <div className="form-group">
                                    <label>Email:</label>
                                    <input
                                        className="form-control"
                                        type="email"
                                        value={formData.email}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                    />
                                    {formErrors.email && <span className="error">{formErrors.email}</span>}
                                </div>
                                <div className="form-group">
                                    <label>Họ tên:</label>
                                    <input
                                        className="form-control"
                                        type="text"
                                        value={formData.fullName}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Số điện thoại:</label>
                                    <input
                                        className="form-control"
                                        type="text"
                                        value={formData.phoneNumber}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                                    />
                                    {formErrors.phoneNumber && <span className="error">{formErrors.phoneNumber}</span>}
                                </div>
                            </div>
                            <div className="form-column">
                                <div className="form-group">
                                    <label>Giới tính:</label>
                                    <select
                                        className="form-control"
                                        value={formData.sex}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, sex: e.target.value })}
                                    >
                                        <option value="" disabled>Select your option</option>
                                        <option value="MALE">Nam</option>
                                        <option value="FEMALE">Nữ</option>
                                        <option value="OTHER">Khác</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Ngày sinh:</label>
                                    <input
                                        className="form-control"
                                        type="date"
                                        value={formData.birthDay}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, birthDay: e.target.value })}
                                    />
                                    {formErrors.birthDay && <span className="error">{formErrors.birthDay}</span>}
                                </div>
                                <div className="form-group">
                                    <label>Địa chỉ:</label>
                                    <input
                                        className="form-control"
                                        type="text"
                                        value={formData.address}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="button-group">
                            <button type="submit" className="btn" onClick={handleSubmit}>Cập nhật</button>
                            <button type="button" className="btn" id="btn-change" onClick={handleChangePass}>Đổi mật khẩu</button>
                            <button type="button" className="btn" id="btn-back-info" onClick={handleBack}>Trở lại</button>
                        </div>
                    </form>
                </div>
            </div>

            <Footer />
        </>
    );
};

export default Info;
