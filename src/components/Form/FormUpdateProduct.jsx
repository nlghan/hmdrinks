import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Cookies from 'js-cookie';
import './FormUpdateProduct.css'; // Ensure this CSS file exists and is properly styled

const FormUpdateProduct = ({ product, onClose, onUpdate }) => {
    const [proId, setProId] = useState(product.proId);
    const [cateId, setCateId] = useState(product.cateId);
    const [proName, setProName] = useState(product.proName);
    const [proImg, setProImg] = useState(product.proImg || '');
    const [description, setDescription] = useState(product.description);

    const [priceS, setPriceS] = useState('');
    const [stockS, setStockS] = useState('');
    const [priceM, setPriceM] = useState('');
    const [stockM, setStockM] = useState('');
    const [priceL, setPriceL] = useState('');
    const [stockL, setStockL] = useState('');

    // State variables for variant IDs
    const [varIdS, setVarIdS] = useState(null);
    const [varIdM, setVarIdM] = useState(null);
    const [varIdL, setVarIdL] = useState(null);

    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const [availableCategories, setAvailableCategories] = useState([]);
    const [selectedCategoryName, setSelectedCategoryName] = useState('');
    
    // State to hold the highest varId
    const [highestVarId, setHighestVarId] = useState(0);

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`); 
        return parts.length === 2 ? parts.pop().split(';').shift() : null;
    };

    // Fetch available categories for the dropdown
    useEffect(() => {
        const fetchAvailableCategories = async () => {
            try {
                const token = getCookie('access_token');
                if (!token) {
                    console.warn("No access token found. Categories won't be loaded.");
                    return;
                }

                const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/cate/list-category?page=1&limit=100`;
                const response = await axios.get(apiUrl, {
                    headers: {
                        'Accept': '*/*',
                        'Authorization': `Bearer ${token}`,
                    },
                });

                if (response.data && response.data.categoryResponseList) {
                    setAvailableCategories(response.data.categoryResponseList);
                    const category = response.data.categoryResponseList.find(cat => cat.cateId === product.cateId);
                    if (category) {
                        setSelectedCategoryName(category.cateName);
                    }
                } else {
                    console.warn("No categories found in the response.");
                }
            } catch (err) {
                console.error("Error fetching categories:", err);
                setError("Không thể tải danh mục. Vui lòng thử lại sau.");
            }
        };

        fetchAvailableCategories();
    }, [product.cateId]);

    // Fetch variants for the product
    useEffect(() => {
        const fetchVariants = async () => {
            try {
                const token = getCookie('access_token');
                if (!token) {
                    console.warn("No access token found. Variants won't be loaded.");
                    return;
                }

                const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/product/variants/${proId}`;
                const response = await axios.get(apiUrl, {
                    headers: {
                        'Accept': '*/*',
                        'Authorization': `Bearer ${token}`,
                    },
                });

                if (response.data && response.data.responseList) {
                    const variants = response.data.responseList;
                    const sizeVariants = { S: {}, M: {}, L: {} };

                    // Set highest varId
                    const varIds = variants.map(variant => variant.varId);
                    const maxVarId = Math.max(...varIds, 0);
                    setHighestVarId(maxVarId);

                    // Map the fetched variants to their respective sizes
                    variants.forEach(variant => {
                        sizeVariants[variant.size] = variant;

                        // Set varIds for each size
                        if (variant.size === 'S') setVarIdS(variant.varId);
                        else if (variant.size === 'M') setVarIdM(variant.varId);
                        else if (variant.size === 'L') setVarIdL(variant.varId);
                    });

                    setPriceS(sizeVariants.S.price || '');
                    setStockS(sizeVariants.S.stock || '');
                    setPriceM(sizeVariants.M.price || '');
                    setStockM(sizeVariants.M.stock || '');
                    setPriceL(sizeVariants.L.price || '');
                    setStockL(sizeVariants.L.stock || '');
                } else {
                    console.warn("No variants found in the response.");
                }
            } catch (err) {
                console.error("Error fetching variants:", err);
                setError("Không thể tải biến thể sản phẩm. Vui lòng thử lại sau.");
            }
        };

        if (proId) {
            fetchVariants();
        }
    }, [proId]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
    
        // Simple client-side validation for each variant
        const validateVariant = (price, stock) => {
            if (!price || price <= 0) return "Giá không hợp lệ.";
            if (!stock || stock < 0) return "Số lượng không hợp lệ.";
            return null;
        };
    
        // Validate all variants
        const errorS = validateVariant(priceS, stockS);
        const errorM = validateVariant(priceM, stockM);
        const errorL = validateVariant(priceL, stockL);
    
        // Store variant updates or creation requests
        const variantRequests = [];
    
        const isValidURL = (string) => {
            try {
                new URL(string);
                return true;
            } catch (_) {
                return false;
            }
        };
    
        if (!isValidURL(proImg)) {
            setError("URL Hình Ảnh không hợp lệ.");
            setLoading(false);
            return;
        }
    
        const token = getCookie('access_token');
        if (!token) {
            setError("Bạn cần đăng nhập để thực hiện thao tác này.");
            setLoading(false);
            return;
        }
    
        try {
            // Update product details
            const productUpdateUrl = `${import.meta.env.VITE_API_BASE_URL}/product/update`;
            const productPayload = {
                proId: Number(proId),
                cateId: Number(cateId),
                proName: proName.trim(),
                proImg: proImg.trim(),
                description: description.trim(),
            };
    
            console.log("Updating product with payload:", productPayload); // Log the payload for debugging
    
            const productResponse = await axios.put(productUpdateUrl, productPayload, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
    
            console.log("Product Update Response:", productResponse); // Log the response for debugging
    
            if (productResponse.status !== 200) {
                throw new Error("Cập nhật sản phẩm thất bại.");
            }
    
            // Now handle variants creation and updates
            const variantUpdateUrl = `${import.meta.env.VITE_API_BASE_URL}/productVar/update`;
            const variantCreateUrl = `${import.meta.env.VITE_API_BASE_URL}/productVar/create`; // New URL for creating variants
    
            // Prepare variant updates with varId
            const variantUpdates = [
                { varId: varIdS, size: "S", price: Number(priceS), stock: Number(stockS) },
                { varId: varIdM, size: "M", price: Number(priceM), stock: Number(stockM) },
                { varId: varIdL, size: "L", price: Number(priceL), stock: Number(stockL) },
            ];
    
            // Loop through each variant and prepare requests
            for (const variant of variantUpdates) {
                if (variant.price && variant.stock) {
                    // If both price and stock are provided, update the variant
                    const variantPayload = {
                        varId: variant.varId,
                        proId: Number(proId),
                        size: variant.size,
                        price: variant.price,
                        stock: variant.stock,
                    };
                    variantRequests.push(axios.put(variantUpdateUrl, variantPayload, {
                        headers: {
                            'Accept': '*/*',
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json',
                        },
                    }));
                } else {
                    // If price or stock is missing, create a new variant
                    const newVarId = highestVarId + 1; // Calculate new varId
                    const variantPayload = {
                        proId: Number(proId),
                        size: variant.size,
                        price: variant.price || 0, // Set default price if empty
                        stock: variant.stock || 0,   // Set default stock if empty
                    };
                    variantRequests.push(axios.post(variantCreateUrl, variantPayload, {
                        headers: {
                            'Accept': '*/*',
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json',
                        },
                    }));
                    
                    // Update highestVarId for the next iteration
                    highestVarId++;
                }
            }
    
            // Wait for all variant requests to complete
            await Promise.all(variantRequests);
    
            // If all requests are successful, notify parent component
            onUpdate();
            alert("Cập nhật sản phẩm thành công!");
            onClose();
        } catch (err) {
            console.error("Error updating product:", err);
            setError("Cập nhật sản phẩm không thành công. Vui lòng thử lại sau.");
        } finally {
            setLoading(false);
        }
    };
    
     
    return (
        <div className="form-update-product">
            <form onSubmit={handleSubmit} style={{
                background: "white",
                width: "600px",
                padding: "20px",
            }}>
                <h2>Cập nhật sản phẩm</h2>
                {error && <p className="error-message">{error}</p>}
                <div className="form-group">
                    <label htmlFor="cateId">Danh mục sản phẩm:</label>
                    <select
                        id="cateId"
                        value={cateId}
                        onChange={(e) => setCateId(e.target.value)}
                    >
                        {availableCategories.map((category) => (
                            <option key={category.cateId} value={category.cateId}>
                                {category.cateName}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="form-group">
                    <label htmlFor="proName">Tên sản phẩm:</label>
                    <input
                        id="proName"
                        type="text"
                        value={proName}
                        onChange={(e) => setProName(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="proImg">Hình ảnh URL:</label>
                    <input
                        id="proImg"
                        type="text"
                        value={proImg}
                        onChange={(e) => setProImg(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="description">Mô tả:</label>
                    <textarea
                        id="description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    ></textarea>
                </div>

                <div className="pro-update-variant-container">
                    <h3>Cập nhật biến thể</h3>
                    {['S', 'M', 'L'].map(size => (
                        <div key={size} className="pro-update-variant-group">
                            <h4>Kích thước {size}</h4>
                            <div className="pro-update-variant-fields">
                                <div className="pro-update-field-group">
                                    <label>Giá:</label>
                                    <input
                                        type="number"
                                        value={size === 'S' ? priceS : size === 'M' ? priceM : priceL}
                                        onChange={(e) => {
                                            if (size === 'S') setPriceS(e.target.value);
                                            else if (size === 'M') setPriceM(e.target.value);
                                            else setPriceL(e.target.value);
                                        }}
                                        required
                                    />
                                </div>
                                <div className="pro-update-field-group">
                                    <label>Số lượng:</label>
                                    <input
                                        type="number"
                                        value={size === 'S' ? stockS : size === 'M' ? stockM : stockL}
                                        onChange={(e) => {
                                            if (size === 'S') setStockS(e.target.value);
                                            else if (size === 'M') setStockM(e.target.value);
                                            else setStockL(e.target.value);
                                        }}
                                        required
                                    />
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                <button type="submit" disabled={loading}>
                    {loading ? 'Đang cập nhật...' : 'Cập nhật'}
                </button>
                <button type="button" onClick={onClose}>Hủy</button>
            </form>
        </div>
    );
};

export default FormUpdateProduct;
