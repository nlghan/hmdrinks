import React, { useEffect, useState } from 'react';
import { BarChart } from '@mui/x-charts/BarChart';
import './HorizontalBars.css';
import axios from 'axios';

const HorizontalBars = ({ width, height }) => {
  const [data, setData] = useState([]); // State to hold the fetched data
  const [error, setError] = useState("");
  const [data1, setData1] = useState([]); // State to hold the aggregated data for the chart

  const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
  };
  const fetchFavorites = async () => {
    try {
      const token = getCookie('access_token');
      if (!token) {
        setError("Bạn cần đăng nhập để xem thông tin này.");
        return;
      }

      // New API call to fetch total favorite counts
      const response = await axios.get(`http://localhost:1010/api/fav-item/list`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      const totalCountFavorites = response.data.totalCountFavoriteList;

      // Sort by totalCount in descending order and limit to top 10
      const sortedFavorites = totalCountFavorites
        .sort((a, b) => b.totalCount - a.totalCount)
        .slice(0, 10);

      // Prepare data1 with proName and totalCount
      const newData1 = sortedFavorites.map(item => ({
        product: item.proName,
        userFav: item.totalCount,
      }));

      console.log('Aggregated data for chart:', newData1);
      setData1(newData1);

    } catch (error) {
      console.error('Error fetching favorites:', error);
      setError("Không thể lấy thông tin yêu thích.");
    }
  };

  useEffect(() => {
    fetchFavorites(); // Call fetchFavorites when component mounts
  }, []);

  const chartSetting = {
    xAxis: [
      {
        label: 'Lượt yêu thích của người dùng',
        tick: {
          angle: -45,
        },
      },
    ],
    width: width,
    height: height,
  };

  return (
    <div className="horizontal-bars">
      <div className="horizontal-chart-title">Sản phẩm yêu thích HMDrinks</div>
      <div className="horizontal-chart-container">
        <BarChart
          className="bar-chart"
          dataset={data1} // Truyền data1 vào dataset
          yAxis={[{ scaleType: 'band', dataKey: 'product' }]}
          series={[
            {
              dataKey: 'userFav',
              label: 'Nước uống yêu thích',              
              valueFormatter: (value) => `${value} lượt yêu thích`,
              color: '#EEA2AD',
            },
          ]}
          layout="horizontal"
          margin={{ left: 115, right: 50, top: 20, bottom: 20 }} // Thêm khoảng trống bên trái
          {...chartSetting}
        />
      </div>
    </div>
  );
};

export default HorizontalBars;