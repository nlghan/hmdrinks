import React from "react";
import { Routes, Route, Navigate, useLocation } from "react-router-dom";
import Navbar from './components/Navbar/Navbar';
import Footer from './components/Footer/Footer';
import Login from './pages/Login/Login';
import LoginGG from './pages/Login/LoginGG'; // Import LoginGG
import Register from './pages/Register/Register';
import Home from './pages/Home/Home';
import ShipperHome from './pages/Shipper/HomeShipper';
import AboutShipper from './pages/Shipper/AboutShipper';
import AnalyticsShipper from './pages/Shipper/AnalyticsShipper';
import InfoShipper from './pages/Shipper/InfoShipper';
import Dashboard from './pages/Admin/Dashboard';
import Analytics from './pages/Admin/Analytics';
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
import Response from "./pages/Admin/Response";
import News from "./pages/Admin/News";
import PostVoucher from './pages/Post/PostVoucher';
import ProductDetail from "./pages/Menu/ProductDetail";
import { useAuth } from './context/AuthProvider';
import { CartProvider } from "./context/CartContext";
import Cart from "./pages/Cart/Cart";
import Favorite from "./pages/Favorite/Favorite";
import { FavoriteProvider } from './context/FavoriteContext';
import Order from "./pages/Order/Order";
import PaymentStatus from "./pages/Payment/PaymentStatus";
import PaymentOnlineStatus from "./pages/Payment/PaymentOnlineStatus";
import PaymentOnlineStatusVnpay from "./pages/Payment/PaymentOnlineStatusVNPay";
import Orders from "./pages/Admin/Orders";
import ShipmentDetail from "./pages/Shipper/ShipmentDetail";
import MyOrder from "./pages/Order/MyOrder";
import { PaymentStatusProvider } from "./context/PaymentStatusContext";
import MyOrderDetail from "./pages/Order/MyOrderDetail";
import Cookies from 'js-cookie';

// Thêm hàm helper để lấy role từ token
const getRoleFromToken = (token) => {
  try {
    const payload = token.split('.')[1];
    const decodedPayload = JSON.parse(atob(payload));
    return decodedPayload.Roles;
  } catch (error) {
    return null;
  }
};

// Component để kiểm tra role và điều hướng
const LoginRedirect = () => {
  const { isLoggedIn } = useAuth();
  const token = Cookies.get('access_token');
  const role = token ? getRoleFromToken(token) : null;

  if (!isLoggedIn) {
    return <Login />; // Redirect to Login if not logged in
  }

  if (role && role.includes("SHIPPER")) {
    return <Navigate to="/shipper-home" />;
  }

  return <Navigate to="/home" />;
};

import PaymentOnlineStatusPayos from "./pages/Payment/PaymentOnlineStatusPayos";
import IntermediaryPage from "./pages/Payment/IntermediaryPage";
import PaymentOnlineStatusMomo from "./pages/Payment/PaymentOnlineStatusMomo";

const App = () => {
  const location = useLocation();
  const { isLoggedIn } = useAuth();

  return (
    <FavoriteProvider>
      <CartProvider>
        <PaymentStatusProvider>
          <div className='app'>
            <Routes location={location}>
              <Route path="/" element={<Home />} />
              <Route path="/home" element={isLoggedIn ? <Home /> : <Navigate to="/" />} />
              <Route path="/shipper-home" element={<ShipperHome />} />
              <Route path="/login" element={<LoginRedirect />} />
              <Route path="/login-gg" element={<LoginGG />} />
              <Route path="/register" element={isLoggedIn ? <Navigate to="/home" /> : <Register />} />
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/analytics" element={<Analytics />} />
              <Route path="/user" element={<User />} />
              <Route path="/info" element={isLoggedIn ? <Info /> : <Navigate to="/login" />} />
              <Route path="/shipper-info" element={isLoggedIn ? <InfoShipper /> : <Navigate to="/login" />} />
              <Route path="/about" element={<About />} /> {/* Không cần kiểm tra đăng nhập */}
              <Route path="/shipper-about" element={<AboutShipper />} />
              <Route path="/shipper-analytics" element={<AnalyticsShipper />} />
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
              <Route path="/order" element={<Order />} />
              <Route path="/marketing/:postId" element={<PostVoucher />} />
              <Route path="/contact" element={<Contact />} />
              <Route path="/response" element={<Response />} />
              <Route path="/payment-status" element={<PaymentStatus />} />
              <Route path="/payment-online-status" element={<PaymentOnlineStatus />} />
              <Route path="/intermediary-page" element={<IntermediaryPage />} />
              <Route path="/payment-online-status-payos" element={<PaymentOnlineStatusPayos />} />
              <Route path="/payment-online-status-momo" element={<PaymentOnlineStatusMomo />} />
              <Route path="/payment-online-status-vnpay" element={<PaymentOnlineStatusVnpay />} />
              <Route path="/orders" element={<Orders />} />
              <Route path="/my-orders" element={<MyOrder />} />
              <Route
                path="/my-order-detail/:shipmentId"
                element={isLoggedIn ? <MyOrderDetail /> : <Navigate to="/home" />}
              />
              <Route
                path="/shipment-detail/:shipmentId"
                element={isLoggedIn ? <ShipmentDetail /> : <Navigate to="/home" />}
              />
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </div>
        </PaymentStatusProvider>

      </CartProvider>
    </FavoriteProvider>
  );
};

export default App;
