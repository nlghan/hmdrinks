import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useParams, useNavigate } from 'react-router-dom';
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
                // Fetch post details
                const response = await axios.get(`http://localhost:1010/api/post/view/${postId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}` // Gửi token trong header
                    }
                });
                setPost(response.data); // Thiết lập dữ liệu bài đăng
                console.log('Post details fetched:', response.data);

                // Fetch all vouchers
                const responseVouchers = await axios.get('http://localhost:1010/api/voucher/view/all', {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                console.log('Vouchers fetched:', responseVouchers.data);

                const fetchedVouchers = responseVouchers.data.voucherResponseList;
                console.log('Fetched Vouchers:', fetchedVouchers); // Log danh sách vouchers
                
                // Tìm voucher tương ứng với post
                const matchingVoucher = fetchedVouchers.find(voucher => voucher.postId === Number(postId));
                console.log('Matching voucher found:', matchingVoucher); // Log voucher tìm thấy
                
                setVoucher(matchingVoucher || null); // Cập nhật voucher
                console.log('Matching voucher found:', matchingVoucher);

            } catch (err) {
                setError(err.message);
                console.error('Error fetching post details:', err.response ? err.response.data : err); // In ra lỗi chi tiết nếu có
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
        <div className="marketing-post-voucher-container">
            <button type="button" className="marketing-btn-back" id="btn-back" onClick={() => navigate('/home')}>
                Trở lại
            </button>
    
            {post && (
                <div className="marketing-post-details">
                    <h3>{post.title}</h3>
                    <p className="marketing-post-short-date">
                        <strong></strong> {post.date_create}
                    </p>
                    <p className="marketing-post-short-description">
                        <strong></strong> {post.shortDescription}
                    </p>
                    <p className="marketing-post-description">{post.description}</p>
                    {post.isDeleted && <p className="marketing-post-status"><strong>Trạng Thái:</strong> Đã xóa</p>}
                </div>
            )}
            {voucher ? (
                <div className="marketing-form-details-post-voucher">
                    <h4>Voucher</h4>
                    <div className="marketing-voucher-details">
                        <span className={`marketing-voucher-key ${voucher.status === 'ACTIVE' ? 'active' : 'inactive'}`}>
                            {voucher.key}
                        </span>
                        <span className="marketing-voucher-discount">
                            Giảm giá: {voucher.discount} VND
                        </span>
                        <p></p>                        
                        <span className="marketing-voucher-status">
                            Trạng thái: {voucher.status}
                        </span>
                        <div className="marketing-voucher-dates">
                            <small>Bắt đầu từ: {formatDate(voucher.startDate)}</small>
                            <small> - </small>
                            <small>Kết thúc lúc: {formatDate(voucher.endDate)}</small>
                        </div>
                    </div>
                </div>
            ) : (
                <div className="marketing-no-voucher">
                    <p>Không có voucher nào cho bài đăng này.</p>
                </div>
            )}
    
            {/* Chuyển hình ảnh xuống cuối */}
            {post && (
                <div className="marketing-post-image">
                    <img src={post.url} alt={post.title} />
                </div>
            )}
        </div>
    );
    
    
};

export default PostVoucher;
