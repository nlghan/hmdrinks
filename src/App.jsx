import React from 'react'
import './components/Navbar/Navbar.css'
import Navbar from './components/Navbar/Navbar'
import Footer from './components/Footer/Footer'

const App = () => {
  return (
    <div className='app'>
      <Navbar/>
      
      <Footer/>
    </div>
  )
}

export default App