import React, { createContext, useContext, useState } from 'react';

// Create context to share payment status across components
const PaymentStatusContext = createContext();

// Custom hook to use the PaymentStatusContext
export const usePaymentStatus = () => {
  return useContext(PaymentStatusContext);
};

export const PaymentStatusProvider = ({ children }) => {
  const [paymentStatus, setPaymentStatus] = useState(null); // Stores 'success' or 'failure'
  const [isPaymentSuccess, setIsPaymentSuccess] = useState(false); // True if payment is successful

  const updateStatus = (status) => {
    setPaymentStatus(status); // Update status
    if (status === 'success') {
      setIsPaymentSuccess(true); // Set success flag to true
    } else {
      setIsPaymentSuccess(false); // Set success flag to false
    }
  };

  return (
    <PaymentStatusContext.Provider value={{ paymentStatus, isPaymentSuccess, updateStatus }}>
      {children}
    </PaymentStatusContext.Provider>
  );
};
