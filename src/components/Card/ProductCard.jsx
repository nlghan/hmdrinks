import React from 'react';
import "./ProductCard.css"
function ProductCard({ product }) {
    return (
        <div className="product-card">
            <img src={product.image} alt={product.name} />
            <h3>{product.name}</h3>
            <p className='product-card-p'>{product.description}</p>
            <div className='product-card-price'>
                <p className='product-card-p card-product-price'>Giá: {product.price}</p>
                <button className="add-cart">+</button>

            </div>

        </div>
    );
}

export default ProductCard;
