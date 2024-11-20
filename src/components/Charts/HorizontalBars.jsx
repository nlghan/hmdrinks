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
      const token = getCookie('access_token'); // Assuming you have a function to get the cookie
      if (!token) {
        setError("Bạn cần đăng nhập để xem thông tin này.");
        return;
      }

      const firstPageUrl = `http://localhost:1010/api/admin/listUser?page=1&limit=100`; // Adjust limit as needed
      const firstPageResponse = await axios.get(firstPageUrl, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      const totalPages = firstPageResponse.data.totalPage;
      let allUsers = [];

      // Fetch all users
      for (let page = 1; page <= totalPages; page++) {
        const url = `http://localhost:1010/api/admin/listUser?page=${page}&limit=100`;
        const response = await axios.get(url, {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });
        allUsers = allUsers.concat(response.data.detailUserResponseList || []);
      }

      // Fetch favorites for each user
      const favoritesPromises = allUsers.map(async (user) => {
        try {
          const favResponse = await axios.get(`http://localhost:1010/api/fav/list-fav/${user.userId}`, {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          });
          console.log(`Favorites for user ${user.userId}:`, favResponse.data); // Log dữ liệu yêu thích cho từng người dùng
          return { userId: user.userId, favId: favResponse.data.favId || 0 }; // Lưu userId và favId
        } catch (error) {
          if (error.response && error.response.status === 404) {
            console.warn(`No favorites found for user ${user.userId}, skipping...`); // Log cảnh báo nếu không tìm thấy
          } else {
            console.error(`Error fetching favorites for user ${user.userId}:`, error); // Log lỗi khác
          }
          return null; // Trả về null nếu có lỗi
        }
      });

      const favoritesData = await Promise.all(favoritesPromises);
      const validFavorites = favoritesData.filter(fav => fav !== null); // Lọc ra các kết quả hợp lệ

      // Gọi API để lấy danh sách các mục yêu thích cho từng favId
      const itemsPromises = validFavorites.map(async (fav) => {
        const itemsResponse = await axios.get(`http://localhost:1010/api/fav/list-favItem/${fav.favId}`, {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });
        console.log(`Items for favorite ID ${fav.favId}:`, itemsResponse.data); // Log dữ liệu các mục yêu thích

        // Lưu các favItemId tương ứng với từng proId
        const productCounts = {};
        itemsResponse.data.favouriteItemResponseList.forEach(item => {
          const { favItemId, proId } = item;
          if (!productCounts[proId]) {
            productCounts[proId] = { count: 0, favItemId: favItemId };
          }
          productCounts[proId].count += 1; // Tăng số lượng cho proId
        });

        return { userId: fav.userId, productCounts }; // Lưu userId và danh sách số lượng sản phẩm
      });

      const itemsData = await Promise.all(itemsPromises);
      console.log('All items fetched:', itemsData); // Log tất cả các mục đã được lấy

      // Gọi API để lấy proName cho từng proId
      const productPromises = itemsData.flatMap(async (data) => {
        const productNames = {};
        for (const proId in data.productCounts) {
          const productResponse = await axios.get(`http://localhost:1010/api/product/view/${proId}`, {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          });
          if (productResponse.status === 200) {
            const proName = productResponse.data.proName;
            productNames[proId] = { proName, count: data.productCounts[proId].count }; // Lưu proName và số lượng
          }
        }
        return { userId: data.userId, productNames }; // Lưu userId và danh sách proName
      });

      const finalProductData = await Promise.all(productPromises);
      console.log('Final product data:', finalProductData); // Log dữ liệu sản phẩm cuối cùng

      // Xử lý finalProductData để cập nhật data1
      const aggregatedData = {};

      finalProductData.forEach(userData => {
        const { productNames } = userData;
        for (const proId in productNames) {
          const { proName, count } = productNames[proId];
          if (!aggregatedData[proName]) {
            aggregatedData[proName] = { userFav: 0 };
          }
          aggregatedData[proName].userFav += count; // Cộng dồn số lượng yêu thích
        }
      });

      // Chuyển đổi aggregatedData thành mảng cho data1
      const newData1 = Object.keys(aggregatedData).map(proName => ({
        product: proName,
        userFav: aggregatedData[proName].userFav,
      }));

      console.log('Aggregated data for chart:', newData1); // Log dữ liệu đã được tổng hợp
      setData1(newData1); // Set the aggregated data for the chart

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