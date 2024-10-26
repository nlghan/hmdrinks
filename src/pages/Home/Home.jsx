import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Footer from "../../components/Footer/Footer.jsx";
import Navbar from "../../components/Navbar/Navbar.jsx";
import PostCard from "../../components/Card/PostCard.jsx";
import "./Home.css";

const Home = () => {
    const [posts, setPosts] = useState([]);
    const [visiblePosts, setVisiblePosts] = useState(3);
    const [showLoginPrompt, setShowLoginPrompt] = useState(false);
    const navigate = useNavigate();

    // Fetch posts from API
    const fetchPosts = async () => {
        try {
            const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/post/view/all?page=1&limit=30`);
            setPosts(response.data.listPosts);
        } catch (error) {
            console.error('Error fetching posts:', error);
        }
    };

    useEffect(() => {
        fetchPosts();
    }, []);

    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            console.log("Decoded UserId from token:", decodedPayload.UserId); // Log UserId
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Unable to decode token:", error);
            return null;
        }
    };

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`); 
        if (parts.length === 2) {
            const cookieValue = parts.pop().split(';').shift();
            console.log("Cookie value:", cookieValue); // Log cookie value
            return cookieValue;
        }
    };

    const handleDetailsClick = (postId) => {
        console.log("handleDetailsClick function called");
        console.log("PostId clicked:", postId); // Log postId
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

    const handleViewMore = () => {
        setVisiblePosts(posts.length);
    };

    return (
        <div className="home-container">
            <Navbar />
            <div className="home-content">
                <div className="post-grid">
                    {posts.slice(0, visiblePosts).map((post) => (
                       <PostCard
                       key={post.postId}
                       image={post.url || "defaultImage.jpg"}
                       title={post.title}
                       description={post.shortDescription}
                       buttonText="Chi tiết"
                       onClick={() => handleDetailsClick(post.postId)} // Đảm bảo rằng bạn truyền hàm này
                   />
                   
                   
                    ))}
                </div>
                {visiblePosts < posts.length && (
                    <button className="view-more-btn" onClick={handleViewMore}>
                        Xem thêm
                    </button>
                )}
            </div>
            {showLoginPrompt && (
                <div className="login-modal">
                    <div className="login-modal-content">
                        <p>Bạn cần đăng nhập để xem chi tiết bài đăng.</p>
                        <button onClick={() => setShowLoginPrompt(false)}>Đóng</button>
                        <a href="/login">Đăng nhập</a>
                    </div>
                </div>
            )}
            <Footer />
        </div>
    );
};

export default Home;
