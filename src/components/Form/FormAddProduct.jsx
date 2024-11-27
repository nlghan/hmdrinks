import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './FormAddProduct.css'; // Ensure you have this CSS file
import { useNavigate } from 'react-router-dom';

const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
};

const FormAddProduct = ({ onClose }) => {
    const [formData, setFormData] = useState({
        cateId: '',
        proName: '',
        description: ''
    });

    const [variants, setVariants] = useState([{ size: '', price: '', stock: '' }]);
    const [categories, setCategories] = useState([]);
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [files, setFiles] = useState([]);
    const [loading, setLoading] = useState(false); // Loading state
    const [isCreating, setIsCreating] = useState(false); // Trạng thái khi tạo mới danh mục
    const navigate = useNavigate()


    const availableSizes = ['S', 'M', 'L'];

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const token = getCookie('access_token');
                const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/cate/list-category?page=1&limit=100`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (response.data) {
                    setCategories(response.data.categoryResponseList);
                }
            } catch (error) {
                console.error('Error fetching categories:', error);
                setErrorMessage('Không thể lấy danh sách danh mục.');
            }
        };

        fetchCategories();
    }, []);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleFileChange = (e) => {
        const selectedFiles = Array.from(e.target.files);
        setFiles(selectedFiles);
    };

    const handleVariantChange = (index, e) => {
        const { name, value } = e.target;
        const newVariants = [...variants];
        newVariants[index][name] = value;
        setVariants(newVariants);
    };

    const addVariant = () => {
        if (variants.length < 3) {
            setVariants([...variants, { size: '', price: '', stock: '' }]);
            setErrorMessage('');
        } else {
            setErrorMessage('Bạn chỉ có thể thêm tối đa 3 biến thể.');
        }
    };

    const deleteVariant = (index) => {
        if (variants.length > 1) {
            const newVariants = variants.filter((_, i) => i !== index);
            setVariants(newVariants);
            setErrorMessage('');
        }
    };

    const handleSubmit = async () => {
        const { cateId, proName, description } = formData;
    
        const token = getCookie('access_token');
        if (!token) {
            setErrorMessage("Bạn cần đăng nhập để thêm sản phẩm.");
            return;
        }
    
        if (!cateId || !proName || !description) {
            setErrorMessage("Vui lòng không để trống bất kỳ trường thông tin nào.");
            return;
        }
    
        // Kiểm tra giá và số lượng của các biến thể
        for (const variant of variants) {
            if (!variant.size || !variant.price || !variant.stock) {
                setErrorMessage("Vui lòng điền đầy đủ thông tin cho tất cả các biến thể.");
                return;
            }
    
            // Kiểm tra giá phải lớn hơn 1000 và không âm
            const price = parseInt(variant.price, 10);
            if (price <= 1000 || price < 0) {
                setErrorMessage("Giá sản phẩm phải lớn hơn 1000VND");
                return;
            }
    
            // Kiểm tra số lượng phải lớn hơn 0 và không âm
            const stock = parseInt(variant.stock, 10);
            if (stock <= 0) {
                setErrorMessage("Số lượng sản phẩm phải lớn hơn 0.");
                return;
            }
        }
    
        setIsSubmitting(true);
    
        try {
            setLoading(true);
            setIsCreating(true);  // Set loading to true when starting to submit
            const productResponse = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/product/create`, {
                cateId,
                proName,
                description
            }, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
    
            if (productResponse.data) {
                const proId = productResponse.data.proId;
    
                const imageFormData = new FormData();
                files.forEach(file => {
                    imageFormData.append('files', file);
                });
    
                await axios.post(`${import.meta.env.VITE_API_BASE_URL}/image/product-image/upload?proId=${proId}`, imageFormData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'multipart/form-data'
                    }
                });
    
                for (const variant of variants) {
                    if (variant.size && variant.price && variant.stock) {
                        await axios.post(`${import.meta.env.VITE_API_BASE_URL}/productVar/create`, {
                            proId,
                            size: variant.size,
                            price: parseInt(variant.price, 10),
                            stock: parseInt(variant.stock, 10)
                        }, {
                            headers: {
                                'Authorization': `Bearer ${token}`,
                                'Content-Type': 'application/json'
                            }
                        });
                    }
                }
    
                setErrorMessage("");
    
                setTimeout(() => {
                    setSuccessMessage("Thêm sản phẩm thành công!");
                    onClose();
                }, 1000);
            }
        } catch (error) {
            console.error('Error:', error);
            if (error.response) {
                if (error.response.status === 409) {
                    setErrorMessage("Sản phẩm cùng tên đã tồn tại. Vui lòng thử lại");
                } else {
                    setErrorMessage(error.response.data.message || 'Đã xảy ra lỗi khi thêm sản phẩm.');
                }
            }
        } finally {
            setLoading(false);
            setIsSubmitting(false);
            setIsCreating(false);  // Set loading to false after the response is received
        }
    };
    
    
    return (

        <div className="add-product-overlay">
            {isCreating && (
                <div className="loading-overlay active">
                    <div className="loading-spinner"></div>
                </div>
            )}

            <div className="form-add-product-container" onClick={(e) => e.stopPropagation()}>

                <div className="form-add-product">

                    <h2>Thêm sản phẩm</h2>
                    {errorMessage && <p className="error-message">{errorMessage}</p>}
                    {successMessage && <p className="success-message">{successMessage}</p>}


                    <div className="form-sections-add">
                        <div className="form-container-add">
                            <div className="product-info-section">
                                <h3>Thông tin sản phẩm</h3>
                                <form >
                                    <div className="form-group">
                                        <label htmlFor="cateId">Danh mục</label>
                                        <select
                                            id="cateId"
                                            name="cateId"
                                            value={formData.cateId}
                                            onChange={handleInputChange}
                                            required
                                        >
                                            <option value="">Chọn danh mục</option>
                                            {categories.map(category => (
                                                <option key={category.cateId} value={category.cateId}>
                                                    {category.cateName}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label htmlFor="proName">Tên sản phẩm</label>
                                        <input
                                            type="text"
                                            id="proName"
                                            name="proName"
                                            value={formData.proName}
                                            onChange={handleInputChange}
                                            required
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label htmlFor="proImg">Hình ảnh sản phẩm</label>
                                        <input
                                            type="file"
                                            id="proImg"
                                            name="proImg"
                                            accept="image/*"
                                            multiple
                                            onChange={handleFileChange}
                                            required
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label htmlFor="description">Mô tả</label>
                                        <textarea
                                            id="description"
                                            name="description"
                                            value={formData.description}
                                            onChange={handleInputChange}
                                            required
                                        />
                                    </div>
                                </form>
                            </div>

                            <div className="variants-section">
                                <h3 style={{ display: 'inline-block', marginRight: '10px' }}>Thông tin biến thể sản phẩm <span><button
                                    type="button"
                                    onClick={addVariant}
                                    className="add-variant-button"
                                    style={{ display: 'inline', fontSize: '24px', lineHeight: '24px', borderRadius:'100%', backgroundColor:'white', color:'green' }}
                                >
                                    +
                                </button></span></h3>

                                <div>
                                    {variants.map((variant, index) => (
                                        <div key={index} className={`variant-group ${index === 0 ? 'first-variant' : ''}`}>
                                            <div className="form-group">
                                                <label htmlFor={`size-${index}`}>Size</label>
                                                <select
                                                    id={`size-${index}`}
                                                    name="size"
                                                    value={variant.size}
                                                    onChange={(e) => handleVariantChange(index, e)}
                                                    required
                                                >
                                                    <option value="">Chọn size</option>
                                                    {availableSizes.map(size => (
                                                        <option key={size} value={size} disabled={variants.some(v => v.size === size && v !== variant)}>
                                                            {size}
                                                        </option>
                                                    ))}
                                                </select>
                                            </div>
                                            <div className="form-group">
                                                <label htmlFor={`price-${index}`}>Giá</label>
                                                <input
                                                    type="number"
                                                    id={`price-${index}`}
                                                    name="price"
                                                    value={variant.price}
                                                    onChange={(e) => handleVariantChange(index, e)}
                                                    required
                                                />
                                            </div>
                                            <div className="form-group">
                                                <label htmlFor={`stock-${index}`}>Số lượng</label>
                                                <input
                                                    type="number"
                                                    id={`stock-${index}`}
                                                    name="stock"
                                                    value={variant.stock}
                                                    onChange={(e) => handleVariantChange(index, e)}
                                                    required
                                                />
                                            </div>
                                            {index !== 0 && (
                                                <button
                                                    type="button"
                                                    className="delete-variant-button"
                                                    onClick={() => deleteVariant(index)}
                                                >
                                                    X
                                                </button>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>

                        <div className="add-pro-form-actions">
                            <button type="button" id="add-submit-btn" onClick={handleSubmit} disabled={loading}>
                                {loading ? 'Đang thêm...' : 'Thêm'}
                            </button>
                            <button type="button" id="add-pro-cancel-btn" style={{
                                cursor: loading ? 'not-allowed' : 'pointer',
                                opacity: loading ? 0.4 : 1,
                            }} onClick={onClose} disabled={loading} >
                                {loading ? 'Hủy' : 'Hủy'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FormAddProduct;
