import React, { useEffect, useState } from 'react';
import { GoogleMap, LoadScript, Marker, DirectionsService, DirectionsRenderer } from '@react-google-maps/api';

const MapComponent = () => {
    const mapContainerStyle = {
        width: '100%',
        height: '500px',
    };

    // State để lưu tọa độ
    const [location, setLocation] = useState({
        lat: 10.762622, // Tọa độ mặc định
        lng: 106.660172,
    });

    // Sử dụng geolocation để lấy vị trí người dùng
    useEffect(() => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    setLocation({
                        lat: position.coords.latitude,
                        lng: position.coords.longitude,
                    });
                },
                (error) => {
                    console.error("Error getting location:", error);
                }
            );
        } else {
            console.log("Geolocation is not supported by this browser.");
        }
    }, []);

    const mapOptions = {
        styles: [], // Đặt styles rỗng để sử dụng theme mặc định (sáng)
        disableDefaultUI: false, // Hiện các controls mặc định
    };

    return (
        <LoadScript googleMapsApiKey={import.meta.env.VITE_GOOGLE_MAPS_API_KEY}>
            <GoogleMap
                mapContainerStyle={mapContainerStyle}
                center={location}
                zoom={15}
                options={mapOptions}  // Thêm options vào đây
            >
                <Marker position={location} />
            </GoogleMap>
        </LoadScript>
    );
};

export default MapComponent;
