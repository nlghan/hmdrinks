import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom'; // Nhập Router
import './components/Navbar/Navbar.css';
import Navbar from './components/Navbar/Navbar';
import Footer from './components/Footer/Footer';
import Login from './pages/Login'; // Nhập trang Login

const App = () => {
  return (
    <Router> {/* Thêm Router */}
      <div className='app'>
        {/* <Navbar/> */}
        
        <Routes>
          <Route path="/login" element={<Login />} /> {/* Đặt route cho Login */}
          {/* Bạn có thể thêm các route khác ở đây */}
        </Routes>

        {/* <Footer/> */}
      </div>
    </Router>
  );
};

export default App;
