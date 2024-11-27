import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './Favorite.css';
import { useNavigate } from 'react-router-dom';
import FavCard from '../../components/Card/FavCard';
import { useFavorite } from '../../context/FavoriteContext';
import { useCart } from '../../context/CartContext'; // Import your Cart context
import la from "../../assets/img/la.png";

const Favorite = () => {
    const navigate = useNavigate();
    const { favoriteItems, productDetails, categoryDetails, removeFavorite, errorMessage, deleteAll } = useFavorite();
    const { addToCart } = useCart(); // Destructure addToCart from Cart context
    const [visibleIndex, setVisibleIndex] = useState(0);
    const itemsPerPage = 3; // Maximum items to show at a time
    const [showSuccess, setShowSuccess] = useState(false);
    const [showError, setShowError] = useState(false);

    const handleDeleteFavorite = async (favItemId) => {
        try {
            await removeFavorite({ favItemId }, 'favorite'); // Specify source as 'favorite'
        } catch (error) {
            console.error("Delete error:", error);
        }
    };

    const handleDeleteAll = async () => {
        deleteAll();
    };

    const handleNext = () => {
        if (visibleIndex + itemsPerPage < favoriteItems.length) {
            setVisibleIndex(visibleIndex + itemsPerPage);
        }
    };

    const handlePrev = () => {
        if (visibleIndex - itemsPerPage >= 0) {
            setVisibleIndex(visibleIndex - itemsPerPage);
        }
    };

    const handleAddToCart = async (proId, size, quantity, proName) => {
        try {
            // Gọi addToCart với cấu trúc sản phẩm yêu cầu
            const { status, message } = await addToCart({
                productId: proId,
                size: size,
                quantity: quantity, // Truyền quantity vào đây
                name: proName // Truyền tên sản phẩm vào đây
            });

            if (status === 400) {                // Nếu API trả về lỗi 400, hiển thị thông báo lỗi và không hiển thị success
                setMessage(message || "Đã đạt giới hạn số lượng cho sản phẩm này!");
                setShowSuccess(false);
                return;
            }
            // // Nếu thành công (status 200), hiển thị thông báo thành công
            console.log("check TF", showSuccess);
            setShowSuccess(true);
            setMessage(message || "Thêm vào giỏ hàng thành công!");
            setTimeout(() => {
                setShowSuccess(false);
            }, 2000);

        } catch (error) {
            // Xử lý lỗi không mong muốn trong quá trình gọi API
            setMessage("Đã có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng.");
            setShowSuccess(false);
        }
    };

    return (
        <>
            <Navbar currentPage={'Yêu thích'} />
            {errorMessage && <p className="error-message">{errorMessage}</p>}

            <div className="fav-container">
                <h1 className="fav-title">Danh sách yêu thích</h1>
                <div className='delete-all-button' style={{ marginBottom: '0px' }} onClick={handleDeleteAll}>Xóa tất cả</div>
                <div className="carousel-container">

                    <button onClick={handlePrev} disabled={visibleIndex === 0} className="carousel-button"><i className='ti-arrow-left' /></button>

                    <div className="favorites-container">
                        {favoriteItems.slice(visibleIndex, visibleIndex + itemsPerPage).map(item => (
                            <FavCard
                                key={item.favItemId}
                                product={{
                                    proId: item.proId,
                                    size: item.size,
                                    price: productDetails[item.proId]?.price,
                                    proName: productDetails[item.proId]?.proName || "Loading...",
                                    images: productDetails[item.proId]?.images || [],
                                    cateName: categoryDetails[productDetails[item.proId]?.cateId]?.cateName || "Loading..."
                                }}
                                onClick={() => handleAddToCart(item.proId, item.size, 1, productDetails[item.proId]?.proName || "Unknown Product")} // Pass the product name
                                onDeleteFavorite={() => handleDeleteFavorite(item.favItemId)} // Pass favItemId here
                            />
                        ))}
                        {showSuccess && (
                            <div className="product-card-success-animation">
                                <div className="product-card-success-modal">
                                    <div className="product-card-success-icon">
                                        <div className="product-card-success-icon-circle">
                                            <svg className="product-card-checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                                                <circle className="product-card-checkmark-circle" cx="26" cy="26" r="25" fill="none" />
                                                <path className="product-card-checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
                                            </svg>
                                        </div>
                                    </div>
                                    <h3>Thêm vào giỏ hàng thành công!</h3>
                                </div>
                            </div>
                        )}
                    </div>

                    <button onClick={handleNext} disabled={visibleIndex + itemsPerPage >= favoriteItems.length} className="carousel-button"><i className='ti-arrow-right' /></button>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default Favorite;
