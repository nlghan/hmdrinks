import React, { useEffect, useState } from 'react';
import './Analytics.css';
import GaugeCard from '../../components/Card/GaugeCardAna';
import Header from '../../components/Header/Header';
import CustomChart from '../../components/Charts/CustomChart';
import HorizontalBars from '../../components/Charts/HorizontalBars';
import CustomPieChart from '../../components/Charts/PieChart'; 
import axios from 'axios';

const Analytics = () => {
    const [waitingCount, setWaitingCount] = useState(0);
    const [shippingCount, setShippingCount] = useState(0);
    const [successCount, setSuccessCount] = useState(0);
    const [cancelledCount, setCancelledCount] = useState(0);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [percentages, setPercentages] = useState([0, 0, 0, 0]); // [waiting, shipping, success, cancelled]
    const [isLoading, setIsLoading] = useState(true);

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
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
                return response.data.total;
            }));

            setWaitingCount(counts[0]);
            setShippingCount(counts[1]);
            setSuccessCount(counts[2]);
            setCancelledCount(counts[3]);

            const total = counts.reduce((acc, count) => acc + count, 0);
            const newPercentages = counts.map(count => (total > 0 ? (count / total) * 100 : 0));
            setPercentages(newPercentages);
            setIsLoading(false);

        } catch (error) {
            console.error('Lỗi khi gọi API:', error);
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchShipmentCounts(); // Gọi hàm khi component mount
    }, []);

    return (
        <div className="analytics">
            <Header isMenuOpen={isMenuOpen} toggleMenu={() => setIsMenuOpen(!isMenuOpen)} title="Thống kê" />
            {isLoading ? (
                    <div className="loading-animation">
                        <div className="loading-modal">
                            <div className="loading-spinner">
                                <div className="spinner"></div>
                            </div>
                            <h3>Đang xử lý dữ liệu...</h3>
                            <p>Vui lòng đợi trong giây lát</p>
                        </div>
                    </div>
            ) : (
                <>
                    <div className="analytics-row1">
                        <GaugeCard
                            percentage={percentages[0].toFixed(1)} // Phần trăm cho trạng thái WAITING
                            width='280px'
                            height='180px'
                            data="⏳ĐANG CHỜ"
                            description="Các đơn hàng đang chờ"
                            color="#FFA07A" // Pastel Orange
                            backgroundColor="#FFF5E6" // Light Pastel Orange
                        />
                        <GaugeCard
                            percentage={percentages[1].toFixed(1)} // Phần trăm cho trạng thái SHIPPING
                            width='280px'
                            height='180px'
                            data="🚚 ĐANG GIAO"
                            description="Các đơn hàng đang giao"
                            color="#87CEFA" // Pastel Blue
                            backgroundColor="#E6F7FF" // Light Pastel Blue
                        />
                        <GaugeCard
                            percentage={percentages[2].toFixed(1)} // Phần trăm cho trạng thái SUCCESS
                            width='280px'
                            height='180px'
                            data="✅ ĐÃ GIAO"
                            description="Các đơn hàng thành công"
                            color="#90EE90" // Pastel Green
                            backgroundColor="#F0FFF0" // Light Pastel Green
                        />
                        <GaugeCard
                            percentage={percentages[3].toFixed(1)} // Phần trăm cho trạng thái CANCELLED
                            width='280px'
                            height='180px'
                            data="❌ HỦY ĐƠN"
                            description="Các đơn hàng đã hủy"
                            color="#FFB6C1" // Pastel Pink
                            backgroundColor="#FFF0F5" // Light Pastel Pink
                        />
                        <CustomPieChart />
                    </div>
                    <div className="analytics-row">
                        <CustomChart />
                        <HorizontalBars width={600} height={500}/>
                    </div>
                </>
            )}
        </div>
    );
};

export default Analytics;
