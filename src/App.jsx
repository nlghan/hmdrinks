import React from "react";
import { Routes, Route, Navigate, useLocation } from "react-router-dom";
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
import Product from "./pages/Admin/Product";
import Menu from "./pages/Menu/Menu";
import NewsUser from "./pages/News/NewsUser";
import Contact from "./pages/Contact/Contact";
import News from "./pages/Admin/News";
import PostVoucher from './pages/Post/PostVoucher';
import ProductDetail from "./pages/Menu/ProductDetail"; 
import { useAuth } from './context/AuthProvider'; 
import { CartProvider } from "./context/CartContext";
import Cart from "./pages/Cart/Cart";
import Favorite from "./pages/Favorite/Favorite";
import { FavoriteProvider } from './context/FavoriteContext';

const App = () => {
  const location = useLocation();
  const { isLoggedIn } = useAuth();

  return (
    <FavoriteProvider>
      <CartProvider>
        <div className='app'>
          <Routes location={location}>
            <Route path="/" element={<Home />} />
            <Route path="/home" element={<Home />} />
            <Route path="/login" element={isLoggedIn ? <Navigate to="/home" /> : <Login />} />
            <Route path="/register" element={isLoggedIn ? <Navigate to="/home" /> : <Register />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/user" element={<User />} />
            <Route path="/info" element={isLoggedIn ? <Info /> : <Navigate to="/login" />} />
            <Route path="/about" element={<About />} /> {/* Không cần kiểm tra đăng nhập */}
            <Route path="/menu" element={<Menu />} />
            <Route path="/change" element={isLoggedIn ? <ChangePassword /> : <Navigate to="/login" />} />
            <Route path="/send-mail" element={<SendMail />} />
            <Route path="/category" element={<Category />} />
            <Route path="/product" element={<Product />} />
            <Route path="/product/:productId" element={<ProductDetail />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/favorite" element={<Favorite />} />
            <Route path="/news" element={<News />} />
            <Route path="/post" element={<NewsUser />} />
            <Route path="/marketing/:postId" element={<PostVoucher />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </div>
      </CartProvider>
    </FavoriteProvider>
  );
};

export default App;
