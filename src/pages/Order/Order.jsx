import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './Order.css';
import axios from 'axios';
import { redirect, useLocation, useNavigate } from 'react-router-dom';
import bidv from '../../assets/img/bidv.jpg'
import momo from '../../assets/img/momo.png'
import zalo from '../../assets/img/zalo.png'
import vn from '../../assets/img/vnpay.png'
import payos from '../../assets/img/payos.png'
import { useCart } from '../../context/CartContext';
const Order = () => {
    const { state } = useLocation();
    const { orderData } = state || {};
    const { ensureCartExists } = useCart();
    console.log('Dữ liệu nhận được từ state:', state);

    const [currentStep, setCurrentStep] = useState("confirmation");
    const [selectedPaymentMethod, setSelectedPaymentMethod] = useState("");
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
    const [isEditing, setIsEditing] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);
    const [orderDetails, setOrderDetails] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [showSuccess, setShowSuccess] = useState(false);
    const [showError, setShowError] = useState(false);
    const [showErroApi, setShowErroApi] = useState(false);
    const [showErroConfirm, setShowErroConfirm] = useState(false);
    const navigate = useNavigate();

    // Get UserId from token
    const getUserIdFromToken = (token) => {
        try {
            // Tách payload từ token
            const payload = token.split('.')[1];
            // Giải mã payload từ base64
            const decodedPayload = JSON.parse(atob(payload));

            // Ép kiểu UserId thành int (số nguyên)
            return parseInt(decodedPayload.UserId, 10); // 10 là hệ cơ số thập phân
        } catch (error) {
            console.error("Cannot decode token:", error);
            return null;
        }
    };


    // Get Cookie by name
    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    // Format Currency
    const formatCurrency = (value) => {
        return new Intl.NumberFormat('vi-VN', {
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
        }).format(value) + ' VND';
    };

    // Handle Payment Method Selection
    const handlePaymentMethodChange = (e) => {
        setSelectedPaymentMethod(e.target.value);
    };

    // Handle Proceed to Payment
    const handleProceedToPayment = async () => {
        if (currentStep === "confirmation") {
            const token = getCookie('access_token');
            const userId = getUserIdFromToken(token)
            try {
                setLoading(true); // Bắt đầu quá trình xác nhận đơn hàng
                setIsLoading(true);
                // Gọi API xác nhận đơn hàng
                const response = await fetch('http://localhost:1010/api/orders/confirm', {
                    method: 'POST',
                    headers: {
                        'accept': '*/*',
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        userId: userId,
                        orderId: orderData.orderId,
                    }),
                });

                // Kiểm tra nếu API trả về thành công
                if (response.ok) {
                    const data = await response.json();
                    console.log('Đơn hàng đã được xác nhận:', data);

                    // Nếu xác nhận thành công, chuyển sang bước thanh toán
                    setCurrentStep("payment");
                } else {
                    // Nếu có lỗi, hiển thị thông báo lỗi
                    const errorData = await response.json();
                    console.error('Xác nhận đơn hàng thất bại:', errorData);
                    setShowErroConfirm(true);
                    setTimeout(() => {
                        setShowErroConfirm(false);
                    }, 2000);
                }
            } catch (error) {
                console.error('Có lỗi khi gọi API:', error);
                setShowErroApi(true);
                setTimeout(() => {
                    setShowErroApi(false);
                }, 2000);
            } finally {
                setLoading(false); // Kết thúc quá trình tải dữ liệu
                setIsLoading(false);
            }

        } else if (currentStep === "payment") {
            setIsLoading(true); // Bắt đầu quá trình tải dữ liệu cho thanh toán

            // Thực hiện thanh toán dựa trên phương thức đã chọn
            try {
                if (selectedPaymentMethod === 'cashOnDelivery') {
                    await handleCash();
                } else if (selectedPaymentMethod === 'creditCard') {
                    await handleCreditCard();
                } else if (selectedPaymentMethod === 'momo') {
                    await handleMomo();
                } else if (selectedPaymentMethod === 'zaloPay') {
                    await handleZaloPay();
                } else if (selectedPaymentMethod === 'vnPay') {
                    await handleVnPay();
                } else {
                    setError('Vui lòng chọn phương thức thanh toán.');
                }
            } catch (error) {
                console.error('Có lỗi khi thực hiện thanh toán:', error);
                setError('Có lỗi xảy ra trong quá trình thanh toán.');
            } finally {
                setIsLoading(false); // Kết thúc quá trình tải dữ liệu
            }
        }
    };

    // Handle Cash on Delivery Payment
    const handleCash = async () => {
        console.log("Thanh toán khi nhận hàng");

        // Lấy thông tin từ localStorage hoặc cookie (ví dụ: userId, orderId)
        const token = getCookie('access_token');
        if (!token) {
            console.error("Không tìm thấy token");
            return;
        }

        const userId = getUserIdFromToken(token);  // Hàm đã được định nghĩa ở trên
        const orderId = orderData.orderId;  // Giả sử orderId đã có từ phía trước (có thể lấy từ state hoặc props)

        try {
            // Gửi yêu cầu POST đến API tạo thanh toán
            const response = await axios.post(
                'http://localhost:1010/api/payment/create/cash',
                {
                    orderId: orderId,
                    userId: userId
                },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            // Kiểm tra phản hồi API
            if (response.data.statusPayment === 'PENDING' || response.data.statusPayment === 'COMPLETED') {
                console.log("Thanh toán đã được tạo, trạng thái: PENDING");

                // Điều hướng đến trang PaymentStatus và truyền trạng thái "success"
                navigate('/payment-status', { state: { status: 'success' } });
            }
        } catch (error) {
            console.error("Có lỗi xảy ra khi tạo thanh toán:", error);
            navigate('/payment-status', { state: { status: 'failure' } });
        }
    };

    // Handle Credit Card Payment
    const handleCreditCard = async () => {
        console.log("Thanh toán bằng thẻ ngân hàng");
        // Bắt đầu quá trình tải dữ liệu

        const token = getCookie('access_token');
        if (!token) {
            console.error("Không tìm thấy token");
            setIsLoading(false); // Kết thúc quá trình tải dữ liệu
            return;
        }

        const userId = getUserIdFromToken(token);  // Assuming this function is defined
        const orderId = orderData.orderId;         // Assuming orderData contains the orderId

        try {
            // Send a POST request to the payment creation API
            const response = await axios.post(
                'http://localhost:1010/api/payment/create/credit/payOs',
                {
                    orderId: orderId,
                    userId: userId
                },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            // Check the API response
            if (response.data.statusPayment === 'PENDING') {
                console.log("Thanh toán đã được tạo, trạng thái: PENDING");
                const link = response.data.linkPayment;

                // Redirect to the link received from the response
                window.location.href = link;
            } else if (response.data.statusPayment === 'COMPLETED') {
                console.log("Thanh toán thành công CHO VOUCHER LỚN.");
                navigate('/payment-status', { state: { status: 'success' } });
            } else {
                console.error("Thanh toán thất bại hoặc không xác định.");
                navigate('/payment-status', { state: { status: 'failure' } });
            }
            
        } catch (error) {
            console.error("Có lỗi xảy ra khi tạo thanh toán:", error);
            navigate('/payment-status', { state: { status: 'failure' } });
        } 
    };

    // Handle MOMO Payment
    const handleMomo = async () => {
        console.log("Thanh toán qua MOMO");
        const token = getCookie('access_token');
        if (!token) {
            console.error("Không tìm thấy token");
            return;
        }

        const userId = getUserIdFromToken(token);  // Assuming this function is defined
        const orderId = orderData.orderId;         // Assuming orderData contains the orderId

        try {
            // Send a POST request to the payment creation API
            const response = await axios.post(
                'http://localhost:1010/api/payment/create/credit/momo',
                {
                    orderId: orderId,
                    userId: userId
                },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            // Check the API response
            if (response.data.statusPayment === 'PENDING') {
                console.log("Thanh toán đã được tạo, trạng thái: PENDING");
                const link = response.data.linkPayment;

                // Redirect to the link received from the response
                window.location.href = link;
            } else if (response.data.statusPayment === 'COMPLETED') {
                console.log("Thanh toán thành công CHO VOUCHER LỚN.");
                navigate('/payment-status', { state: { status: 'success' } });
            } else {
                console.error("Thanh toán thất bại hoặc không xác định.");
                navigate('/payment-status', { state: { status: 'failure' } });
            }
        } catch (error) {
            console.error("Có lỗi xảy ra khi tạo thanh toán:", error);
            navigate('/payment-status', { state: { status: 'failure' } });
        }
    };

    // Handle ZaloPay Payment
    const handleZaloPay = async () => {
        console.log("Thanh toán qua ZaloPay");
        const token = getCookie('access_token');
        if (!token) {
            console.error("Không tìm thấy token");
            return;
        }

        const userId = getUserIdFromToken(token);  // Assuming this function is defined
        const orderId = orderData.orderId;         // Assuming orderData contains the orderId

        try {
            // Send a POST request to the payment creation API
            const response = await axios.post(
                'http://localhost:1010/api/payment/create/credit/zaloPay',
                {
                    orderId: orderId,
                    userId: userId
                },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            // Check the API response
            if (response.data.statusPayment === 'PENDING') {
                console.log("Thanh toán đã được tạo, trạng thái: PENDING");
                const link = response.data.linkPayment;

                // Redirect to the link received from the response
                window.location.href = link;
            } else if (response.data.statusPayment === 'COMPLETED') {
                console.log("Thanh toán thành công CHO VOUCHER LỚN.");
                navigate('/payment-status', { state: { status: 'success' } });
            } else {
                console.error("Thanh toán thất bại hoặc không xác định.");
                navigate('/payment-status', { state: { status: 'failure' } });
            }
        } catch (error) {
            console.error("Có lỗi xảy ra khi tạo thanh toán:", error);
            navigate('/payment-status', { state: { status: 'failure' } });
        }
    };


    // Handle VNPay Payment
    const handleVnPay = async () => {
        console.log("Thanh toán qua VNPay");
        // Add logic to handle VNPay payment
        const token = getCookie('access_token');
        if (!token) {
            console.error("Không tìm thấy token");
            return;
        }

        const userId = getUserIdFromToken(token);  // Assuming this function is defined
        const orderId = orderData.orderId;         // Assuming orderData contains the orderId

        try {
            // Send a POST request to the payment creation API
            const response = await axios.post(
                'http://localhost:1010/api/payment/create/credit/vnPay',
                {
                    orderId: orderId,
                    userId: userId,
                    ipAddress: "string"
                },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            // Check the API response
            if (response.data.statusPayment === 'PENDING') {
                console.log("Thanh toán đã được tạo, trạng thái: PENDING");
                const link = response.data.linkPayment;

                // Redirect to the link received from the response
                window.location.href = link;
            } else if (response.data.statusPayment === 'COMPLETED') {
                console.log("Thanh toán thành công CHO VOUCHER LỚN.");
                navigate('/payment-status', { state: { status: 'success' } });
            } else {
                console.error("Thanh toán thất bại hoặc không xác định.");
                navigate('/payment-status', { state: { status: 'failure' } });
            }
        } catch (error) {
            console.error("Có lỗi xảy ra khi tạo thanh toán:", error);
            navigate('/payment-status', { state: { status: 'failure' } });
        }
    };

    // Handle PayOS Payment
    const handlePayOS = () => {
        console.log("Thanh toán qua PayOS");
        // Add logic to handle PayOS payment
    };

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
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                const userInfo = userResponse.data;
                let addressParts = (userInfo.address || '').split(',').map(part => part.trim());
                const [street, ward, district, city] = [
                    addressParts[0] || '', addressParts[1] || '', addressParts[2] || '', addressParts[3] || ''
                ];

                setUserData({
                    fullName: userInfo.fullName || '',
                    email: userInfo.email || '',
                    phone: userInfo.phone || '',
                    address: userInfo.address || '',
                    street, ward, district, city,
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

    useEffect(() => {
        const fetchOrderDetails = async () => {
            const token = getCookie('access_token');
            if (!token) return;

            try {
                const response = await axios.get(`http://localhost:1010/api/orders/detail-item/${orderData.orderId}`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                setOrderDetails(response.data);
            } catch (error) {
                console.error("Error fetching order details:", error);
                setError("Không thể lấy chi tiết đơn hàng.");
            }
        };

        if (orderData?.orderId) {
            fetchOrderDetails();
        }
    }, [orderData]);

    const handleCancelOrder = async () => {
        const token = getCookie('access_token');
        if (!token) {
            setError("Bạn cần đăng nhập để hủy đơn hàng.");
            return;
        }

        try {
            const userId = getUserIdFromToken(token);

            // Kiểm tra trạng thái của currentStep
            if (currentStep === 'confirmation') {
                // Gọi API confirm-cancel nếu currentStep là confirmation
                const response = await axios.put('http://localhost:1010/api/orders/cancel-order',
                    {
                        orderId: orderData.orderId,
                        userId: userId
                    },
                    {
                        headers: { 'Authorization': `Bearer ${token}` }
                    });

                // Kiểm tra phản hồi từ API
                if (response.data === 'Order cancelled successfully') {
                    console.log("Vào api cancel");
                    setShowSuccess(true);
                    setTimeout(() => {
                        setShowSuccess(false);
                        navigate('/menu');
                    }, 2000);

                } else {
                    setShowError(true);
                    setTimeout(() => {
                        setShowError(false);
                    }, 2000);
                }
            } else if (currentStep === 'payment') {
                // Gọi API cancel-order nếu currentStep là payment
                const response = await axios.put('http://localhost:1010/api/orders/cancel-order',
                    {
                        orderId: orderData.orderId,
                        userId: userId
                    },
                    {
                        headers: { 'Authorization': `Bearer ${token}` }
                    });

                // Kiểm tra phản hồi từ API
                if (response.data === 'Order cancelled successfully') {
                    await ensureCartExists(userId);
                    setShowSuccess(true);
                    setTimeout(() => {
                        setShowSuccess(false);
                        navigate('/menu');
                    }, 2000);

                    // Điều hướng về trang menu sau khi hủy thành công
                } else {
                    setShowError(true);
                    setTimeout(() => {
                        setShowError(false);
                    }, 2000);
                }
            } else {
                setError("Không thể hủy đơn hàng trong trạng thái này.");
            }
        } catch (error) {
            console.error("Error canceling order:", error);
            setError("Không thể hủy đơn hàng.");
        }
    };

    const handleEditToggle = () => {
        setIsEditing(!isEditing);
    };

    return (
        <>
            <Navbar currentPage={'Thanh toán'} />
            <div className="order-background-container">
                <div className="order-container">
                    <div className="content">
                        {/* Progress Steps */}
                        <div className="progress-steps">
                            <div className={`step ${currentStep === "confirmation" ? "active" : ""}`}>
                                <i className="fas fa-shopping-basket"></i>
                                <span>1. Xác nhận</span>
                            </div>
                            <div className="progress-line"></div>
                            <div className={`step ${currentStep === "payment" ? "active" : ""}`}>
                                <i className="fas fa-credit-card"></i>
                                <span>2. Thanh toán</span>
                            </div>
                            {isLoading && (
                                <div className="loading-animation">
                                    <div className="loading-modal">
                                        <div className="loading-spinner">
                                            <div className="spinner"></div>
                                        </div>
                                        <h3>Đang xử lý...</h3>
                                        <p>Vui lòng đợi trong giây lát</p>
                                    </div>
                                </div>
                            )}
                        </div>

                        {/* Invoice and Customer Information */}
                        {/* Content for Confirmation Step */}

                        {currentStep === "confirmation" && (
                            <div className="info-order-sections">
                                <div className="info-section">
                                    {orderDetails && (
                                        <div className="order-items">
                                            <h2 style={{ marginBottom: '20px' }}>Chi tiết đơn hàng</h2>
                                            {orderDetails.listItemOrders.map(item => (
                                                <div key={item.cartItemId} className="order-item">
                                                    <div>
                                                        <p><strong>Sản phẩm:</strong> {item.proName}</p>
                                                        <p><strong>Kích thước:</strong> {item.size} <span> x {item.quantity} x {formatCurrency(item.priceItem)}</span></p>
                                                    </div>
                                                    <p style={{ textAlign: 'end', color: 'black' }}><strong>Tổng: {formatCurrency(item.totalPrice)}</strong></p>
                                                    <hr />
                                                </div>
                                            ))}
                                        </div>
                                    )}

                                    <h2 style={{ marginBottom: '20px' }}>Tổng hóa đơn</h2>
                                    <p><strong>Giá sản phẩm: </strong>{formatCurrency(orderData.totalPrice)}</p>
                                    <p><strong>Phí vận chuyển: </strong>{formatCurrency(orderData.deliveryFee)}</p>
                                    <p><strong>Giảm giá: </strong>{formatCurrency(orderData.discountPrice)}</p>
                                    <hr />
                                    <p style={{ color: 'black', fontSize: '25px', textAlign: 'center' }}>
                                        <strong>
                                            Tổng cộng:
                                            {formatCurrency(Math.max(orderData.totalPrice + orderData.deliveryFee - orderData.discountPrice, 0))}
                                        </strong>
                                    </p>

                                </div>
                                <div className="info-section">
                                    <h2 style={{ marginBottom: '20px' }}>Thông tin khách hàng</h2>
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
                                                style={{ marginBottom: '0px' }}
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
                                        <div className="form-group" style={{ gap: '0px' }}>
                                            <label style={{ marginBottom: '0px' }}>Thành phố:</label>
                                            <select
                                                className="form-control"
                                                value={userData.city}
                                                disabled={!isEditing}
                                                style={{ fontSize: '14px', padding: '10px' }}
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
                                            <div className="form-group" style={{ gap: '0px' }}>
                                                <label style={{ marginBottom: '0px' }}>Quận/Huyện:</label>
                                                <select
                                                    className="form-control"
                                                    value={userData.district}
                                                    disabled={!isEditing}
                                                    style={{ fontSize: '14px', padding: '10px' }}
                                                >
                                                    <option value="">Chọn quận/huyện</option>
                                                    {districts.map((district) => (
                                                        <option key={district.districtId} value={district.districtName}>
                                                            {district.districtName}
                                                        </option>
                                                    ))}
                                                </select>
                                            </div>

                                            <div className="form-group" style={{ gap: '0px' }}>
                                                <label style={{ marginBottom: '0px' }}>Xã/Phường:</label>
                                                <select
                                                    className="form-control"
                                                    value={userData.ward}
                                                    disabled={!isEditing}
                                                    style={{ fontSize: '14px', padding: '10px' }}
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
                                        {isLoading && (
                                            <div className="loading-animation">
                                                <div className="loading-modal">
                                                    <div className="loading-spinner">
                                                        <div className="spinner"></div>
                                                    </div>
                                                    <h3>Đang xử lý...</h3>
                                                    <p>Vui lòng đợi trong giây lát</p>
                                                </div>
                                            </div>
                                        )}



                                        {showError && (
                                            <div className="error-animation">
                                                <div className="error-modal">
                                                    <div className="error-icon">
                                                        <div className="error-icon-circle">
                                                            <svg className="cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                                                <circle className="cross-circle" cx="26" cy="26" r="25" fill="none" />
                                                                <path className="cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36" />
                                                            </svg>
                                                        </div>
                                                    </div>
                                                    <h3>Đơn hàng không thể hủy!</h3>
                                                    <p>Không hủy được đơn hàng!.</p>
                                                </div>
                                            </div>
                                        )}

                                        {showErroConfirm && (
                                            <div className="error-animation">
                                                <div className="error-modal">
                                                    <div className="error-icon">
                                                        <div className="error-icon-circle">
                                                            <svg className="cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                                                <circle className="cross-circle" cx="26" cy="26" r="25" fill="none" />
                                                                <path className="cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36" />
                                                            </svg>
                                                        </div>
                                                    </div>
                                                    <h3>Đơn hàng không thể hủy!</h3>
                                                    <p>Không hủy được đơn hàng!.</p>
                                                </div>
                                            </div>
                                        )}

                                        {showErroApi && (
                                            <div className="error-animation">
                                                <div className="error-modal">
                                                    <div className="error-icon">
                                                        <div className="error-icon-circle">
                                                            <svg className="cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                                                <circle className="cross-circle" cx="26" cy="26" r="25" fill="none" />
                                                                <path className="cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36" />
                                                            </svg>
                                                        </div>
                                                    </div>
                                                    <h3>Vui lòng thử lại!</h3>
                                                </div>
                                            </div>
                                        )}
                                        <div className="form-group" style={{ gap: '0px' }}>
                                            <label style={{ marginBottom: '0px' }}>Đường:</label>
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
                        )}

                        {/* Content for Payment Step */}
                        {currentStep === "payment" && (
                            <div className="payment-method-section">
                                <h2>Phương thức thanh toán</h2>
                                <div className="payment-options">
                                    <div>
                                        <label>
                                            <input
                                                type="radio"
                                                name="paymentMethod"
                                                value="cashOnDelivery"
                                                onChange={handlePaymentMethodChange}
                                            /> Thanh toán khi nhận hàng
                                        </label>
                                    </div>

                                    <div>
                                        <label>
                                            <input
                                                type="radio"
                                                name="paymentMethod"
                                                value="creditCard"
                                                onChange={handlePaymentMethodChange}
                                            /> Thẻ ngân hàng
                                            <img src={bidv} style={{ width: '50px', height: '40px', flex: '0.4' }} />
                                        </label>
                                    </div>

                                    <div>
                                        <label>
                                            <input
                                                type="radio"
                                                name="paymentMethod"
                                                value="momo"
                                                onChange={handlePaymentMethodChange}
                                            /> MOMO
                                            <img src={momo} style={{ width: '50px', height: '40px', flex: '0.4' }} />
                                        </label>
                                    </div>

                                    <div>
                                        <label>
                                            <input
                                                type="radio"
                                                name="paymentMethod"
                                                value="zaloPay"
                                                onChange={handlePaymentMethodChange}
                                            /> ZaloPay
                                            <img src={zalo} style={{ width: '50px', height: '40px', flex: '0.4' }} />
                                        </label>
                                    </div>

                                    <div>
                                        <label>
                                            <input
                                                type="radio"
                                                name="paymentMethod"
                                                value="vnPay"
                                                onChange={handlePaymentMethodChange}
                                            /> VNPay
                                            <img src={vn} style={{ width: '50px', height: '40px', flex: '0.4' }} />
                                        </label>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Action Buttons */}
                        <div className="order-action-buttons">
                            <button className="checkout-button" onClick={handleProceedToPayment}>
                                {currentStep === "confirmation" ? 'Xác nhận' : 'Thanh toán'}
                            </button>
                            <button className="back-button" onClick={handleCancelOrder}>Hủy</button>
                            {showSuccess && (
                                <div className="order-success-animation">
                                    <div className="order-success-modal">
                                        <div className="order-success-icon">
                                            <div className="order-success-icon-circle">
                                                <svg className="order-checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                                    <circle className="order-checkmark-circle" cx="26" cy="26" r="25" fill="none" />
                                                    <path className="order-checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
                                                </svg>
                                            </div>
                                        </div>
                                        <h3>Hủy đơn hàng thành công!</h3>
                                        <p>Bạn đã hủy đơn hàng.</p>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default Order;
