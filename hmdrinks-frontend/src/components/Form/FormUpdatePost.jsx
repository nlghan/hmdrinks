import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './FormUpdatePost.css';
import { formatISO } from 'date-fns';
import mammoth from 'mammoth';

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
        startDate: '2024-12-10 10:23:49',
        endDate: '2024-12-31 10:23:49',
        status: 'ACTIVE',
        typePost: 'NEW',
        url: '',  // Add url field here
        voucherId: null,
        number: 0
    });

    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isCreating, setIsCreating] = useState(false);
    const [loading, setLoading] = useState(false);


    const authToken = getCookie('access_token'); // Replace with your actual token retrieval logic
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

    // Fetch post data based on the provided postId
    useEffect(() => {
        const fetchPostData = async () => {
            try {
                const postResponse = await axios.get(`http://localhost:1010/api/post/view/${postId}`, {
                    headers: {
                        'Authorization': `Bearer ${authToken}`,
                        'accept': '*/*',
                    },
                });

                if (postResponse.status === 200) {
                    const postData = postResponse.data.body;
                    setFormData((prev) => ({
                        ...prev,
                        title: postData.title,
                        description: postData.description,
                        shortDescription: postData.shortDescription,
                        typePost: postData.typePost,
                        imageUrl: postData.url,
                    }));

                    // Fetch voucher data list and match with postId
                    const voucherListResponse = await axios.get('http://localhost:1010/api/voucher/view/all', {
                        headers: {
                            'Authorization': `Bearer ${authToken}`,
                            'accept': '*/*',
                        },
                    });

                    if (voucherListResponse.status === 200) {
                        const matchingVoucher = voucherListResponse.data.body.voucherResponseList.find(
                            (voucher) => voucher.postId === postId
                        );

                        if (matchingVoucher) {
                            // Fetch specific voucher details
                            const voucherResponse = await axios.get(
                                `http://localhost:1010/api/voucher/view/${matchingVoucher.voucherId}`, {
                                headers: {
                                    'Authorization': `Bearer ${authToken}`,
                                    'accept': '*/*',
                                },
                            }
                            );

                            if (voucherResponse.status === 200) {
                                const { voucherId, key, discount, number, startDate, endDate, status } = voucherResponse.data.body;
                                setFormData((prev) => ({
                                    ...prev,
                                    keyVoucher: key,
                                    discount: discount,
                                    startDate: formatDateTime(startDate),
                                    endDate: formatDateTime(endDate),
                                    status: status,
                                    number: number,
                                    voucherId: voucherId,
                                }));
                            }
                        }

                    }
                }
            } catch (error) {
                setErrorMessage('Lỗi khi tải dữ liệu bài đăng hoặc voucher');
                console.error('Error fetching post/voucher data:', error);
            }
        };

        fetchPostData();
    }, [postId, authToken]);

    // Update form fields as users type
    const handleInputChange = (event) => {
        const { name, value } = event.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: value,
        }));
    };

    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Không thể giải mã token:", error);
            return null;
        }
    };

    const userId = getUserIdFromToken(authToken);
    const validateNotEmpty = (formData) => {
        for (const key in formData) {
            if (formData[key] === "" || formData[key] === null || formData[key] === undefined) {
                return `Không được để trống thông tin.`;
            }
        }
        return null;
    };
    const validatePositiveNumber = (value, fieldName) => {
        if (isNaN(value) || value <= 0 || !Number.isInteger(Number(value))) {
            return `Trường ${fieldName} phải là số tự nhiên lớn hơn 0.`;
        }
        return null;
    };
    const validateDates = (startDate, endDate) => {
        const now = new Date();
        const start = new Date(startDate);
        const end = new Date(endDate);

        if (start <= now) {
            return 'Thời gian bắt đầu phải lớn hơn thời gian hiện tại.';
        }
        if (end <= start) {
            return 'Thời gian kết thúc phải lớn hơn thời gian bắt đầu.';
        }
        return null;
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
    
        if (end <= start) {
            setErrorMessage('Ngày kết thúc phải lớn hơn ngày bắt đầu.');
            return false;
        }
    
        setErrorMessage('');
        return true;
    };
    

    const handleSubmit = async (event) => {
        event.preventDefault();
        const token = getCookie('access_token'); // Ensure the token is retrieved here if not already available
        if (!validateForm()) {
            return;
        }
        try {
            setLoading(true);
            setIsCreating(true);
            let imageUrl = formData.imageUrl;

            // Check if a new file is selected for upload
            if (formData.file) {
                const formDataImage = new FormData();
                formDataImage.append('file', formData.file);

                // Wait for the image upload to complete before proceeding
                const imageUploadResponse = await axios.post(
                    `http://localhost:1010/api/image/post/upload?postId=${postId}`,
                    formDataImage,
                    {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'accept': '*/*',
                            'Content-Type': 'multipart/form-data',
                        },
                    }
                );

                if (imageUploadResponse.status === 200) {
                    imageUrl = imageUploadResponse.data.url; // Update the image URL if upload is successful
                } else {
                    setErrorMessage('Lỗi khi tải ảnh lên');
                    return;
                }
            }

            // Prepare updated post data
            const updatedPostData = {
                postId,                // Post ID
                userId,                // Replace with actual user ID
                title: formData.title,
                description: formData.description,
                shortDescription: formData.shortDescription,
                typePost: formData.typePost,
                url: imageUrl,         // Use the updated image URL
            };

            // Check if the status of voucher is "inactive" or "EXPIRED"
            if (formData.status === "INACTIVE" || formData.status === "EXPIRED") {
                // Disable voucher first by calling the API
                const disableVoucherResponse = await axios.put(
                    'http://localhost:1010/api/voucher/disable',
                    { id: formData.voucherId }, // Send voucher ID to disable
                    {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'accept': '*/*',
                            'Content-Type': 'application/json',
                        },
                    }
                );

                if (disableVoucherResponse.status !== 200) {
                    setErrorMessage('Có lỗi xảy ra khi vô hiệu hóa voucher');
                    return; // Exit the function if the voucher disable call fails
                }
            }

            // Update post information using the correct endpoint
            const postUpdateResponse = await axios.put(
                'http://localhost:1010/api/post/update',
                updatedPostData,
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'accept': '*/*',
                        'Content-Type': 'application/json',
                    },
                }
            );

            if (postUpdateResponse.status === 200) {
                setSuccessMessage('Bài đăng đã được cập nhật thành công!');
                onSave(updatedPostData); // Call onSave with the updated post data

                // Prepare voucher update data from formData
                const voucherUpdateData = {
                    voucherId: formData.voucherId, // Use voucherId from formData
                    key: formData.keyVoucher,       // Use key from formData
                    startDate: formatDateTime(formData.startDate), // Format startDate
                    endDate: formatDateTime(formData.endDate),     // Format endDate
                    discount: formData.discount,
                    number: formData.number,        // Use discount from formData
                    status: formData.status,        // Use status from formData
                    postId: postId,                 // Use the same postId
                };

                console.log('Data for voucher update:', voucherUpdateData);

                // Attempt to update the voucher
                try {
                    const voucherUpdateResponse = await axios.put(
                        'http://localhost:1010/api/voucher/update',
                        voucherUpdateData,
                        {
                            headers: {
                                'Authorization': `Bearer ${token}`,
                                'accept': '*/*',
                                'Content-Type': 'application/json',
                            },
                        }
                    );

                    if (voucherUpdateResponse.status === 200) {
                        setSuccessMessage('Voucher đã được cập nhật thành công!');
                        setTimeout(() => {
                            if (onClose) onClose(); // Close the form after successful update
                        }, 1000);
                    }
                } catch (updateError) {
                    // Check if the error is a 404 (not found)
                    if (updateError.response && updateError.response.status === 404) {
                        try {
                            // Create a new voucher since the update failed
                            const voucherCreateData = {
                                startDate: formatDateTime(formData.startDate), // Format startDate
                                endDate: formatDateTime(formData.endDate),
                                keyVoucher: formData.keyVoucher,
                                discount: formData.discount,
                                status: formData.status,
                                postId: postId,
                            };

                            console.log('Creating new voucher with data:', voucherCreateData);

                            const voucherCreateResponse = await axios.post(
                                'http://localhost:1010/api/voucher/create',
                                voucherCreateData,
                                {
                                    headers: {
                                        'Authorization': `Bearer ${token}`,
                                        'accept': '*/*',
                                        'Content-Type': 'application/json',
                                    },
                                }
                            );

                            if (voucherCreateResponse.status === 200) {
                                const newVoucherId = voucherCreateResponse.data.voucherId; // Capture the new voucher ID
                                console.log('New voucher created successfully with ID:', newVoucherId);

                                // Update the voucher with the new voucherId
                                const updateVoucherData = {
                                    voucherId: newVoucherId,
                                    startDate: formatDateTime(formData.startDate),
                                    endDate: formatDateTime(formData.endDate),
                                    keyVoucher: formData.keyVoucher,
                                    discount: formData.discount,
                                    status: formData.status,
                                    postId: postId,
                                };

                                const updateVoucherResponse = await axios.put(
                                    `http://localhost:1010/api/voucher/update/${newVoucherId}`,
                                    updateVoucherData,
                                    {
                                        headers: {
                                            'Authorization': `Bearer ${token}`,
                                            'Content-Type': 'application/json',
                                        },
                                    }
                                );

                                if (updateVoucherResponse.status === 200) {
                                    setSuccessMessage('Voucher đã được cập nhật thành công!');
                                    setTimeout(() => {
                                        if (onClose) onClose(); // Close the form after successful update
                                    }, 1000);
                                } else {
                                    setErrorMessage('Có lỗi xảy ra khi cập nhật voucher mới.');
                                }
                            } else {
                                setErrorMessage('Có lỗi xảy ra khi tạo voucher mới');
                            }
                        } catch (creationError) {
                            console.error('Error creating new voucher:', creationError);
                            setErrorMessage('Có lỗi xảy ra khi tạo voucher mới.');
                        }
                    } else {
                        setTimeout(() => {
                            if (onClose) onClose(); // Close the form after successful update
                        }, 1000);
                        // setErrorMessage('Đã xảy ra sự cố. Vui lòng thử lại sau.');
                    }
                }
            } else {
                setErrorMessage('Có lỗi xảy ra khi cập nhật bài đăng');
            }
        } catch (error) {
            // setErrorMessage('Có lỗi xảy ra khi cập nhật bài đăng hoặc voucher');
            console.error('Error updating post/voucher:', error);
        } finally {
            setLoading(false);
            setIsCreating(false);
        }
    };

    // Khi bạn cập nhật số lượng
    const updateQuantity = (newQuantity) => {
        setFormData((prevData) => ({
            ...prevData,
            number: newQuantity, // Cập nhật số lượng mới
        }));
    };

    return (
        <div className="form-update-post-container">
            {isCreating && (
                <div className="loading-overlay active">
                    <div className="loading-spinner"></div>
                </div>
            )}
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

                            <label htmlFor="wordFile">Tải nội dung từ file Word</label>
                            <input type="file" id="wordFile" name="wordFile" accept=".docx" onChange={handleWordFileChange} />

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
                        <div className="form-update-post-group">
                            <label htmlFor="typePost">Loại bài đăng</label>
                            <select id="typePost" name="typePost" value={formData.typePost} onChange={handleInputChange}>
                                <option value="EVENT">Sự kiện</option>
                                <option value="NEW">Món mới</option>
                                <option value="DISCOUNT">Giảm giá</option>
                            </select>
                        </div>
                        <div className="form-update-post-group-image">
                            <label htmlFor="file">
                                {formData.imageUrl ? 'Cập nhật ảnh mới' : 'Ảnh minh họa'}
                            </label>
                            <div className="image-preview">
                                {formData.imageUrl && (
                                    <>
                                        <img src={formData.imageUrl} alt="Post Thumbnail" id="thumbnail" />
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
                            <button type="submit" className="form-update-post-submit" disabled={loading} style={{
                                cursor: loading ? 'not-allowed' : 'pointer',
                                width: '170px',
                                backgroundColor: '#00B087',
                                position: 'relative',
                                left: '50px',
                                marginLeft: '100px'

                            }}
                                onMouseOver={(e) => (e.target.style.backgroundColor = '#17d4a8')} // Màu hover
                                onMouseOut={(e) => (e.target.style.backgroundColor = '#00B087')} // Trả về màu cũ
                            >
                                {loading ? 'Đang cập nhật...' : 'Cập nhật'}</button>
                            <button type="button" className="form-update-post-cancel" disabled={loading} onClick={onClose} style={{
                                cursor: loading ? 'not-allowed' : 'pointer',
                                width: '170px',
                                position: 'relative',
                                right: '-250px',
                                backgroundColor: '#c73b48',

                            }}
                                onMouseOver={(e) => (e.target.style.backgroundColor = '#f03748')} // Màu hover
                                onMouseOut={(e) => (e.target.style.backgroundColor = '#c73b48')}
                            >
                                {loading ? 'Hủy' : 'Hủy'}</button>
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
                            <label htmlFor="number">Số lượng</label>
                            <input
                                type="number"
                                id="number"
                                name="number"
                                value={formData.number}
                                onChange={handleInputChange}
                            />
                        </div>
                        <div className="form-update-post-group">
                            <label htmlFor="startDate">Ngày bắt đầu</label>
                            <input
                                type="datetime-local"
                                id="startDate"
                                name="startDate"
                                value={formData.startDate}
                                onChange={handleInputChange}
                            />
                        </div>
                        <div className="form-update-post-group">
                            <label htmlFor="endDate">Ngày kết thúc</label>
                            <input
                                type="datetime-local"
                                id="endDate"
                                name="endDate"
                                value={formData.endDate}
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
