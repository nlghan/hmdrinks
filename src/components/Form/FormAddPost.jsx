import React, { useState } from 'react';
import axios from 'axios';
import './FormAddPost.css';

// Function to retrieve token from cookies
const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);

    if (parts.length === 2) return parts.pop().split(';').shift();
};

const FormAddPost = ({ userId, onClose, onSubmit }) => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        shortDescription: '',
        file: null,
        keyVoucher: '', // Mã giảm giá
        discount: 0,
        startDate: '2024-11-12 07:01:29',
        endDate: '2024-11-12 08:01:29',
        status: 'ACTIVE', // Trạng thái voucher
    });

    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        if (name === 'file') {
            setFormData({ ...formData, [name]: e.target.files[0] });
        } else {
            setFormData({ ...formData, [name]: value });
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
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    };


    const handleSubmit = async (e) => {
        e.preventDefault();
        const { title, description, shortDescription, file, keyVoucher, discount, startDate, endDate, status } = formData;
        const token = getCookie('access_token');  // Ensure the token is fetched correctly

        if (!token) {
            setErrorMessage("Bạn cần đăng nhập để thêm bài đăng.");
            return;
        }

        if (!title || !description || !shortDescription) {
            setErrorMessage("Vui lòng không để trống bất kỳ trường thông tin nào.");
            return;
        }

        try {
            // 1. Create the post
            const postData = {
                url: '',
                description,
                title,
                shortDescription,
                userId,
            };

            console.log("Creating post with data:", postData);  // Log for debugging
            const postResponse = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/post/create`, postData, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            const postId = postResponse.data.postId;  // Get the postId from response
            console.log("Post created successfully with ID:", postId);  // Log ID of the created post

            // 2. Upload the image if there's any
            let imageUrl = '';
            if (file) {
                const imageFormData = new FormData();
                imageFormData.append('file', file);

                console.log("Uploading image with data:", imageFormData);  // Log for debugging
                const uploadResponse = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/image/post/upload?postId=${postId}`, imageFormData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'multipart/form-data',
                    },
                });

                imageUrl = uploadResponse.data.url;
                console.log("Image uploaded successfully with URL:", imageUrl);  // Log the uploaded image URL

                // Update the post with the uploaded image URL
                await axios.put(`${import.meta.env.VITE_API_BASE_URL}/post/update`, {
                    url: imageUrl,
                    description,
                    title,
                    shortDescription,
                    userId,
                    postId,
                }, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });
            }

            // 3. Create the voucher
            const formattedStartDate = formatDateTime(startDate);
            const formattedEndDate = formatDateTime(endDate);
            const voucherData = {
                keyVoucher, 
                discount,
                startDate: formattedStartDate,  // Use formatted startDate
                endDate: formattedEndDate,      // Use formatted endDate
                status,
                postId,  // Link the voucher to the post
            };

            console.log("Creating voucher with data:", voucherData);  // Log for debugging
            const voucherResponse = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/voucher/create`, voucherData, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            console.log("Voucher created successfully:", voucherResponse.data);  // Log voucher creation success

            // Set success message and clear error
            setSuccessMessage("Bài đăng và voucher đã được tạo thành công!");
            setErrorMessage("");
            onSubmit();
            setTimeout(() => {
                setSuccessMessage('');
                onClose();
            }, 2000);

        } catch (error) {
            console.error('Error creating post or voucher:', error);  // Log error for debugging

            if (error.response) {
                console.log("Response data:", error.response.data);  // Log response data for debugging
                console.log("Response status:", error.response.status);  // Log response status
                setErrorMessage(error.response.data.message || 'Đã xảy ra lỗi trong quá trình tạo.');
            } else {
                setErrorMessage('Đã xảy ra sự cố. Vui lòng thử lại sau.');
            }
        }
    };




    return (
        <div className="form-add-post-container">
            <div className="form-add-post-wrapper">
                <h2>Thêm Bài Đăng và Voucher</h2>
                {errorMessage && <p className="form-add-post-error">{errorMessage}</p>}
                {successMessage && <p className="form-add-post-success">{successMessage}</p>}
                <form onSubmit={handleSubmit} className="form-add-post-columns">
                    <div className="form-add-post-column">
                        <h3>Thông tin Bài Đăng</h3>
                        <div className="form-add-post-group">
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
                        <div className="form-add-post-group">
                            <label htmlFor="description">Mô tả</label>
                            <textarea
                                id="description"
                                name="description"
                                value={formData.description}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-add-post-group">
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
                        <div className="form-add-post-group">
                            <label htmlFor="file">Ảnh minh họa</label>
                            <input
                                type="file"
                                id="file"
                                name="file"
                                accept="image/*"
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                    </div>

                    <div className="form-add-post-column">
                        <h3>Thông tin Voucher</h3>
                        <div className="form-add-post-group">
                            <label htmlFor="keyVoucher">Mã Voucher</label>
                            <input
                                type="text"
                                id="keyVoucher"
                                name="keyVoucher"
                                value={formData.keyVoucher}
                                onChange={handleInputChange}
                            />
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="discount">Giảm giá</label>
                            <input
                                type="number"
                                id="discount"
                                name="discount"
                                value={formData.discount}
                                onChange={handleInputChange}
                            />
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="startDate">Ngày bắt đầu</label>
                            <input
                                type="datetime-local"
                                id="startDate"
                                name="startDate"
                                value={formData.startDate}
                                onChange={handleInputChange}
                            />
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="endDate">Ngày kết thúc</label>
                            <input
                                type="datetime-local"
                                id="endDate"
                                name="endDate"
                                value={formData.endDate}
                                onChange={handleInputChange}
                            />
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="status">Trạng thái</label>
                            <select
                                id="status"
                                name="status"
                                value={formData.status}
                                onChange={handleInputChange}
                            >
                                <option value="ACTIVE">ACTIVE</option>
                                <option value="INACTIVE">INACTIVE</option>
                            </select>
                        </div>
                    </div>

                   
                </form>
                <div className="form-add-post-actions">
                        <button type="submit">Thêm Bài Đăng và Voucher</button>
                        <button type="button" onClick={onClose}>Hủy</button>
                    </div>
            </div>
        </div>
    );
};

export default FormAddPost;
