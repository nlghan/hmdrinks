import * as React from 'react';
import { LineChart } from '@mui/x-charts/LineChart';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';

const monthData = {
  'Tháng 1': 31,
  'Tháng 2': (year) => (isLeapYear(year) ? 29 : 28),
  'Tháng 3': 31,
  'Tháng 4': 30,
  'Tháng 5': 31,
  'Tháng 6': 30,
  'Tháng 7': 31,
  'Tháng 8': 31,
  'Tháng 9': 30,
  'Tháng 10': 31,
  'Tháng 11': 30,
  'Tháng 12': 31,
};

const isLeapYear = (year) => {
  return (year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0);
};

export default function LineChartShip({ pData, xLabels, selectedMonth, setSelectedMonth, selectedYear, setSelectedYear }) {
  // Update xLabels based on selected month
  const daysInMonth = typeof monthData[selectedMonth] === 'function' ? monthData[selectedMonth](selectedYear) : monthData[selectedMonth];
  const updatedXLabels = Array.from({ length: daysInMonth }, (_, i) => `Ngày ${i + 1}`);

  return (
    <div>
      <h2>Biểu đồ trạng thái đơn</h2>
      <Select value={selectedMonth} onChange={(e) => setSelectedMonth(e.target.value)}>
        {Object.keys(monthData).map((month) => (
          <MenuItem key={month} value={month}>
            {month}
          </MenuItem>
        ))}
      </Select>
      <Select value={selectedYear} onChange={(e) => setSelectedYear(e.target.value)}>
        {Array.from({ length: 10 }, (_, index) => new Date().getFullYear() - index).map((year) => (
          <MenuItem key={year} value={year}>
            {year}
          </MenuItem>
        ))}
      </Select>
      <LineChart
        width={500}
        height={300}
        series={[{ data: pData, label: 'pv', yAxisId: 'leftAxisId' }]}
        xAxis={[{ scaleType: 'point', data: updatedXLabels }]} // Use updatedXLabels
        yAxis={[{ id: 'leftAxisId' }]}
      />
    </div>
  );
}
