import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Swal from 'sweetalert2';
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
    const [isCreating, setIsCreating] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

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

                const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/admin/product/variants/${proId}`;
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

                    // Đảm bảo số lượng bằng 0 vẫn được hiển thị
                    setPriceS(sizeVariants.S.price ?? '');
                    setStockS(sizeVariants.S.stock ?? 0);
                    setPriceM(sizeVariants.M.price ?? '');
                    setStockM(sizeVariants.M.stock ?? 0);
                    setPriceL(sizeVariants.L.price ?? '');
                    setStockL(sizeVariants.L.stock ?? 0);
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

            const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/admin/list-image/${proId}`;
            const response = await axios.get(apiUrl, {
                headers: {
                    'Accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });

            // Kiểm tra và lấy danh sách ảnh từ body của response
            if (response.data && response.data.body.productImageResponseList) {
                setProductImages(response.data.body.productImageResponseList);
                console.log("lấy ảnh r nè: ", response.data.body.productImageResponseList);
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
        const result = await Swal.fire({
            title: 'Xác nhận xóa ảnh',
            text: 'Bạn có chắc chắn muốn xóa hình ảnh này?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Có',
            cancelButtonText: 'Không',
        });

        if (!result.isConfirmed) return; // Nếu người dùng chọn "Không", thoát khỏi hàm

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
                setSuccessMessage('Hình ảnh đã được xóa thành công!');
                setSelectedFiles([]);
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

        const result = await Swal.fire({
            title: 'Xác nhận xóa tất cả ảnh',
            text: 'Bạn có chắc chắn muốn xóa tất cả hình ảnh không?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Có',
            cancelButtonText: 'Không',
        });

        // Nếu người dùng nhấn "Không", thoát khỏi hàm
        if (!result.isConfirmed) return;

        setDeletingAllImages(true); // Bắt đầu trạng thái loading

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
                setSuccessMessage('Tất cả hình ảnh đã được xóa thành công.');
                setSelectedFiles([]);
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
    const [isUploading, setIsUploading] = useState(false); // Thêm trạng thái isUploading

    const handleFileUpload = async () => {
        if (selectedFiles.length === 0) {
            setUploadMessage("Vui lòng chọn ít nhất một hình ảnh để tải lên.");
            console.log("Vui lòng chọn ít nhất một hình ảnh.");
            return;
        }

        // Tạo FormData và thêm các tệp hình ảnh
        const imageFormData = new FormData();
        selectedFiles.forEach(file => {
            imageFormData.append('files', file);
        });

        const token = getCookie('access_token');
        if (!token) {
            setUploadMessage("Bạn cần đăng nhập để thực hiện thao tác này.");
            console.log("Chưa đăng nhập.");
            return;
        }

        setIsUploading(true);

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
                setProductImages(uploadResponse.data.productImageResponseList); // Cập nhật hình ảnh ngay sau khi tải lên thành công
                setUploadMessage("Hình ảnh đã được tải lên thành công.");
                console.log("Hình ảnh đã được tải lên thành công.");

                // Fetch lại hình ảnh mới nhất từ API
                await fetchProductDetails();
            } else {
                setUploadMessage("Hình ảnh đã được tải lên thành công.");
                await fetchProductDetails();
                setSelectedFiles([]); // Đảm bảo reset selectedFiles
                console.log("Hình ảnh đã được tải lên thành công.");
            }

            // Reset selected files sau khi tải lên thành công
            setSelectedFiles([]); // Đảm bảo reset selectedFiles
        } catch (err) {
            console.error("Error uploading images:", err);
            setUploadMessage("Có lỗi xảy ra khi tải lên hình ảnh. Vui lòng thử lại sau.");
        } finally {
            setSelectedFiles([]);
            setIsUploading(false);
        }
    };


    // Sử dụng useEffect để log giá trị uploadMessage khi nó thay đổi
    useEffect(() => {
        console.log("uploadMessage đã thay đổi: ", uploadMessage);
    }, [uploadMessage]); // Gọi effect khi uploadMessage thay đổi

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        // Simple client-side validation for each variant
        const validateVariant = (price, stock) => {
            if (price !== undefined && (price <= 1000 || !price)) return "Giá không hợp lệ.";
            if (stock !== undefined && (stock === '' || stock < 0)) return "Số lượng không hợp lệ.";
            return null;
        };

        // Validate only the variants that have changes
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
                { varId: varIdS, size: "S", price: priceS, stock: stockS },
                { varId: varIdM, size: "M", price: priceM, stock: stockM },
                { varId: varIdL, size: "L", price: priceL, stock: stockL },
            ];

            // Loop through each variant and prepare requests only for those that have changed
            for (const variant of variantUpdates) {
                
                if (variant.price !== undefined || variant.stock !== undefined) {
                    
                    if (variant.varId === null) {
                        if (variant.stock === 0) {
                            setError(`Số lượng của variant ${variant.size} phải lớn hơn 0.`);
                            setLoading(false);
                            return;
                        }
                        // Create new variant if varId is null
                        const variantPayload = {
                            proId: Number(proId),
                            size: variant.size,
                            price: variant.price || 0,  // Default price if not provided
                            stock: variant.stock || 0,  // Default stock if not provided
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
                            price: variant.price || 0,  // Default price if not provided
                            stock: variant.stock || 0,  // Default stock if not provided
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
            setSuccessMessage('Cập nhật sản phẩm thành công!');
            onClose();
        } catch (err) {
            console.error("Error updating product or variants:", err);

            if (err.response) {
                // Check if the error is 409 (Conflict)
                if (err.response.status === 409) {
                    setError("Đã tồn tại sản phẩm cùng tên.");
                } else {
                    setError("Cập nhật sản phẩm thất bại. Vui lòng thử lại sau.");
                }
            } else {
                setError("Đã xảy ra lỗi. Vui lòng thử lại.");
            }
        } finally {
            setLoading(false);
        }
    };




    return (
        <div className="form-update-product">
            {isCreating && (
                <div className="loading-overlay active">
                    <div className="loading-spinner"></div>
                </div>
            )}
            {errorMessage && <p className="form-add-post-error">{errorMessage}</p>}
            {successMessage && <p className="form-add-post-success">{successMessage}</p>}
            <form onSubmit={handleSubmit} style={{
                background: "white",
                width: "100%",
                maxWidth: "900px",
                padding: "20px",
                borderRadius: "8px",
                boxShadow: "0 2px 8px rgba(0, 0, 0, 0.1)",
                display: "flex", // Use flexbox for layout
                gap: "20px", // Space between sections
                flexDirection: "column",
                marginTop: '5px',
                marginBottom: '20px',
                overflowY: "auto", // Allow scrolling when content exceeds maxHeight
                paddingTop: '10px'

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
                                    // Nếu chưa có ảnh thì hiển thị thông báo
                                    <p>{uploadMessage || "Chưa có ảnh tải lên"}</p>
                                ) : (
                                    // Nếu có ảnh, hiển thị ảnh và nút xóa
                                    productImages.map((image, index) => (
                                        <div key={index} className="image-container">
                                            <img
                                                src={image.linkImage || image.url} // Kiểm tra lại key nếu cần
                                                alt={`Product ${index}`}

                                            />
                                            <button
                                                type="button"
                                                className="delete-button"
                                                onClick={() => handleDeleteImage(image.id, index)}
                                                disabled={deletingImageId === image.id}
                                            >
                                                {deletingImageId === image.id ? 'Đang xóa...' : '🗑️'}
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
                                style={{ marginBottom: "15px" }}
                            />

                            {/* Hiển thị thông báo upload */}
                            <div style={{ display: 'flex', justifyContent: 'space-around' }}>
                                <button
                                    type="button"
                                    onClick={handleFileUpload}
                                    style={{ backgroundColor: '#4095e8', position: 'relative', borderRadius: '20px' }}
                                    disabled={isUploading} // Disable button khi đang tải lên
                                >
                                    {isUploading ? (
                                        <>
                                            <span className="loading-spinner-button">
                                                <svg width="20" height="20" viewBox="0 0 50 50" className="spin" xmlns="http://www.w3.org/2000/svg">
                                                    <circle cx="25" cy="25" r="20" stroke="gray" strokeWidth="5" fill="none" />
                                                    <circle cx="25" cy="25" r="20" stroke="blue" strokeWidth="5" fill="none" strokeDasharray="125.6" strokeDashoffset="0" strokeLinecap="round">
                                                        <animate attributeName="stroke-dashoffset" values="0;251.2" dur="1s" keyTimes="0;1" repeatCount="indefinite" />
                                                    </circle>
                                                </svg>
                                            </span>
                                            Đang tải lên...
                                        </>
                                    ) : (
                                        'Tải lên hình ảnh'
                                    )}
                                </button>

                                <button
                                    type="button"
                                    onClick={handleDeleteAllImages}
                                    disabled={deletingAllImages || isUploading}
                                    style={{ borderRadius: '20px', cursor: (deletingAllImages || isUploading) ? 'not-allowed' : 'pointer', }}
                                >
                                    {deletingAllImages ? 'Đang xóa tất cả...' : 'Xóa tất cả hình ảnh'}
                                </button>
                            </div>



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
                                                const value = e.target.value === '' ? null : parseFloat(e.target.value); // Set to null if empty
                                                if (size === 'S') setPriceS(value);
                                                else if (size === 'M') setPriceM(value);
                                                else setPriceL(value);
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
                                                const value = e.target.value === '' ? '' : parseInt(e.target.value, 10);
                                                if (size === 'S') setStockS(value);
                                                else if (size === 'M') setStockM(value);
                                                else setStockL(value);
                                            }}
                                            required
                                            min="0"
                                        />
                                    </div>
                                </div>
                            </div>
                        ))}
                        <div className="form-group">
                            <label htmlFor="description">Mô tả:</label>
                            <textarea
                                id="description"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                style={{ height: "100px" }}
                            ></textarea>
                        </div>
                    </div>
                </div>

                <div className="update-pro-form-actions">
                    <button type="submit" disabled={loading || isUploading} id="update-pro-submit-button" style={{
                        cursor: (loading || isUploading) ? 'not-allowed' : 'pointer',
                    }}>
                        {loading ? 'Đang cập nhật...' : 'Cập nhật'}
                    </button>
                    <button type="button" onClick={onClose} disabled={loading || isUploading} id="update-pro-cancel-button" style={{
                        cursor: (loading || isUploading) ? 'not-allowed' : 'pointer',
                    }}>
                        {loading ? 'Hủy' : 'Hủy'}</button>
                </div>
            </form>
        </div>
    );
}

export default FormUpdateProduct;
