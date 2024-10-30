import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './FormUpdatePost.css';
import { formatISO } from 'date-fns';

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
        voucherId: null,
    });

    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const authToken = getCookie('access_token'); // Replace with your actual token retrieval logic

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
                                const { voucherId, key, discount, startDate, endDate, status } = voucherResponse.data.body;
                                setFormData((prev) => ({
                                    ...prev,
                                    keyVoucher: key,
                                    discount: discount,
                                    startDate: startDate,
                                    endDate: endDate,
                                    status: status,
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
        const { name, value, files } = event.target;
        setFormData((prev) => ({
            ...prev,
            [name]: files ? files[0] : value,
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


    const handleSubmit = async (event) => {
        event.preventDefault();
        const token = getCookie('access_token'); // Ensure the token is retrieved here if not already available

        try {
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
                url: imageUrl,         // Use the updated image URL
            };

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
                    startDate: formatDate(formData.startDate), // Format startDate
                    endDate: formatDate(formData.endDate),     // Format endDate
                    discount: formData.discount,     // Use discount from formData
                    status: formData.status,         // Use status from formData
                    postId: postId,                  // Use the same postId
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
                                    startDate: formatDate(formData.startDate),
                                    endDate: formatDate(formData.endDate),
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
                        setErrorMessage('Đã xảy ra sự cố. Vui lòng thử lại sau.');
                    }
                

                }
            } else {
                setErrorMessage('Có lỗi xảy ra khi cập nhật bài đăng');
            }
        } catch (error) {
            setErrorMessage('Có lỗi xảy ra khi cập nhật bài đăng hoặc voucher');
            console.error('Error updating post/voucher:', error);
        }
    };


    // Hàm định dạng ngày tháng theo yêu cầu
    const formatDate = (date) => {
        // Create a new Date object assuming the input is in local time
        const localDate = new Date(date);

        // Get the components of the date as local time
        const year = localDate.getFullYear(); // Get local year
        const month = String(localDate.getMonth() + 1).padStart(2, '0'); // Month is 0-indexed
        const day = String(localDate.getDate()).padStart(2, '0');
        const hours = String(localDate.getHours()).padStart(2, '0');
        const minutes = String(localDate.getMinutes()).padStart(2, '0');

        // Return the formatted date as a string in the 'YYYY-MM-DDTHH:MM' format
        return `${year}-${month}-${day}T${hours}:${minutes}`; // Format as 'YYYY-MM-DDTHH:MM'
    };





    // Helper function to format the datetime-local value
    const formatDateTime = (dateTime) => {
        const date = new Date(dateTime);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${year}-${month}-${day}T${hours}:${minutes}`;
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
