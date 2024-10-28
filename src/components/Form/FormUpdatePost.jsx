import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './FormUpdatePost.css';

const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);

    if (parts.length === 2) return parts.pop().split(';').shift();
};

const FormUpdatePost = ({ post, postId, onClose, onSave }) => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        shortDescription: '',
        file: null,
        keyVoucher: '',
        discount: 0,
        startDate: '',
        endDate: '',
        status: 'ACTIVE',
        imageUrl: '',
        fileName: '',
        voucherId: '',
    });

    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');


    useEffect(() => {
        const fetchPostDetails = async () => {
            const token = getCookie('access_token'); // Lấy token từ cookie
            console.log('Fetching post details for postId:', postId); // Log postId

            try {
                // 1. Fetch post details using postId
                const postResponse = await axios.get(`http://localhost:1010/api/post/view/${postId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}` // Gửi token trong header
                    }
                });

                console.log('Post details response:', postResponse.data); // Log phản hồi của bài đăng

                const { title, description, shortDescription, bannerUrl } = postResponse.data.body; // Sửa để truy cập đúng dữ liệu
                setFormData(prevState => ({
                    ...prevState,
                    title,
                    description,
                    shortDescription,
                    imageUrl: bannerUrl,
                }));

                // 2. Fetch all vouchers
                const voucherResponse = await axios.get('http://localhost:1010/api/voucher/view/all', {
                    headers: {
                        'Authorization': `Bearer ${token}` // Gửi token trong header
                    }
                });

                console.log('Vouchers response:', voucherResponse.data); // Log phản hồi của vouchers

                // Kiểm tra cấu trúc của response
                const fetchedVouchers = voucherResponse.data.body.voucherResponseList || []; // Sửa để đảm bảo có giá trị mặc định
                console.log('Fetched vouchers list:', fetchedVouchers); // Log danh sách vouchers đã lấy

                if (Array.isArray(fetchedVouchers)) {
                    // Tìm voucher tương ứng với post
                    const matchingVoucher = fetchedVouchers.find(voucher => voucher.postId === postId);
                    if (matchingVoucher) {
                        console.log('Found matching voucher:', matchingVoucher); // Log voucher tìm thấy
                        // Cập nhật voucher nếu tìm thấy
                        setFormData(prevState => ({
                            ...prevState,
                            keyVoucher: matchingVoucher.key,
                            discount: matchingVoucher.discount,
                            startDate: matchingVoucher.startDate,
                            endDate: matchingVoucher.endDate,
                            status: matchingVoucher.status,
                            voucherId: matchingVoucher.voucherId,
                        }));
                    } else {
                        console.log(`No voucher found for postId: ${postId}`); // Log nếu không tìm thấy voucher
                    }
                } else {
                    console.error("voucherResponseList is not an array or is undefined");
                }
            } catch (err) {
                console.error('Error fetching post details:', err); // In ra lỗi nếu có
                setErrorMessage('Không thể tải thông tin bài đăng hoặc voucher. Vui lòng thử lại sau.');
            } finally {
                console.log('Fetch post details operation completed.'); // Log khi hoàn tất quá trình
            }
        };

        fetchPostDetails();
    }, [postId]);



    const handleInputChange = (event) => {
        const { name, value, files } = event.target;
        if (name === 'file') {
            // Nếu người dùng chọn tệp, cập nhật tên tệp và URL ảnh
            const file = files[0];
            if (file) {
                const imageUrl = URL.createObjectURL(file); // Tạo URL cho ảnh đã chọn
                setFormData(prevState => ({
                    ...prevState,
                    fileName: file.name, // Lưu tên tệp
                    file: file, // Lưu tệp để tải lên sau
                    imageUrl, // Cập nhật URL ảnh
                }));
            }
        } else {
            // Cập nhật các trường khác
            setFormData(prevState => ({
                ...prevState,
                [name]: value,
            }));
        }
    };



    // Helper function to format the datetime-local value
    const formatDateTime = (dateTime) => {
        const date = new Date(dateTime);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        const milliseconds = String(date.getMilliseconds()).padStart(3, '0');
        return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}.${milliseconds}Z`;
    };


    const handleSubmit = async (e) => {
        e.preventDefault();
        console.log("Post ID at submit:", postId);
        const { title, description, shortDescription, file, keyVoucher, discount, startDate, endDate, status, imageUrl, voucherId } = formData;
        const token = getCookie('access_token');
        const userId = 1;

        if (!token) {
            setErrorMessage("Bạn cần đăng nhập để thực hiện thao tác này.");
            return;
        }
        console.log("Token:", token);
        if (!title || !description || !shortDescription) {
            setErrorMessage("Vui lòng không để trống bất kỳ trường thông tin nào.");
            return;
        }

        try {
            const postData = {
                postId,
                title,
                description,
                shortDescription,
                userId,
                url: imageUrl,
            };

            const postResponse = await axios.put(
                `${import.meta.env.VITE_API_BASE_URL}/post/update`,
                postData,
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    }
                }
            );

            console.log("Post updated successfully:", postResponse.data);

            let bannerUrl = "";
            if (file) {
                const imageFormData = new FormData();
                imageFormData.append('file', file);
                imageFormData.append('postId', postId);

                try {
                    const uploadResponse = await axios.post(
                        `${import.meta.env.VITE_API_BASE_URL}/image/post/upload`,
                        imageFormData,
                        {
                            headers: {
                                'Authorization': `Bearer ${token}`,
                                'Content-Type': 'multipart/form-data',
                            }
                        }
                    );
                    bannerUrl = uploadResponse.data.url;
                } catch (uploadError) {
                    console.error('Error uploading image:', uploadError);
                    setErrorMessage('Có lỗi xảy ra khi tải ảnh lên.');
                    return;
                }
            }

            if (bannerUrl) {
                postData.bannerUrl = bannerUrl;
                await axios.put(
                    `${import.meta.env.VITE_API_BASE_URL}/post/update`,
                    postData,
                    {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json',
                        }
                    }
                );
            }

            const formattedStartDate = formatDateTime(startDate);
            const formattedEndDate = formatDateTime(endDate);

            const voucherData = {
                voucherId,
                key: keyVoucher,
                discount,
                startDate: formattedStartDate,
                endDate: formattedEndDate,
                status,
                postId,
            };
            console.log("Updating voucher with data:", voucherData);
            let voucherResponse;
            try {
                voucherResponse = await axios.put(
                    `${import.meta.env.VITE_API_BASE_URL}/voucher/update`,
                    voucherData,
                    {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json',
                        }
                    }
                );
                console.log("Voucher updated successfully:", voucherResponse.data);
            } catch (error) {
                console.error("Error updating voucher:", error.response ? error.response.data : error.message);
            }

            setSuccessMessage("Bài đăng và voucher đã được cập nhật thành công!");
            setErrorMessage("");

            if (typeof onSave === 'function') {
                onSave({ ...postResponse.data, bannerUrl, voucherResponse: voucherResponse?.data });
            }

            setTimeout(() => {
                setSuccessMessage('');
                onClose();
            }, 2000);

        } catch (error) {
            console.error('Error updating post or voucher:', error);

            setErrorMessage(
                error.response?.data?.message || 'Đã xảy ra sự cố. Vui lòng thử lại sau.'
            );
        }
    };





    return (
        <div className="form-update-post-container">
            <div className="form-update-post-wrapper">
                <h2>Cập Nhật Bài Đăng và Voucher</h2>
                {errorMessage && <p className="form-update-post-error">{errorMessage}</p>}
                {successMessage && <p className="form-update-post-success">{successMessage}</p>}
                <form onSubmit={handleSubmit} className="form-update-post-columns">
                    <div className="form-update-post-column">
                        <h3>Thông tin Bài Đăng</h3>
                        <div className="form-update-post-group">
                            <label htmlFor="title">Tiêu đề</label>
                            <input
                                type="text"
                                id="title"
                                name="title"
                                value={formData.title}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-update-post-group">
                            <label htmlFor="description">Mô tả</label>
                            <textarea
                                id="description"
                                name="description"
                                value={formData.description}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-update-post-group">
                            <label htmlFor="shortDescription">Mô tả ngắn</label>
                            <input
                                type="text"
                                id="shortDescription"
                                name="shortDescription"
                                value={formData.shortDescription}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-update-post-group-image">
                            <label htmlFor="file">
                                {formData.imageUrl ? 'Cập nhật ảnh mới' : 'Ảnh minh họa'}
                            </label>
                            <div className="image-preview">
                                {formData.imageUrl && (
                                    <>
                                        <img src={formData.imageUrl} alt="Post Thumbnail" className="thumbnail" />
                                        <p>Ảnh hiện tại</p>
                                    </>
                                )}
                            </div>
                            <input
                                type="file"
                                id="file"
                                name="file"
                                accept="image/*"
                                onChange={handleInputChange}
                                style={{ display: 'block' }} // Luôn hiển thị ô input
                            />
                            {formData.fileName && !formData.imageUrl && (
                                <p>Đã chọn tệp: {formData.fileName}</p> // Hiển thị tên tệp đã chọn
                            )}


                        </div>


                        <div className="form-update-post-actions">
                            <button type="submit" className="form-update-post-submit">Cập nhật</button>
                            <button type="button" className="form-update-post-cancel" onClick={onClose}>Hủy</button>
                        </div>
                    </div>

                    <div className="form-update-post-column">
                        <h3>Thông tin Voucher</h3>
                        <div className="form-update-post-group">
                            <label htmlFor="keyVoucher">Mã Voucher</label>
                            <input
                                type="text"
                                id="keyVoucher"
                                name="keyVoucher"
                                value={formData.keyVoucher || ''}
                                onChange={handleInputChange}
                            />
                        </div>
                        <div className="form-update-post-group">
                            <label htmlFor="discount">Giảm giá</label>
                            <input
                                type="number"
                                id="discount"
                                name="discount"
                                value={formData.discount}
                                onChange={handleInputChange}
                            />
                        </div>
                        <div className="form-update-post-group">
                            <label htmlFor="startDate">Ngày bắt đầu</label>
                            <input
                                type="datetime-local"
                                id="startDate"
                                name="startDate"
                                value={formatDateTime(formData.startDate).slice(0, 16)} // Chỉ lấy yyyy-MM-ddTHH:mm
                                onChange={handleInputChange}
                            />
                        </div>
                        <div className="form-update-post-group">
                            <label htmlFor="endDate">Ngày kết thúc</label>
                            <input
                                type="datetime-local"
                                id="endDate"
                                name="endDate"
                                value={formatDateTime(formData.endDate).slice(0, 16)} // Chỉ lấy yyyy-MM-ddTHH:mm
                                onChange={handleInputChange}
                            />
                        </div>

                        <div className="form-update-post-group">
                            <label htmlFor="status">Trạng thái</label>
                            <select
                                id="status"
                                name="status"
                                value={formData.status}
                                onChange={handleInputChange}

                            >
                                <option value="ACTIVE">Kích hoạt</option>
                                <option value="INACTIVE">Không kích hoạt</option>
                            </select>
                        </div>

                    </div>
                </form>
            </div>
        </div>
    );
};

export default FormUpdatePost;
