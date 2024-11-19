import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import NavbarShipper from '../../components/Navbar/NavbarShipper';
import Footer from '../../components/Footer/Footer';
import LoadingAnimation from '../../components/Animation/LoadingAnimation';
import ErrorMessage from '../../components/Animation/ErrorMessage';
import './ShipmentDetail.css';

const ShipmentDetail = () => {
    const { shipmentId } = useParams(); // Nhận shipmentId từ URL
    const [shipment, setShipment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchShipmentDetail = async () => {
            setLoading(true);
            setError(null);

            const token = document.cookie.split('; ').find(row => row.startsWith('access_token='))?.split('=')[1];
            if (!token) {
                setError('Vui lòng đăng nhập lại.');
                setLoading(false);
                return;
            }

            try {
                const response = await axios.get(
                    `${import.meta.env.VITE_API_BASE_URL}/shipment/detail/${shipmentId}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                setShipment(response.data);
                setLoading(false);
            } catch (err) {
                console.error('Error fetching shipment detail:', err);
                setError('Không thể tải thông tin đơn hàng.');
                setLoading(false);
            }
        };

        fetchShipmentDetail();
    }, [shipmentId]);

    if (loading) {
        return <LoadingAnimation />;
    }

    if (error) {
        return <ErrorMessage message={error} />;
    }

    return (
        <>
            <NavbarShipper currentPage="Chi Tiết Đơn Hàng" />
            <div className="shipment-detail-container">
                <h1>Chi Tiết Đơn Hàng</h1>
                <div className="shipment-detail-card">
                    <h2>{shipment.customerName}</h2>
                    <p><strong>Địa chỉ giao hàng:</strong> {shipment.address}</p>
                    <p><strong>Số điện thoại:</strong> {shipment.phoneNumber}</p>
                    <p><strong>Trạng thái:</strong> {shipment.status}</p>
                    <p><strong>Ngày tạo:</strong> {shipment.dateCreated}</p>
                    <p><strong>Ghi chú:</strong> {shipment.notes || 'Không có ghi chú.'}</p>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px' }}>
                        <button className="btn-back" onClick={() => navigate(-1)}>
                            Quay lại
                        </button>
                        <button
                            className="btn-complete"
                            onClick={() => console.log(`Hoàn thành đơn hàng: ${shipmentId}`)}
                        >
                            Hoàn thành
                        </button>
                    </div>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default ShipmentDetail;
