import * as React from 'react';
import { PieChart } from '@mui/x-charts/PieChart';
import './PieChart.css';
import axios from 'axios';
import axiosInstance from '../../utils/axiosConfig';

const valueFormatter = (value) => `${Number(value)}%`;


export default function CustomPieChart() {
    const [percentages, setPercentages] = React.useState([0, 0, 0, 0]);

    const getCookie = (name) => {
        const value1 = `; ${document.cookie}`;
        const parts = value1.split(`; ${name}=`);

        return parts.length === 2 ? parts.pop().split(';').shift() : null;
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
                const response = await axiosInstance.get('http://localhost:1010/api/shipment/view/listByStatus', {
                    params: {
                        page: 1,
                        limit: 100,
                        status: status,
                    },
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });
                return response.data.total || 0; // Giả sử API trả về tổng số lượng trong trường `total`
            }));

            const total = counts.reduce((acc, count) => acc + count, 0);
            const newPercentages = counts.map(count => (total > 0 ? (count / total) * 100 : 0));
            setPercentages(newPercentages);
        } catch (error) {
            console.error('Lỗi khi gọi API:', error);
        }
    };

    React.useEffect(() => {
        fetchShipmentCounts();
    }, []);

    if (percentages.every(value => value === 0)) {
        return <div>Đang tải dữ liệu...</div>;
    }

    const sampleData = [
        { name: 'Đang chờ', value: percentages[0] || 0, label: 'Đang chờ', color: '#f76c34'},
        { name: 'Đang giao', value: percentages[1] || 0, label: 'Đang giao' , color: '#3ab2fc'},
        { name: 'Thành công', value: percentages[2] || 0, label: 'Thành công' , color: '#29ab29'},
        { name: 'Đã hủy', value: percentages[3] || 0, label: 'Đã hủy' , color: '#c24255'},
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
                            data: sampleData,
                            highlightScope: { fade: 'global', highlight: 'item' },
                            faded: { innerRadius: 30, additionalRadius: -30, color: 'gray' },
                            
                        },
                    ]}
                    height={250}
                />
            </div>
        </div>
    );
}
