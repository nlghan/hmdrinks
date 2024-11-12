import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useParams, useNavigate } from 'react-router-dom';
import Footer from "../../components/Footer/Footer.jsx";
import Navbar from "../../components/Navbar/Navbar.jsx";
import { assets } from "../../assets/assets.js";
import './PostVoucher.css'; // Import CSS cho styling

// Hàm để lấy giá trị cookie
const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
};

const PostVoucher = () => {
    const { postId } = useParams(); // Lấy postId từ URL
    const navigate = useNavigate(); // Khởi tạo useNavigate
    const [post, setPost] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [voucher, setVoucher] = useState(null); // State cho voucher

    useEffect(() => {
        const fetchPostDetails = async () => {
            const token = getCookie('access_token'); // Lấy token từ cookie
            try {
                const response = await axios.get(`http://localhost:1010/api/post/view/${postId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}` // Gửi token trong header
                    }
                });
                // Truy cập dữ liệu bài đăng đúng cách
                setPost(response.data.body); // Thiết lập dữ liệu bài đăng

                // Fetch all vouchers
                const responseVouchers = await axios.get('http://localhost:1010/api/voucher/view/all', {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                console.log("Raw API response:", responseVouchers.data); // Kiểm tra phản hồi đầy đủ từ API
                const fetchedVouchers = responseVouchers.data.body.voucherResponseList || [];
                console.log("Fetched Vouchers:", fetchedVouchers); // Log dữ liệu voucher

                if (Array.isArray(fetchedVouchers)) {
                    // Tìm voucher tương ứng với post
                    const matchingVoucher = fetchedVouchers.find(
                        (voucher) => String(voucher.postId) === String(postId)
                    );
                    setVoucher(matchingVoucher || null); // Cập nhật voucher

                    setVoucher(matchingVoucher || null); // Cập nhật voucher
                } else {
                    console.error("voucherResponseList is not an array or is undefined");
                    setVoucher(null);
                }

            } catch (err) {
                setError(err.message);
                console.error('Error fetching post details:', err); // In ra lỗi nếu có
            } finally {
                setLoading(false);
            }
        };

        fetchPostDetails();
    }, [postId]);

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;


    // Hàm định dạng ngày
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    };

    return (
        <>
            <Navbar />
            <div 
                className="post-voucher-container"
                style={{
                    '--post-background': `url(${post?.url || assets.avtrang})`
                }}
            >
                <button type="button" className="btn-back fade-in" onClick={() => navigate('/home')}>
                    Trở lại
                </button>

                {post && (
                    <div className="post-content-wrapper">
                        <div className="post-banner zoom-in1">
                            <div className="post-details fade-in">
                                <h3 className="post-title">{post.title}</h3>
                                <p className="post-date">{post.date_create}</p>
                                <p className="post-short-desc">{post.shortDescription}</p>
                            </div>
                            
                            <div className="post-image-wrapper">
                                <img 
                                    src={post.url || assets.avtrang} 
                                    alt={post.title}
                                    className="post-banner-image"
                                />
                            </div>
                        </div>

                        <div className="post-description-section slide-in-left">
                            <p>{post.description}</p>
                        </div>

                        {voucher && (
                            <div className="voucher-section slide-in-right">
                                <div className="voucher-card">
                                    <div className="voucher-content">
                                        <span className={`voucher-key-user ${voucher.status === 'ACTIVE' ? 'active' : 'inactive'}`}>
                                            {voucher.key}
                                        </span>
                                        <div className="voucher-details">
                                            <span className="voucher-discount">
                                                {voucher.discount.toLocaleString()} VND
                                            </span>
                                            <div className="voucher-dates">
                                                <span>Từ: {formatDate(voucher.startDate)}</span>
                                                
                                                <span>Đến: {formatDate(voucher.endDate)}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
            <Footer />
        </>
    );
};

export default PostVoucher;
 