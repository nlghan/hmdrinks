import React, { useState, useEffect } from 'react';
import './ImageSlider.css'; // Ensure you have a CSS file for styles

const ImageSlider = ({ images }) => {
    const [currentIndex, setCurrentIndex] = useState(0);

    const nextSlide = () => {
        setCurrentIndex((prevIndex) => (prevIndex + 1) % images.length);
    };

    const prevSlide = () => {
        setCurrentIndex((prevIndex) => (prevIndex - 1 + images.length) % images.length);
    };

    // Effect for automatic sliding every 1 second
    useEffect(() => {
        const intervalId = setInterval(() => {
            nextSlide();
        }, 2000); // Change the image every 1000ms (1 second)

        return () => {
            clearInterval(intervalId); // Clear interval on component unmount
        };
    }, [images.length]); // Dependency on images.length to avoid issues when images change

    return (
        <div className="slider-container">
            <button className="slider-button" onClick={prevSlide}><i className='ti-arrow-left'/></button>
            <div className="slide" style={{ backgroundImage: `url(${images[currentIndex]})` }}>
                {/* You can add more content here if needed */}
            </div>
            <button className="slider-button" onClick={nextSlide}><i className='ti-arrow-right'/></button>
        </div>
    );
};

export default ImageSlider;
