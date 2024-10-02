import React from 'react';
import backgroundImage from '../../assets/img/1.jpg'; // Đường dẫn đến ảnh của bạn

const About = () => {
  return (
    <div style={{
      background: `url(${backgroundImage}) no-repeat center center fixed`,
      backgroundSize: 'cover',
      minHeight: '100vh', // Đảm bảo chiều cao đầy đủ
      color: 'white', // Màu chữ (có thể thay đổi theo ý bạn)
      display: 'flex', // Căn giữa nội dung
      alignItems: 'center',
      justifyContent: 'center',
      opacity:0.5
    }}>
      <h1>About Page</h1>
    </div>
  );
};

export default About;
