import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useParams, useNavigate } from 'react-router-dom';
import Footer from "../../components/Footer/Footer.jsx";
import Navbar from "../../components/Navbar/Navbar.jsx";
import { assets } from "../../assets/assets.js";
import './PostVoucher.css';
import voucherImg from '../../assets/img/vc-b.png';

const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
};
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

const PostVoucher = () => {
    const { postId } = useParams();
    const navigate = useNavigate();
    const [post, setPost] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [voucher, setVoucher] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [showSuccess, setShowSuccess] = useState(false);
    const [showError, setShowError] = useState(false);
    const [isVoucherClaimed, setIsVoucherClaimed] = useState(false);

    useEffect(() => {
        const fetchPostDetails = async (voucherId) => {
            const token = getCookie('access_token');
            try {
                const response = await axios.get(`http://localhost:1010/api/post/view/${postId}`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                setPost(response.data.body);

                const responseVouchers = await axios.get('http://localhost:1010/api/voucher/view/all', {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                const fetchedVouchers = responseVouchers.data.body.voucherResponseList || [];
                const matchingVoucher = fetchedVouchers.find(
                    (voucher) => String(voucher.postId) === String(postId)
                );
                console.log("Fetched Voucher Data:", matchingVoucher);
                setVoucher(matchingVoucher || null);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchPostDetails();
    }, [postId]);

    const claimVoucher = async (voucherId) => {
        setIsLoading(true);
        const token = getCookie('access_token');
        try {
            const userId = getUserIdFromToken(token);
            const payload = { userId, voucherId };

            const response = await axios.post(
                'http://localhost:1010/api/user-voucher/get-voucher',
                payload,
                { headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' } }
            );

            setIsLoading(false);
            setShowSuccess(true);
            setIsVoucherClaimed(true);
            setTimeout(() => {
                setShowSuccess(false);
            }, 2000);
        } catch (err) {
            console.error('Error claiming voucher:', err);
            setIsLoading(false);
            setShowError(true);
            setTimeout(() => {
                setShowError(false);
            }, 2000);
        }
    };



    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    };

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;

    return (
        <>
            <Navbar currentPage={'Chi tiết bài viết'} />
            <div
                className="post-voucher-container"
                style={{
                    '--post-background': `url(${post?.url || assets.avtrang})`
                }}
            >
                <button type="button" className="btn-back fade-in" onClick={() => navigate('/post')}>
                    Trở lại
                </button>

                {post && (
                    <div className="post-content-wrapper">
                        <div className="post-banner zoom-in1">
                            <div className="post-details fade-in">
                                <h3 className="post-title" style={{ color: 'black' }}>{post.title}</h3>
                                <p className="post-date">{post.date_create}</p>
                                <p className="post-short-desc" style={{ color: 'black' }} >{post.shortDescription}</p>
                                <div className="post-description-section slide-in-left">
                                    <p style={{ color: 'black' }}>{post.description}</p>
                                </div>
                            </div>
                            <div className="post-image-wrapper">
                                <img
                                    src={post.url || assets.avtrang}
                                    alt={post.title}
                                    className="post-banner-image"
                                />
                            </div>
                        </div>

                        {voucher && (() => {
                            if (voucher.number === 0) {
                                return <h3 className="post-title2">Đã hết Voucher!</h3>;
                            }
                            const currentDate = new Date();
                            const startDate = new Date(voucher.startDate);
                            const endDate = new Date(voucher.endDate);

                            if (currentDate < startDate) {
                                return <h3 className="post-title2">Voucher chưa tới thời hạn thu thập</h3>;
                            }

                            if (currentDate > endDate) {
                                return <h3 className="post-title2">Voucher đã quá thời hạn thu thập</h3>;
                            }

                            return (
                                <>
                                    <h3 className="post-title2">Thu thập voucher</h3>
                                    <div className="voucher-wrapper">
                                        <div
                                            className="voucher-section"
                                            style={{ "--animation-duration": `${voucher.number * 4}s` }}
                                        >
                                            {[...Array(voucher.number)].map((_, index) => (
                                                <div className="voucher-card" key={`original-${index}`}>
                                                    <div className="voucher-image">
                                                        <img src={voucherImg} alt="voucher" />
                                                    </div>
                                                    <div className="voucher-content">
                                                        <span className={`voucher-key ${voucher.status === 'ACTIVE' ? 'active' : 'inactive'}`}>
                                                            {voucher.key}
                                                        </span>
                                                        <div className="voucher-details">
                                                            <span className="voucher-discount">{voucher.discount.toLocaleString()} VND</span>
                                                            <div className="voucher-dates">
                                                                <span>Từ: {formatDate(voucher.startDate)}</span>
                                                                <br />
                                                                <span>Đến: {formatDate(voucher.endDate)}</span>
                                                            </div>
                                                        </div>
                                                        <button
                                                            className={`voucher-claim-btn ${isVoucherClaimed ? 'disabled' : ''}`}
                                                            onClick={() => claimVoucher(voucher.voucherId)}
                                                            disabled={isVoucherClaimed}
                                                        >
                                                            {isVoucherClaimed ? 'Đã nhận' : 'Nhận Ngay'}
                                                        </button>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                </>
                            );
                        })()}

                    </div>
                )}
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

            {showSuccess && (
                <div className="success-animation">
                    <div className="success-modal">
                        <div className="success-icon">
                            <div className="success-icon-circle">
                                <svg className="checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                    <circle className="checkmark-circle" cx="26" cy="26" r="25" fill="none" />
                                    <path className="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
                                </svg>
                            </div>
                        </div>
                        <h3>Nhận voucher thành công!</h3>
                        <p>Chúc mừng bạn đã nhận được voucher.</p>
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
                        <h3>Bạn đã thu thập voucher rồi!</h3>
                        <p>Một người dùng chỉ nhận voucher một lần.</p>
                    </div>
                </div>
            )}

            <Footer />
        </>
    );
};

export default PostVoucher;
