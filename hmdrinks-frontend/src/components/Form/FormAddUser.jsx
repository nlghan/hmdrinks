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
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [showPassword, setShowPassword] = useState(false);


    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };
    const validatePassword = (password) => {
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
        return passwordRegex.test(password);
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

        if (!validatePassword(password)) {
            setErrorMessage('Mật khẩu không hợp lệ, vui lòng nhập mật khẩu gồm 8 chữ số trở lên, gồm ký tự đặc biệt, chữ thường, chữ hoa và số!');
            return;
        }

        setIsSubmitting(true);

        try {
            const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/admin/create-account`, formData, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.data) {
                // Nếu response trả về status code là 409
                if (response.data.statusCodeValue === 409) {
                    setErrorMessage("Người dùng đã tồn tại!");
                    return;
                }
                // Nếu thêm người dùng thành công
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
        const errorMessage2 = error.response.data;

        // Kiểm tra kiểu dữ liệu của `errorMessage2`
        console.log('Error message from server:', errorMessage2);

        if (typeof errorMessage2 === 'string') {
            if (errorMessage2.includes("User name already exists")) {
                setErrorMessage("Email hoặc tên người dùng đã tồn tại. Vui lòng thử email khác.");
            } else if (errorMessage2.includes("Role is wrong")) {
                setErrorMessage("Vai trò không hợp lệ. Vui lòng kiểm tra lại.");
            } else {
                setErrorMessage(errorMessage2 || 'Đã xảy ra lỗi khi tạo người dùng.');
            }
        } else {
            // Nếu `errorMessage2` không phải là chuỗi
            setErrorMessage('Đã xảy ra lỗi không xác định từ máy chủ.');
        }
    } else if (error.request) {
        setErrorMessage('Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.');
    } else {
        setErrorMessage('Đã xảy ra sự cố. Vui lòng thử lại sau.');
    }
        } finally {
            // Kết thúc xử lý
            setIsSubmitting(false);
        }


    };
    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword); // Đảo ngược giá trị của showPassword
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
                        <div style={{
                            width: '100%',
                            position: 'relative',

                        }}>
                            <input
                                type={showPassword ? 'text' : 'password'}
                                id="password"
                                name="password"
                                value={formData.password}
                                onChange={handleInputChange}
                                required
                            />
                            <i
                                className={`ti-eye ${showPassword ? 'show' : 'hide'}`}
                                onClick={togglePasswordVisibility}
                            ></i>
                        </div>
                    </div>
                    <div className="form-group">
                        <label htmlFor="role">Vai trò</label>
                        <select
                            id="role"
                            name="role"
                            value={formData.role}
                            onChange={handleInputChange}
                        >
                            <option value="CUSTOMER">Khách hàng</option>
                            <option value="SHIPPER">Nhân viên</option>
                            <option value="ADMIN">Quản trị viên</option>
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
                        <button type="button" id="submit-btn"
                            style={{
                                cursor: isSubmitting ? 'not-allowed' : 'pointer',
                                opacity: isSubmitting ? 0.4 : 1,
                            }} onClick={handleSubmit} disabled={isSubmitting} >Thêm</button>
                        <button type="button" id="cancel-btn" style={{
                            cursor: isSubmitting ? 'not-allowed' : 'pointer',
                            opacity: isSubmitting ? 0.4 : 1,
                        }} onClick={onClose} disabled={isSubmitting}>Hủy</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default FormAddUser;
