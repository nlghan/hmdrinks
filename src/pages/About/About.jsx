import React from 'react';
import backgroundImage from '../../assets/img/1.jpg'; // Đường dẫn đến ảnh của bạn
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import "./About.css"

const About = () => {
    return (
        <><><Navbar currentPage="Giới thiệu"/>
            <div style={{
                background: `url(${backgroundImage}) no-repeat center center fixed`,
                backgroundSize: 'cover',
                minHeight: '100vh', // Đảm bảo chiều cao đầy đủ
                color: 'white', // Màu chữ (có thể thay đổi theo ý bạn)
                display: 'flex', // Căn giữa nội dung
                alignItems: 'center',
                justifyContent: 'center',
                opacity: 0.1
            }}>
                <div class="container-product">
                <div class="overlay-product"></div>
                <h1>About Page</h1>
                /</div>

              

            </div></><Footer /></>
    );
};

export default About;
