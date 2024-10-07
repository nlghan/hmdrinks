import React from "react";
import { Routes, Route, Navigate, useLocation } from "react-router-dom";
// Tạm thời loại bỏ TransitionGroup và CSSTransition để dễ dàng kiểm tra
// import { CSSTransition, TransitionGroup } from "react-transition-group";
import Navbar from './components/Navbar/Navbar';
import Footer from './components/Footer/Footer';
import Login from './pages/Login/Login';
import Register from './pages/Register/Register';
import Home from './pages/Home/Home';
import Dashboard from './pages/Admin/Dashboard';
import User from './pages/Admin/User';
import Info from './pages/Info/Info';
import About from "./pages/About/About";
import ChangePassword from "./pages/Password/ChangePassword";
import SendMail from "./pages/Password/SendMail";
import Category from "./pages/Admin/Category";
import FormAddUser from './components/FormAddUser';
import { useAuth } from './context/AuthProvider'; // Import hook

const App = () => {
  const location = useLocation();
  const { isLoggedIn } = useAuth(); // Sử dụng trạng thái đăng nhập từ context

  return (
    <div className='app'>
      <Routes location={location}>
        {/* Nếu truy cập root "/", hiển thị Home luôn */}
        <Route path="/" element={<Home />} />
        <Route path="/home" element={<Home />} />
        <Route path="/login" element={isLoggedIn ? <Navigate to="/home" /> : <Login />} />
        <Route path="/register" element={isLoggedIn ? <Navigate to="/home" /> : <Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/user" element={<User />} />
        <Route path="/info" element={isLoggedIn ? <Info /> : <Navigate to="/login" />} /> {/* Cập nhật route cho trang Info */}
        <Route path="/about" element={isLoggedIn ? <About /> : <Navigate to="/login" />} /> {/* Cập nhật route cho trang Info */}
        <Route path="/change" element={isLoggedIn ? <ChangePassword /> : <Navigate to="/login" />} /> {/* Cập nhật route cho trang Info */}
        <Route path="/send-mail" element={<SendMail />} />
        <Route path="/user" element={<User />} />
        <Route path="/category" element={<Category />} />
        <Route path="/formAddUser" element={<FormAddUser />} />

        {/* Wildcard route để chuyển bất kỳ đường dẫn nào không hợp lệ về Home */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </div>
  );
};

export default App;
