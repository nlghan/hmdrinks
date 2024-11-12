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
        const payload = token.split('.')[1];
        const decodedPayload = JSON.parse(atob(payload));
        return decodedPayload.UserId;
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
        const token = getCookie('access_token');
        try {
            const userId = getUserIdFromToken(token);
    
            // Log payload để kiểm tra nội dung gửi lên server
            const payload = { userId, voucherId };
            console.log("Payload gửi lên server:", payload);
            
            const response = await axios.post(
                'http://localhost:1010/api/user-voucher/get-voucher',
                payload,
                { headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' } }
            );
            
            alert('Voucher collected successfully!');
        } catch (err) {
            console.error('Error claiming voucher:', err);
            alert('Failed to claim voucher.');
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
            <Navbar />
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
                                <h3 className="post-title">{post.title}</h3>
                                <p className="post-date">{post.date_create}</p>
                                <p className="post-short-desc">{post.shortDescription}</p>
                                <div className="post-description-section slide-in-left">
                                    <p>{post.description}</p>
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

                        {voucher && (
                            <>
                                <h3 className="post-title2">Thu thập voucher</h3>
                                <div className="voucher-section slide-in-right">
                                    <div className="voucher-card">
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
                                            <button className="voucher-claim-btn" onClick={() => claimVoucher(voucher.voucherId)}>Nhận Ngay</button>
                                        </div>
                                    </div>
                                </div>
                            </>
                        )}
                    </div>
                )}
            </div>
            <Footer />
        </>
    );
};

export default PostVoucher;
