import React, { useEffect, useState, useRef } from 'react';
import backgroundImage from '../../assets/img/1.jpg';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import trachanh from '../../assets/img/about.png';
import gt from '../../assets/img/gtc.png';
import naturalIcon from '../../assets/img/hab_left_icon_2.png';
import freshIcon from '../../assets/img/docdao.png';
import qualityIcon from '../../assets/img/chatluong.png';
import productIcon from '../../assets/img/den.png';
import axios from 'axios';
import nv from '../../assets/img/nv.png'
import gh from '../../assets/img/gh.png'
import tt from '../../assets/img/tt.png'
import ic from '../../assets/img/icon.png'
import './About.css';

const About = () => {
    const [counts, setCounts] = useState({
        sellers: 0,
        monthlySales: 0,
        customers: 0,
        annualSales: 0
    });

    const introduceTitleRef = useRef(null);
    const introduceImageRef = useRef(null);
    const introduceLeftRefs = useRef([]); // Ref cho phần tử bên trái
    const introduceRightRefs = useRef([]); // Ref cho phần tử bên phải
    const featuresSectionRef = useRef(null); // Ref cho .features-section
    const featureItemsRef = useRef([]);
    const [error, setError]= useState('')


    useEffect(() => {
        fetchResponses();
        const targetCounts = {
            sellers: 2050,
            monthlySales: 3300,
            customers: 4000,
            annualSales: 2500
        };

        const updateCounts = () => {
            setCounts(prevCounts => {
                const newCounts = { ...prevCounts };
                let completed = true;

                for (const key in targetCounts) {
                    if (newCounts[key] < targetCounts[key]) {
                        completed = false;
                        newCounts[key] += 50;
                    } else {
                        newCounts[key] = targetCounts[key];
                    }
                }

                if (completed) clearInterval(intervalId);
                return newCounts;
            });
        };

        const intervalId = setInterval(updateCounts, 20);
        return () => clearInterval(intervalId);
    }, []);

    useEffect(() => {
        const observerOptions = {
            threshold: 0.5,
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.remove('hidden');
                    if (entry.target === introduceTitleRef.current) {
                        entry.target.classList.add('fade-in');
                    } else if (entry.target === introduceImageRef.current) {
                        entry.target.classList.add('zoom-in1');
                    } else if (introduceLeftRefs.current.includes(entry.target)) {
                        entry.target.classList.add('slide-in-left');
                    } else if (introduceRightRefs.current.includes(entry.target)) {
                        entry.target.classList.add('slide-in-right', 'fade-in');
                    } else if (entry.target === featuresSectionRef.current) {
                        entry.target.classList.add('slide-up');
                    } else if (featureItemsRef.current.includes(entry.target)) {
                        entry.target.classList.add('visible');
                    }
                } else {
                    entry.target.classList.add('hidden');
                    if (introduceLeftRefs.current.includes(entry.target)) {
                        entry.target.classList.remove('slide-in-left', 'fade-in');
                    } else if (introduceRightRefs.current.includes(entry.target)) {
                        entry.target.classList.remove('slide-in-right', 'fade-in');
                    } else if (entry.target === featuresSectionRef.current) {
                        // entry.target.classList.remove('slide-up');
                    } else if (featureItemsRef.current.includes(entry.target)) {
                        entry.target.classList.remove('visible');
                    }
                }
            });
        }, observerOptions);

        // Observe elements
        if (introduceTitleRef.current) observer.observe(introduceTitleRef.current);
        if (introduceImageRef.current) observer.observe(introduceImageRef.current);
        introduceLeftRefs.current.forEach(ref => {
            if (ref) observer.observe(ref);
        });
        introduceRightRefs.current.forEach(ref => {
            if (ref) observer.observe(ref);
        });
        if (featuresSectionRef.current) observer.observe(featuresSectionRef.current);
        featureItemsRef.current.forEach(ref => {
            if (ref) observer.observe(ref);
        });

        // Cleanup function
        return () => {
            if (introduceTitleRef.current) observer.unobserve(introduceTitleRef.current);
            if (introduceImageRef.current) observer.unobserve(introduceImageRef.current);
            introduceLeftRefs.current.forEach(ref => {
                if (ref) observer.unobserve(ref);
            });
            introduceRightRefs.current.forEach(ref => {
                if (ref) observer.unobserve(ref);
            });
            if (featuresSectionRef.current) observer.unobserve(featuresSectionRef.current);
            featureItemsRef.current.forEach(ref => {
                if (ref) observer.unobserve(ref);
            });
        };
    }, []);
    const [responses, setResponses] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPage, setTotalPage] = useState(1);
    const [totalRe, setTotalRe] = useState();
    const [limit, setLimit] = useState(8);

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const fetchResponses = async () => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const response = axios.get(`http://localhost:1010/api/contact/view/all?page=1&limit=100`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            console.log('Response data:', data.total);

            setResponses(data.listContacts || []);
            setCurrentPage(data.currentPage);
            setTotalPage(data.totalPage);
            setLimit(data.limit);
            setTotalRe(data.total)

        } catch (error) {
            console.error('Error fetching responses:', error);
        }
    };


    return (
        <>
            <Navbar currentPage="Giới thiệu" />
            <div className="about-container">
                <section
                    className="about-banner"
                    style={{
                        backgroundImage: `url(${trachanh})`,
                    }}
                >
                    <div className='about-banner-container'>
                        <div className="about-banner-content">
                            <h2 className="about-banner-title">Câu chuyện kinh doanh</h2>
                            <p className="about-banner-description">
                                Tại HMDrinks, chúng mình mang đến cho bạn những loại trà trái cây, trà hoa quả và trà sữa đặc biệt, mỗi sản phẩm đều chứa đựng hương vị tự nhiên và tươi mới.
                            </p>
                            <p className="about-banner-description">
                                Hãy để HMDrinks đồng hành cùng bạn trong từng khoảnh khắc, biến mỗi lần bạn tìm đến chúng mình thành một trải nghiệm khó quên.
                            </p>
                        </div>
                        <div className="about-banner-image">
                            <img src={trachanh} alt="Summer Drink" className="about-banner-drink-image" />
                        </div>
                    </div>
                    <div className="about-stat-cards">
                        <div className="about-stat-card">
                            <div className="about-stat-icon">Phản hồi tích cực 💬</div>
                            <div className="about-stat-text">Nhũng phản hồi tích cực từ khách hàng giúp chúng tôi cải thiện từng ngày.</div>
                        </div>
                        <div className="about-stat-card">
                            <div className="about-stat-icon">Bài đăng nổi bật 📢</div>
                            <div className="about-stat-text">Khám phá hàng ngàn bài viết chia sẻ trải nghiệm nước uống từ cộng đồng.</div>
                        </div>
                        <div className="about-stat-card">
                            <div className="about-stat-icon">Khách hàng tiềm năng 🧑‍🤝‍🧑</div>
                            <div className="about-stat-text">Tham gia cùng cộng đồng khách hàng thân thiết để nhận ưu đãi đặc biệt.</div>
                        </div>
                        <div className="about-stat-card">
                            <div className="about-stat-icon">Đánh giá chất lượng⭐</div>
                            <div className="about-stat-text">Luôn đặt chất lượng đồ uống lên hàng đầu để làm hài lòng khách hàng.</div>
                        </div>
                    </div>

                </section>
                <section className="introduce-section">
                    <h2 className="introduce-title hidden" ref={introduceTitleRef}>Tất cả những gì HMDRINKS làm để biến thức uống của bạn <span style={{ color: '#009387' }}>thành trải nghiệm khó quên</span></h2>
                    <div className="introduce-content">
                        <div className="introduce-column">
                            <div className="introduce-item-left" ref={el => introduceLeftRefs.current[0] = el}>
                                <div className="introduce-circle"></div>
                                <div className="introduce-circle"></div>
                                <div className="introduce-circle"></div>
                                <img src={naturalIcon} alt="100% Tự Nhiên" className="introduce-icon" />
                                <h3>100% Tự Nhiên</h3>
                                <p>Hương vị thuần khiết từ nguyên liệu tự nhiên và tươi ngon được tuyển chọn theo tiêu chuẩn cao từ những nông trại uy tín hàng đầu Việt Nam</p>
                            </div>
                            <div className="introduce-item-left" ref={el => introduceLeftRefs.current[1] = el}>
                                <div className="introduce-circle"></div>
                                <div className="introduce-circle"></div>
                                <div className="introduce-circle"></div>
                                <img src={freshIcon} alt="Luôn tươi mới" className="introduce-icon" />
                                <h3>Độc đáo</h3>
                                <p>Khám phá bộ sưu tập đồ uống đa dạng từ HMDrinks, bao gồm trà thanh mát, cà phê thơm lừng và nước ép trái cây tươi ngon. Mỗi sản phẩm đều được chế biến tỉ mỉ để mang đến hương vị đặc trưng riêng biệt.</p>
                            </div>
                        </div>

                        <img src={gt} alt="Stay Healthy" className="introduce-image hidden" ref={introduceImageRef} />

                        <div className="introduce-column">
                            <div className="introduce-item-right" ref={el => introduceRightRefs.current[0] = el}>
                                <img src={productIcon} alt="Sản phẩm tự nhiên" className="introduce-icon" style={{ width: '40px', height: '40px', marginTop: '5px' }} />
                                <div className="introduce-circle"></div>
                                <div className="introduce-circle"></div>
                                <div className="introduce-circle"></div>
                                <h3>Sáng tạo</h3>
                                <p>Đội ngũ pha chế của chúng mình luôn sáng tạo và phát triển các công thức đồ uống mới, kết hợp hài hòa giữa truyền thống và hiện đại, mang đến những trải nghiệm hương vị độc đáo.</p>
                            </div>
                            <div className="introduce-item-right" ref={el => introduceRightRefs.current[1] = el}>
                                <img src={qualityIcon} alt="Chất lượng hàng đầu" className="introduce-icon" />
                                <div className="introduce-circle"></div>
                                <div className="introduce-circle"></div>
                                <div className="introduce-circle"></div>
                                <h3>Tận tâm</h3>
                                <p>Chúng mình mang đến trải nghiệm dịch vụ tuyệt vời, từ tư vấn lựa chọn sản phẩm đến giao hàng nhanh chóng, luôn nỗ lực đáp ứng và vượt qua mong đợi của bạn.</p>
                            </div>
                        </div>
                    </div>
                </section>

                <section className="features-section hidden" ref={featuresSectionRef}>
                    <div className="sub">
                        <h2 className="introduce-title" style={{ marginTop: '30px' }}>
                            Dịch vụ của <span style={{ color: '#009387' }}>HMDrinks 🔥</span>
                        </h2>
                        <div className='feature-item-container'>
                            <div className="feature-item" ref={el => featureItemsRef.current[0] = el}>
                                <img src={nv} alt="Hỗ Trợ 12/7" className="feature-icon" />
                                <h3>Hỗ Trợ 12/7</h3>
                                <p>Đội ngũ HMDrinks luôn có mặt để hỗ trợ bất cứ khi nào bạn cần.</p>
                            </div>
                            <div className="feature-item" ref={el => featureItemsRef.current[1] = el}>
                                <img src={tt} alt="Thanh Toán Tiện Dụng" className="feature-icon" />
                                <h3>Thanh Toán Tiện Dụng</h3>
                                <p>Chúng tôi hỗ trợ thanh toán online qua Ví điện tử tiện dụng và trực tiếp.</p>
                            </div>
                            <div className="feature-item" ref={el => featureItemsRef.current[2] = el}>
                                <img src={gh} alt="Giao Hàng Nhanh Chóng" className="feature-icon" />
                                <h3>Giao Hàng Nhanh Chóng</h3>
                                <p>Bạn sẽ vẫn cảm nhận được sự mát lạnh của thức uống khi nhận hàng.</p>
                            </div>
                        </div>
                    </div>
                </section>

            </div>
            <Footer />
        </>
    );
};

export default About;
