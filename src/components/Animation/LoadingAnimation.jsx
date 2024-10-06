import React, { useEffect, useRef } from 'react';
import lottie from 'lottie-web';

const LoadingAnimation = ({ animationPath, width = 500, height = 500, isVisible }) => {
    const containerRef = useRef(null);

    useEffect(() => {
        if (animationPath) {
            const animation = lottie.loadAnimation({
                container: containerRef.current,
                renderer: 'svg',
                loop: true,
                autoplay: true,
                path: animationPath,
            });

            // Error handling
            animation.addEventListener('data_failed', () => {
                console.error("Lottie animation failed to load.");
            });

            return () => {
                animation.destroy(); // Cleanup the animation on unmount
            };
        }
    }, [animationPath]);

    // Do not render if not visible
    if (!isVisible) return null;

    return (
        <div
            style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: '100vh',  // Makes sure the div takes the full height of the viewport
            }}
        >
            <div
                ref={containerRef}
                style={{ width: width, height: height }}  // Customizable dimensions for the animation
            ></div>
        </div>
    );
};

export default LoadingAnimation;
