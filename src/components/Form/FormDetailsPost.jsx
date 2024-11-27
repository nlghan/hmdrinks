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
                    const matchingVoucher = fetchedVouchers.find(voucher => voucher.postId === postId);
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

    // Log giá trị của post và voucher sau khi cập nhật
    useEffect(() => {
        console.log('post:', post);
        console.log('voucher:', voucher);
    }, [post, voucher]);

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
        <div className="form-details-post-container" sttyle={{height: '1000px'}}>
            <div className="form-details-post-header">
                <h2 className="form-details-post-title">Chi Tiết Bài Đăng</h2><button
                    onClick={onClose}
                    style={{
                        height: '35px',
                        width: '35px',
                        borderRadius: '50%',
                        backgroundColor: '#e8e6e6',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        border: 'none'
                    }}
                    className="form-details-post-close-button"
                >
                    <i className="ti-close" style={{ color: '#f21b1b', fontSize: '18px' }}></i>
                </button>


            </div>
            <div className="form-details-post-content">
                <div className="form-details-post-details">
                    {post ? (
                        <>
                            <div className="form-details-post-image">
                                {post.url ? (
                                    <img src={post.url} alt={post.title} />
                                ) : (
                                    <p>Hình ảnh không có sẵn</p>
                                )}
                            </div>
                            <h3 className="form-details-post-item-title">{post.title}</h3>
                            <p className="form-details-post-item-date">
                                <strong></strong> {post.dateCreated ? formatDate(post.dateCreated) : 'N/A'}
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
                                    <span className={`voucher-key-detail ${voucher && voucher.status === 'ACTIVE' ? 'active' : 'expired'}`}>
                                        {voucher ? voucher.key : 'N/A'}
                                    </span>
                                    <span className="voucher-discount-detail">
                                        {voucher ? `${voucher.discount} VNĐ` : 'N/A'}
                                    </span>
                                    <span className="voucher-discount-detail">
                                        {voucher ? `${voucher.number} voucher` : 'N/A'}
                                    </span>

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

                            {post.isDeleted && <p className="form-details-post-status"><strong>Trạng Thái:</strong> Đã xóa</p>}
                        </>
                    ) : (
                        <p>Đang tải bài đăng...</p>
                    )}
                </div>
            </div>
        </div>
    );

};

export default FormDetailsPost;
