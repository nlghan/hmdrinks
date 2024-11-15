import React, { useState, useEffect } from 'react';
import { useCart } from '../../context/CartContext';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './Cart.css'; // Use a CSS file for styling
import { useNavigate } from 'react-router-dom';

const Cart = () => {
    const navigate = useNavigate();
    const { cartItems, increase, decrease, clearCart, deleteOneItem, cartId,  selectedVoucher, note, setSelectedVoucher, setNote, isCreating, handleCheckout } = useCart();
    const [vouchers, setVouchers] = useState([]);

    const [selectedItem, setSelectedItem] = useState(null);
    

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
    const subtotal = cartItems.reduce((acc, item) => acc + item.totalPrice * item.quantity, 0);
    const [discount, setDiscount] = useState(0);
    const shipping = 0; // Assume free shipping for now
    const total = subtotal - discount + shipping;

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


    const handleVoucherChange = async (e) => {
        const selectedVoucherId = e.target.value;
        setSelectedVoucher(selectedVoucherId);

        console.log("Selected Voucher ID: ", selectedVoucherId);  // Log ID chọn được

        if (selectedVoucherId) {
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
                    setDiscount(discount);  // Cập nhật giá trị giảm giá
                    console.log("Giảm giá nè: ", discount);  // Log giảm giá
                } else {
                    // Nếu API trả về lỗi hoặc không có voucher
                    setDiscount(0);
                    console.error("Không tìm thấy voucher hoặc có lỗi trong việc lấy dữ liệu.");
                }
            } catch (error) {
                console.error("Có lỗi xảy ra khi gọi API: ", error);
                setDiscount(0);  // Nếu có lỗi trong API call, set discount = 0
            }
        } else {
            setDiscount(0);  // Nếu không có voucher chọn thì giảm giá = 0
        }
    };

    console.log("cartID để order:", cartId)

    
    const handleCheckoutClick = () => {
        handleCheckout();  // Call handleCheckout when user clicks "Checkout"

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
    

    return (
        <>
            <Navbar currentPage={'Giỏ hàng'} />
            <div className="cart-background-container">
            {isCreating && (
                <div className="loading-overlay active">
                    <div className="loading-spinner"></div>
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
                                        {selectedItem ? `Xóa sản phẩm` : `Xóa tất cả (${cartItems.length} sản phẩm)`}
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
                                                const totalPrice = group.items.reduce((total, item) => total + item.totalPrice * item.quantity, 0);
                                                return (
                                                    <React.Fragment key={index}>
                                                        {group.items.map((item, subIndex) => (
                                                            <tr
                                                                key={subIndex}
                                                                onClick={() => handleRowClick(item.cartItemId)}
                                                            >
                                                                {subIndex === 0 && (
                                                                    <>
                                                                        <td rowSpan={group.items.length}>
                                                                            <img src={item.image} alt={item.name} className="img-cart" />
                                                                        </td>
                                                                        <td rowSpan={group.items.length}>{item.name}</td>
                                                                    </>
                                                                )}
                                                                <td>{formatCurrency(item.totalPrice)}</td>
                                                                <td className={selectedItem === item.cartItemId ? 'selected-size' : ''}>
                                                                    {item.size}
                                                                </td>
                                                                <td>
                                                                    <div>
                                                                        <button
                                                                            onClick={(e) => {
                                                                                e.stopPropagation();
                                                                                decrease(item.cartItemId);
                                                                            }}
                                                                        >
                                                                            -
                                                                        </button>
                                                                        {item.quantity}
                                                                        <button
                                                                            onClick={(e) => {
                                                                                e.stopPropagation();
                                                                                increase(item.cartItemId);
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
                                    <select
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
                                    </select>


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
                    </div>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default Cart;