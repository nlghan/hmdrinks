import React, { useState, useEffect, useRef } from 'react';
import './Contact.css';
import { assets } from '../../assets/assets';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import trachanh from '../../assets/img/about.png';
import axios from 'axios';
import axiosInstance from '../../utils/axiosConfig';


const Contact = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    message: ''
  });
  const [showLoginPrompt, setShowLoginPrompt] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [showError, setShowError] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prevState => ({
      ...prevState,
      [name]: value
    }));
  };

  const getUserIdFromToken = (token) => {
    try {
      const payload = token.split('.')[1];
      const decodedPayload = JSON.parse(atob(payload));
      return decodedPayload.UserId;
    } catch (error) {
      console.error("Cannot decode token:", error);
      return null;
    }
  };

  const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
  };


  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    const token = getCookie('access_token');
    const userId = getUserIdFromToken(token);

    if (!userId) {
      setIsLoading(false);
      setShowLoginPrompt(true);
    } else {
      const contactData = {
        userId: userId,
        description: formData.message
      };

      try {
        const response = await axiosInstance.post('http://localhost:1010/api/contact/create', contactData, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        setIsLoading(false);
        setShowSuccess(true);
        setTimeout(() => {
          setShowSuccess(false);
        }, 2000);

        setFormData({
          name: '',
          email: '',
          phone: '',
          message: ''
        });
      } catch (error) {
        setIsLoading(false);
        setShowError(true);
        setTimeout(() => {
          setShowError(false);
        }, 2000);
      }
    }
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
      {showSuccess && (
        <div className="contact-success-animation">
          <div className="contact-success-modal">
            <div className="contact-success-icon">
              <div className="contact-success-icon-circle">
                <svg className="contact-checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                  <circle className="contact-checkmark-circle" cx="26" cy="26" r="25" fill="none" />
                  <path className="contact-checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
                </svg>
              </div>
            </div>
            <h3>Gửi thành công!</h3>
            <p>Chúng tôi sẽ liên hệ với bạn sớm nhất có thể.</p>
          </div>
        </div>
      )}
      {showLoginPrompt && (
        <div className="login-modal">
          <div className="login-modal-content">
            <p>Bạn cần đăng nhập để liên hệ với chúng tôi.</p>
            <a href="/login">Đăng nhập</a>
            <button onClick={() => setShowLoginPrompt(false)}>Đóng</button>
          </div>
        </div>
      )}
      {showError && (
        <div className="contact-error-animation">
          <div className="contact-error-modal">
            <div className="contact-error-icon">
              <div className="error-icon-circle">
                <svg className="contact-cross" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                  <circle className="contact-cross-circle" cx="26" cy="26" r="25" fill="none" />
                  <path className="contact-cross-line" fill="none" d="M16,16 L36,36 M36,16 L16,36" />
                </svg>
              </div>
            </div>
            <h3>Gửi thất bại!</h3>
            <p>Có lỗi xảy ra, vui lòng thử lại.</p>
          </div>
        </div>
      )}
      {isLoading && (
        <div className="contact-loading-animation">
          <div className="contact-loading-modal">
            <div className="contact-loading-spinner">
              <div className="contact-spinner"></div>
            </div>
            <h3>Đang xử lý...</h3>
            <p>Vui lòng đợi trong giây lát</p>
          </div>
        </div>
      )}

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
              <button type="submit" className="contact-submit-btn1" ref={el => formGroupRefs.current[4] = el} >Gửi Ngay</button>


            </form>

          </div>
        </div>
      </div>
      <Footer />
    </>
  );
};

export default Contact;