import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './Favorite.css';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import FavCard from '../../components/Card/FavCard';

const Favorite = () => {
    const navigate = useNavigate();
    const [favoriteItems, setFavoriteItems] = useState([]);
    const [productDetails, setProductDetails] = useState({});
    const [categoryDetails, setCategoryDetails] = useState({});
    const [errorMessage, setErrorMessage] = useState(null);

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

    // Fetch favorite items, product details, and category details on component mount
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
                
                // Fetch product details for each item
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

                // Fetch category details for each product
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
                    favItemId: favItemId // Pass the ID of the favorite item to delete
                }
            });

            // Filter out the deleted item from the state
            setFavoriteItems(prevItems => prevItems.filter(item => item.favItemId !== favItemId));
        } catch (error) {
            setErrorMessage("An error occurred while deleting the favorite item. Please try again.");
            console.error("Delete error:", error);
        }
    };

    return (
        <>
            <Navbar currentPage={'Yêu thích'} />
            {errorMessage && <p className="error-message">{errorMessage}</p>}
            
            <div className="fav-container">
                <h1 className="fav-title">Danh sách yêu thích</h1>
                <div className="favorites-container">
                    {favoriteItems.map(item => (
                        <FavCard 
                            key={item.favItemId} 
                            product={{
                                proId: item.proId, 
                                size: item.size,
                                proName: productDetails[item.proId]?.proName || "Loading...", // Default loading text
                                images: productDetails[item.proId]?.images || [], // Pass all images to the slider
                                cateName: categoryDetails[productDetails[item.proId]?.cateId]?.cateName || "Loading...", // Default loading text for category
                            }} 
                            onClick={() => navigate(`/product/${item.proId}`)} // Navigate to product details on card click
                            onDeleteFavorite={() => handleDeleteFavorite(item.favItemId)} // Pass the delete function
                        />
                    ))}
                </div>
            </div>
            <Footer />
        </>
    );
};

export default Favorite;
