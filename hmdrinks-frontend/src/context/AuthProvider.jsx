import React, { createContext, useContext, useState } from 'react';

// Tạo context
const AuthContext = createContext();

// Tạo provider
export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(() => {
    // Lấy trạng thái đăng nhập từ sessionStorage khi khởi tạo
    return sessionStorage.getItem("isLoggedIn") === "true";
  });

  // Hàm login
  const login = () => {
    setIsLoggedIn(true);
    sessionStorage.setItem("isLoggedIn", "true");
  };

  // Hàm logout
  const logout = () => {
    setIsLoggedIn(false);
    sessionStorage.removeItem("isLoggedIn");
  };

  // Cung cấp giá trị của context cho các component con
  return (
    <AuthContext.Provider value={{ isLoggedIn, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// Hook để sử dụng AuthContext
export const useAuth = () => useContext(AuthContext);
