import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './Favorite.css';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import FavCard from '../../components/Card/FavCard';
import la from "../../assets/img/la.png"

const Favorite = () => {
    const navigate = useNavigate();
    const [favoriteItems, setFavoriteItems] = useState([]);
    const [productDetails, setProductDetails] = useState({});
    const [categoryDetails, setCategoryDetails] = useState({});
    const [errorMessage, setErrorMessage] = useState(null);
    const [visibleIndex, setVisibleIndex] = useState(0); // Manage visible items index

    const itemsPerPage = 3; // Maximum items to show at a time

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

    const token = getCookie('access_token');

    useEffect(() => {
        const fetchFavoriteItems = async () => {
            const userId = getUserIdFromToken(token);
            try {
                const response = await axios.get(`http://localhost:1010/api/fav/list-favItem/${userId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'accept': '*/*'
                    }
                });

                const items = response.data.favouriteItemResponseList;
                setFavoriteItems(items);

                const productRequests = items.map(item => 
                    axios.get(`http://localhost:1010/api/product/view/${item.proId}`, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'accept': '*/*'
                        }
                    })
                );

                const productResponses = await Promise.all(productRequests);
                const products = productResponses.reduce((acc, productResponse) => {
                    const productData = productResponse.data;
                    acc[productData.proId] = {
                        proName: productData.proName,
                        images: productData.productImageResponseList.map(img => img.linkImage),
                        cateId: productData.cateId
                    };
                    return acc;
                }, {});

                setProductDetails(products);

                const categoryRequests = Object.values(products).map(product =>
                    axios.get(`http://localhost:1010/api/cate/view/${product.cateId}`, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'accept': '*/*'
                        }
                    })
                );

                const categoryResponses = await Promise.all(categoryRequests);
                const categories = categoryResponses.reduce((acc, categoryResponse) => {
                    const categoryData = categoryResponse.data;
                    acc[categoryData.cateId] = {
                        cateName: categoryData.cateName,
                        cateImg: categoryData.cateImg
                    };
                    return acc;
                }, {});

                setCategoryDetails(categories);
            } catch (error) {
                setErrorMessage("An error occurred while fetching favorite items. Please try again.");
                console.error(error);
            }
        };

        fetchFavoriteItems();
    }, [token]);

    const handleDeleteFavorite = async (favItemId) => {
        const userId = getUserIdFromToken(token);
        try {
            await axios.delete(`http://localhost:1010/api/fav-item/delete/${favItemId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'accept': '*/*',
                    'Content-Type': 'application/json'
                },
                data: {
                    userId: userId,
                    favItemId: favItemId
                }
            });

            setFavoriteItems(prevItems => prevItems.filter(item => item.favItemId !== favItemId));
        } catch (error) {
            setErrorMessage("An error occurred while deleting the favorite item. Please try again.");
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
                <image src={la}/>
                
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
                                    cateName: categoryDetails[productDetails[item.proId]?.cateId]?.cateName || "Loading...", 
                                }} 
                                onClick={() => navigate(`/product/${item.proId}`)}
                                onDeleteFavorite={() => handleDeleteFavorite(item.favItemId)} 
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
