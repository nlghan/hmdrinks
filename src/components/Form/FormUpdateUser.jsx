import React from 'react';
import axios from 'axios';
import './FormUpdateUser.css';
import { assets } from "../../assets/assets.js";

const FormUpdateUser = ({ user, onClose, onSave }) => {
    const [formData, setFormData] = React.useState({
        userId: user.userId,
        userName: user.userName,
        fullName: user.fullName,
        birthDate: user.birthDate,
        address: user.address,
        email: user.email,
        phoneNumber: user.phoneNumber,
        sex: user.sex,
        role: user.role,
        password: '',
        avatar: user.avatar,
        dateUpdated: user.dateUpdated 
    });

    const [error, setError] = React.useState(null);
    const [initialData] = React.useState(formData); 
    const [successMessage, setSuccessMessage] = React.useState(''); 

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prevState) => ({ ...prevState, [name]: value }));

        // Kiểm tra khi có sự thay đổi
        if (name === "email" && value !== initialData.email) {
            if (!validateEmail(value)) {
                setError("Email không hợp lệ.");
            } else {
                setError(null);
            }
        }

        if (name === "phoneNumber" && value !== initialData.phoneNumber) {
            if (!validatePhoneNumber(value)) {
                setError("Số điện thoại phải có 10 hoặc 11 chữ số.");
            } else {
                setError(null);
            }
        }

        if (name === "address" && value !== initialData.address) {
            if (!validateAddress(value)) {
                setError("Địa chỉ phải theo định dạng '_,_,_' (ví dụ: 'đường ABC, quận 1, TP.HCM').");
            } else {
                setError(null);
            }
        }
    };

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`); 
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const token = getCookie('access_token');
            console.log('Token:', token);

            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const requestData = {
                userId: formData.userId,
                fullName: formData.fullName,
                userName: formData.userName,
                email: formData.email,
                phoneNumber: formData.phoneNumber,
                role: formData.role,
                password: formData.password || undefined,
                dateUpdated: new Date().toISOString() 
            };

            console.log('Submitting form data:', requestData);

            const response = await axios.put('http://localhost:1010/api/admin/update-account', requestData, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.status === 200) {
                onSave({ ...response.data, dateUpdated: requestData.dateUpdated });
                setSuccessMessage('Cập nhật người dùng thành công!'); 
                setTimeout(() => {
                    setSuccessMessage(''); 
                }, 5000);
                onClose();
            }
        } catch (error) {
            console.error('Có lỗi xảy ra khi cập nhật thông tin người dùng:', error.response ? error.response.data : error.message);
            setError('Không thể cập nhật thông tin. Vui lòng kiểm tra lại!');
        }
    };

    return (
        <div className="form-update-user">
            <div className="form-header-update-user">
                <h2>Cập nhật Người Dùng</h2>
                <button className="form-update-close-btn" onClick={onClose}>Đóng</button>
            </div>
            <div className="form-content-update-user">
                <div className="form-update-avatar-section">
                    <div className="form-update-user-avatar">
                        {user.avatar ? (
                            <img src={user.avatar} alt="Avatar" className="user-avatar1" />
                        ) : (
                            <img src={assets.avtrang} alt="" className="user-avatar1" />
                        )}
                    </div>
                    <div className="form-update-user-role">
                        <span>{formData.role}</span>
                    </div>
                </div>

                <form className="form-update-user-details-table" onSubmit={handleSubmit}>
                    <div className="form-update-detail-row">
                        <label>User ID:</label>
                        <span>{formData.userId}</span>
                    </div>
                    <div className="form-update-detail-row">
                        <label>Tên đăng nhập:</label>
                        <input type="text" name="userName" value={formData.userName} onChange={handleChange} />
                    </div>
                    <div className="form-update-detail-row">
                        <label>Họ và tên:</label>
                        <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} />
                    </div>
                    <div className="form-update-detail-row">
                        <label>Ngày sinh:</label>
                        <input type="date" name="birthDate" value={formData.birthDate} onChange={handleChange} />
                    </div>
                    <div className="form-update-detail-row">
                        <label>Địa chỉ:</label>
                        <input type="text" name="address" value={formData.address} onChange={handleChange} />
                    </div>
                    <div className="form-update-detail-row">
                        <label>Email:</label>
                        <input type="text" name="email" value={formData.email} onChange={handleChange} />
                    </div>
                    <div className="form-update-detail-row">
                        <label>Điện thoại:</label>
                        <input type="text" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} />
                    </div>
                    <div className="form-update-detail-row">
                        <label>Giới tính:</label>
                        <select name="sex" value={formData.sex} onChange={handleChange}>
                            <option value="Male">Nam</option>
                            <option value="Female">Nữ</option>
                            <option value="Other">Khác</option>
                        </select>
                    </div>
                    <div className="form-update-detail-row">
                        <label>Vai trò:</label>
                        <input type="text" name="role" value={formData.role} onChange={handleChange} />
                    </div>
                </form>
            </div>
            <button type="button" className="form-update-save-btn" onClick={handleSubmit}>Lưu</button>
            {error && <div className="error-message">{error}</div>}
            {successMessage && <div className="success-message1">{successMessage}</div>} 
        </div>
    );
};

const validateEmail = (email) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
};

const validatePhoneNumber = (phone) => {
    const regex = /^\d{10,11}$/;
    return regex.test(phone);
};

const validateAddress = (address) => {
    const regex = /^[^,]+,[^,]+,[^,]+$/; 
    return regex.test(address);
};

export default FormUpdateUser;
