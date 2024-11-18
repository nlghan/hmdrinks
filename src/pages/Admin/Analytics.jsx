import React, { useEffect, useState } from 'react';
import './Analytics.css';
import { assets } from '../../assets/assets';
import Cookies from 'js-cookie'; 
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Header from '../../components/Header/Header';
import GaugeCard from '../../components/Card/GaugeCardRes';
import HorizontalBars from '../../components/Charts/HorizontalBars';
import CustomChart from '../../components/Charts/CustomChart';

const Analytics = () => {
    const navigate = useNavigate();
    const [isMenuOpen, setIsMenuOpen] = useState(false); 
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        const loggedIn = sessionStorage.getItem("isLoggedIn");
        setIsLoggedIn(loggedIn === "true");
    }, []);

    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    const sampleData = [
        { product: 'Sinh tố dâu', userFav: 5 },
        { product: 'Nước ép cam', userFav: 20 },
        { product: 'Cà phê sữa', userFav: 30 },
        { product: 'Bạc xĩu', userFav: 50 },
        { product: 'Trà sữa khoai môn', userFav: 15 },
        { product: 'Sinh tố kiwi', userFav: 5 },
        { product: 'Nước ép táo', userFav: 20 },

    ];

    return (
        <div className="analytics">
            <Header isMenuOpen={isMenuOpen} toggleMenu={toggleMenu} title="Analytics" />
            <div className={`analytics-row ${isMenuOpen ? 'dimmed' : ''}`}>
                <GaugeCard 
                    percentage={75}
                    width='350px'
                    height='180px'
                    number={350}
                    description="This is a sample description"
                    color='#efa0ed'
                    backgroundColor='#f9f9f9'
                />
                <HorizontalBars 
                    width={500}
                    height={400}
                    data={sampleData}
                />
                <CustomChart />
            </div>
        </div>
    );
};

export default Analytics;
