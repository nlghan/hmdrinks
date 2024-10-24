import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './FormDetailsPost.css'; // Đảm bảo bạn đã tạo file CSS

// Function to retrieve token from cookies
const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
};

const FormDetailsPost = ({ postId, onClose }) => {
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
                setPost(response.data); // Thiết lập dữ liệu bài đăng

                // Fetch all vouchers
                const responseVouchers = await axios.get('http://localhost:1010/api/voucher/view/all', {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                const fetchedVouchers = responseVouchers.data.voucherResponseList;
                // Tìm voucher tương ứng với post
                const matchingVoucher = fetchedVouchers.find(voucher => voucher.postId === postId);
                setVoucher(matchingVoucher || null); // Cập nhật voucher

                console.log('Post details:', response.data); // In ra log chi tiết bài đăng
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

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    };

    return (
        <div className="form-details-post-container">
            <div className="form-details-post-header">
                <h2 className="form-details-post-title">Chi Tiết Bài Đăng</h2>
                <button onClick={onClose} className="form-details-post-close-button">x</button>
            </div>
            <div className="form-details-post-content">
                <div className="form-details-post-details">
                    <h3 className="form-details-post-item-title">{post.title}</h3>
                    <p className="form-details-post-item-date">
                        <strong></strong> {formatDate(post.dateCreated)}
                    </p>
                    <p className="form-details-post-item-short-description">
                        <strong> </strong> {post.shortDescription}
                    </p>
                    <p className="form-details-post-item-description">
                        <strong> </strong> {post.description}
                    </p>
                    <div className="form-details-post-voucher">
                        <h4>Thông Tin Voucher</h4>
                        <div className="voucher-item">
                            <span className={`voucher-key ${voucher.status === 'ACTIVE' ? 'active' : 'expired'}`}>
                                {voucher ? voucher.key : 'N/A'}
                            </span>
                            <span className="voucher-discount">
                                {voucher ? `${voucher.discount} VNĐ` : 'N/A'}
                            </span>
                            {/* <span className="voucher-status">
                                {voucher ? voucher.status : 'N/A'}
                            </span> */}
                        </div>
                        <div className="voucher-dates">
                            <span className="voucher-start-date">
                                {voucher ? `Ngày bắt đầu: ${new Date(voucher.startDate).toLocaleDateString('vi-VN')}` : 'N/A'}
                            </span>
                            <span className="voucher-end-date">
                                {voucher ? `Ngày kết thúc: ${new Date(voucher.endDate).toLocaleDateString('vi-VN')}` : 'N/A'}
                            </span>
                        </div>
                    </div>


                    <div className="form-details-post-image">
                        <img src={post.url} alt={post.title} />
                    </div>
                    {post.isDeleted && <p className="form-details-post-status"><strong>Trạng Thái:</strong> Đã xóa</p>}

                </div>
            </div>
        </div>
    );
};

export default FormDetailsPost;
