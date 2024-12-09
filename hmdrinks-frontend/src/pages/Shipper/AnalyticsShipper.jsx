import React, { useState, useEffect } from 'react';
import './AnalyticsShipper.css';
import { assets } from '../../assets/assets';
import NavbarShipper from '../../components/Navbar/NavbarShipper';
import Footer from '../../components/Footer/Footer';
import GaugeCard from '../../components/Card/GaugeCardShip';
import axios from 'axios';
import CustomChart from '../../components/Charts/CustomChartShip';

const AnalyticsShipper = () => {
  const [percentages, setPercentages] = useState([0, 0, 0, 0]);
  const [successfulShipments, setSuccessfulShipments] = useState([]);
  const [xLabels, setXLabels] = useState([]);
  const [month, setMonth] = useState('Tháng 1');
  const [year, setYear] = useState(new Date().getFullYear());

  const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
  };
  const getUserIdFromToken = (token) => {
    try {
      const payload = token.split('.')[1];
      const decodedPayload = JSON.parse(atob(payload));
      return decodedPayload.UserId;
    } catch (error) {
      console.error("Cannot decode token:", error);
      return null;
    }
  };

  const fetchShipmentCounts = async () => {
    try {
      const token = getCookie('access_token');
      if (!token) {
        console.error("Không tìm thấy token xác thực.");
        return;
      }

      const userId = getUserIdFromToken(token);
      const statuses = ['WAITING', 'SHIPPING', 'SUCCESS', 'CANCELLED'];
      const counts = await Promise.all(statuses.map(async (status) => {
        const response = await axios.get('http://localhost:1010/api/shipment/shipper/listShippment', {
          params: {
            page: 1,
            limit: 100,
            status: status,
            userId: userId,
          },
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });
        console.log('kq', response);
        return response.data.total;
      }));

      const total = counts.reduce((acc, count) => acc + count, 0);
      const newPercentages = counts.map(count => (total > 0 ? (count / total) * 100 : 0));
      setPercentages(newPercentages);

    } catch (error) {
      console.error('Lỗi khi gọi API:', error);
    }
  };

  useEffect(() => {
    fetchShipmentCounts();
  }, [month, year]);
  return (
    <>
      <NavbarShipper currentPage="Thống Kê" />
      <div className="analytics-shipper">
        {/* Biểu đồ CustomChart */}
        <div className="ship-chart-container">
          <CustomChart />
        </div>

        {/* Các thẻ GaugeCard */}
        <div className="ship-cards-container">

          <GaugeCard
            percentage={percentages[0].toFixed(1)}
            width='350px'
            height='150px'
            data="⏳ ĐANG CHỜ"
            description="Các đơn hàng đang chờ"
            color="#FFA07A"
            backgroundColor="#FFF5E6"
          />
          <GaugeCard
            percentage={percentages[1].toFixed(1)}
            width='350px'
            height='150px'
            data="🚚 ĐANG GIAO"
            description="Các đơn hàng đang giao"
            color="#87CEFA"
            backgroundColor="#E6F7FF"
          />
          <GaugeCard
            percentage={percentages[2].toFixed(1)}
            width='350px'
            height='150px'
            data="✅ ĐÃ GIAO"
            description="Các đơn hàng thành công"
            color="#90EE90"
            backgroundColor="#F0FFF0"
          />
          <GaugeCard
            percentage={percentages[3].toFixed(1)}
            width='350px'
            height='150px'
            data="❌ HỦY ĐƠN"
            description="Các đơn hàng đã hủy"
            color="#FFB6C1"
            backgroundColor="#FFF0F5"
          />
        </div>
      </div>
      <Footer />
    </>
  );
};

export default AnalyticsShipper;