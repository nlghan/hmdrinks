import * as React from 'react';
import { PieChart } from '@mui/x-charts/PieChart';
import './CustomChart.css';
import axios from 'axios';

// Hàm định dạng giá trị
const valueFormatter = (value) => {
    // Kiểm tra xem giá trị có phải là số không
    if (typeof value === 'number') {
        return `${value.toFixed(2)}%`; // Định dạng giá trị với 2 chữ số thập phân
    }
    return '0%'; // Trả về 0% nếu không phải là số
}

export default function CustomPieChart() {
    const [percentages, setPercentages] = React.useState([0, 0, 0, 0]); // [waiting, shipping, success, cancelled]

    const getCookie = (name) => {
        const value1 = `; ${document.cookie}`;
        const parts = value1.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const fetchShipmentCounts = async () => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                console.error("Không tìm thấy token xác thực.");
                return;
            }

            const statuses = ['WAITING', 'SHIPPING', 'SUCCESS', 'CANCELLED'];
            const counts = await Promise.all(statuses.map(async (status) => {
                const response = await axios.get('http://localhost:1010/api/shipment/view/listByStatus', {
                    params: {
                        page: 1,
                        limit: 100,
                        status: status,
                    },
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });
                return response.data.total; // Giả sử API trả về tổng số lượng trong trường `total`
            }));

            const total = counts.reduce((acc, count) => acc + count, 0);
            const newPercentages = counts.map(count => (total > 0 ? (count / total) * 100 : 0));
            setPercentages(newPercentages);
            console.log(typeof percentages[0]);
        } catch (error) {
            console.error('Lỗi khi gọi API:', error);
        }
    };

    React.useEffect(() => {
        fetchShipmentCounts(); // Gọi hàm khi component mount
    }, []);

    // Dữ liệu mẫu cho biểu đồ hình tròn, sử dụng các giá trị phần trăm
    const sampleData = [
        { name: 'Đang chờ', value: percentages[0] },
        { name: 'Đang giao', value: percentages[1] },
        { name: 'Thành công', value: percentages[2] },
        { name: 'Đã hủy', value: percentages[3] },
    ];

    return (
        <div className="pie-chart">
            <div className="pie-chart-title">
                Phân phối trạng thái đơn hàng
            </div>
            <div className="pie-chart-container">
                <PieChart
                    series={[
                        {
                            data: sampleData, // Sử dụng dữ liệu mẫu với các giá trị phần trăm
                            highlightScope: { fade: 'global', highlight: 'item' },
                            faded: { innerRadius: 30, additionalRadius: -30, color: 'gray' },
                            valueFormatter,
                        },
                    ]}
                    height={200}
                />
            </div>
        </div>
    );
}
