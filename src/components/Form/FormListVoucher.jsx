import React from 'react';
import './FormListVoucher.css';

const FormListVoucher = ({ vouchers, onClose }) => {
    return (
        <div className="voucher-overlay">
            <div className="voucher-overlay-content">
                <h3 id='h3-list-voucher'><div >Danh sách Voucher</div> <span><button className="voucher-close-btn" onClick={onClose}>Đóng</button></span></h3>
                {/* Sử dụng bảng để hiển thị danh sách voucher */}
                <table className="voucher-table">
                    <thead>
                        <tr>
                            <th>Mã voucher</th>
                            <th>Tên voucher</th>
                            <th>Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        {vouchers && vouchers.length > 0 ? (
                            vouchers.map((voucher, index) => (
                                <tr key={index}>
                                    <td>{voucher.voucherId}</td>
                                    <td>{voucher.key}</td>
                                    <td>{voucher.status}</td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="2">Không có voucher nào</td>
                            </tr>
                        )}
                    </tbody>
                </table>
                
            </div>
        </div>
    );
};

export default FormListVoucher;
