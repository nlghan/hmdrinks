import React from 'react';
import LoadingAnimation from './LoadingAnimation';
import './ErrorMessage.css'; // Import file CSS

const ErrorMessage = ({ path, message }) => {
  return (
    <div className="errorContainer">
      <div className="animationContainer">
        <LoadingAnimation animationPath={path}/>
      </div>
      <div className="messageContainer">
        <marquee width="50%" direction="left" scrollamount="5" behavior="scroll">
          {message}
        </marquee>
      </div>
    </div>
  );
};

export default ErrorMessage;
