import React, { useState } from 'react';
import { useCart } from '../../context/CartContext';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './Cart.css'; // Use a CSS file for styling
import { useNavigate } from 'react-router-dom';

const Cart = () => {
    const navigate = useNavigate();
    const { cartItems, increase, decrease, clearCart, deleteOneItem } = useCart(); // Destructure clearCart and deleteOneItem from context

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
            await deleteOneItem(selectedItem); // Delete the selected item using deleteOneItem
            setSelectedItem(null); // Reset selected item after deletion
        } else {
            await clearCart(); // Clear the entire cart if no selected item
        }
    };

    // Calculate subtotal, discount, shipping, and total
    const subtotal = cartItems.reduce((acc, item) => acc + item.totalPrice * item.quantity, 0);
    const discount = 0; // Assume no discount for now
    const shipping = 0; // Assume free shipping for now
    const total = subtotal - discount + shipping;

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

    const [selectedItem, setSelectedItem] = useState(null); // State to store selected item

    // Handle row click to select/deselect item
    const handleRowClick = (itemId) => {
        setSelectedItem(prevItemId => prevItemId === itemId ? null : itemId);
    };

    // Navigate back to menu
    const handBack = () => {
        navigate('/menu');
    };

    // Calculate the total number of products in the cart (this is recalculated after any item is deleted or quantity changes)
    const totalProducts = cartItems.reduce((acc, item) => acc + item.quantity, 0);

    return (
        <>
            <Navbar currentPage={'Giỏ hàng'}/>
            <div className="cart-background-container">
                <div className="cart-all-container">
                    <h1 className="cart-title">Giỏ hàng</h1>
                    <div className="cart-container">
                        {cartItems.length === 0 ? (
                            <p>Giỏ hàng của bạn trống.</p>
                        ) : (
                            <>
                                <section className="cart-details">
                                    <div className="delete-all-button" onClick={handleClearSelectedItems}>
                                        {selectedItem ? `Xóa sản phẩm` : `Xóa tất cả (${totalProducts} sản phẩm)`}
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
                                                                onClick={() => handleRowClick(item.cartItemId)} // Handle row click
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
                                                                {/* Apply special class to the Size column when selected */}
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
                                                                                increase(item.cartItemId); // This will now check stock before increasing
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

                                    <textarea placeholder="GHI CHÚ" className="cart-note"></textarea>
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
                                    <button className="checkout-button">Thanh toán <i className='ti-arrow-right' style={{ fontSize: '12px' }} /></button>
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
