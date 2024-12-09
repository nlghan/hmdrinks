import React from 'react'
import './Footer.css'
const Footer = () => {
  return (
    <footer >
                <div className="footer-left">
                    <h2 className='name-footer'>HMDRINKS</h2>
                    <p>Hương vị tuyệt hảo cho cuộc sống năng động.</p>
                    <div className="social-icons">
                        <a href="#"><i className="fab fa-facebook"></i></a>
                        <a href="#"><i className="fab fa-instagram"></i></a>
                        <a href="#"><i className="fab fa-twitter"></i></a>
                        <a href="#"><i className="fab fa-linkedin"></i></a>
                    </div>
                </div>
                <div className="footer-right">
                    <p>LIÊN HỆ</p>
                    <p>Điện thoại: 0927181339</p>
                    <p>Hotline: 0927181339</p>
                    <p>Email: giahan05092003@gmail.com</p>
                    <p>Địa chỉ: 1 Võ Văn Ngân, Linh Chiểu, Thủ Đức</p>
                </div>
                <div className="footer-right">
                    <p>CHÍNH SÁCH VÀ QUY ĐỊNH</p>
                    <p>Điều khoản và điều kiện</p>
                    <p>Chính sách bảo mật thông tin</p>
                </div>
            </footer>
  )
}

export default Footer