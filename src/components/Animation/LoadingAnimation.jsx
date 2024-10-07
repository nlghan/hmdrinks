import React, { useEffect, useRef } from 'react';
import lottie from 'lottie-web';

const LoadingAnimation = ({ animationPath, width = 500, height = 500, isVisible = true }) => {
    const containerRef = useRef(null);

    useEffect(() => {
        let animation;

        if (animationPath && isVisible) {
            animation = lottie.loadAnimation({
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
        }

        return () => {
            if (animation) {
                animation.destroy(); // Cleanup the animation on unmount
            }
        };
    }, [animationPath, isVisible]);

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
