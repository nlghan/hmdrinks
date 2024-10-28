import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

// Initialize FavoriteContext
const FavoriteContext = createContext();

// Custom hook to use FavoriteContext
export const useFavorite = () => {
    return useContext(FavoriteContext);
};

export const FavoriteProvider = ({ children }) => {
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

    // Fetch favorite items on mount
    const fetchFavoriteItems = async () => {
        const userId = getUserIdFromToken(token);
        try {
            const favResponse = await axios.get(`http://localhost:1010/api/fav/list-favItem/${userId}`, {
                headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
            });

            const items = favResponse.data.favouriteItemResponseList;
            setFavoriteItems(items);

            const productRequests = items.map(item =>
                axios.get(`http://localhost:1010/api/product/view/${item.proId}`, {
                    headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
                })
            );
            const productResponses = await Promise.all(productRequests);
            const products = productResponses.reduce((acc, response) => {
                const product = response.data;
                acc[product.proId] = {
                    proName: product.proName,
                    images: product.productImageResponseList.map(img => img.linkImage),
                    cateId: product.cateId
                };
                return acc;
            }, {});
            setProductDetails(products);

            const categoryRequests = Object.values(products).map(product =>
                axios.get(`http://localhost:1010/api/cate/view/${product.cateId}`, {
                    headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
                })
            );
            const categoryResponses = await Promise.all(categoryRequests);
            const categories = categoryResponses.reduce((acc, response) => {
                const category = response.data;
                acc[category.cateId] = {
                    cateName: category.cateName,
                    cateImg: category.cateImg
                };
                return acc;
            }, {});
            setCategoryDetails(categories);
        } catch (error) {
            setErrorMessage("An error occurred while fetching favorite items.");
            console.error(error);
        }
    };

    useEffect(() => {
        fetchFavoriteItems();
    }, [token]);

    // Add product to favorites
    const addFavorite = async (product) => {
        const userId = getUserIdFromToken(token);
        let favId;

        try {
            const favResponse = await axios.get(`http://localhost:1010/api/fav/list-fav/${userId}`, {
                headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
            });

            if (favResponse.data && favResponse.data.favId) {
                favId = favResponse.data.favId;
            } else {
                const createFavResponse = await axios.post(
                    'http://localhost:1010/api/fav/create',
                    { userId },
                    { headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json', 'accept': '*/*' } }
                );
                favId = createFavResponse.data.favId;
            }

            const addItemResponse = await axios.post(
                'http://localhost:1010/api/fav-item/insert',
                { userId, favId, proId: product.proId, size: product.size },
                { headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json', 'accept': '*/*' } }
            );

            setFavoriteItems((prevItems) => [...prevItems, addItemResponse.data]);
            // Fetch the updated favorite items
            await fetchFavoriteItems();
        } catch (error) {
            setErrorMessage("Error adding product to favorites.");
            console.error("Add favorite error:", error);
        }
    };

    // Remove product from favorites
    // Remove product from favorites
const removeFavorite = async (product, source) => {
    const userId = getUserIdFromToken(token);

    // If called from ProductCard, check for proId
    if (source === 'productCard') {
        const favItem = favoriteItems.find(item => item.proId === product.proId); // Find the favItem based on proId
        
        if (!favItem) {
            console.error("Favorite item not found");
            return; // Item is not in favorites
        }

        try {
            await axios.delete(`http://localhost:1010/api/fav-item/delete/${favItem.favItemId}`, {
                headers: { 
                    'Authorization': `Bearer ${token}`, 
                    'Content-Type': 'application/json', 
                    'accept': '*/*' 
                },
                data: { userId, favItemId: favItem.favItemId }
            });
            setFavoriteItems((prevItems) => prevItems.filter(item => item.favItemId !== favItem.favItemId));
        } catch (error) {
            setErrorMessage("Error removing product from favorites.");
            console.error(error);
        }
    } else if (source === 'favorite') {
        // If called from Favorite page, use the passed favItemId directly
        try {
            await axios.delete(`http://localhost:1010/api/fav-item/delete/${product.favItemId}`, {
                headers: { 
                    'Authorization': `Bearer ${token}`, 
                    'Content-Type': 'application/json', 
                    'accept': '*/*' 
                },
                data: { userId, favItemId: product.favItemId }
            });
            setFavoriteItems((prevItems) => prevItems.filter(item => item.favItemId !== product.favItemId));
        } catch (error) {
            setErrorMessage("Error removing product from favorites.");
            console.error(error);
        }
    } else {
        console.error("Invalid source provided to removeFavorite function.");
    }
};


    return (
        <FavoriteContext.Provider value={{ favoriteItems, productDetails, categoryDetails, addFavorite, removeFavorite, errorMessage }}>
            {children}
        </FavoriteContext.Provider>
    );
};
