import * as React from 'react';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import { ResponsiveChartContainer } from '@mui/x-charts/ResponsiveChartContainer';
import { LinePlot, MarkPlot } from '@mui/x-charts/LineChart';
import { BarPlot } from '@mui/x-charts/BarChart';
import { ChartsXAxis } from '@mui/x-charts/ChartsXAxis';
import { ChartsYAxis } from '@mui/x-charts/ChartsYAxis';
import { ChartsGrid } from '@mui/x-charts/ChartsGrid';
import { ChartsTooltip } from '@mui/x-charts/ChartsTooltip';
import RobotoFont from '../../assets/font/themify-icons/fonts/Roboto-Regular.ttf';
import Button from '@mui/material/Button';
// import html2canvas from 'html2canvas';
import { PDFDocument } from 'pdf-lib';
import html2canvas from 'html2canvas';
import 'jspdf-autotable';
import './CustomChart.css';
import domtoimage from 'dom-to-image';
import jsPDF from 'jspdf';
import axios from 'axios';
import robotoRegular from '../../assets/font/Roboto-Regular-normal';
import robotoBold from '../../assets/font/Roboto-Bold-bold'

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

const dataset = (month, year, successfulShipments, paymentAmounts) => {
  const daysInMonth = typeof monthData[month] === 'function' ? monthData[month](year) : monthData[month];
  return Array.from({ length: daysInMonth }, (_, index) => ({
    day: index + 1,
    precip: paymentAmounts[index] || 0,
    max: index < successfulShipments.length ? successfulShipments[index] : 0,
  }));
};

const series = [
  { type: 'line', dataKey: 'max', color: '#fe5f55' },
  { type: 'bar', dataKey: 'precip', color: '#bfdbf7', yAxisId: 'rightAxis' },
];

export default function CustomChart() {
  const [reverseX, setReverseX] = React.useState(false);
  const [reverseLeft, setReverseLeft] = React.useState(false);
  const [reverseRight, setReverseRight] = React.useState(false);
  const [selectedMonth, setSelectedMonth] = React.useState('Tháng 1');
  const [selectedYear, setSelectedYear] = React.useState(new Date().getFullYear());
  const [data, setData] = React.useState(dataset('Tháng 1', selectedYear, [], []));
  const [successfulShipments, setSuccessfulShipments] = React.useState([]);

  // Get Cookie by name
  const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
  };
  const fetchShipments = async (month, year) => {
    try {
      const token = getCookie('access_token');
      if (!token) {
        console.error("Không tìm thấy token xác thực.");
        return;
      }

      // Check if month is valid
      if (!monthData[month]) {
        console.error("Tháng không hợp lệ:", month);
        return;
      }

      const daysInMonth = typeof monthData[month] === 'function' ? monthData[month](year) : monthData[month];
      const shipmentCounts = Array(daysInMonth).fill(0);
      const paymentAmounts = Array(daysInMonth).fill(0);
      let totalPages = 1;
      let currentPage = 1;

      // Fetch shipments
      while (currentPage <= totalPages) {
        const response = await axios.get('http://localhost:1010/api/shipment/view/listByStatus', {
          params: {
            page: currentPage,
            limit: 100,
            status: 'SUCCESS',
          },
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });

        console.log('Dữ liệu shipment:', response.data);
        const shipments = response.data.listShipment;

        totalPages = response.data.totalPages || 1;

        for (const shipment of shipments) {
          const date = new Date(shipment.dateCreated);
          if (date.getMonth() === new Date(`${month} 1, ${year}`).getMonth() && date.getFullYear() === year) {
            shipmentCounts[date.getDate() - 1] += 1;

            // Fetch payment details for each shipment
            const paymentId = shipment.paymentId; // Assuming paymentId is available in shipment
            if (paymentId) {
              await fetchPaymentDetails(paymentId, date.getDate() - 1, paymentAmounts);
            }
          }
        }

        currentPage++;
      }

      setSuccessfulShipments(shipmentCounts);
      setData(dataset(month, year, shipmentCounts, paymentAmounts));
    } catch (error) {
      console.error('Lỗi khi gọi API:', error);
    }
  };

  // Function to fetch payment details
  const fetchPaymentDetails = async (paymentId, dayIndex, paymentAmounts) => {
    try {
      const token = getCookie('access_token');
      const response = await axios.get(`http://localhost:1010/api/payment/view/${paymentId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.status === 200) {
        const paymentAmount = response.data.amount; // Assuming the response contains the amount
        console.log('payment:', paymentAmount);
        paymentAmounts[dayIndex] += paymentAmount; // Accumulate the payment amount for the corresponding day
        console.log('Updated paymentAmounts:', paymentAmounts);
      }
    } catch (error) {
      console.error(`Lỗi khi gọi API cho paymentId ${paymentId}:`, error);
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', {
      currency: 'VND',   // Đơn vị tiền tệ Việt Nam Đồng
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(parseFloat(price));
  };

  const handleExportPDF = async () => {
    const chartElement = document.querySelector('.custom-chart-container');
    if (!chartElement) {
      console.error('chartElement không tồn tại.');
      return;
    }

    try {
      const dataUrl = await domtoimage.toPng(chartElement);

      // Tạo file PDF
      const pdf = new jsPDF();

      // Sử dụng font đã được thêm qua sự kiện addFonts
      pdf.setFont('Roboto-Bold', 'bold');

      // Tiêu đề chính
      pdf.setFontSize(18);
      pdf.setTextColor('#1d6587'); // Màu chữ xám đậm
      pdf.text('Báo cáo thống kê doanh thu', 105, 20, { align: 'center' }); // Căn giữa

      // Thông tin tháng/năm
      pdf.setFont('Roboto-Regular', 'normal');
      pdf.setFontSize(12);
      pdf.text(`Tháng: ${selectedMonth}`, 20, 30);
      pdf.text(`Năm: ${selectedYear}`, 20, 40);

      // Kiểm tra kích thước ảnh
      const imgWidth = 190;
      const imgHeight = (chartElement.offsetHeight * imgWidth) / chartElement.offsetWidth;

      // Thêm biểu đồ vào PDF
      pdf.addImage(dataUrl, 'PNG', 10, 50, imgWidth, imgHeight);

      // Tạo danh sách ngày trong tháng
      const daysInMonth = typeof monthData[selectedMonth] === 'function'
        ? monthData[selectedMonth](selectedYear)
        : monthData[selectedMonth];
      const dates = Array.from({ length: daysInMonth }, (_, i) => {
        const day = i + 1;
        const formattedDate = `${day < 10 ? '0' + day : day}/${selectedMonth < 10 ? '0' + selectedMonth : selectedMonth}/${selectedYear}`;
        return formattedDate;
      });

      // Tạo dữ liệu bảng
      const tableData = dates.map((date, index) => [
        index + 1, // Số thứ tự
        date, // Ngày/Tháng/Năm
        successfulShipments[index] || 0, // Số lượng đơn hàng
        data[index]?.precip ? `${formatPrice(data[index].precip)} VND` : '', // Giá trị đơn hàng
      ]);

      // Thêm trang mới
      pdf.addPage();
      pdf.setFont('Roboto-Bold', 'bold');
      pdf.setFontSize(18);
      pdf.setTextColor('#1d6587'); // Màu chữ xám đậm
      pdf.text('Dữ liệu chi tiết', 105, 20, { align: 'center' });

      // Tùy chỉnh bảng
      pdf.autoTable({
        head: [['STT', 'Ngày', 'Số đơn hàng', 'Doanh thu']],
        body: tableData,
        startY: 30,
        theme: 'striped', // Chủ đề: kẻ sọc
        styles: {
          font: 'Roboto-Regular', // Font trong bảng
          fontSize: 10, // Cỡ chữ trong bảng
          textColor: '#333333', // Màu chữ trong bảng
          halign: 'center', // Căn giữa nội dung
          valign: 'middle', // Căn giữa dọc
          lineColor: [44, 62, 80], // Màu đường viền
          lineWidth: 0.1, // Độ dày đường viền
        },
        headStyles: {
          fillColor: [230, 247, 255], // Màu xanh nhạt cho nền tiêu đề
          textColor: '#333333', // Màu chữ đậm
          fontSize: 11, // Kích thước font trong đầu bảng
          fontStyle: 'bold', // Kiểu chữ đậm cho tiêu đề
          halign: 'center', // Căn giữa tiêu đề bảng
        },
        bodyStyles: {
          fillColor: [255, 255, 255], // Nền trắng cho các ô dữ liệu
          textColor: '#333333', // Màu chữ trong bảng
          halign: 'center', // Căn giữa dữ liệu trong bảng
        },
        alternateRowStyles: {
          fillColor: [230, 247, 255], // Màu xanh nhạt cho các dòng kẻ sọc
        },
      });

      // Thêm ngày giờ hiện tại vào cuối trang
      const currentDate = new Date();
      const formattedDate = currentDate.toLocaleString('vi-VN', {
        weekday: 'long', // Ngày trong tuần
        year: 'numeric', // Năm
        month: 'long', // Tháng
        day: 'numeric', // Ngày
        hour: 'numeric', // Giờ
        minute: 'numeric', // Phút
        second: 'numeric', // Giây
      });


      pdf.setFont('Roboto-Regular', 'normal');
      pdf.setFontSize(10);
      pdf.setTextColor('#555555'); // Màu xám nhẹ cho ngày giờ
      pdf.text(`Xuất báo cáo: ${formattedDate}`, 105, pdf.internal.pageSize.height - 20, { align: 'center' });

      // Lưu file PDF
      pdf.save(`Bao_cao_${selectedMonth}_${selectedYear}.pdf`);
    } catch (error) {
      console.error('Lỗi khi xuất file PDF:', error);
    }
  };




  React.useEffect(() => {
    fetchShipments(selectedMonth, selectedYear);
  }, [selectedMonth, selectedYear]);

  const handleMonthChange = (event) => {
    const month = event.target.value;
    setSelectedMonth(month);
    setData(dataset(month, selectedYear, successfulShipments, []));
  };

  const handleYearChange = (event) => {
    const year = event.target.value;
    setSelectedYear(year);
    setData(dataset(selectedMonth, year, successfulShipments, []));
  };

  return (
    <Stack className="custom-chart" sx={{ width: '100%' }}>
      <div className="custom-chart-title">
        Biểu đồ doanh thu các ngày trong tháng
        <Select
          value={selectedMonth}
          onChange={handleMonthChange}
          sx={{ marginLeft: 2 }}
        >
          {Object.keys(monthData).map((month) => (
            <MenuItem key={month} value={month}>
              {month}
            </MenuItem>
          ))}
        </Select>
        <Select
          value={selectedYear}
          onChange={handleYearChange}
          sx={{ marginLeft: 2 }}
        >
          {Array.from({ length: 10 }, (_, index) => new Date().getFullYear() - index).map((year) => (
            <MenuItem key={year} value={year}>
              {year}
            </MenuItem>
          ))}
        </Select>
        <Button
          sx={{ marginLeft: 2 }}
          onClick={handleExportPDF}
        >
          Xuất PDF
        </Button>

      </div>
      <Stack direction="row">
        <FormControlLabel
          checked={reverseX}
          control={
            <Checkbox onChange={(event) => setReverseX(event.target.checked)} />
          }
          label="Đảo ngược biểu đồ"
          labelPlacement="end"
        />
        <FormControlLabel
          checked={reverseLeft}
          control={
            <Checkbox onChange={(event) => setReverseLeft(event.target.checked)} />
          }
          label="Đảo ngược trái"
          labelPlacement="end"
        />
        <FormControlLabel
          checked={reverseRight}
          control={
            <Checkbox onChange={(event) => setReverseRight(event.target.checked)} />
          }
          label="Đảo ngược phải"
          labelPlacement="end"
        />
      </Stack>
      <Box className="custom-chart-container" sx={{ width: '950px' }} >
        <ResponsiveChartContainer
          series={series}
          xAxis={[{
            scaleType: 'band',
            dataKey: 'day',
            label: 'Các ngày trong tháng',
            reverse: reverseX,
          }]}
          yAxis={[
            { id: 'leftAxis', reverse: reverseLeft },
            {
              id: 'rightAxis',
              reverse: reverseRight,
              tickFormatter: (value) => {
                if (value >= 1000000) {
                  return `${(value / 1000000).toFixed(1)}M`;
                } else if (value >= 1000) {
                  return `${(value / 1000).toFixed(1)}K`;
                }
                return value;
              }
            },
          ]}
          dataset={data}
          height={400}
        >

          <ChartsGrid horizontal />
          <BarPlot />
          <LinePlot />
          <MarkPlot />
          <ChartsXAxis />
          <ChartsYAxis axisId="leftAxis" />
          <ChartsYAxis axisId="rightAxis" position="right" />
          <ChartsTooltip />
        </ResponsiveChartContainer>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '-20px' }}>
          <h6 style={{ margin: 0 }}>Tổng đơn hàng </h6>
          <h6 style={{ margin: 0 }}>Tổng giá trị (VND)</h6>
        </div>
      </Box>
    </Stack>
  );
} 