import React, { useEffect } from 'react';
import Cookies from 'js-cookie';
import { useNavigate } from 'react-router-dom'; // Dùng hook navigate để điều hướng trong React Router
import { useAuth } from '../../context/AuthProvider'; // Import useAuth từ context

const LoginGG = () => {
  const navigate = useNavigate(); // Hook để điều hướng đến các trang khác
  const { login } = useAuth(); // Lấy hàm login từ context để gọi khi đăng nhập thành công

  // Hàm giải mã token để lấy role
  const getRoleFromToken = (token) => {
    try {
      const payload = token.split('.')[1]; // Lấy phần payload của token (second part)
      const decodedPayload = JSON.parse(atob(payload)); // Giải mã base64 và chuyển thành JSON

      console.log("Role:", decodedPayload.Roles);
      console.log("User ID:", decodedPayload.userId);

      return decodedPayload.Roles || []; // Trả về role mặc định nếu không có thông tin role
    } catch (error) {
      console.error("Không thể giải mã token:", error);
      return []; // Trả về mảng rỗng nếu không giải mã được
    }
  };

  // Hàm chuyển hướng trang theo role
  const redirectToPage = (role) => {
    let redirectUrl = '/home'; // Mặc định là trang home

    if (role.includes("ADMIN")) {
      redirectUrl = '/dashboard'; // Redirect đến trang quản trị
    } else if (role.includes("CUSTOMER")) {
      redirectUrl = '/home'; // Redirect đến trang khách hàng
    } else if (role.includes("SHIPPER")) {
      redirectUrl = '/shipper-home'; // Redirect đến trang shipper
    }

    // Log ra URL sẽ chuyển hướng
    console.log("Redirecting to:", redirectUrl);

    // Chuyển hướng đến trang tương ứng
    navigate(redirectUrl);
  };

  // Hàm xử lý nhận token từ URL và giải mã
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const accessToken = params.get('access_token');
    const refreshToken = params.get('refresh_token');

    if (accessToken && refreshToken) {
      // Lưu access_token và refresh_token vào Cookies (hoặc localStorage nếu cần)
      Cookies.set('access_token', accessToken, { expires: 7 });
      Cookies.set('refresh_token', refreshToken, { expires: 7 });

      // Giải mã và lấy role từ access_token
      const role = getRoleFromToken(accessToken);

      // Gọi hàm login để set trạng thái đăng nhập
      login(); // Gọi login khi đăng nhập thành công

      // Chuyển hướng theo role và log URL chuyển hướng
      redirectToPage(role);
    } else {
      console.error('No tokens found in URL');
    }
  }, [login]); // Chỉ chạy khi component mount và khi hàm login thay đổi

  return (
    <div>LoginGG - Đang xử lý đăng nhập...</div>
  );
};

export default LoginGG;
