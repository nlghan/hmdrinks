import React, { useState } from 'react';
import axios from 'axios';
import mammoth from 'mammoth';
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
        url: '',
        number: 0
    });
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isCreating, setIsCreating] = useState(false);
    const [hoveredButton, setHoveredButton] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleMouseEnter = (button) => {
        setHoveredButton(button);
    };

    const handleMouseLeave = () => {
        setHoveredButton(null);
    };

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
    const validateForm = () => {
        const { title, description, shortDescription, typePost, startDate, endDate, keyVoucher, discount, number } = formData;
    
        if (!title || !description || !shortDescription || !typePost || !startDate || !endDate || !keyVoucher || !discount || !number) {
            setErrorMessage('Tất cả các trường thông tin đều bắt buộc.');
            return false;
        }
    
        if (isNaN(discount) || discount <= 0) {
            setErrorMessage('Giảm giá phải là số tự nhiên lớn hơn 0.');
            return false;
        }
    
        if (isNaN(number) || number <= 0) {
            setErrorMessage('Số lượng phải là số tự nhiên lớn hơn 0.');
            return false;
        }
    
        const currentTime = new Date();
        const start = new Date(startDate);
        const end = new Date(endDate);
    
        if (start <= currentTime) {
            setErrorMessage('Ngày bắt đầu phải lớn hơn thời gian hiện tại.');
            return false;
        }
    
        if (end <= start) {
            setErrorMessage('Ngày kết thúc phải lớn hơn ngày bắt đầu.');
            return false;
        }
    
        setErrorMessage('');
        return true;
    };
    

    const handleFileChange = (e) => {
        setFormData((prevData) => ({
            ...prevData,
            file: e.target.files[0],
        }));
    };

    // Hàm để tải nội dung từ file Word và đặt vào description
    const handleWordFileChange = async (event) => {
        const file = event.target.files[0];
        if (file && file.name.endsWith('.docx')) {
            try {
                const arrayBuffer = await file.arrayBuffer();
                const result = await mammoth.extractRawText({ arrayBuffer });
                setFormData((prevData) => ({
                    ...prevData,
                    description: result.value,  // Cập nhật description với nội dung file Word
                }));
            } catch (error) {
                console.error("Lỗi khi tải file Word:", error);
            }
        } else {
            setErrorMessage('Vui lòng chọn file .docx hợp lệ');
        }
    };

    const handleSubmit = async () => {
        if (!validateForm()) {
            return;
        }
        const { title, description, shortDescription, typePost, startDate, endDate, keyVoucher, discount, status, file, number } = formData;

        try {
            setLoading(true);
            const token = getCookie('access_token');
            const headers = { Authorization: `Bearer ${token}` };

            setIsCreating(true);
            const postResponse = await axios.post(
                'http://localhost:1010/api/post/create',
                {
                    title,
                    description,
                    shortDescription,
                    typePost,
                    userId,
                    url: formData.url
                },
                { headers }
            );
            const postId = postResponse.data.postId;

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
                setFormData(prevData => ({ ...prevData, url: imageUrl }));
            }

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
                    number
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
            if (error.response && error.response.status === 409) {
                setErrorMessage('Bài đăng có voucher đã tồn tại.');
            } else {
                setErrorMessage('Đã xảy ra lỗi khi thêm bài đăng hoặc voucher.');
            }
            setSuccessMessage('');
        } finally {
            setLoading(false);
            setIsCreating(false);
        }
    };


    return (
        <div className="form-add-post-container">
            <div className="form-add-post-wrapper">
                <h2>Thêm Bài Đăng và Voucher</h2>
                {errorMessage && <p className="form-add-post-error" style={{color: 'red'}}>{errorMessage}</p>}
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
                            <label htmlFor="wordFile">Tải nội dung từ file Word</label>
                            <input type="file" id="wordFile" name="wordFile" accept=".docx" onChange={handleWordFileChange} />
                        </div>

                        <div className="form-add-post-group">
                            <label htmlFor="shortDescription">Mô tả ngắn</label>
                            <input type="text" id="shortDescription" name="shortDescription" value={formData.shortDescription} onChange={handleInputChange} />
                        </div>
                        <div className="form-add-post-group">
                            <label htmlFor="typePost">Loại bài đăng</label>
                            <select id="typePost" name="typePost" value={formData.typePost} onChange={handleInputChange}>
                                <option value="EVENT">Sự kiện</option>
                                <option value="NEW">Món mới</option>
                                <option value="DISCOUNT">Giảm giá</option>
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
                                <option value="ACTIVE">Kích hoạt</option>
                                <option value="INACTIVE">Không kích hoạt</option>
                            </select>
                        </div>
                    </div>
                </form>
                <div className="form-add-post-actions" style= {{justifyContent: "space-around"}}>
                    <button type="button" onClick={handleSubmit} disabled={loading} style={{
                        backgroundColor: hoveredButton === 'save' ? '#17d4a8' : '#00B087',
                        color: 'white',
                        transition: 'background-color 0.3s',
                        cursor: loading ? 'not-allowed' : 'pointer',
                        width: '150px'

                    }}
                        onMouseEnter={() => handleMouseEnter('save')}
                        onMouseLeave={handleMouseLeave}
                    >
                        {loading ? 'Đang thêm...' : 'Thêm'}
                    </button>
                    <button type="button" onClick={onClose} disabled={loading} style={{
                        backgroundColor: hoveredButton === 'cancel' ?  '#f03748': '#c73b48',
                        color: 'white',
                        transition: 'background-color 0.3s',
                        cursor: loading ? 'not-allowed' : 'pointer',
                        opacity: loading ? 0.4 : 1,
                        width: '150px'
                    }}
                        onMouseEnter={() => handleMouseEnter('cancel')}
                        onMouseLeave={handleMouseLeave}
                    >
                        {loading ? 'Hủy' : 'Hủy'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default FormAddPost;
