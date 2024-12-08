import React, { useState, useEffect } from 'react';
import { useCart } from '../../context/CartContext';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './Cart.css'; // Use a CSS file for styling
import { useNavigate } from 'react-router-dom';
import { Autocomplete, TextField } from '@mui/material'; // Import Autocomplete and TextField

const Cart = () => {
    const navigate = useNavigate();
    const { cartItems, increase, increaseQuantity, decrease, clearCart, deleteOneItem, cartId, selectedVoucher, note, setSelectedVoucher, setNote, isCreating, handleCheckout, totalOfCart, changeSize } = useCart();

    const [vouchers, setVouchers] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [showSuccess, setShowSuccess] = useState(false);
    const [showError, setShowError] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [showErrorOutStock, setShowErrorOutStock] = useState(false);
    const [availableSizes, setAvailableSizes] = useState({});


    const handleNoteChange = (event) => {
        setNote(event.target.value); // Update note
    };

    // Helper function to format prices in VND
    const formatCurrency = (value) => {
        return new Intl.NumberFormat('vi-VN', {
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
        }).format(value) + ' VND';
    };

    // Function to handle clearing items (all or selected)
    const handleClearSelectedItems = async () => {
        if (selectedItem) {
            await deleteOneItem(selectedItem);
            setSelectedItem(null);
        } else {
            await clearCart();
        }
    };
    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };
    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Không thể giải mã token:", error);
            return null;
        }
    };

    // Calculate subtotal, discount, shipping, and total
    // Tính subtotal, discount, shipping, và total
    const subtotal = cartItems.reduce((acc, item) => acc + item.totalPrice, 0);
    const [discount, setDiscount] = useState(0);
    const shipping = 0; // Giả sử miễn phí vận chuyển
    let total = subtotal - discount + shipping;

    // Kiểm tra nếu tổng cộng nhỏ hơn hoặc bằng 0 thì gán nó thành 0
    if (total < 0) {
        total = 0;
    }


    // Fetch user's vouchers on component load
    useEffect(() => {
        const fetchVouchers = async () => {
            const token = getCookie('access_token');

            // Get userId from token
            const userId = getUserIdFromToken(token);
            console.log("User ID: ", userId);  // Log userId for debugging

            if (!userId) {
                console.error('User ID not found or invalid token');
                return;
            }

            try {
                // Fetch the list of vouchers for the user
                const response = await fetch(`http://localhost:1010/api/user-voucher/view-all/${userId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,  // Dynamically use token here
                    }
                });

                const data = await response.json();
                console.log('Vouchers data: ', data);  // Log API response for debugging

                if (data && data.getVoucherResponseList) {
                    // Filter out USED vouchers and fetch key and discount for each remaining voucher
                    const vouchersWithKeys = await Promise.all(
                        data.getVoucherResponseList
                            .filter(voucher => voucher.status !== "USED")  // Exclude "USED" vouchers
                            .map(async (voucher) => {
                                const voucherResponse = await fetch(`http://localhost:1010/api/voucher/view/${voucher.voucherId}`, {
                                    headers: {
                                        'accept': '*/*',
                                    },
                                });
                                const voucherData = await voucherResponse.json();

                                // Add key and discount to each voucher
                                return {
                                    ...voucher,
                                    key: voucherData.body.key,
                                    discount: voucherData.body.discount,
                                };
                            })
                    );

                    setVouchers(vouchersWithKeys);  // Set the vouchers with their key and discount
                } else {
                    console.error('No vouchers found or unexpected data format');
                }

            } catch (error) {
                console.error('Error fetching vouchers:', error);
            }
        };

        fetchVouchers();
    }, []);


    const handleVoucherChange = async (event, newValue) => {
        // Nếu giỏ hàng trống, reset selectedVoucher và discount
        if (cartItems.length === 0) {
            setSelectedVoucher(null);
            setDiscount(0);
            console.log("Giỏ hàng trống, voucher đã được reset.");
            return;
        }
    
        if (newValue) {
            const selectedVoucherId = newValue.voucherId; // Lấy voucherId từ giá trị mới
            setSelectedVoucher(selectedVoucherId);
    
            console.log("Selected Voucher ID: ", selectedVoucherId); // Log ID chọn được
    
            try {
                // Gọi API để lấy thông tin voucher
                const response = await fetch(`http://localhost:1010/api/voucher/view/${selectedVoucherId}`, {
                    method: 'GET',
                    headers: {
                        'accept': '*/*',
                    },
                });
    
                const data = await response.json();
    
                if (response.ok) {
                    // Cập nhật discount nếu tìm thấy voucher
                    const discount = data.body.discount;
                    setDiscount(discount); // Cập nhật giá trị giảm giá
                    console.log("Giảm giá nè: ", discount); // Log giảm giá
                } else {
                    // Nếu API trả về lỗi hoặc không có voucher
                    setDiscount(0);
                    console.error("Không tìm thấy voucher hoặc có lỗi trong việc lấy dữ liệu.");
                }
            } catch (error) {
                console.error("Có lỗi xảy ra khi gọi API: ", error);
                setDiscount(0); // Nếu có lỗi trong API call, set discount = 0
            }
        } else {
            setSelectedVoucher(null); // Xóa voucher nếu không chọn
            setDiscount(0); // Giảm giá = 0
        }
    };
    
    useEffect(() => {
        if (cartItems.length === 0) {
            setSelectedVoucher(null);
            setDiscount(0);
        }
    }, [cartItems]);

    console.log("cartID để order:", cartId)


    const handleCheckoutClick = () => {
        // Kiểm tra nếu giỏ hàng có sản phẩm có số lượng = 0
        const hasOutOfStockItems = groupedItems.some(group =>
            group.items.some(item => item.quantity === 0)
        );

        if (hasOutOfStockItems) {
            setShowErrorOutStock(true);
            setTimeout(() => {
                setShowErrorOutStock(false);
            }, 2000);
        } else {
            handleCheckout();
            setIsLoading(true)
        }
    };


    const handleQuantityChange = (itemId, newQuantity) => {
        if (newQuantity > 0) {
            // Update cartItems with the new quantity
            increaseQuantity(itemId, newQuantity);
        } else {
            // If quantity is zero or less, remove the item (optional behavior)
            deleteOneItem(itemId);
        }
    };



    // Group products by productId
    const groupedItems = cartItems.reduce((acc, item) => {
        const existingGroup = acc.find(group => group.id === item.productId);
        if (existingGroup) {
            existingGroup.items.push(item);
        } else {
            acc.push({ id: item.productId, items: [item] });
        }
        return acc;
    }, []);

    // Handle row click to select/deselect item
    const handleRowClick = (itemId) => {
        setSelectedItem(prevItemId => prevItemId === itemId ? null : itemId);
    };

    // Navigate back to menu
    const handBack = () => {
        navigate('/menu');
    };

    const [editingItemId, setEditingItemId] = useState(null);
    const [editingValue, setEditingValue] = useState("");

    // Hàm để lấy danh sách size có sẵn cho sản phẩm
    const fetchAvailableSizes = async (productId) => {
        try {
            const response = await fetch(`http://localhost:1010/api/product/variants/${productId}`);
            const data = await response.json();
            return data.responseList.map(variant => variant.size);
        } catch (error) {
            console.error('Error fetching sizes:', error);
            return [];
        }
    };

    // Thêm useEffect để lấy sizes khi component mount
    useEffect(() => {
        const loadSizes = async () => {
            const sizeMap = {};
            for (const item of cartItems) {
                if (!sizeMap[item.productId]) {
                    sizeMap[item.productId] = await fetchAvailableSizes(item.productId);
                }
            }
            setAvailableSizes(sizeMap);
        };
        loadSizes();
    }, [cartItems]);

    return (
        <>
            <Navbar currentPage={'Giỏ hàng'} />
            <div className="cart-background-container">
                {isCreating && (
                    <div className="loading-overlay active">
                        <div className="loading-spinner"></div>
                    </div>
                )}

                {isLoading && (
                    <div className="loading-animation">
                        <div className="loading-modal">
                            <div className="loading-spinner">
                                <div className="spinner"></div>
                            </div>
                            <h3>Đang xử lý...</h3>
                            <p>Vui lòng đợi trong giây lát</p>
                        </div>
                    </div>
                )}

                {showSuccess && (
                    <div className="success-animation">
                        <div className="success-modal">
                            <div className="success-icon">
                                <div className="success-icon-circle">
                                    <svg className="checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                        <circle className="checkmark-circle" cx="26" cy="26" r="25" fill="none" />
                                        <path className="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
                                    </svg>
                                </div>
                            </div>
                            <h3>Nhận voucher thành công!</h3>
                            <p>Chúc mừng bạn đã nhận được voucher.</p>
                        </div>
                    </div>
                )}

                {showError && (
                    <div className="error-animation">
                        <div className="error-modal">
                            <div className="error-icon">
                                <div className="error-icon-circle">
                                    <svg className="cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                        <circle className="cross-circle" cx="26" cy="26" r="25" fill="none" />
                                        <path className="cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36" />
                                    </svg>
                                </div>
                            </div>
                            <h3>Số lượng phải lớn hơn 0</h3>
                            <p>Vui lòng nhập số lượng khác</p>
                        </div>
                    </div>
                )}


                <div className="cart-all-container">
                    <h1 className="cart-title">Giỏ hàng</h1>
                    <div className="cart-container">
                        {cartItems.length === 0 ? (
                            <p>Giỏ hàng của bạn trống.</p>
                        ) : (
                            <>
                                <section className="cart-details">
                                    <div className="delete-all-button" onClick={handleClearSelectedItems}>
                                        {selectedItem ? `Xóa sản phẩm` : `Xóa tất cả (${totalOfCart} sản phẩm)`}
                                    </div>
                                    <table className="cart-table">
                                        <thead>
                                            <tr>
                                                <th>Hình ảnh</th>
                                                <th>Sản phẩm</th>
                                                <th>Giá</th>
                                                <th>Size</th>
                                                <th>Số lượng</th>
                                                <th>Tổng tiền</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {groupedItems.map((group, index) => {
                                                const totalPrice = group.items.reduce((total, item) => total + item.totalPrice, 0);
                                                return (
                                                    <React.Fragment key={index}>
                                                        {group.items.map((item, subIndex) => (
                                                            <tr
                                                                key={subIndex}
                                                                onClick={() => handleRowClick(item.cartItemId)}
                                                                className={item.quantity === 0 ? 'out-of-stock' : ''}  // Thêm class nếu số lượng = 0
                                                            >
                                                                {subIndex === 0 && (
                                                                    <>
                                                                        <td rowSpan={group.items.length}>
                                                                            <img src={item.image} alt={item.name} className="img-cart" />
                                                                        </td>
                                                                        <td rowSpan={group.items.length}>{item.name}</td>
                                                                    </>
                                                                )}
                                                                <td>{formatCurrency(item.price)}</td>
                                                                <td className={selectedItem === item.cartItemId ? 'selected-size' : ''}>
                                                                    <select
                                                                        value={item.size}
                                                                        onChange={(e) => {
                                                                            const newSize = e.target.value;
                                                                            console.log('Selected Size:', newSize);
                                                                            console.log('Cart Item ID:', item.cartItemId);
                                                                            changeSize(item.cartItemId, newSize);
                                                                        }}
                                                                        onClick={(e) => e.stopPropagation()}
                                                                        className="size-select"
                                                                    >
                                                                        <option value="S">S</option>
                                                                        <option value="M">M</option>
                                                                        <option value="L">L</option>
                                                                    </select>
                                                                </td>
                                                                <td>
                                                                    <div style={{ display: 'flex' }}>
                                                                        <button
                                                                            onClick={(e) => {
                                                                                e.stopPropagation();
                                                                                if (item.quantity > 0) decrease(item.cartItemId);  // Giảm số lượng, không cho phép nhỏ hơn 1
                                                                            }}
                                                                        >
                                                                            -
                                                                        </button>
                                                                        <input
                                                                            type="text"
                                                                            value={editingItemId === item.cartItemId ? editingValue : item.quantity} // Hiển thị giá trị đang chỉnh sửa hoặc giá trị gốc
                                                                            onChange={(e) => {
                                                                                const newQuantity = e.target.value;
                                                                                if (/^\d*$/.test(newQuantity)) { // Cho phép chuỗi rỗng hoặc số
                                                                                    setEditingValue(newQuantity); // Lưu giá trị vào state tạm thời
                                                                                }
                                                                            }}
                                                                            onKeyDown={(e) => {
                                                                                if (e.key === 'Enter') {
                                                                                    const parsedValue = parseInt(editingValue, 10);
                                                                                    if (parsedValue > 0) {
                                                                                        item.quantity = parsedValue; // Cập nhật giá trị vào item tạm thời
                                                                                        handleQuantityChange(item.cartItemId, parsedValue) // Gọi API để cập nhật dữ liệu thực tế
                                                                                            .then(() => {
                                                                                                console.log("Cập nhật thành công trên server:", parsedValue);
                                                                                            })
                                                                                            .catch((error) => {
                                                                                                console.error("Lỗi khi cập nhật server:", error);
                                                                                            });
                                                                                        setEditingItemId(null); // Kết thúc trạng thái chỉnh sửa
                                                                                    } else {
                                                                                        setShowError(true);
                                                                                        setTimeout(() => {
                                                                                            setShowError(false);
                                                                                        }, 2000);
                                                                                    }
                                                                                }
                                                                            }}
                                                                            onFocus={() => {
                                                                                setEditingItemId(item.cartItemId); // Bắt đầu chỉnh sửa
                                                                                setEditingValue(""); // Cho phép nhập giá trị mới
                                                                            }}
                                                                            onBlur={() => {
                                                                                if (!editingValue) {
                                                                                    setEditingValue(item.quantity.toString()); // Hiển thị lại giá trị cũ nếu chưa nhập gì
                                                                                }
                                                                                setEditingItemId(null); // Kết thúc chỉnh sửa
                                                                            }}
                                                                        />

                                                                        <button
                                                                            onClick={(e) => {
                                                                                e.stopPropagation();
                                                                                increase(item.cartItemId);  // Tăng số lượng
                                                                            }}
                                                                        >
                                                                            +
                                                                        </button>
                                                                    </div>
                                                                </td>

                                                                {subIndex === 0 && (
                                                                    <td rowSpan={group.items.length}>{formatCurrency(totalPrice)}</td>
                                                                )}
                                                            </tr>
                                                        ))}
                                                    </React.Fragment>
                                                );
                                            })}
                                        </tbody>
                                    </table>

                                    {/* Voucher selection box */}
                                    {/* <select
                                        className="voucher-select"
                                        value={selectedVoucher || ''}
                                        onChange={handleVoucherChange}  // Dùng handleVoucherChange để xử lý
                                        style={{ marginTop: '20px' }}
                                    >
                                        <option value="">Chọn voucher</option>
                                        {vouchers.map((voucher) => (
                                            <option key={voucher.voucherId} value={voucher.voucherId}>
                                                {voucher.key} - {formatCurrency(voucher.discount)}
                                            </option>
                                        ))}
                                    </select> */}
                                    <div style={{ marginTop: '20px' }}>
                                        <Autocomplete
                                            options={vouchers} // Danh sách vouchers
                                            getOptionLabel={(option) =>
                                                `${option.key} - ${formatCurrency(option.discount)}`
                                            } // Hiển thị key và giảm giá
                                            value={vouchers.find((voucher) => voucher.voucherId === selectedVoucher) || null}
                                            onChange={handleVoucherChange} // Truyền đúng hàm xử lý
                                            renderInput={(params) => (
                                                <TextField {...params} label="Chọn voucher" variant="outlined" />
                                            )} // Render giao diện input
                                            isOptionEqualToValue={(option, value) =>
                                                option.voucherId === value?.voucherId
                                            } // Đảm bảo lựa chọn chính xác
                                            noOptionsText="Không tìm thấy voucher" // Hiển thị khi không có kết quả
                                        />

                                    </div>

                                    <textarea placeholder="GHI CHÚ" className="cart-note" value={note} onChange={handleNoteChange}></textarea>
                                </section>

                                <section className="cart-summary">
                                    <h2>Tạm tính</h2>
                                    <div className="summary-item">
                                        <span>Tổng tiền: </span>
                                        <span>{formatCurrency(subtotal)}</span>
                                    </div>
                                    <div className="summary-item">
                                        <span>Giảm giá: </span>
                                        <span>{formatCurrency(discount)}</span>
                                    </div>
                                    <div className="summary-item">
                                        <span>Phí vận chuyển: </span>
                                        <span>{formatCurrency(shipping)}</span>
                                    </div>
                                    <div className="summary-item total">
                                        <span>Tổng cộng: </span>
                                        <span>{formatCurrency(total)}</span>
                                    </div>

                                    <button className="checkout-button" onClick={handleCheckoutClick}>
                                        Thanh toán <i className='ti-arrow-right' style={{ fontSize: '12px' }} />
                                    </button>

                                    <button className="continue-shopping-button" onClick={handBack}>Tìm thêm sản phẩm khác <i className='ti-arrow-right' style={{ fontSize: '12px' }} /></button>
                                </section>
                            </>
                        )}
                        {showError && (
                            <div className="error-animation">
                                <div className="error-modal">
                                    <div className="error-icon">
                                        <div className="error-icon-circle">
                                            <svg className="cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                                <circle className="cross-circle" cx="26" cy="26" r="25" fill="none" />
                                                <path className="cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36" />
                                            </svg>
                                        </div>
                                    </div>
                                    <h3>Số lượng phải lớn hơn 0!</h3>
                                    <p>Vui lòng chọn lại số lượng phù hợp.</p>
                                </div>
                            </div>
                        )}
                        {showErrorOutStock && (
                            <div className="error-animation">
                                <div className="error-modal">
                                    <div className="error-icon">
                                        <div className="error-icon-circle">
                                            <svg className="cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                                <circle className="cross-circle" cx="26" cy="26" r="25" fill="none" />
                                                <path className="cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36" />
                                            </svg>
                                        </div>
                                    </div>
                                    <h3>Vui lòng xóa các sản phẩm có số lượng bằng 0 trước khi thanh toán.!</h3>                                
                                </div>
                            </div>
                        )}

                    </div>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default Cart;