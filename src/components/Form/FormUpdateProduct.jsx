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

    // State to hold product images
    const [productImages, setProductImages] = useState([]);

    // State to handle deletion loading state
    const [deletingImageId, setDeletingImageId] = useState(null);
    const [deletingAllImages, setDeletingAllImages] = useState(false); // New state for deleting all images

    // New state to hold selected files for upload
    const [selectedFiles, setSelectedFiles] = useState([]);
    const [uploadMessage, setUploadMessage] = useState('');

    // Function to get cookie by name
    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        return parts.length === 2 ? parts.pop().split(';').shift() : null;
    };

    // Function to extract userId from JWT token
    const getUserIdFromToken = (token) => {
        try {
            const payloadBase64 = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payloadBase64));
            return decodedPayload.UserId || null;
        } catch (err) {
            console.error('Error parsing token:', err);
            return null;
        }
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

    // Function to fetch product images
    const fetchProductDetails = async () => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                console.warn("No access token found. Product details won't be loaded.");
                return;
            }

            const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/product/view/${proId}`;
            const response = await axios.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (response.data && response.data.productImageResponseList) {
                setProductImages(response.data.productImageResponseList);
            } else {
                console.warn("No images found in the product details.");
            }
        } catch (err) {
            console.error("Error fetching product details:", err);
            setError("Không thể tải hình ảnh sản phẩm. Vui lòng thử lại sau.");
        }
    };

    // Fetch product images on component mount
    useEffect(() => {
        if (proId) {
            fetchProductDetails();
        }
    }, [proId]);

    // Function to handle image deletion
    const handleDeleteImage = async (imageId, index) => {
        const confirmDelete = window.confirm("Bạn có chắc chắn muốn xóa hình ảnh này?");
        if (!confirmDelete) return;

        setDeletingImageId(imageId);

        const token = getCookie('access_token');
        if (!token) {
            setError("Bạn cần đăng nhập để thực hiện thao tác này.");
            setDeletingImageId(null);
            return;
        }

        const deleteUrl = `${import.meta.env.VITE_API_BASE_URL}/product/image/deleteOne`;

        // Assuming 'stt' corresponds to the image's position (1-based index)
        const stt = index + 1;

        try {
            const deleteResponse = await axios.delete(deleteUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                data: {
                    proId: proId,
                    id: stt,
                },
            });

            if (deleteResponse.status === 200) {
                // Re-fetch the product images after deletion
                await fetchProductDetails(); // Make sure this function is defined
                alert("Hình ảnh đã được xóa thành công.");
            } else {
                throw new Error("Xóa hình ảnh thất bại.");
            }
        } catch (err) {
            console.error("Error deleting image:", err);
            setError("Xóa hình ảnh không thành công. Vui lòng thử lại sau.");
        } finally {
            setDeletingImageId(null);
        }
    };
    // Function to handle delete all images
    const handleDeleteAllImages = async () => {
        const confirmDelete = window.confirm('Bạn có chắc chắn muốn xóa tất cả hình ảnh không?');
        if (!confirmDelete) return;

        setDeletingAllImages(true); // Start loading state
        const token = getCookie('access_token');
        if (!token) {
            setError('Bạn cần đăng nhập để thực hiện thao tác này.');
            setDeletingAllImages(false);
            return;
        }

        const userId = getUserIdFromToken(token); // Extract userId from token
        if (!userId) {
            setError('Không thể xác thực người dùng.');
            setDeletingAllImages(false);
            return;
        }

        const deleteUrl = `${import.meta.env.VITE_API_BASE_URL}/product/image/deleteAll`;

        try {
            const deleteResponse = await axios.delete(deleteUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                data: {
                    userId: userId,
                    proId: proId,
                },
            });

            if (deleteResponse.status === 200) {
                // Re-fetch the product images after deleting all
                await fetchProductDetails();
                alert('Tất cả hình ảnh đã được xóa thành công.');
            } else {
                throw new Error('Xóa tất cả hình ảnh thất bại.');
            }
        } catch (err) {
            console.error('Error deleting all images:', err);
            setError('Xóa tất cả hình ảnh không thành công. Vui lòng thử lại sau.');
        } finally {
            setDeletingAllImages(false); // End loading state
        }
    };


    // Function to handle file input change
    const handleFileChange = (event) => {
        setSelectedFiles(Array.from(event.target.files));
    };

    // Function to upload files
    const handleFileUpload = async () => {
        if (selectedFiles.length === 0) {
            setUploadMessage("Vui lòng chọn ít nhất một hình ảnh để tải lên.");
            return;
        }
    
        // Tạo FormData và thêm các tệp hình ảnh
        const imageFormData = new FormData();
        selectedFiles.forEach(file => {
            imageFormData.append('files', file); // Sử dụng 'files' cho phép tải lên nhiều tệp
        });
    
        const token = getCookie('access_token');
        if (!token) {
            setUploadMessage("Bạn cần đăng nhập để thực hiện thao tác này.");
            return;
        }
    
        try {
            // Gửi yêu cầu tải lên hình ảnh
            const uploadResponse = await axios.post(
                `${import.meta.env.VITE_API_BASE_URL}/image/product-image/upload?proId=${proId}`,
                imageFormData,
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'multipart/form-data',
                    },
                }
            );
    
            // Kiểm tra và cập nhật trạng thái sản phẩm hình ảnh
            if (uploadResponse.data && uploadResponse.data.productImageResponseList) {
                setProductImages(uploadResponse.data.productImageResponseList);
                setUploadMessage("Hình ảnh đã được tải lên thành công.");
                setSelectedFiles([]); // Xóa danh sách tệp đã chọn
            } 
        } catch (err) {
            console.error("Error uploading images:", err);
            setUploadMessage("Có lỗi xảy ra khi tải lên hình ảnh. Vui lòng thử lại sau.");
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        // Simple client-side validation for each variant
        const validateVariant = (price, stock) => {
            if (!price || price <= 0) return "Giá không hợp lệ.";
            if (stock === '' || stock < 0) return "Số lượng không hợp lệ.";
            return null;
        };

        // Validate all variants
        const errorS = validateVariant(priceS, stockS);
        const errorM = validateVariant(priceM, stockM);
        const errorL = validateVariant(priceL, stockL);

        if (errorS || errorM || errorL) {
            setError(errorS || errorM || errorL);
            setLoading(false);
            return;
        }

        // Validate image URL if no new images are being uploaded
        const isValidURL = (string) => {
            try {
                new URL(string);
                return true;
            } catch (_) {
                return false;
            }
        };

        // Check if the product has an existing image or if a new image is required
        if (proImg && !isValidURL(proImg)) {
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

            const variantUpdateUrl = `${import.meta.env.VITE_API_BASE_URL}/productVar/update`;
            const variantCreateUrl = `${import.meta.env.VITE_API_BASE_URL}/productVar/create`; // URL for creating new variants

            // Prepare variant updates with varId
            const variantRequests = [];
            const variantUpdates = [
                { varId: varIdS, size: "S", price: Number(priceS), stock: Number(stockS) },
                { varId: varIdM, size: "M", price: Number(priceM), stock: Number(stockM) },
                { varId: varIdL, size: "L", price: Number(priceL), stock: Number(stockL) },
            ];

            // Loop through each variant and prepare requests
            for (const variant of variantUpdates) {
                if (variant.price !== '' && variant.stock !== '') {
                    // If the varId is null, create a new variant
                    if (variant.varId === null) {
                        // Create new variant
                        const variantPayload = {
                            proId: Number(proId),
                            size: variant.size,
                            price: variant.price,
                            stock: variant.stock,
                        };
                        variantRequests.push(axios.post(variantCreateUrl, variantPayload, {
                            headers: {
                                'Accept': '*/*',
                                'Authorization': `Bearer ${token}`,
                                'Content-Type': 'application/json',
                            },
                        }));
                    } else {
                        // Update existing variant
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
                    }
                }
            }

            // Wait for all variant requests to complete
            if (variantRequests.length > 0) {
                await Promise.all(variantRequests);
            }

            // Notify parent component
            onUpdate();
            alert("Cập nhật sản phẩm thành công!");
            onClose();
        } catch (err) {
            console.error("Error updating product or variants:", err);
            setError("Cập nhật sản phẩm thất bại. Vui lòng thử lại sau.");
        } finally {
            setLoading(false);
        }
    };


    return (
        <div className="form-update-product">
            <form onSubmit={handleSubmit} style={{
                background: "white",
                width: "100%",
                maxWidth: "900px",
                padding: "20px",
                borderRadius: "8px",
                boxShadow: "0 2px 8px rgba(0, 0, 0, 0.1)",
                display: "flex", // Use flexbox for layout
                gap: "20px", // Space between sections
                flexDirection: "column"
            }}>
                <div style={{
                display: "flex"
                }}>
                    {/* Left Section for Product Information */}
                    <div className="product-info-section">
                        <h2>Cập nhật sản phẩm</h2>
                        {error && <p className="error-message">{error}</p>}
                        <div className="form-group">
                            <label htmlFor="cateId">Danh mục sản phẩm:</label>
                            <select
                                id="cateId"
                                value={cateId}
                                onChange={(e) => setCateId(e.target.value)}
                                disabled
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

                        {/* Image Upload Section */}
                        <div className="image-upload">
                            <h3>Hình ảnh sản phẩm:</h3>
                            <div className="uploaded-images">
                                {productImages.length === 0 ? (
                                    <p>Chưa có hình ảnh nào được tải lên.</p>
                                ) : (
                                    productImages.map((image, index) => (
                                        <div key={index} className="image-container">
                                            <img src={image.url} alt={`Product ${index}`} />
                                            <button
                                                type="button"
                                                onClick={() => handleDeleteImage(image.id, index)}
                                                disabled={deletingImageId === image.id}
                                            >
                                                {deletingImageId === image.id ? 'Đang xóa...' : 'Xóa'}
                                            </button>
                                        </div>
                                    ))
                                )}
                            </div>

                            {/* File input for image upload */}
                            <input
                                type="file"
                                multiple
                                accept="image/*"
                                onChange={handleFileChange}
                                style={{marginBottom:"15px"}}
                            />
                            {uploadMessage && <div className="upload-message">{uploadMessage}</div>}
                            <button
                                type="button"
                                onClick={handleFileUpload}
                            >
                                Tải lên hình ảnh
                            </button>
                            <button
                                type="button"
                                onClick={handleDeleteAllImages}
                                disabled={deletingAllImages}
                            >
                                {deletingAllImages ? 'Đang xóa tất cả...' : 'Xóa tất cả hình ảnh'}
                            </button>
                        </div>
                        <div className="form-group">
                            <label htmlFor="description">Mô tả:</label>
                            <textarea
                                id="description"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                            ></textarea>
                        </div>
                    </div>

                    {/* Right Section for Product Variants */}
                    <div className="variant-section">
                        <h2>Cập nhật biến thể</h2>
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
                                            min="0"
                                            step="0.01"
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
                                            min="0"
                                        />
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="form-actions">
                    <button type="submit" disabled={loading} className="submit-button">
                        {loading ? 'Đang cập nhật...' : 'Cập nhật'}
                    </button>
                    <button type="button" onClick={onClose} className="cancel-button">Hủy</button>
                </div>
            </form>
        </div>
    );
}

export default FormUpdateProduct;
