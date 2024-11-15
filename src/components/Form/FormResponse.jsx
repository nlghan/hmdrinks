import React, { useState } from 'react';
import axios from 'axios';
import './FormResponse.css';

function FormResponse({ response, onClose }) {
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [content, setContent] = useState(response?.content || '');
    const [loading, setLoading] = useState(false);

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
    
        // Kiểm tra nếu nội dung bị trống
        if (!content.trim()) {
            setError('Nội dung phản hồi không được để trống.');
            return;
        }
    
        const req = {
            contactId: response.contactId,
            content: content.trim(),
        };
    
        try {
            setLoading(true);
            const token = getCookie('access_token');
            if (!token) {
                setError('Bạn chưa đăng nhập. Vui lòng đăng nhập để tiếp tục.');
                return;
            }
    
            console.log('Token:', token);
            console.log('Request Payload:', req);
    
            const apiResponse = await axios.put(
                'http://localhost:1010/api/contact/contact/response',
                req,
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                }
            );
    
            console.log('API Response:', apiResponse);
            setMessage('Phản hồi đã được gửi thành công!');
            setError('');
            setContent(''); // Reset nội dung sau khi gửi
            setTimeout(() => onClose(), 1000); // Đóng form sau 2 giây
        } catch (error) {
            console.error('Error responding to contact:', error);
            if (error.response) {
                setError(
                    error.response.data?.message || 'Có lỗi xảy ra khi gửi phản hồi.'
                );
            } else {
                setError('Không thể kết nối đến server. Vui lòng thử lại.');
            }
            setMessage('');
        } finally {
            setLoading(false);
        }
    };
    

    return (
        <div className="form-response">
            <div className="form-header-response">
                <h2>Phản hồi cho người dùng {response.userId}</h2>
                <button className="close-btn" onClick={onClose}>Đóng</button>
            </div>
            
            <div className="response-info">
                <div className="response-description">
                    <strong>Nội dung thư:</strong>
                    <p>{response.description}</p>
                </div>
                <div className="response-date">
                    <strong>Ngày gửi:</strong>
                    <p>{new Date(response.createDate).toLocaleDateString()}</p>
                </div>
            </div>

            <form onSubmit={handleSubmit}>
                <div>
                    <label>Nội dung phản hồi:</label>
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" disabled={loading}>Gửi phản hồi</button>
                {loading && (
                    <div className="form-response-loading">
                        <div className="form-response-loading-spinner"></div>
                    </div>
                )}
                {message && <div className="success-message">{message}</div>}
                {error && <div className="error-message">{error}</div>}
            </form>
        </div>
    );
}

export default FormResponse; 