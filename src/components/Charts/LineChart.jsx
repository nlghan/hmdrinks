import React, { useEffect, useState } from 'react';
import { LineChart } from '@mui/x-charts/LineChart';
import axios from 'axios';
import './LineChart.css';

export default function LineChartComponent() {
  const [data, setData] = useState({ xAxis: [], series: [] });

  const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
  };

  useEffect(() => {
    const fetchData = async () => {
      const token = getCookie('access_token');
      if (!token) {
        console.error("Bạn cần đăng nhập để xem thông tin này.");
        return;
      }

      try {
        const response = await axios.get('http://localhost:1010/api/product/list-with-avg-rating?page=1&limit=100', {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });

        const products = response.data.products;

        const xAxisData = [0];
        const seriesData = [0];

        products.forEach((product, index) => {
          const proId = index + 1;
          const avgRating = Math.min(Math.max(product.avgRating, 0), 5);

          if (avgRating > 0) {
            xAxisData.push(proId);
            seriesData.push(avgRating);
          }
        });

        setData({ xAxis: xAxisData, series: [{ data: seriesData, area: true, baseline: 'min' }] });
      } catch (error) {
        console.error("Có lỗi xảy ra khi gọi API:", error);
      }
    };

    fetchData();
  }, []);

  return (
    <div className="linechart-container">
      <style>
        {`
          .MuiAreaElement-root {
            stroke: #ff5733;
            fill: #a1d8e0;
          }
        `}
      </style>
      <LineChart
        xAxis={[{ data: data.xAxis}]}
        series={data.series}
        width={600}
        height={550}
      />
    </div>
  );
}
