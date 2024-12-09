import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import Footer from "../../components/Footer/Footer.jsx";
import NavbarShipper from "../../components/Navbar/NavbarShipper.jsx";
import './InfoShipper.css'; // Import CSS
import '../../assets/assets.js';
import { assets } from "../../assets/assets.js";
import LoadingAnimation from "../../components/Animation/LoadingAnimation.jsx";
import ErrorMessage from "../../components/Animation/ErrorMessage.jsx";
import FormListVoucher from "../../components/Form/FormListVoucher.jsx";

const Info = () => {
    const [formData, setFormData] = useState({
        email: '',
        fullName: '',
        phoneNumber: '',
        avatar: '',
        sex: '',
        birthDay: '',
        address: '',
        street: '',
        ward: '', // Sử dụng wardName thay vì ward
        district: '',
        city: '',
    });

    const navigate = useNavigate();
    const [wardId, setWardId] = useState(''); // Thêm wardId vào state riêng
    const [selectedFile, setSelectedFile] = useState(null);
    const [previewImage, setPreviewImage] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [isUploading, setIsUploading] = useState(false);
    const [provinceId, setProvinceId] = useState('');
    const [districtId, setDistrictId] = useState('');
    const [districts, setDistricts] = useState([]);
    const [wards, setWards] = useState([]);
    const [provinces, setProvinces] = useState([]);
    const [formErrors, setFormErrors] = useState({
        email: '',
        phoneNumber: '',
        birthDay: ''
    });
    const [isEditing, setIsEditing] = useState(false);
    const [voucherList, setVoucherList] = useState([]); // Danh sách voucher
    const [showFormListVoucher, setShowFormListVoucher] = useState(false);
    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Cannot decode token:", error);
            return null;
        }
    };

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    useEffect(() => {
        const fetchUserInfo = async () => {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                setLoading(false);
                return;
            }

            const userId = getUserIdFromToken(token);
            if (!userId) {
                setError("Không thể lấy userId từ token.");
                setLoading(false);
                return;
            }

            try {
                const userResponse = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/user/info/${userId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                const userInfo = userResponse.data;

                // Tách địa chỉ thành các phần riêng biệt (street, ward, district, city)
                let addressParts = (userInfo.address || '').split(',').map(part => part.trim());

                // Kiểm tra và xử lý giá trị "None" hoặc "null" trong địa chỉ
                const [street, ward, district, city] = [
                    addressParts[0] && addressParts[0] !== 'None' && addressParts[0] !== 'null' ? addressParts[0] : '',
                    addressParts[1] && addressParts[1] !== 'None' && addressParts[1] !== 'null' ? addressParts[1] : '',
                    addressParts[2] && addressParts[2] !== 'None' && addressParts[2] !== 'null' ? addressParts[2] : '',
                    addressParts[3] && addressParts[3] !== 'None' && addressParts[3] !== 'null' ? addressParts[3] : ''
                ];

                console.log("Address Parts:", addressParts);

                setFormData({
                    email: userInfo.email || '',
                    fullName: userInfo.fullName || '',
                    phoneNumber: userInfo.phone || '',
                    avatar: userInfo.avatar || '',
                    sex: userInfo.sex || '',
                    birthDay: userInfo.birth_date ? formatBirthDayForInput(userInfo.birth_date) : '',
                    street,
                    ward,
                    district,
                    city
                });

                setPreviewImage(userInfo.avatar || '');

            } catch (err) {
                console.error("Lỗi khi lấy thông tin người dùng:", err);
                setError("Không thể lấy thông tin người dùng.");
            } finally {
                setLoading(false);
            }
        };

        fetchUserInfo();
    }, []);

    useEffect(() => {
        if (!formData.city) return;  // Kiểm tra trước khi gọi fetchProvinces

        const fetchProvinces = async () => {
            try {
                const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/province/listAll`);
                const provinces = response.data.responseList;

                let selectedProvince = null;

                // Kiểm tra xem thành phố trong formData có hợp lệ không
                if (formData.city && formData.city.trim() !== 'None') {
                    selectedProvince = provinces.find(province => province.provinceName === formData.city);
                }

                if (!selectedProvince) {
                    setError("Không tìm thấy tỉnh.");
                    setLoading(false);
                    return;
                }

                // Set provinceId từ selectedProvince
                setProvinceId(selectedProvince.provinceId);

                // Kiểm tra district
                if (!formData.district || formData.district.trim() === 'None') {
                    setDistrictId(null);
                    return;
                }

                const districtResponse = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/province/list-district?provinceId=${selectedProvince.provinceId}`);
                const districts = districtResponse.data.districtResponseList;
                setDistricts(districts);

                const selectedDistrict = districts.find(d => d.districtName === formData.district);
                if (selectedDistrict) {
                    setDistrictId(selectedDistrict.districtId);
                } else {
                    setError(`Không tìm thấy huyện "${formData.district}".`);
                    setLoading(false);
                    return;
                }

                // Kiểm tra ward
                if (!formData.ward || formData.ward.trim() === 'None') {
                    setWardId(null);
                    return;
                }

                console.log("District ID:", selectedDistrict.districtId);

                const wardResponse = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/province/list-ward?districtId=${selectedDistrict.districtId}`);
                console.log("Ward Response Data:", wardResponse.data);

                const wards = wardResponse.data.responseList;
                setWards(wards);

                console.log("Form Data Ward:", formData.ward);
                const selectedWard = wards.find(w => w.wardName.trim() === formData.ward.trim());  // So sánh chính xác tên

                console.log("Selected Ward:", selectedWard);

                if (selectedWard) {
                    setWardId(selectedWard.wardId);
                } else {
                    setError("Không tìm thấy xã/phường.");
                    setLoading(false);
                    return;
                }


            } catch (err) {
                console.error("Lỗi khi lấy danh sách tỉnh:", err);
                setError("Không thể lấy danh sách tỉnh.");
            }
        };

        fetchProvinces();
    }, [formData.city, formData.district, formData.ward]);  // Trigger khi formData thay đổi



    const handleWardChange = (e) => {
        const selectedWardId = e.target.value;
        setWardId(selectedWardId);

        // Tìm ward từ selectedWardId
        const selectedWard = wards.find(ward => ward.wardId === parseInt(selectedWardId));

        if (selectedWard) {
            // Cập nhật formData với ID và tên xã/phường đã chọn
            setFormData(prevFormData => ({
                ...prevFormData,
                ward: selectedWard.wardName,  // Lưu wardName vào formData
                wardId: selectedWardId       // Lưu wardId vào formData
            }));

            console.log("Selected Ward Name:", selectedWard.wardName);
        } else {
            console.log("Không tìm thấy xã/phường với ID:", selectedWardId);
        }
    };


    const fetchWards = async (districtId) => {
        try {
            console.log("Fetching wards for districtId: " + districtId);
            const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/province/list-ward?districtId=${districtId}`, {
                headers: {
                    'Authorization': 'Bearer ' + getCookie('access_token')
                }
            });

            if (response.data.responseList) {
                const wardsList = response.data.responseList;
                setWards(wardsList); // Cập nhật lại danh sách xã/phường
                console.log("Danh sách xã/phường khi fetch:", wardsList);
            } else {
                setWards([]); // Nếu không có xã/phường, reset danh sách
                console.log("Không có xã/phường cho huyện này.");
            }
        } catch (error) {
            console.error("Error fetching wards:", error);
            setWards([]); // Reset wards trong trường hợp lỗi
        }
    };



    const formatBirthDayForInput = (date) => {
        const birthDate = new Date(date);
        const year = birthDate.getFullYear();
        const month = String(birthDate.getMonth() + 1).padStart(2, '0'); // Months are 0-indexed
        const day = String(birthDate.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };


    // Fetch provinces data
    useEffect(() => {
        axios.get(`${import.meta.env.VITE_API_BASE_URL}/province/listAll`, {
            headers: {
                'Authorization': 'access_token'
            }
        })
            .then(response => {
                setProvinces(response.data.responseList);
            })
            .catch(error => {
                console.error("Error fetching provinces:", error);
            });
    }, []);

    const fetchDistricts = async (provinceId) => {
        try {
            console.log("Fetching districts for provinceId: " + provinceId);
            const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/province/list-district?provinceId=${provinceId}`, {
                headers: {
                    'Authorization': 'Bearer ' + getCookie('access_token')
                }
            });

            const districtsList = response.data.districtResponseList || [];
            setDistricts(districtsList);
        } catch (error) {
            console.error("Error fetching districts:", error);
            setDistricts([]); // Reset districts in case of error
        }
    };


    const handleCityChange = async (e) => {
        const selectedCity = e.target.value;
        const selectedProvince = provinces.find(province => province.provinceName === selectedCity);

        setProvinceId(selectedProvince ? selectedProvince.provinceId : '');
        setFormData({
            ...formData,
            city: selectedCity,
            district: '',  // Reset district khi một tỉnh mới được chọn
            ward: ''  // Reset ward khi một tỉnh mới được chọn
        });

        if (selectedProvince && selectedProvince.provinceId) {
            fetchDistricts(selectedProvince.provinceId);  // Fetch districts cho tỉnh mới
        }
    };


    const handleDistrictChange = (e) => {
        const selectedDistrictId = e.target.value;
        setDistrictId(selectedDistrictId);

        const selectedDistrict = districts.find(district => district.districtId === parseInt(selectedDistrictId));

        if (selectedDistrict) {
            setFormData({
                ...formData,
                district: selectedDistrict.districtName,
                ward: '' // Reset ward when district changes
            });

            fetchWards(selectedDistrictId);  // Fetch wards when district changes
        }
    };


    const handleBack = () => navigate('/shipper-home');
    const handleChangePass = () => navigate('/change');

    const handleSubmitImg = async (e) => {
        e.preventDefault();
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (!token || !userId) {
            setError("You need to log in.");
            return;
        }

        if (selectedFile) {
            const formData = new FormData();
            formData.append("file", selectedFile);
            setIsUploading(true);

            try {
                const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/image/user/upload?userId=${userId}`, formData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'multipart/form-data'
                    }
                });
                setPreviewImage(response.data.url);
                alert("Avatar updated successfully!");
            } catch (error) {
                console.error("Update error:", error);
                setError("Unable to update avatar.");
            } finally {
                setIsUploading(false);
            }
        }
    };

    const validateForm = () => {
        const errors = { email: '', phoneNumber: '', birthDay: '' };
        const phoneRegex = /^[0-9]{10}$/;

        if (!formData.email.includes('@')) errors.email = 'Invalid email!';
        if (!phoneRegex.test(formData.phoneNumber)) errors.phoneNumber = 'Phone number must be 10 digits!';

        const birthDate = new Date(formData.birthDay);
        if (birthDate > new Date()) errors.birthDay = 'Invalid birth date!';

        setFormErrors(errors);
        return Object.values(errors).every(error => error === '');
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        setSelectedFile(file);
        setPreviewImage(URL.createObjectURL(file));
    };


    const handleSubmit = async (e) => {
        e.preventDefault();
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (!token || !userId) {
            setError("You need to log in.");
            return;
        }

        if (!validateForm()) return;

        const updatedData = {
            userId,
            email: formData.email,
            fullName: formData.fullName,
            phoneNumber: formData.phoneNumber,
            avatar: formData.avatar,
            sex: formData.sex,
            birthDay: formData.birthDay,
            address: `${formData.street}, ${formData.ward}, ${formData.district}, ${formData.city}`,
        };


        // Log the data to preview before sending
        console.log("Data being sent:", updatedData);

        try {
            await axios.put(`${import.meta.env.VITE_API_BASE_URL}/user/info-update`, updatedData, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            alert("Profile updated successfully!");
        } catch (error) {
            console.error("Update error:", error);
            setError("Unable to update profile.");
        }
    };

    const handleViewVoucher = async (e) => {
        e.preventDefault(); // Ngăn chặn form submit mặc định
        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);
    
        try {
            const response = await fetch(`http://localhost:1010/api/user-voucher/view-all/${userId}`, {
                method: 'GET',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                }
            });
    
            if (response.ok) {
                const data = await response.json();
                const voucherList = data.getVoucherResponseList; // Lấy danh sách voucher từ phản hồi
    
                // Fetch key cho từng voucher trong danh sách
                const updatedVoucherList = await Promise.all(
                    voucherList.map(async (voucher) => {
                        try {
                            const voucherResponse = await fetch(`http://localhost:1010/api/voucher/view/${voucher.voucherId}`, {
                                method: 'GET',
                                headers: {
                                    'accept': '*/*',
                                    'Authorization': `Bearer ${token}`,
                                }
                            });
    
                            if (voucherResponse.ok) {
                                const voucherData = await voucherResponse.json();
                                return { ...voucher, key: voucherData.body.key }; // Cập nhật key vào voucher
                            } else {
                                console.error(`Lỗi khi lấy thông tin voucher ${voucher.voucherId}:`, voucherResponse.status);
                                return voucher; // Nếu có lỗi, trả lại voucher không thay đổi
                            }
                        } catch (error) {
                            console.error('Lỗi khi gọi API chi tiết voucher:', error);
                            return voucher; // Trả lại voucher gốc nếu có lỗi
                        }
                    })
                );
    
                // Cập nhật voucherList với key mới và hiển thị form
                setVoucherList(updatedVoucherList);
                setShowFormListVoucher(true); // Mở overlay khi nhận được dữ liệu
            } else {
                console.error('Lỗi khi lấy danh sách voucher:', response.status);
            }
        } catch (error) {
            console.error('Lỗi khi gọi API:', error);
        }
    };
    



    if (loading) {
        return <LoadingAnimation animationPath="https://lottie.host/your_animation_url.json" isVisible={loading} />;
    }

    if (error) {
        return <ErrorMessage path="https://lottie.host/your_error_animation_url.json" message={error} />;
    }

    return (
        <>
            <NavbarShipper currentPage={'Thông tin cá nhân'} />
            <div className="body-info">
                <div className="container">
                    <h2>
                        Thông tin cá nhân
                        <i
                            className="ti-pencil"
                            style={{ fontSize: '20px', color: 'green', cursor: 'pointer', marginLeft: '5px' }}
                            onClick={() => setIsEditing(!isEditing)}
                        />
                    </h2>

                    <form className="user-info-form">
                        <div className="avatar-container">
                            <div className="avatar-image-wrapper">
                                {previewImage ? (
                                    <>
                                        <img src={previewImage} alt={''} className="avatar-image" />
                                        <div className="image-overlay"></div> {/* Overlay for the blur effect */}
                                        <div className="btn-gr-img slideUp">
                                            <button
                                                type="button"
                                                id="btn-upload"
                                                onClick={() => document.getElementById('file-upload').click()}
                                            >
                                                < i className="ti-cloud-up" />
                                            </button>
                                            <button
                                                type="button"
                                                id="btn-save"
                                                onClick={handleSubmitImg}
                                            >
                                                < i className="ti-save" />
                                            </button>
                                        </div>
                                    </>
                                ) : (
                                    <img src={assets.avtrang} alt="" className="avatar-image" />
                                )}
                            </div>


                            <input
                                type="file"
                                accept="image/*"
                                id="file-upload"
                                onChange={handleFileChange}
                                style={{ display: 'none' }}
                            />
                        </div>


                        <div className="form-grid">
                            <div className="form-column">
                                <div className="form-group">
                                    <label>Email:</label>
                                    <input
                                        className="form-control"
                                        type="email"
                                        value={formData.email}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                    />
                                    {formErrors.email && <span className="error">{formErrors.email}</span>}
                                </div>
                                <div className="form-group">
                                    <label>Họ tên:</label>
                                    <input
                                        className="form-control"
                                        type="text"
                                        value={formData.fullName}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Giới tính:</label>
                                    <select
                                        className="form-control"
                                        value={formData.sex}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, sex: e.target.value })}
                                    >
                                        <option value="" disabled>Select your option</option>
                                        <option value="MALE">Nam</option>
                                        <option value="FEMALE">Nữ</option>
                                        <option value="OTHER">Khác</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Ngày sinh:</label>
                                    <input
                                        className="form-control"
                                        type="date"
                                        value={formData.birthDay}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, birthDay: e.target.value })}
                                    />
                                    {formErrors.birthDay && <span className="error">{formErrors.birthDay}</span>}
                                </div>

                            </div>
                            <div className="form-column">
                                <div className="form-group">
                                    <label>Số điện thoại:</label>
                                    <input
                                        className="form-control"
                                        type="text"
                                        value={formData.phoneNumber}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                                    />
                                    {formErrors.phoneNumber && <span className="error">{formErrors.phoneNumber}</span>}
                                </div>

                                <div className="form-group">
                                    <label>Thành phố:</label>
                                    <select
                                        className="form-control"
                                        value={formData.city}
                                        disabled={!isEditing}
                                        onChange={handleCityChange}
                                        style={{ fontSize: '14px' }}
                                    >

                                        <option value="">Chọn tỉnh/thành phố</option>
                                        {provinces.map((province) => (
                                            <option key={province.provinceId} value={province.provinceName}>
                                                {province.provinceName}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div style={{ display: 'flex', gap: '15px' }}>
                                    <div className="form-group">
                                        <label>District</label>
                                        <select
                                            className="form-control"
                                            value={districtId}
                                            disabled={!isEditing}
                                            onChange={handleDistrictChange}
                                            style={{ fontSize: '14px' }}
                                        >
                                            <option value="">Select District</option>
                                            {districts.map(district => (
                                                <option key={district.districtId} value={district.districtId}>
                                                    {district.districtName}
                                                </option>
                                            ))}
                                        </select>
                                    </div>



                                    <div className="form-group">
                                        <label>Xã/Phường</label>
                                        <select
                                            className="form-control"
                                            value={wardId || ""}  // Đảm bảo giá trị là wardId
                                            disabled={!isEditing}  // Disable nếu không đang chỉnh sửa
                                            onChange={handleWardChange}  // Sử dụng hàm xử lý thay đổi
                                            style={{ fontSize: '14px' }}
                                        >
                                            <option value="">Select Ward</option>  {/* Mặc định khi không chọn */}
                                            {wards.map((ward) => (
                                                <option key={ward.wardId} value={ward.wardId}>
                                                    {ward.wardName}
                                                </option>
                                            ))}
                                        </select>
                                    </div>


                                </div>

                                <div className="form-group">
                                    <label>Đường:</label>
                                    <input
                                        className="form-control"
                                        type="text"
                                        value={formData.street}
                                        disabled={!isEditing} // Disable based on editing mode
                                        onChange={(e) => setFormData({ ...formData, street: e.target.value })}
                                    />
                                </div>


                            </div>
                        </div>

                        <div className="button-group">
                            <button type="submit" className="btn" onClick={handleSubmit}  disabled={!isEditing}>Cập nhật</button>
                            <button type="button" className="btn" id="btn-change" onClick={handleChangePass}>Đổi mật khẩu</button>
                            <button type="submit" className="btn" id="btn-view-voucher" onClick={handleViewVoucher}>Xem voucher</button>

                            {/* Hiển thị overlay nếu showFormListVoucher là true */}
                            {showFormListVoucher && (
                                <div className="voucher-info-overlay">
                                     <FormListVoucher vouchers={voucherList} onClose={() => setShowFormListVoucher(false)} />
                                </div>
                            )}
                            <button type="button" className="btn" id="btn-back-info" onClick={handleBack}>Trở lại</button>
                        </div>
                    </form>
                </div>
            </div>

            <Footer />
        </>
    );
};

export default Info;
