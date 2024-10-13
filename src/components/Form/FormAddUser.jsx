import React, { useState } from 'react';
import axios from 'axios';
import './FormAddUser.css';

// Function to retrieve token from cookies
const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
};

const FormAddUser = ({ onClose, onSubmit }) => {
    const [formData, setFormData] = useState({
        fullName: '',
        userName: '',
        email: '',
        password: '',
        role: 'CUSTOMER',
        phone: ''
    });

    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState(''); 

    
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

   
    const isValidEmail = (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    };

    // Handle form submission with validations
    const handleSubmit = async () => {
        const { fullName, userName, email, password, phone } = formData;

        const token = getCookie('access_token');
        if (!token) {
            setErrorMessage("Bạn cần đăng nhập để thêm người dùng.");
            return;
        }

        // Check for empty fields
        if (!fullName || !userName || !email || !password) {
            setErrorMessage("Vui lòng không để trống bất kỳ trường thông tin nào.");
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            setErrorMessage("Email không hợp lệ. Vui lòng nhập đúng định dạng email.");
            return;
        }

        // // Validate phone number (must be 10 digits)
        // const phoneRegex = /^[0-9]{10}$/;
        // if (!phoneRegex.test(phone)) {
        //     setErrorMessage("Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại 10 chữ số.");
        //     return;
        // }

        // Validate password (minimum 8 characters)
        if (password.length < 8) {
            setErrorMessage("Mật khẩu phải chứa ít nhất 8 ký tự.");
            return;
        }

        try {
            const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/admin/create-account`, formData, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.data) {
                setSuccessMessage("Thêm người dùng thành công!"); 
                setErrorMessage(""); 
                console.log('User created successfully:', response.data);
                onSubmit(formData); // Pass form data back to parent component
                setTimeout(() => {
                    setSuccessMessage(''); // Clear success message after 2 seconds
                    onClose(); // Close form after success
                }, 1000);
            }
        } catch (error) {
            console.error('Error creating user:', error);
            if (error.response) {
                setErrorMessage(error.response.data.message || 'Người dùng này đã tồn tại.');
            } else if (error.request) {
                setErrorMessage('Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.');
            } else {
                setErrorMessage('Đã xảy ra sự cố. Vui lòng thử lại sau.');
            }
        }
    };

    return (
        <div className="form-add-user-container">
            <div className="form-add-user">
                <h2>Thêm người dùng</h2>
                {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}
                {successMessage && <p style={{ color: 'green' }}>{successMessage}</p>} 
                <form onSubmit={(e) => e.preventDefault()}> 
                    <div className="form-group">
                        <label htmlFor="fullName">Họ và tên</label>
                        <input
                            type="text"
                            id="fullName"
                            name="fullName"
                            value={formData.fullName}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="userName">Tên đăng nhập</label>
                        <input
                            type="text"
                            id="userName"
                            name="userName"
                            value={formData.userName}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="password">Mật khẩu</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="role">Vai trò</label>
                        <select
                            id="role"
                            name="role"
                            value={formData.role}
                            onChange={handleInputChange}
                        >
                            <option value="CUSTOMER">Customer</option>
                            <option value="SHIPPER">Shipper</option>
                            <option value="ADMIN">Admin</option>
                        </select>
                    </div>
                   {/*<div className="form-group">
                        <label htmlFor="phone">Số điện thoại</label>
                        <input
                            type="text"
                            id="phone"
                            name="phone"
                            value={formData.phone}
                            onChange={handleInputChange}
                            required
                        />
                    </div>*/}
                    <div className="user-form-actions">
                        <button type="button" className="submit-btn" onClick={handleSubmit}>Thêm</button>
                        <button type="button" className="cancel-btn" onClick={onClose}>Hủy</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default FormAddUser;
