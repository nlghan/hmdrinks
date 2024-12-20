import React, { useState } from "react";
import axios from "axios"; // Nhập axios
import Footer from "../../components/Footer/Footer.jsx"; // Đảm bảo bạn đã import Footer đúng
import './SendMail.css';
import { useNavigate } from "react-router-dom";

const SendMail = () => {
    const [email, setEmail] = useState("");
    const [otp, setOtp] = useState(""); // State để lưu OTP
    const [isOtpSent, setIsOtpSent] = useState(false); // State để kiểm tra đã gửi OTP hay chưa
    const [message, setMessage] = useState(""); // Để lưu thông báo gửi email
    const [error, setError] = useState(""); // Để lưu thông báo lỗi
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmitChange = async (e) => {
        e.preventDefault();
        // Nếu đang ở trạng thái gửi OTP
        if (!isOtpSent) {
            // Gửi yêu cầu quên mật khẩu
            if (email) {
                try {
                    setIsLoading(true);
                    const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/public/password/forget/send`, {
                        email: email
                    }, {
                        headers: {
                            'Content-Type': 'application/json',
                            'accept': '*/*'
                        }
                    });

                    // Xử lý phản hồi từ backend
                    setMessage(response.data.message);
                    setError(""); // Xóa thông báo lỗi nếu có
                    setIsLoading(false);
                    setIsOtpSent(true); // Chuyển sang trạng thái nhập OTP
                } catch (err) {
                    // Xử lý lỗi
                    if (err.response && err.response.data) {
                        setError(err.response.data.message || "Đã xảy ra lỗi. Vui lòng thử lại.");
                    } else {
                        setError("Đã xảy ra lỗi. Vui lòng thử lại.");
                    }
                    setMessage(""); // Xóa thông báo thành công nếu có
                }
            } else {
                setMessage("");
                setError("Vui lòng nhập địa chỉ email của bạn.");
            }
        } else {
            // Nếu đã gửi OTP, xử lý xác thực OTP
            if (otp) {
                try {
                    setIsLoading(true);
                    const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/public/password/acceptOtp`, {
                        email: email,
                        otp: otp
                    }, {
                        headers: {
                            'Content-Type': 'application/json',
                            'accept': '*/*'
                        }
                    });

                    // Xử lý phản hồi từ server
                    setMessage(response.data.message);
                    setError(""); // Xóa thông báo lỗi nếu có


                    // Tự động chuyển hướng về trang đăng nhập sau 2 giây
                    setTimeout(() => {
                        setIsLoading(false);
                        navigate('/login');
                    }, 2000);
                } catch (err) {
                    // Xử lý lỗi xác thực OTP
                    if (err.response && err.response.data) {
                        setError(err.response.data.message || "Đã xảy ra lỗi. Vui lòng thử lại.");
                    } else {
                        setError("Đã xảy ra lỗi. Vui lòng thử lại.");
                    }
                    setMessage(""); // Xóa thông báo thành công nếu có
                }
            } else {
                setError("Vui lòng nhập mã OTP.");
            }
        }
    };

    const handleBack = () => {
        navigate('/login'); // Điều hướng đến trang Login
    };

    return (
        <>
            <div className="send-mail-page">
                <div className="send-mail-container">
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
                    {/* Nút quay lại */}
                    <i className="ti-arrow-left" id='arrow-back-1' onClick={handleBack} style={{ cursor: 'pointer', marginBottom: '20px', display: "flex" }}></i>
                    <h2 className="send">Quên mật khẩu?</h2>
                    <p>{isOtpSent ? "Vui lòng nhập mã OTP đã gửi đến email của bạn." : "Vui lòng nhập lại email của bạn. Chúng tôi sẽ gửi mã xác nhận thông qua email này."}</p>
                    <form onSubmit={handleSubmitChange} className="send-mail-form">
                        <div className="input-group-pass">
                            {!isOtpSent ? (
                                <input
                                    type="email"
                                    placeholder="Nhập địa chỉ email của bạn"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            ) : (
                                <input
                                    type="text"
                                    placeholder="Nhập mã OTP"
                                    value={otp}
                                    onChange={(e) => setOtp(e.target.value)}
                                    required
                                    className="otp-change"
                                />
                            )}
                            <button className="btn-send" style={{ height: '55px', borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px' }} type="submit">{isOtpSent ? "GỬi" : "GỬI"}</button>
                        </div>
                    </form>
                    {message && <p className="send-mail-message">{message}</p>}
                    {error && <p className="send-mail-error">{error}</p>} {/* Hiển thị thông báo lỗi */}
                    <p className="send-mail-note">Nếu không nhận được mã xác nhận, hãy kiểm tra trong thư rác hoặc ấn “Gửi” để nhận lại mã.</p>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default SendMail;
