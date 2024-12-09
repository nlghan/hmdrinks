import React from 'react';
import './FavCard.css';
import ImageSlider from './ImageSlider';

const FavCard = ({ product, onClick, onDeleteFavorite }) => {
    return (
        <div id="card">
            <div className="card-image">
                <ImageSlider images={product.images} />
                <button className="remove-btn" onClick={onDeleteFavorite}>✖</button> {/* Remove button */}
            </div>
            <div className="card-content" onClick={onClick}>
                <section className="event-tag">
                    <h3>{product.cateName}</h3>
                </section>
                <section className="card-details">
                    <h2>{product.proName}</h2>
                    <p className="posted-by">Giá: {product.price}</p> {/* Correctly access price */}
                    <p className="description">
                        Size: {product.size}
                    </p>
                    <button className=" view-details" >
                    <i className="ti-shopping-cart" /> Đặt mua
                </button>
                </section>
            </div>
        </div>
    );
};

export default FavCard;
