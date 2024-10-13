import React from 'react'
import Footer from "../../components/Footer/Footer.jsx";
import Navbar from "../../components/Navbar/Navbar.jsx";
import Button from '@mui/material/Button';
import {styled} from '@mui/system'
const Home = () => {

    const MyButton = styled(Button)({
        display: 'flex',
        padding: '10px 20px',
        alignItems: 'center', // Phải dùng camelCase cho CSS properties
        borderRadius: '12px',
        border: '2px solid #2E2F35',
        boxShadow: '3px 3px 0px 0px #2E2F35',
        fontSize: '16px',
        cursor: 'pointer',
    });
    return (
        <>
            <Navbar />
            <div>Home</div>
            <MyButton>Hello world</MyButton>
            <Footer />
        </>
    )
}

export default Home