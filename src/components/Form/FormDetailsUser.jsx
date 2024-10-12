import React from 'react';
import './FormDetailsUser.css';
import { assets } from "../../assets/assets.js";

const FormDetailsUser = ({ user, onClose }) => {
    return (
        <div className="form-details-user">
            <div className="form-header-details-user">
                <h2>Chi tiết Người Dùng</h2>
                <button className="close-btn" onClick={onClose}>Đóng</button>
            </div>
            <div className="form-content-details-user">
                <div className="avatar-section">
                    {user.avatar ? (
                        <img src={user.avatar} alt="Avatar" className="user-avatar" />
                    ) : (
                        <img src={assets.avtrang} alt="" className="user-avatar" />
                    )}

                    <div className="user-role">
                        <span>{user.role}</span>
                    </div>
                    <div className="user-dates">
                        <div>Ngày tạo: {user.dateCreated ? new Date(user.dateCreated).toLocaleDateString() : 'N/A'}</div>
                        <div>Ngày cập nhật: {user.dateUpdated ? new Date(user.dateUpdated).toLocaleDateString() : 'N/A'}</div>
                    </div>
                </div>

                <div className="user-details-table">
                    <div className="detail-row">
                        <label>User ID:</label>
                        <span>{user.userId}</span>
                    </div>
                    <div className="detail-row">
                        <label>Tên đăng nhập:</label>
                        <span>{user.userName}</span>
                    </div>
                    <div className="detail-row">
                        <label>Họ và tên:</label>
                        <span>{user.fullName}</span>
                    </div>
                    <div className="detail-row">
                        <label>Ngày sinh:</label>
                        <span>{user.birth_date ? new Date(user.birth_date).toLocaleDateString() : 'N/A'}</span>
                    </div>
                    <div className="detail-row">
                        <label>Địa chỉ:</label>
                        <span>{user.address}</span>
                    </div>
                    <div className="detail-row">
                        <label>Email:</label>
                        <span>{user.email}</span>
                    </div>
                    <div className="detail-row">
                        <label>Điện thoại:</label>
                        <span>{user.phone}</span>
                    </div>
                    <div className="detail-row">
                        <label>Giới tính:</label>
                        <span>{user.sex}</span>
                    </div>
                    <div className="detail-row">
                        <label>Trạng thái:</label>
                        <span>{user.isDelete ? 'Đã xóa' : 'Hoạt động'}</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FormDetailsUser;