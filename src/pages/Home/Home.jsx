import React from 'react';
import Footer from "../../components/Footer/Footer.jsx";
import Navbar from "../../components/Navbar/Navbar.jsx";
import "./Home.css"

const Home = () => {
    return (
        <div className="container-home"> {/* This is the container wrapping everything */}
            <Navbar />
            <div className="content-home">
                <h1>Home</h1>
                <h1>Home</h1>
                <h1>Home</h1>
                <h1>Home</h1>
                <h1>Home</h1>
                <h1>Home</h1>
                <h1>Home</h1>
                <h1>Home</h1>
                <h1>Home</h1>
                <h1>Home</h1>
                <h1>Home</h1>
                <h1>Home</h1>
                {/* Other content of the home page can go here */}
            </div>
            <Footer />
        </div>
    );
};

export default Home;
