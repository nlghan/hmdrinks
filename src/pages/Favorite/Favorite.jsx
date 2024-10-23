import React, { useState } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import ProductCard from '../../components/Card/ProductCard';
import './Favorite.css';
import backgroundImage from '../../assets/img/5.jpg'; // Use the same background image
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
            <Navbar />
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
                        opacity: 0.25,
                        zIndex: -1,
                    }}
                ></div>

                {/* Favorite products content */}
                <div className="container-favorite">
                    <div className="overlay-favorite">
                        <div className='menu-category'>
                            <h2 className='menu-product-h2'>DANH MỤC YÊU THÍCH</h2>
                            <ul className="menu-product-category">
                                <li
                                    onClick={() => handleCategorySelect(null)}
                                    className={!selectedCategoryId ? 'active-category' : ''}
                                >
                                    ❤️ Tất cả
                                </li>
                                {categories.map((category) => (
                                    <li
                                        key={category.cateId}
                                        onClick={() => handleCategorySelect(category.cateId)}
                                        className={selectedCategoryId === category.cateId ? 'active-category' : ''}
                                    >
                                        ❤️ {category.cateName}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </div>

                    <h2 className='favorite-title'>Sản Phẩm Yêu Thích</h2>
                    <div className="favorite-products">
                        {filteredProducts.length > 0 ? (
                            filteredProducts.map((product, index) => (
                                <ProductCard
                                    key={product.proId}
                                    product={{
                                        name: product.name,
                                        size: product.size,
                                        price: product.price,
                                        image: product.image || backgroundImage,
                                    }}
                                    onClick={() => handleProductCardClick(product)} // Handle card click
                                    onAddToCart={() => handleAddToCart(product)} // Handle "Đặt mua" click
                                    className="zoom-in"
                                    style={{ animationDelay: `${index * 0.1}s` }} // Delay for animation
                                />
                            ))
                        ) : (
                            <p>Không có sản phẩm yêu thích nào trong danh mục này.</p>
                        )}
                    </div>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default Favorite;
