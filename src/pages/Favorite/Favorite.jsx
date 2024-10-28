import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './Favorite.css';
import { useNavigate } from 'react-router-dom';
import FavCard from '../../components/Card/FavCard';
import { useFavorite } from '../../context/FavoriteContext';
import la from "../../assets/img/la.png";

const Favorite = () => {
    const navigate = useNavigate();
    const { favoriteItems, productDetails, categoryDetails, removeFavorite, errorMessage } = useFavorite();
    const [visibleIndex, setVisibleIndex] = useState(0);

    const itemsPerPage = 3; // Maximum items to show at a time

    const handleDeleteFavorite = async (favItemId) => {
        try {
            // Pass an object with favItemId to remove from favorites
            await removeFavorite({ favItemId }, 'favorite'); // Specify source as 'favorite'
        } catch (error) {
            console.error("Delete error:", error);
        }
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

    return (
        <>
            <Navbar currentPage={'Yêu thích'} />
            {errorMessage && <p className="error-message">{errorMessage}</p>}
            
            <div className="fav-container">
                <h1 className="fav-title">Danh sách yêu thích</h1>
                
                <div className="carousel-container">
                    <button onClick={handlePrev} disabled={visibleIndex === 0} className="carousel-button">{"<"}</button>
                    
                    <div className="favorites-container">
                        {favoriteItems.slice(visibleIndex, visibleIndex + itemsPerPage).map(item => (
                            <FavCard 
                                key={item.favItemId} 
                                product={{
                                    proId: item.proId, 
                                    size: item.size,
                                    proName: productDetails[item.proId]?.proName || "Loading...", 
                                    images: productDetails[item.proId]?.images || [], 
                                    cateName: categoryDetails[productDetails[item.proId]?.cateId]?.cateName || "Loading..."
                                }} 
                                onClick={() => navigate(`/product/${item.proId}`)}
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
