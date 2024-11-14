import React, { useEffect } from 'react';
import Lottie from 'react-lottie';
import { useLocation, useNavigate } from 'react-router-dom';
import './PaymentStatus.css';

import successAnimation from '../../assets/lottie/payment_success.json';
import failureAnimation from '../../assets/lottie/payment_fail.json';

const PaymentStatus = () => {
    const { state } = useLocation();
    const navigate = useNavigate();
    const status = state ? state.status : 'failure';

    const successOptions = {
        loop: false,
        autoplay: true,
        animationData: successAnimation,
        rendererSettings: {
            preserveAspectRatio: 'xMidYMid slice'
        }
    };

    const failureOptions = {
        loop: false,
        autoplay: true,
        animationData: failureAnimation,
        rendererSettings: {
            preserveAspectRatio: 'xMidYMid slice'
        }
    };

    useEffect(() => {
        const timeout = setTimeout(() => {
            if (status === 'success') {
                navigate('/home'); // Navigate to home on success
            } else {
                navigate('/menu'); // Navigate to menu on failure
            }
        }, 3000); // Redirect after 3.5 seconds

        return () => clearTimeout(timeout);
    }, [status, navigate]);

    return (
        <div className={`payment-status ${status}`}>
            {status === 'success' ? (
                <div>
                    <Lottie options={successOptions} height={400} width={400} />
                    <p style={{ color: 'green' }}>Thanh toán thành công</p>
                </div>
            ) : (
                <div>
                    <Lottie options={failureOptions} height={400} width={400} />
                    <p style={{ color: 'red' }}>Thanh toán thất bại</p>
                </div>
            )}
        </div>
    );
};

export default PaymentStatus;
