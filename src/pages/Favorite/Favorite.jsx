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
        // Call addToCart with the required product structure, including product name
        await addToCart({
            productId: proId,
            size: size,
            quantity: quantity, // Pass the quantity here
            name: proName // Pass the product name here
        });
    };

    return (
        <>
            <Navbar currentPage={'Yêu thích'} />
            {errorMessage && <p className="error-message">{errorMessage}</p>}
            
            <div className="fav-container">
                <h1 className="fav-title">Danh sách yêu thích</h1>
                <div className='delete-all-button' style={{ marginBottom: '0px' }} onClick={handleDeleteAll}>Xóa tất cả</div>
                <div className="carousel-container">
                    
                    <button onClick={handlePrev} disabled={visibleIndex === 0} className="carousel-button">{"<"}</button>
                    
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
                    </div>

                    <button onClick={handleNext} disabled={visibleIndex + itemsPerPage >= favoriteItems.length} className="carousel-button">{">"}</button>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default Favorite;
