import React from 'react';
import './NewsUser.css';
import Navbar from '../../components/Navbar/Navbar';
import trachanh from '../../assets/img/trachanh.png';


const NewsUser = () => {
  return (
    <>
      <Navbar currentPage={"Tin tức"} />
      <div className='news-user-container'>
        <section
          className="news-user-banner"
          style={{
            backgroundImage: `url(${trachanh})`,
          }}

        >
          <div className='banner-user-cotainer'>
            <div className="banner-content">
              <h2 className="banner-title pacifico-regular">HMDrinks chào hè</h2>
              <p className="banner-description">
                Tại HMDrinks, chúng mình mang đến cho bạn những loại trà trái cây, nước ép và trà sữa đặc biệt,
                mỗi sản phẩm đều chứa đựng hương vị tự nhiên và tươi mới. Được chế biến từ những nguyên liệu chọn lọc kỹ lưỡng
                và qua quy trình sản xuất hiện đại, chúng mình cam kết mang lại cho bạn những trải nghiệm thú vị,
                không chỉ ngon miệng mà còn tốt cho sức khỏe.
              </p>
              <p className="banner-description">
                Hãy để HMDrinks đồng hành cùng bạn trong từng khoảnh khắc, biến mỗi lần bạn tìm đến chúng mình
                thành một trải nghiệm khó quên.
              </p>
            </div>
            <div className="banner-image">
              <img
                src={trachanh}
                alt="Summer Drink"
                className="banner-drink-image"
              />
            </div>
          </div>

        </section>
        <section className="news-user-section">
          <h3 className="news-user-title">TIN TỨC</h3>
          <div className="news-user-cards">
            {[1, 2, 3].map((item, index) => (
              <div className="news-user-card" key={index}>
                <img
                  src="https://via.placeholder.com/250"
                  alt="Soda mat lanh chao he"
                  className="news-user-image"
                />
                <h4 className="news-user-card-title">SODA MÁT LẠNH CHÀO HÈ</h4>
                <p className="news-user-card-description">
                  Thưởng thức soda mát lạnh với những hương vị đặc biệt từ thiên nhiên. Sảng khoái và sẵn sàng cho mùa hè năng động!
                </p>
                <button className="news-user-card-button">Chi tiết</button>
              </div>
            ))}
          </div>
          <button className="news-user-see-more">Xem thêm</button>
        </section>
      </div>
    </>
  );
};

export default NewsUser;
