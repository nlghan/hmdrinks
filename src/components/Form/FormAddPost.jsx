import React, { useState } from 'react';
import axios from 'axios';
import './FormAddPost.css';

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
        keyVoucher: '',
        discount: 0,
        startDate: '2024-12-10 10:23:49',
        endDate: '2024-12-31 10:23:49',
        status: 'ACTIVE',
        typePost: 'NEW',
        url: '',  // Add url field here
        number:0
    });
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isCreating, setIsCreating] = useState(false);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: name === 'startDate' || name === 'endDate' ? formatDateTime(value) : value,
        }));
    };

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

    const handleFileChange = (e) => {
        setFormData((prevData) => ({
            ...prevData,
            file: e.target.files[0],
        }));
    };

    const handleSubmit = async () => {
        const { title, description, shortDescription, typePost, startDate, endDate, keyVoucher, discount, status, file, number } = formData;
    
        try {
            const token = getCookie('access_token');
            const headers = { Authorization: `Bearer ${token}` };
    
            // Step 1: Create Post
            setIsCreating(true);
            const postResponse = await axios.post(
                'http://localhost:1010/api/post/create',
                {
                    title,
                    description,
                    shortDescription,
                    typePost,
                    userId,
                    url: formData.url  // Include url in post data
                },
                { headers }
            );
            const postId = postResponse.data.postId;
    
            // Step 2: Upload Image for Post, retrieve URL
            let imageUrl = null;
            if (file) {
                const imageData = new FormData();
                imageData.append('file', file);
    
                const imageResponse = await axios.post(
                    `http://localhost:1010/api/image/post/upload?postId=${postId}`,
                    imageData,
                    {
                        headers: {
                            ...headers,
                            'Content-Type': 'multipart/form-data'
                        }
                    }
                );
    
                imageUrl = imageResponse.data.imageUrl;
                setFormData(prevData => ({ ...prevData, url: imageUrl })); // Update url
            }
    
            // Step 3: Create Voucher with number field added
            const formattedStartDate = formatDateTime(startDate);
            const formattedEndDate = formatDateTime(endDate);
    
            await axios.post(
                'http://localhost:1010/api/voucher/create',
                {
                    keyVoucher,
                    discount,
                    startDate: formattedStartDate,
                    endDate: formattedEndDate,
                    status,
                    postId: postId,
                    number // Add the missing `number` field here
                },
                { headers }
            );
    
            setSuccessMessage('Tạo bài đăng và voucher thành công!');
            setErrorMessage('');
            setTimeout(() => {
                onSubmit();
                if (onClose) onClose();
            }, 1000);
        } catch (error) {
            setIsCreating(false);
            setErrorMessage('Đã xảy ra lỗi khi thêm bài đăng hoặc voucher.');
            setSuccessMessage('');
        }finally{
            setIsCreating(false);
        }
    };
    

    return (
        <div className="form-add-post-container">
            {isCreating && (
                <div className="loading-overlay active">
                    <div className="loading-spinner"></div>
                </div>
            )}
            <div className="form-add-post-wrapper">          
                <h2>Thêm Bài Đăng và Voucher</h2>
                {errorMessage && <p className="form-add-post-error">{errorMessage}</p>}
                {successMessage && <p className="form-add-post-success">{successMessage}</p>}
                <form className="form-add-post-columns" onSubmit={(e) => e.preventDefault()}>
                    <div className="form-add-post-column">
                        <h3>Thông tin Bài Đăng</h3>
                        <div className="form-add-post-group">
                            <label htmlFor="title">Tiêu đề</label>
                            <input type="text" id="title" name="title" value={formData.title} onChange={handleInputChange} />
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="description">Mô tả</label>
                            <textarea id="description" name="description" value={formData.description} onChange={handleInputChange} />
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="shortDescription">Mô tả ngắn</label>
                            <input type="text" id="shortDescription" name="shortDescription" value={formData.shortDescription} onChange={handleInputChange} />
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="typePost">Loại bài đăng</label>
                            <select id="typePost" name="typePost" value={formData.typePost} onChange={handleInputChange}>
                                <option value="EVENT">EVENT</option>
                                <option value="NEW">NEW</option>
                                <option value="DISCOUNT">DISCOUNT</option>
                            </select>
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="file">Ảnh minh họa</label>
                            <input type="file" id="file" name="file" accept="image/*" onChange={handleFileChange} />
                        </div>
                    </div>

                    <div className="form-add-post-column">
                        <h3>Thông tin Voucher</h3>
                        <div className="form-add-post-group">
                            <label htmlFor="keyVoucher">Mã Voucher</label>
                            <input type="text" id="keyVoucher" name="keyVoucher" value={formData.keyVoucher} onChange={handleInputChange} />
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="discount">Giảm giá</label>
                            <input type="number" id="discount" name="discount" value={formData.discount} onChange={handleInputChange} />
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="number">Số lượng</label>
                            <input type="number" id="number" name="number" value={formData.number} onChange={handleInputChange} />
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
                            <select id="status" name="status" value={formData.status} onChange={handleInputChange}>
                                <option value="ACTIVE">ACTIVE</option>
                                <option value="INACTIVE">INACTIVE</option>
                            </select>
                        </div>
                    </div>
                </form>
                <div className="form-add-post-actions">
                    <button type="button" onClick={handleSubmit} >Lưu</button>
                    <button type="button" onClick={onClose}>Hủy</button>
                </div>
            </div>
        </div>
    );
};

export default FormAddPost;
