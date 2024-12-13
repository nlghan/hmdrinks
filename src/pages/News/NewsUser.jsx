import React, { useEffect, useState, useRef } from 'react';
import axios from 'axios';
import axiosInstance from '../../utils/axiosConfig';
import './NewsUser.css';
import Navbar from '../../components/Navbar/Navbar';
import trachanh from '../../assets/img/trachanh.png';
import { useNavigate } from 'react-router-dom';
import Footer from "../../components/Footer/Footer.jsx";

const NewsUser = () => {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const postsPerPage = 4;
  const [totalPages, setTotalPages] = useState(1);
  const navigate = useNavigate();
  const [showLoginPrompt, setShowLoginPrompt] = useState(false);

  const newsSectionRef = useRef(null); // Reference for news section
  const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
      const cookieValue = parts.pop().split(';').shift();
      console.log("Cookie value:", cookieValue); // Log cookie value
      return cookieValue;
    }
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
  const fetchPosts = async (page) => {
    try {
      setLoading(true);
      const response = await axiosInstance.get(`${import.meta.env.VITE_API_BASE_URL}/post/view/all/desc?page=${page}&limit=${postsPerPage}`);
      setPosts(response.data.listPosts);
      setTotalPages(response.data.totalPages);
      setLoading(false);


    } catch (error) {
      console.error('Error fetching posts:', error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts(currentPage);
   
  }, [currentPage]);

  const handlePageChange = (page) => {
    if (page > 0 && page <= totalPages) {
      setCurrentPage(page);
      const halfHeight = document.body.scrollHeight / 3;
      window.scrollTo({
        top: halfHeight,
        behavior: "smooth" // Thêm hiệu ứng cuộn mượt
      });
    }
  };

  // Scroll-based animation effect
  useEffect(() => {
    const observerOptions = {
      threshold: 0.5,
    };

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('fade-in');
          observer.unobserve(entry.target); // Only trigger once
        }
      });
    }, observerOptions);

    if (newsSectionRef.current) {
      observer.observe(newsSectionRef.current);
    }

    return () => {
      if (newsSectionRef.current) {
        observer.unobserve(newsSectionRef.current);
      }
    };
  }, []);

  useEffect(() => {
    // Cuộn đầu trang nếu đến từ trang khác (vd: từ home)
    if (location.pathname === '/post') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }, [location]);

  const handleDetailsClick = (postId) => {
    const token = getCookie('access_token');
    const userId = getUserIdFromToken(token);

    console.log("Token:", token); // Log token
    console.log("UserId:", userId); // Log userId

    if (!userId) {
      setShowLoginPrompt(true);
    } else {
      console.log("Navigating to /marketing/" + postId); // Log navigation
      navigate(`/marketing/${postId}`);
    }
  };


  return (
    <>
      <Navbar currentPage={"Tin tức"} />
      <div className='news-user-container'>
        <section
          className="news-user-banner"
          style={{
            backgroundImage: `url(${trachanh})`,
          }}
        >
          <div className='banner-user-cotainer'>
            <div className="banner-content">
              <h2 className="banner-title pacifico-regular">HMDrinks chào hè</h2>
              <p className="banner-description">
                Tại HMDrinks, chúng mình mang đến cho bạn những loại trà trái cây, nước ép và trà sữa đặc biệt,
                mỗi sản phẩm đều chứa đựng hương vị tự nhiên và tươi mới. Được chế biến từ những nguyên liệu chọn lọc kỹ lưỡng
                và qua quy trình sản xuất hiện đại, chúng mình cam kết mang lại cho bạn những trải nghiệm thú vị,
                không chỉ ngon miệng mà còn tốt cho sức khỏe.<br />
                Hãy để HMDrinks đồng hành cùng bạn trong từng khoảnh khắc, biến mỗi lần bạn tìm đến chúng mình thành một trải nghiệm khó quên.
              </p>
            </div>
            <div className="banner-image">
              <img src={trachanh} alt="Summer Drink" className="banner-drink-image" />
            </div>
          </div>
        </section>

        {/* News Section with ref */}
        <section className="news-user-section hidden" ref={newsSectionRef}>
          <h3 className="news-user-title">TIN TỨC</h3>
          <div className="news-user-cards">
            {loading ? (
              <p>Loading...</p>
            ) : (
              posts.map((post) => (
                <div className="news-user-card" key={post.postId} onClick={() => handleDetailsClick(post.postId)} >
                  <img src={post.url || "https://via.placeholder.com/250"} alt={post.title || "Tin tức"} className="news-user-image" />
                  <div className="news-user-card-content">
                    <p className="news-user-card-date">
                      <span role="img" aria-label="calendar">📅</span> {post.dateCreated}
                    </p>
                    <h4 className="news-user-card-title">{post.title}</h4>
                    <p className="news-user-card-description">{post.shortDescription}</p>
                    <button
                      className="news-user-card-button"
                      onClick={() => handleDetailsClick(post.postId)} // Đặt hàm trong một arrow function
                    >
                      Chi tiết
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>

          <div className="menu-product-pagination" style={{ marginBottom: '20px' }}>
            {/* <span className={`pagination-arrow ${currentPage === 1 ? 'disabled' : ''}`} onClick={() => handlePageChange(currentPage - 1)}>
              <i className='ti-arrow-left' id='arrow' />
            </span> */}
            {Array.from({ length: totalPages }, (_, index) => (
              <span key={index + 1} className={`pagination-dot ${currentPage === index + 1 ? 'active' : ''}`} onClick={() => handlePageChange(index + 1)}>
                •
              </span>
            ))}
            {/* <span className={`pagination-arrow ${currentPage === totalPages ? 'disabled' : ''}`} onClick={() => handlePageChange(currentPage + 1)}>
              <i className='ti-arrow-right' id='arrow' />
            </span> */}
          </div>
        </section>
        {showLoginPrompt && (
          <div className="login-modal">
            <div className="login-modal-content">
              <p>Bạn cần đăng nhập để xem chi tiết bài đăng.</p>
              <a href="/login">Đăng nhập</a>
              <button onClick={() => setShowLoginPrompt(false)}>Đóng</button>
            </div>
          </div>
        )}
      </div>
      <Footer />
    </>
  );
};

export default NewsUser;
