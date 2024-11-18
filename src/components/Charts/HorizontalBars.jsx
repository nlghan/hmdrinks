import * as React from 'react';
import { BarChart } from '@mui/x-charts/BarChart';
import './HorizontalBars.css';

const HorizontalBars = ({ width, height, data }) => {
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
          dataset={data}
          yAxis={[{ scaleType: 'band', dataKey: 'product',  }]}
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