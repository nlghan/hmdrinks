import React, { useState}  from 'react';
import axios from 'axios';
import './FormUpdateUser.css';
import { assets } from "../../assets/assets.js";

const FormUpdateUser = ({ user, onClose, onSave }) => {
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };

    const [formData, setFormData] = React.useState({
        userId: user.userId,
        userName: user.userName,
        fullName: user.fullName,
        birthDate: user.birth_date ? formatDate(user.birth_date) : '', // Định dạng ngày
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
    const [loading, setLoading] = useState(false); // Loading state

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
                setError("Số điện thoại phải có 10 chữ số.");
            } else {
                setError(null);
            }
        }

        // if (name === "address" && value !== initialData.address) {
        //     if (!validateAddress(value)) {
        //         setError("Địa chỉ phải theo định dạng '_,_,_'");
        //     } else {
        //         setError(null);
        //     }
        // }
    };

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const handleSubmit = async (e) => {
        e.preventDefault();        
        if (!validateEmail(formData.email)) {
            setError("Email không hợp lệ.");         
            return;
        }
        if (!validatePhoneNumber(formData.phoneNumber)) {
            setError("Số điện thoại phải có 10 chữ số.");            
            return;
        }
        // if (!validateAddress(formData.address)) {
        //     setError("Địa chỉ phải theo định dạng '_,_,_' ");
            
        //     return;
        // }
        
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
            if (response.status === 409) {
                setError("Email người dùng đã tồn tại. Vui lòng kiểm tra lại!");
                return; // Dừng xử lý tiếp theo nếu gặp lỗi 409
            }

            if (response.status === 200) {
                onSave({ ...response.data, dateUpdated: requestData.dateUpdated });
                setSuccessMessage('Cập nhật người dùng thành công!');
                setTimeout(() => {
                    setSuccessMessage('');
                }, 3000);
                onClose();
            }

        } catch (error) {
            console.error(
                'Có lỗi xảy ra khi cập nhật thông tin người dùng:',
                error.response ? error.response.data : error.message
            );

            if (error.response) {
                if (error.response.status === 409) {
                    // Hiển thị thông báo lỗi xung đột từ server
                    const serverMessage = error.response.data;

                    // Xử lý lỗi trả về từ server
                    if (serverMessage.includes("Email already exists")) {
                        setError("Email người dùng người dùng đã tồn tại. Vui lòng kiểm tra lại!");
                    } else {
                        setError(serverMessage || 'Xung đột dữ liệu. Vui lòng kiểm tra lại!');
                    }
                } else {
                    setError(error.response.data || 'Không thể cập nhật thông tin. Vui lòng kiểm tra lại!');
                }
            } else if (error.request) {
                setError('Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.');
            } else {
                setError('Đã xảy ra lỗi không xác định. Vui lòng thử lại sau!');
            }
        }

    };
    const getRoleText = (role) => {
        const roleMap = {
            'ADMIN': 'Quản trị viên',
            'CUSTOMER': 'Khách hàng',
            'SHIPPER': 'Nhân viên'
        };
        
        return roleMap[role] || role;
    };

    return (
        <div className="form-update-user">
            <div className="form-header-update-user">
                <h2>Cập nhật Người Dùng</h2>
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
                        <span>{getRoleText(formData.role)}</span>
                    </div>
                    {error && <div className="error-message" style={{padding: '0px'}}>{error}</div>}
                    {successMessage && <div className="success-message1">{successMessage}</div>}
                </div>

                <form className="form-update-user-details-table" onSubmit={handleSubmit}>
                    <div className="form-update-detail-row">
                        <label>User ID:</label>
                        <span>{formData.userId}</span>
                    </div>
                    <div className="form-update-detail-row">
                        <label>Tên đăng nhập:</label>
                        <input type="text" name="userName" value={formData.userName} onChange={handleChange} disabled />
                    </div>
                    <div className="form-update-detail-row">
                        <label>Họ và tên:</label>
                        <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} />
                    </div>
                    {/* <div className="form-update-detail-row">
                        <label>Ngày sinh:</label>
                        <input type="date" name="birthDate" value={formData.birthDate} onChange={handleChange} />
                    </div> */}
                    {/* <div className="form-update-detail-row">
                        <label>Địa chỉ:</label>
                        <input type="text" name="address" value={formData.address} onChange={handleChange} />
                    </div> */}
                    <div className="form-update-detail-row">
                        <label>Email:</label>
                        <input type="text" name="email" value={formData.email} onChange={handleChange} />
                    </div>
                    <div className="form-update-detail-row">
                        <label>Điện thoại:</label>
                        <input type="text" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} />
                    </div>
                    {/* <div className="form-update-detail-row">
                        <label>Giới tính:</label>
                        <select name="sex" value={formData.sex} onChange={handleChange}>
                            <option value="MALE">Nam</option>
                            <option value="FEMALE">Nữ</option>
                            <option value="OTHER">Khác</option>
                        </select>
                    </div> */}
                    <div className="form-update-detail-row">
                        <label>Vai trò:</label>
                        <select name="role" value={formData.role} onChange={handleChange}>
                            <option value="CUSTOMER">Khách hàng</option>
                            <option value="SHIPPER">Nhân viên</option>
                        </select>
                    </div>
                </form>



            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-start', marginTop: '13px', alignItems: 'flex-end' }}>
                <button type="button" className="form-update-save-btn"
                    disabled={loading}
                    style={{ backgroundColor: '#00B087', width: '180px', marginRight: '20px', cursor: loading ? 'not-allowed' : 'pointer' }}
                    onMouseOver={(e) => (e.target.style.backgroundColor = '#17d4a8')} // Màu hover
                    onMouseOut={(e) => (e.target.style.backgroundColor = '#00B087')} // Trả về màu cũ
                    onClick={handleSubmit}>Lưu</button>
                <button className="form-update-close-btn"
                    disabled={loading}
                    style={{ backgroundColor: '#c73b48', width: '180px', marginLeft: '20px', cursor: loading ? 'not-allowed' : 'pointer' }} onClick={onClose}
                    onMouseOver={(e) => (e.target.style.backgroundColor = '#f03748')} // Màu hover
                    onMouseOut={(e) => (e.target.style.backgroundColor = '#c73b48')}>Hủy</button>
            </div>

        </div >
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
