import React, { useState } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import ProductCard from '../../components/Card/ProductCard';
import './Favorite.css';
import backgroundImage from '../../assets/img/2.jpg'; // Use the same background image
import { useNavigate } from 'react-router-dom';
import { useCart } from '../../context/CartContext';

const Favorite = () => {
    const navigate = useNavigate();
    const { addToCart } = useCart(); // Cart context

    // Static favorite products for now
    const favoriteProducts = [
        {
            proId: 1,
            name: 'Trà Sữa Matcha',
            size: 'M',
            price: '45.000',
            image: 'https://example.com/matcha.jpg',
            category: 'Trà Sữa',
        },
        {
            proId: 2,
            name: 'Trà Đào Cam Sả',
            size: 'L',
            price: '50.000',
            image: 'https://example.com/tra-dao.jpg',
            category: 'Trà Trái Cây',
        },
        // Add more favorite products with categories here
    ];

    // Static categories for now
    const categories = [
        { cateId: 1, cateName: 'Trà Sữa' },
        { cateId: 2, cateName: 'Trà Trái Cây' },
        { cateId: 3, cateName: 'Sinh Tố' },
        // Add more categories as needed
    ];

    const [selectedCategoryId, setSelectedCategoryId] = useState(null); // State for selected category

    // Handle category selection
    const handleCategorySelect = (categoryId) => {
        setSelectedCategoryId(categoryId);
    };

    // Filter favorite products based on the selected category
    const filteredProducts = selectedCategoryId
        ? favoriteProducts.filter(product => product.category === categories.find(c => c.cateId === selectedCategoryId)?.cateName)
        : favoriteProducts;

    // Handle adding product to cart
    const handleAddToCart = (product) => {
        addToCart({
            productId: product.proId,
            name: product.name,
            price: product.price,
            size: product.size,
            quantity: 1,
            image: product.image,
        });
        alert(`${product.name} đã được thêm vào giỏ hàng!`);
    };

    // Handle product card click (navigate to product detail)
    const handleProductCardClick = (product) => {
        navigate(`/product/${product.proId}`, { state: { product } });
    };

    return (
        <>
            <Navbar currentPage={'Yêu thích'} />
            <div
                style={{
                    position: 'relative',
                    minHeight: '100vh',
                    display: 'flex',
                }}
            >
                {/* Background with overlay */}
                <div
                    style={{
                        background: `url(${backgroundImage}) no-repeat center center fixed`,
                        backgroundSize: 'cover',
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        width: '100%',
                        height: '100%',
                        opacity: 0.5,
                        zIndex: -1,
                    }}
                ></div>

                

                
            </div>
            <Footer />
        </>
    );
};

export default Favorite;
