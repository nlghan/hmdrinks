import React from 'react';
import './FormDetailsProduct.css';

const FormDetailsProduct = ({ product, onClose }) => {
    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    };

    return (
        <div className="fdp-overlay">
            <div className="fdp-modal">
                <div className="fdp-header">
                    <h2>Chi tiết sản phẩm</h2>
                    <button
                        onClick={onClose}
                        style={{
                            height: '35px',
                            width: '35px',
                            borderRadius: '50%',
                            backgroundColor: '#e8e6e6',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            border: 'none'
                        }}
                        className="fdp-close"
                    >
                        <i className="ti-close" style={{ color: '#f21b1b', fontSize: '18px' }}></i>
                    </button>
                </div>
                <div className="fdp-content">
                    <div className="fdp-info">
                        <div className="fdp-row">
                            <label>Số thứ tự:</label>
                            <span>{product.proId}</span>
                        </div>
                        <div className="fdp-row">
                            <label>Tên sản phẩm:</label>
                            <span>{product.proName}</span>
                        </div>
                        <div className="fdp-row">
                            <label>Mô tả:</label>
                            <p className="fdp-description">{product.description}</p>
                        </div>
                        <div className="fdp-row">
                            <label>Trạng thái:</label>
                            <span className={`fdp-status ${product.deleted ? 'inactive' : 'active'}`}>
                                {product.deleted ? 'Không hoạt động' : 'Đang hoạt động'}
                            </span>
                        </div>
                    </div>

                    <div className="fdp-images">
                        <label>Hình ảnh sản phẩm:</label>
                        <div className="fdp-image-grid">
                            {product.images && product.images.map((image, index) => (
                                <img
                                    key={image.id || index} // Sử dụng ID từ API hoặc index nếu không có ID
                                    src={image.linkImage} // Truy cập vào linkImage từ API
                                    alt={`${product.proName} ${index + 1}`}
                                    className="fdp-image"
                                    loading="lazy"
                                />
                            ))}
                        </div>
                    </div>


                    <div className="fdp-variants">
                        <label>Các biến thể:</label>
                        <div className="fdp-variants-list">
                            {product.variants && product.variants.map((variant) => (
                                <div key={variant.varId} className="fdp-variant-item">
                                    <span className="fdp-size">Size: {variant.size}</span>
                                    <span className="fdp-price">Giá: {formatPrice(variant.price)}</span>
                                    <span className="fdp-stock">Tồn kho: {variant.stock}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FormDetailsProduct;
