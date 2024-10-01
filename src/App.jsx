import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Route, Routes, useLocation, Navigate } from "react-router-dom";
import { CSSTransition, TransitionGroup } from "react-transition-group";
import Navbar from './components/Navbar/Navbar';
import Footer from './components/Footer/Footer';
import Login from './pages/Login/Login';
import Register from './pages/Register/Register';
import Home from './pages/Home/Home'; // Nếu bạn có trang Home

const App = () => {
  const location = useLocation();
  const [isLoggedIn, setIsLoggedIn] = useState(false); // State để theo dõi trạng thái đăng nhập

  useEffect(() => {
    const loggedIn = sessionStorage.getItem("isLoggedIn"); // Sử dụng sessionStorage
    setIsLoggedIn(loggedIn === "true"); // Cập nhật trạng thái đăng nhập từ session storage
  }, []);

  return (
    <div className='app'>
      <TransitionGroup>
        <CSSTransition
          key={location.key}
          timeout={300}
          classNames="fade"
        >
          <Routes location={location}>
            <Route path="/" element={<Navigate to="/home" />} />
            <Route path="/home" element={<Home />} />
            <Route path="/login" element={isLoggedIn ? <Navigate to="/home" /> : <Login setIsLoggedIn={setIsLoggedIn} />} />
            <Route path="/register" element={isLoggedIn ? <Navigate to="/home" /> : <Register />} />
          </Routes>
        </CSSTransition>
      </TransitionGroup>
    </div>
  );
};

export default App;
