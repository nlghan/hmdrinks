import React from 'react';
import { useCart } from '../../context/CartContext';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './Cart.css'; // Use a CSS file for styling
import { useNavigate } from 'react-router-dom';

const Cart = () => {
    const navigate = useNavigate();
    const { cartItems, increase, decrease } = useCart(); // Destructure clearCart from context

    // Helper function to format prices in VND
    const formatCurrency = (value) => {
        return new Intl.NumberFormat('vi-VN', {
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
        }).format(value) + ' VND';
    };
    

    // Calculate subtotal, discount, shipping, and total
    const subtotal = cartItems.reduce((acc, item) => acc + item.totalPrice * item.quantity, 0);
    const discount = 0; // Assume no discount for now
    const shipping = 0; // Assume free shipping for now
    const total = subtotal - discount + shipping;

    // Calculate the total number of products in the cart
    const totalProducts = cartItems.reduce((acc, item) => acc + item.quantity, 0);

    // Function to group products by productId
    const groupedItems = cartItems.reduce((acc, item) => {
        const existingGroup = acc.find(group => group.id === item.productId);
        if (existingGroup) {
            existingGroup.items.push(item);
        } else {
            acc.push({ id: item.productId, items: [item] });
        }
        return acc;
    }, []);

    const handBack = () => {
        navigate('/menu');
    };

    const handleClearCart = async () => {
        if (cartId) {
            await clearCart(userId, cartId);; // Use cartId from context
        } else {
            console.error('No cart ID available to clear the cart.');
        }
    };

    return (
        <>
            <Navbar />
            <div className="cart-background-container">
                <div className="cart-all-container">
                    <h1 className="cart-title">Giỏ hàng</h1>
                    <div className="cart-container">
                        {cartItems.length === 0 ? (
                            <p>Giỏ hàng của bạn trống.</p>
                        ) : (
                            <>
                                <section className="cart-details">
                                    <div className="delete-all-button" onClick={''}>
                                        Xóa tất cả ({totalProducts} sản phẩm)
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
                                                        <tr className="item-row">
                                                            <td rowSpan={group.items.length}>
                                                                <img src={group.items[0].image} alt={group.items[0].name} className="img-cart" />
                                                            </td>
                                                            <td rowSpan={group.items.length}>{group.items[0].name}</td>
                                                            <td>{formatCurrency(group.items[0].totalPrice)}</td>
                                                            <td>{group.items[0].size}</td>
                                                            <td>
                                                                <div>
                                                                    <button onClick={() => decrease(group.items[0].cartItemId)}>-</button>
                                                                    {group.items[0].quantity}
                                                                    <button onClick={() => increase(group.items[0].cartItemId)}>+</button>
                                                                </div>
                                                            </td>
                                                            <td rowSpan={group.items.length}>{formatCurrency(totalPrice)}</td>
                                                        </tr>
                                                        {group.items.slice(1).map((item, subIndex) => (
                                                            <tr key={subIndex}>
                                                                <td>{formatCurrency(item.totalPrice)}</td>
                                                                <td>{item.size}</td>
                                                                <td>
                                                                    <div>
                                                                        <button onClick={() => decrease(item.cartItemId)}>-</button>
                                                                        {item.quantity}
                                                                        <button onClick={() => increase(item.cartItemId)}>+</button>
                                                                    </div>
                                                                </td>
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
                                    <button className="checkout-button">Thanh toán <i className='ti-arrow-right' style={{fontSize:'12px'}}/></button>
                                    <button className="continue-shopping-button" onClick={handBack}>Tìm thêm sản phẩm khác <i className='ti-arrow-right' style={{fontSize:'12px'}}/></button>
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
