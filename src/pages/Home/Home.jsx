import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Footer from "../../components/Footer/Footer.jsx";
import Navbar from "../../components/Navbar/Navbar.jsx";
import PostCard from "../../components/Card/PostCard.jsx";
import Map from "../../components/Card/MapComponent.jsx";
import { assets } from "../../assets/assets.js";
import bia from '../../assets/img/banner4.png';
import bia2 from '../../assets/img/banner3.png';
import bia4 from '../../assets/img/banner5.png';
import tet from '../../assets/img/tet.png'
import "./Home.css";

const Home = () => {
    const [posts, setPosts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [visiblePosts, setVisiblePosts] = useState(3);
    const [showLoginPrompt, setShowLoginPrompt] = useState(false);
    const [showLoginPromptMess, setShowLoginPromptMess] = useState('');
    const postRefs = useRef([]);
    const categoryRefs = useRef([]); // Refs for categories
    const navigate = useNavigate();
    const [currentBannerIndex, setCurrentBannerIndex] = useState(0);
    const h2Refs = useRef([]);
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


    const banners = [
        {
            src: bia,
            title: "Sale up to 50%",
            description: "Ưu đãi cực sốc! Giảm giá lên đến 50% cho các sản phẩm yêu thích của bạn. Đừng bỏ lỡ cơ hội này!",
        },
        {
            src: bia2,
            title: "Bộ sưu tập mùa đông",
            description: "Bộ sưu tập mới đã về! Khám phá những mẫu mã độc đáo, xu hướng hot nhất mùa này. Cập nhật ngay để không bị lỡ!",
        },
        {
            src: bia4,
            title: "Ưu đãi giới hạn",
            description: "Ưu đãi có hạn chỉ trong tuần này! Mua sắm ngay để nhận những món quà hấp dẫn trước khi hết! Chớp lấy cơ hội này!",
        },
    ];

    // Fetch posts from API
    const fetchPosts = async () => {
        try {
            const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/post/view/all/desc?page=1&limit=30`);
            const reversedPosts = response.data.listPosts; // Reverse the order of posts
            setPosts(reversedPosts);
        } catch (error) {
            console.error('Error fetching posts:', error);
        }
    };

    // Fetch categories from API
    const fetchCategories = async () => {
        try {
            const response = await fetch('http://localhost:1010/api/cate/list-category?page=1&limit=100');
            const data = await response.json();
            setCategories(data.categoryResponseList || []); // Set categories from API response
        } catch (error) {
            console.error('Error fetching categories:', error);
        }
    };

    useEffect(() => {
        fetchPosts();
        fetchCategories();
    }, []);

    const [selectedCategoryId, setSelectedCategoryId] = useState(null);


    const handleCategoryClick = (categoryId) => {
        navigate('/menu', { state: { selectedCategoryId: categoryId } }); // Truyền state qua navigate
    };



    useEffect(() => {
        // Auto-slide setup for banners
        const interval = setInterval(() => {
            handleNextBanner();
        }, 5000); // Adjust interval as needed

        // Animation reset for banner
        const bannerContent = document.querySelector(".banner-content-home");
        const bannerImage = document.querySelector(".banner-image");

        if (bannerContent) {
            bannerContent.classList.remove("fadeInLeft");
            void bannerContent.offsetWidth; // Trigger reflow
            bannerContent.classList.add("fadeInLeft");
        }

        if (bannerImage) {
            bannerImage.classList.remove("zoom-out");
            void bannerImage.offsetWidth; // Trigger reflow
            bannerImage.classList.add("zoom-out");
        }

        // Scroll-triggered animations for posts, categories, and h2
        const observerOptions = {
            threshold: 0.2,
            rootMargin: '50px',
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add("fade-in"); // Add fade-in class
                } else {
                    entry.target.classList.remove("fade-in"); // Remove when out of view
                }
            });
        }, observerOptions);

        const allRefs = [...postRefs.current, ...categoryRefs.current, ...h2Refs.current];
        allRefs.forEach((ref) => {
            if (ref) observer.observe(ref);
        });

        return () => {
            clearInterval(interval); // Clean up interval
            allRefs.forEach((ref) => {
                if (ref) observer.unobserve(ref); // Clean up observer
            });
        };
    }, [currentBannerIndex, posts, categories, visiblePosts]);



    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) {
            return parts.pop().split(';').shift();
        }
    };

    const handleDetailsClick = (postId) => {
        const token = getCookie('access_token');
        if (!token) {
            setShowLoginPromptMess('Bạn cần đăng nhập để xem chi tiết bài đăng')
            setShowLoginPrompt(true);
        } else {
            navigate(`/marketing/${postId}`);
        }
    };

    const handleViewMore = () => {
        navigate('/post', { replace: true }); // Điều hướng mà không lưu lịch sử vị trí
    };    

    const handlePrevBanner = () => {
        setCurrentBannerIndex((prevIndex) => (prevIndex - 1 + banners.length) % banners.length);
    };

    const handleNextBanner = () => {
        setCurrentBannerIndex((prevIndex) => (prevIndex + 1) % banners.length);
    };

    const feedbackRefs = useRef([]); // Refs for each feedback item

    useEffect(() => {
        const observerOptions = {
            threshold: 0.2, // Trigger when 20% of the element is visible
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry, index) => {
                if (entry.isIntersecting) {
                    // Apply delay based on index
                    entry.target.style.transitionDelay = `${index * 0.2}s`;
                    entry.target.classList.add('visible'); // Add the visible class
                } else {
                    // Remove visible class when out of viewport
                    entry.target.classList.remove('visible');
                }
            });
        }, observerOptions);

        // Observe feedback items
        feedbackRefs.current.forEach((ref) => {
            if (ref) observer.observe(ref);
        });

        return () => {
            feedbackRefs.current.forEach((ref) => {
                if (ref) observer.unobserve(ref);
            });
        };
    }, []);

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        phone: '',
        message: ''
    });
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


    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);

        const token = getCookie('access_token');
        const userId = getUserIdFromToken(token);

        if (!userId) {
            setIsLoading(false);
            setShowLoginPrompt(true);
            setShowLoginPromptMess("Bạn cần đăng nhập để gửi góp ý.");
        } else {
            const contactData = {
                userId: userId,
                description: formData.message
            };

            try {
                const response = await axios.post('http://localhost:1010/api/contact/create', contactData, {
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
            <Navbar />
            <div className="home-container">
                {/* Banner Section */}
                <div
                    className="home-banner"
                    style={{
                        backgroundImage: `url(${banners[currentBannerIndex].src})`,
                        backgroundSize: 'cover',
                        backgroundPosition: 'center',
                    }}
                >
                    <div className="banner-arrow banner-arrow-left" onClick={handlePrevBanner}>
                        <i className="ti-arrow-circle-left" />
                    </div>
                    <div className="banner-content-home">
                        <h1>{banners[currentBannerIndex].title}</h1>
                        <p>{banners[currentBannerIndex].description}</p>
                        <button className="banner-btn" onClick={() => navigate('/menu')}>
                            Xem ngay <i className='ti-arrow-right' style={{ fontSize: '20px', marginLeft: '10px', textAlign: 'center' }} />
                        </button>
                    </div>
                    <img
                        src={banners[currentBannerIndex].src}
                        alt={`Banner ${currentBannerIndex}`}
                        className="banner-image"
                    />
                    <div className="banner-arrow banner-arrow-right" onClick={handleNextBanner}>
                        <i className="ti-arrow-circle-right" />
                    </div>
                </div>

                {/* Categories Section */}
                <div className="categories-home-section">
                    <h2 ref={(el) => h2Refs.current[0] = el}>Danh Mục Sản Phẩm</h2>
                    <div className="categories-home-grid">
                        {categories.map((category, index) => (
                            <div
                                key={category.id}
                                ref={(el) => categoryRefs.current[index] = el} // Attach ref
                                className={`category-card bg-${index % 5}`}
                                onClick={() => handleCategoryClick(category.cateId)}
                            >
                                <img
                                    src={category.cateImg || assets.defaultCategoryImage}
                                    alt={category.cateName}
                                    className="category-image"
                                />
                                <h3 className="category-title">{category.cateName}</h3>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="opening-hours">
                    <h2>Thời Gian Mở Cửa</h2>
                    <div className='opening-container'>
                        <div className="hour-content">

                            <p>
                                Chúng tôi phục vụ các loại nước uống tươi mới mỗi ngày.<br />
                                <strong>Thứ 2 - Thứ 7:</strong> 8:00 - 21:00<br />
                                <strong>Chủ Nhật:</strong> 9:00 - 22:00
                            </p>
                        </div>
                        <div className="hour-image">
                            <img src={tet} alt="Nước uống" />
                        </div>

                    </div>

                </div>



                {/* Posts Section */}
                <div className="home-content">
                    <h2 ref={(el) => h2Refs.current[1] = el}>Bài Viết</h2>
                    <div className="post-grid">
                        {posts.slice(0, visiblePosts).map((post, index) => (
                            <div
                                key={post.postId}
                                ref={el => postRefs.current[index] = el}
                                className="post-card-wrapper"
                            >
                                <PostCard
                                    image={post.url || assets.avtrang}
                                    title={post.title}
                                    description={post.shortDescription}
                                    buttonText="Chi tiết"
                                    onClick={() => handleDetailsClick(post.postId)}
                                />
                            </div>
                        ))}
                    </div>
                    {visiblePosts < posts.length && (
                        <button className="view-more-btn" onClick={handleViewMore}>
                            Xem thêm
                        </button>
                    )}
                </div>

                <div className="feedback-section">
                    <h2>Nhận Xét Của Khách Hàng</h2>
                    <div className="feedback-container">
                        {['Nguyễn Thanh', 'Trần Tâm', 'Huỳnh Đông', 'Gia Hân'].map((name, index) => (
                            <div
                                key={index}
                                ref={(el) => (feedbackRefs.current[index] = el)} // Save refs
                                className="feedback-item"
                            >
                                <div className="feedback-circle">
                                    <p>Chúng tôi rất hài lòng về sản phẩm của công ty. Cảm ơn bạn đã mang đến sản phẩm tuyệt vời như vậy. Chúng tôi sẽ tiếp tục.</p>
                                    <span>{name}</span>
                                </div>
                            </div>
                        ))}

                    </div>


                </div>

                <div className="contact-form" ref={contactFormRef}>
                    <h2 id='contact-home'>Góp Ý Của Bạn</h2>
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
                            <label>Nội dung</label>
                            <textarea
                                name="message"
                                placeholder="Nội dung"
                                value={formData.message}
                                onChange={handleChange}
                                required
                            ></textarea>
                        </div>
                        <button type="submit" className="contact-home-submit-btn" ref={el => formGroupRefs.current[4] = el}>Gửi Ngay</button>


                    </form>
                </div>



                {/* Login Prompt Modal */}
                {showLoginPrompt && (
                    <div className="login-modal">
                        <div className="login-modal-content">
                            <p>{showLoginPromptMess}</p> {/* Display the message */}
                            <a href="/login">Đăng nhập</a>
                            <button onClick={() => setShowLoginPrompt(false)}>Đóng</button>
                        </div>
                    </div>
                )}

                <Map />
                <Footer />
            </div >
        </>
    );
};

export default Home;
