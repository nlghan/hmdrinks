import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthProvider'; // Import AuthProvider
import 'bootstrap/dist/css/bootstrap.min.css';
import '@popperjs/core/dist/umd/popper.min.js'

ReactDOM.render(
  <BrowserRouter>
    <AuthProvider>
      <App />
    </AuthProvider>
  </BrowserRouter>,
  document.getElementById('root')
);
