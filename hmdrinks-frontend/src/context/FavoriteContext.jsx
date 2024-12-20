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
        const token = getCookie('access_token');

        // Only proceed if the user is logged in (token is available)
        if (!token) {
            console.warn("User is not logged in. No favorite items to fetch.");
            return;
        }

        try {
            // Decode userId from token
            const userId = getUserIdFromToken(token);
            if (!userId) {
                console.error("User ID not found in token.");
                return;
            }

            // Fetch favId for the logged-in user
            const favResponse = await axios.get(`http://localhost:1010/api/fav/list-fav/${userId}`, {
                headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
            });

            const favId = favResponse.data.favId;
            if (!favId) {
                console.error("Favorite ID not found for the user.");
                return;
            }

            // Fetch favorite items based on favId
            const favItemsResponse = await axios.get(`http://localhost:1010/api/fav/list-favItem/${favId}`, {
                headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
            });

            const items = favItemsResponse.data.favouriteItemResponseList || [];
            setFavoriteItems(items);

            // Fetch product details for each favorite item
            const productRequests = items.map(item =>
                axios.get(`http://localhost:1010/api/product/view/${item.proId}`, {
                    headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
                })
            );
            const productResponses = await Promise.all(productRequests);

            // Fetch prices for each product variant
            const priceRequests = items.map(item =>
                axios.get(`http://localhost:1010/api/product/variants/${item.proId}`, {
                    headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
                })
            );
            const priceResponses = await Promise.all(priceRequests);

            // Construct products details with name, images, category, and price
            const products = productResponses.reduce((acc, response, index) => {
                const product = response.data;
                const priceData = priceResponses[index].data.responseList;

                // Assuming the first variant is the default
                const variantPrice = priceData.length > 0 ? priceData[0].price : null;

                acc[product.proId] = {
                    proName: product.proName,
                    images: product.productImageResponseList.map(img => img.linkImage),
                    cateId: product.cateId,
                    price: variantPrice
                };
                return acc;
            }, {});
            setProductDetails(products);

            // Fetch category details for each product's category
            const categoryRequests = Object.values(products).map(product =>
                axios.get(`http://localhost:1010/api/cate/view/${product.cateId}`, {
                    headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
                })
            );
            const categoryResponses = await Promise.all(categoryRequests);

            // Construct category details with name and image
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
            console.error("Error fetching favorite items:", error);
            // setErrorMessage("An error occurred while fetching favorite items.");
        }
    };

    useEffect(() => {
        fetchFavoriteItems();
    }, [token]);

    // Add product to favorites
    // Add product to favorites
    // Add product to favorites
    const addFavorite = async (product) => {
        const userId = getUserIdFromToken(token);
        let favId;

        try {
            // Step 1: Attempt to create a new favorite list
            const createFavResponse = await axios.post(
                'http://localhost:1010/api/fav/create',
                { userId },
                { headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json', 'accept': '*/*' } }
            );

            // If the creation was successful, retrieve the new favId
            if (createFavResponse.data && createFavResponse.data.favId) {
                favId = createFavResponse.data.favId; // New favorite list ID
            }
        } catch (error) {
            console.error("Network error when creating favorite:", error);
        }

        // Step 2: If creation failed with statusCodeValue 409, fetch the existing favorite list
        if (!favId) {
            try {
                console.log("Favorite list already exists, fetching existing favorite list...");
                const favResponse = await axios.get(`http://localhost:1010/api/fav/list-fav/${userId}`, {
                    headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
                });

                if (favResponse.data && favResponse.data.favId) {
                    favId = favResponse.data.favId; // Existing favorite list ID
                } else {
                    console.error("No favorite list found after failed creation.");
                    return; // Exit if unable to retrieve existing list
                }
            } catch (fetchError) {
                setErrorMessage("Error fetching existing favorite list.");
                console.error("Fetch existing favorite error:", fetchError);
                return; // Exit if unable to fetch existing list
            }
        }

        // Step 3: Fetch product variants to get the price based on size
        let variantPrice = null; // Initialize variantPrice
        try {
            const variantResponse = await fetch(`http://localhost:1010/api/product/variants/${product.proId}`, {
                method: 'GET',
                headers: {
                    'accept': '*/*',
                    'Authorization': `Bearer ${token}`,
                },
            });

            // Check if the response is OK
            if (!variantResponse.ok) {
                const errorDetails = await variantResponse.text();
                console.error("Failed to fetch product variants:", errorDetails);
                throw new Error(`Error fetching product variants: ${variantResponse.statusText}`);
            }

            // Parse the JSON response
            const variantsData = await variantResponse.json();
            console.log("Variants Data:", variantsData); // Log the entire variants data for debugging

            // Step 4: Find the price of the selected size
            const selectedVariant = variantsData.responseList.find(variant => variant.size === product.size);
            console.log("Selected Variant:", selectedVariant); // Log the selected variant for debugging

            if (selectedVariant) {
                variantPrice = selectedVariant.price; // Get the price for the selected size
                console.log(`Price for size ${product.size}: ${variantPrice}`); // Log the found price
            } else {
                console.error("No matching variant found for the specified size");
                return; // Exit if no matching variant
            }

        } catch (error) {
            console.error("Error fetching product variants:", error);
            setErrorMessage("Error fetching product variants.");
            return; // Exit to prevent further execution
        }

        // Step 5: Now that we have a valid favId, add the item to the favorite list
        try {
            const addItemResponse = await axios.post(
                'http://localhost:1010/api/fav-item/insert',
                { userId, favId, proId: product.proId, size: product.size }, // Do not include price here
                { headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json', 'accept': '*/*' } }
            );

            // Update the state with the new favorite item
            setFavoriteItems((prevItems) => [...prevItems, addItemResponse.data]);

            // Step 6: Fetch product details after adding to favorites
            const productResponse = await axios.get(`http://localhost:1010/api/product/view/${product.proId}`, {
                headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
            });

            const productDetails = {
                proId: productResponse.data.proId,
                proName: productResponse.data.proName,
                images: productResponse.data.productImageResponseList.map(img => img.linkImage),
                cateId: productResponse.data.cateId,
                price: variantPrice // Directly use the variantPrice here
            };

            // Update product details state with the newly added product
            setProductDetails((prevDetails) => ({
                ...prevDetails,
                [productDetails.proId]: productDetails
            }));

            console.log("Product details:", productDetails);

            // Step 7: Fetch category details for the newly added product
            const categoryResponse = await axios.get(`http://localhost:1010/api/cate/view/${productDetails.cateId}`, {
                headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
            });

            const categoryDetails = {
                cateId: categoryResponse.data.cateId,
                cateName: categoryResponse.data.cateName,
                cateImg: categoryResponse.data.cateImg,
            };

            // Update category details state with the newly added category
            setCategoryDetails((prevCategories) => ({
                ...prevCategories,
                [categoryDetails.cateId]: categoryDetails
            }));

        } catch (addError) {
            setErrorMessage("Error adding product to favorites.");
            console.error("Add favorite error:", addError);
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
    // Add deleteAll function in FavoriteProvider
    const deleteAll = async () => {
        const userId = getUserIdFromToken(token);
        let favId;

        try {
            // First, fetch the user's favorite ID
            const favResponse = await axios.get(`http://localhost:1010/api/fav/list-fav/${userId}`, {
                headers: { 'Authorization': `Bearer ${token}`, 'accept': '*/*' }
            });

            if (favResponse.data && favResponse.data.favId) {
                favId = favResponse.data.favId;
            } else {
                console.error("Favorite ID not found for the user.");
                return; // Exit if there's no favorite ID
            }

            // Make the DELETE request to delete all items
            const deleteResponse = await axios.delete(`http://localhost:1010/api/fav/delete-allItem/${favId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                    'accept': '*/*'
                },
                data: { userId, favId }
            });

            // Check if the deletion was successful
            if (deleteResponse.data.message === "Delete all item success") {
                console.log(deleteResponse.data.message);
                setFavoriteItems([]); // Clear the local state for favorite items
            } else {
                console.error("Failed to delete all favorite items.");
            }
        } catch (error) {
            setErrorMessage("Error deleting all favorite items.");
            console.error("Delete all favorites error:", error);
        }
    };

    const favoriteCount = favoriteItems.length;
    const clearFavorites = () => {
        setFavoriteItems([]); // Xóa danh sách yêu thích trong state
        setProductDetails({});
        setCategoryDetails({});
        setErrorMessage(null);
    };



    return (
        <FavoriteContext.Provider value={{ favoriteItems, productDetails, categoryDetails, addFavorite, removeFavorite, errorMessage, deleteAll, favoriteCount, clearFavorites }}>
            {children}
        </FavoriteContext.Provider>
    );
};
