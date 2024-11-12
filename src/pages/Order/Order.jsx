import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './Order.css'; // Import CSS for styling
import axios from 'axios';
import { useLocation } from 'react-router-dom'; // Import useLocation

const Order = () => {
    const { state } = useLocation();  // Get the state from navigate
    const { orderData } = state || {};
    const [userData, setUserData] = useState({
        fullName: '',
        email: '',
        phone: '',
        address: '',
        street: '',
        ward: '',
        district: '',
        city: '',
        avatar: '',
        birthDay: '',
        sex: '',
    });
    const [provinces, setProvinces] = useState([]);
    const [districts, setDistricts] = useState([]);
    const [wards, setWards] = useState([]);
    const [isEditing, setIsEditing] = useState(false); // New state to track edit mode
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);

    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Cannot decode token:", error);
            return null;
        }
    };

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    // Fetch user data and address info on component mount
    useEffect(() => {
        const fetchUserInfo = async () => {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                setLoading(false);
                return;
            }
            const userId = getUserIdFromToken(token);

            try {
                const userResponse = await axios.get(`http://localhost:1010/api/user/info/${userId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    }
                });

                const userInfo = userResponse.data;

                // Handle address splitting
                let addressParts = (userInfo.address || '').split(',').map(part => part.trim());
                const [street, ward, district, city] = [
                    addressParts[0] || '',
                    addressParts[1] || '',
                    addressParts[2] || '',
                    addressParts[3] || ''
                ];

                setUserData({
                    fullName: userInfo.fullName || '',
                    email: userInfo.email || '',
                    phone: userInfo.phone || '',
                    address: userInfo.address || '',
                    street,
                    ward,
                    district,
                    city,
                    avatar: userInfo.avatar || '',
                    birthDay: userInfo.birth_date || '',
                    sex: userInfo.sex || ''
                });

                setLoading(false);

            } catch (error) {
                console.error("Error fetching user data:", error);
                setError("Không thể lấy thông tin người dùng.");
                setLoading(false);
            }
        };

        fetchUserInfo();
    }, []);

    // Fetch provinces based on user address
    useEffect(() => {
        const fetchProvinces = async () => {
            try {
                const response = await axios.get('http://localhost:1010/api/province/listAll');
                setProvinces(response.data.responseList);
            } catch (error) {
                console.error("Error fetching provinces:", error);
                setError("Không thể lấy danh sách tỉnh.");
            }
        };

        fetchProvinces();
    }, []);

    // Fetch districts based on selected city
    useEffect(() => {
        if (userData.city) {
            const fetchDistricts = async () => {
                try {
                    const province = provinces.find(p => p.provinceName === userData.city);
                    if (province) {
                        const response = await axios.get(`http://localhost:1010/api/province/list-district?provinceId=${province.provinceId}`);
                        setDistricts(response.data.districtResponseList);
                    }
                } catch (error) {
                    console.error("Error fetching districts:", error);
                }
            };

            fetchDistricts();
        }
    }, [userData.city, provinces]);

    // Fetch wards based on selected district
    useEffect(() => {
        if (userData.district) {
            const fetchWards = async () => {
                try {
                    const district = districts.find(d => d.districtName === userData.district);
                    if (district) {
                        const response = await axios.get(`http://localhost:1010/api/province/list-ward?districtId=${district.districtId}`);
                        setWards(response.data.responseList);
                    }
                } catch (error) {
                    console.error("Error fetching wards:", error);
                }
            };

            fetchWards();
        }
    }, [userData.district, districts]);

    // Handle input changes when editing
    const handleEditToggle = () => {
        setIsEditing(!isEditing);
    };

    


    return (
        <>
            <Navbar />
            <div className="order-background-container">
                <div className="order-container">
                    <div className="content">
                        {/* Progress Steps */}
                        <div className="progress-steps">
                            <div className="step active">
                                <i className="fas fa-shopping-basket"></i>
                                <span>1. Xác nhận</span>
                            </div>
                            <div className="progress-line"></div>
                            <div className="step">
                                <i className="fas fa-credit-card"></i>
                                <span>2. Thanh toán</span>
                            </div>
                        </div>

                        {/* Invoice and Customer Information */}
                        <div className="info-order-sections">
                            {/* Invoice Section */}
                            <div className="info-section">
                                <h2>Tổng hóa đơn</h2>
                                <p>Giá sản phẩm: <strong>{orderData.totalPrice}</strong></p>
                                <p>Phí vận chuyển: <strong>{orderData.deliveryFee}</strong></p>
                                <p>Giảm giá: <strong>{orderData.discountPrice}</strong></p>
                                <hr />
                                <p><strong>Tổng cộng: {orderData.totalPrice + orderData.deliveryFee - orderData.discountPrice}</strong></p>
                            </div>

                            {/* Customer Information Form */}
                            <div className="info-section">
                                <h2>
                                    Thông tin khách hàng
                                   
                                </h2>
                                <form>
                                    <label>
                                        Họ và tên:
                                        <input
                                            type="text"
                                            value={userData.fullName}
                                            onChange={(e) => setUserData({ ...userData, fullName: e.target.value })}
                                            disabled={!isEditing}
                                        />
                                    </label>
                                    <label>
                                        Địa chỉ Email:
                                        <input
                                            type="email"
                                            value={userData.email}
                                            onChange={(e) => setUserData({ ...userData, email: e.target.value })}
                                            disabled={!isEditing}
                                            style={{marginBottom:'0px'}}
                                        />
                                    </label>
                                    <label>
                                        Số điện thoại:
                                        <input
                                            type="text"
                                            value={userData.phone}
                                            onChange={(e) => setUserData({ ...userData, phone: e.target.value })}
                                            disabled={!isEditing}
                                        />
                                    </label>

                                    {/* Address Fields */}
                                    <div className="form-group" style={{gap:'0px'}}>
                                        <label style={{marginBottom:'0px'}}>Thành phố:</label>
                                        <select
                                            className="form-control"
                                            value={userData.city}
                                            disabled={!isEditing}
                                            style={{ fontSize: '14px',  padding:'10px' }}
                                        >
                                            <option value="">Chọn tỉnh/thành phố</option>
                                            {provinces.map((province) => (
                                                <option key={province.provinceId} value={province.provinceName}>
                                                    {province.provinceName}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div style={{ display: 'flex', gap: '15px' }}>
                                        <div className="form-group" style={{gap:'0px'}}>
                                            <label style={{marginBottom:'0px'}}>Quận/Huyện:</label>
                                            <select
                                                className="form-control"
                                                value={userData.district}
                                                disabled={!isEditing}
                                                style={{ fontSize: '14px',  padding:'10px' }}
                                            >
                                                <option value="">Chọn quận/huyện</option>
                                                {districts.map((district) => (
                                                    <option key={district.districtId} value={district.districtName}>
                                                        {district.districtName}
                                                    </option>
                                                ))}
                                            </select>
                                        </div>

                                        <div className="form-group" style={{gap:'0px'}}>
                                            <label style={{marginBottom:'0px'}}>Xã/Phường:</label>
                                            <select
                                                className="form-control"
                                                value={userData.ward}
                                                disabled={!isEditing}
                                                style={{ fontSize: '14px', padding:'10px' }}
                                            >
                                                <option value="">Chọn xã/phường</option>
                                                {wards.map((ward) => (
                                                    <option key={ward.wardId} value={ward.wardName}>
                                                        {ward.wardName}
                                                    </option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>

                                    <div className="form-group" style={{gap:'0px'}}>
                                        <label style={{marginBottom:'0px'}}>Đường:</label>
                                        <input
                                            className="form-control"
                                            type="text"
                                            value={userData.street}
                                            disabled={!isEditing}
                                        />
                                    </div>

                                    
                                </form>
                            </div>
                        </div>

                        {/* Action Buttons */}
                        <div className="action-buttons">
                            <button className="back-button">Quay lại giỏ hàng</button>
                            <button className="checkout-button" >Thanh toán</button>
                        </div>
                    </div>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default Order;
