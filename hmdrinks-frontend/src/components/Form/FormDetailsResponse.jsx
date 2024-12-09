import React from 'react';
import './FormDetailsResponse.css';

const FormDetailsResponse = ({ response, onClose }) => {
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${day}/${month}/${year}`;
    };

    return (
        <div className="form-details-response">
            <div className="form-header-details-response">
                <h2>Chi tiết Phản Hồi</h2>
                <button className="close-btn" onClick={onClose}>Đóng</button>
            </div>
            <div className="form-content-details-response">
                <div className="response-status-section">
                    <div className="response-status">
                        <span className={`status-${response.status.toLowerCase()}`}>
                            {response.status === 'WAITING' ? 'Đang chờ' :
                             response.status === 'COMPLETED' ? 'Đã duyệt' : 'Từ chối'}
                        </span>
                    </div>
                    <div className="response-dates">
                        <div>Ngày tạo phản hồi: {response.createDate ? formatDate(response.createDate) : 'N/A'}</div>                        
                    </div>
                </div>

                <div className="response-details-table">
                    <div className="detail-row">
                        <label>Mã phản hồi:</label>
                        <span>{response.contactId}</span>
                    </div>
                    <div className="detail-row">
                        <label>Mã người dùng:</label>
                        <span>{response.userId}</span>
                    </div>
                    <div className="detail-row">
                        <label>Nội dung:</label>
                        <span>{response.description}</span>
                    </div>
                    <div className="detail-row">
                        <label>Trạng thái:</label>
                        <span>{response.status === 'WAITING' ? 'Đang chờ' :
                               response.status === 'COMPLETED' ? 'Đã duyệt' : 'Từ chối'}</span>
                    </div>
                    <div className="detail-row">
                        <label>Phản hồi Admin:</label>
                        <span>{response.reply || 'Chưa có phản hồi'}</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FormDetailsResponse;
