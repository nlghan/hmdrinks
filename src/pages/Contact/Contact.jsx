import React, { useState, useEffect, useRef } from 'react';
import './Contact.css';
import { assets } from '../../assets/assets'; 
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import trachanh from '../../assets/img/about.png';

const Contact = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    message: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prevState => ({
      ...prevState,
      [name]: value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Xử lý logic gửi form ở đây
    console.log('Form submitted:', formData);
  };

  // Thêm refs
  const contactInfoRef = useRef(null);
  const contactFormRef = useRef(null);
  const contactItemRefs = useRef([]);
  const formGroupRefs = useRef([]);

  useEffect(() => {
    const observerOptions = {
      threshold: 0.5,
    };

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.remove('hidden');
          entry.target.classList.add('fade-in');
        }
      });
    }, observerOptions);

    // Observe elements
    if (contactInfoRef.current) observer.observe(contactInfoRef.current);
    if (contactFormRef.current) observer.observe(contactFormRef.current);
    
    contactItemRefs.current.forEach(ref => {
      if (ref) observer.observe(ref);
    });
    
    formGroupRefs.current.forEach(ref => {
      if (ref) observer.observe(ref);
    });

    return () => {
      observer.disconnect();
    };
  }, []);

  return (
    <>
      <Navbar currentPage="Liên Hệ" />
      <div className="contact-main">
        <div className="contact-section">
          <div className="contact-info" ref={contactInfoRef}>
            <h2>Thông Tin Liên Hệ</h2>
            <div className="contact-info-item" ref={el => contactItemRefs.current[0] = el}>
              <i className="ti-mobile"></i>
              <div className="contact-info-content">
                <h3>Gọi ngay</h3>
                <p>Chúng tôi luôn sẵn sàng bên bạn 12/7, 7 ngày một tuần.</p>
                <p>Điện thoại: +84 123 456 789</p>
              </div>
            </div>
            <div className="contact-info-item" ref={el => contactItemRefs.current[1] = el}>
              <i className="ti-email"></i>
              <div className="contact-info-content">
                <h3>Nhắn gửi</h3>
                <p>Chỉ cần bạn điền form thì chúng mình sẽ liên lạc ngay trong vòng 24h.</p>
                <p>Email: contact@example.com</p>
              </div>
            </div>

            <div className="contact-info-divider"></div>

            <div className="contact-info-item" ref={el => contactItemRefs.current[2] = el}>
              <i className="ti-location-pin"></i>
              <div className="contact-info-content">
                <h3>Địa chỉ</h3>
                <p>123 Đường ABC, Quận XYZ, TP.HCM</p>
              </div>
            </div>

            <div className="contact-info-item" ref={el => contactItemRefs.current[3] = el}>
              <i className="ti-time"></i>
              <div className="contact-info-content">
                <h3>Giờ làm việc</h3>
                <p>Thứ 2 - Chủ nhật: 8:00 - 22:00</p>
              </div>
            </div>
          </div>

          <div className="contact-form" ref={contactFormRef}>
            <h2>Gửi Thư Cho Chúng Tôi</h2>
            <form onSubmit={handleSubmit}>
              <div className="contact-form-group" ref={el => formGroupRefs.current[0] = el}>
                <label className="required-field">Họ và tên</label>
                <input
                  type="text"
                  name="name"
                  placeholder="Họ và tên"
                  value={formData.name}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="contact-form-group" ref={el => formGroupRefs.current[1] = el}>
                <label className="required-field">Email</label>
                <input
                  type="email"
                  name="email"
                  placeholder="Email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="contact-form-group" ref={el => formGroupRefs.current[2] = el}>
                <label className="required-field">Số điện thoại</label>
                <input
                  type="tel"
                  name="phone"
                  placeholder="Số điện thoại"
                  value={formData.phone}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="contact-form-group" ref={el => formGroupRefs.current[3] = el}>
                <label>Nội dung thư</label>
                <textarea
                  name="message"
                  placeholder="Nội dung thư"
                  value={formData.message}
                  onChange={handleChange}
                  required
                ></textarea>
              </div>
              <button type="submit" className="contact-submit-btn" ref={el => formGroupRefs.current[4] = el}>Gửi Ngay</button>
            </form>
          </div>
        </div>
      </div>
      <Footer />
    </>
  );
};

export default Contact;