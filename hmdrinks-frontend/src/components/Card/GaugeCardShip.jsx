import React, { useEffect, useState } from 'react';
import { Gauge, gaugeClasses } from '@mui/x-charts/Gauge';
import './GaugeCardShip.css';

const GaugeCard = ({ percentage, data, description, number1, color, backgroundColor, number, height, width }) => {
  const [value, setValue] = useState(0);
  const [displayNumber, setDisplayNumber] = useState(0);

  useEffect(() => {
    const duration = 1000;
    const stepTime = 50;
    const steps = duration / stepTime;
    const increment = number / steps;

    const interval = setInterval(() => {
      setDisplayNumber((prev) => {
        if (prev < number) {
          return Math.min(prev + increment, number);
        } else {
          clearInterval(interval);
          return prev;
        }
      });
    }, stepTime);

    return () => clearInterval(interval);
  }, [number]);

  useEffect(() => {
    const duration = 1000;
    const stepTime = 50;
    const steps = duration / stepTime;
    const increment = percentage / steps;

    const interval = setInterval(() => {
      setValue((prev) => {
        if (prev < percentage) {
          return Math.min(prev + increment, percentage);
        } else {
          clearInterval(interval);
          return prev;
        }
      });
    }, stepTime);

    return () => clearInterval(interval);
  }, [percentage]);

  const settings = {
    width: 95,
    height: 95,
    value: value,
  };

  return (
    <div className="ship-gauge-card" style={{ backgroundColor, height, width }}>
      <div className="ship-gauge-container">
        <Gauge
          {...settings}
          cornerRadius="50%"
          sx={(theme) => ({
            [`& .${gaugeClasses.valueText}`]: {
              fontSize: 24,
            },
            [`& .${gaugeClasses.valueArc}`]: {
              fill: color,
              transition: 'fill 0.3s ease-in-out',
            },
            [`& .${gaugeClasses.referenceArc}`]: {
              fill: backgroundColor,
            },
          })}
        />
      </div>
      <div className="ship-gauge-info">
        {number !== undefined ? (
          <h4>{displayNumber} VND</h4>
        ) : (
          <h4>{data}</h4>
        )}
        <p>{number1} {description}</p>
      </div>
    </div>
  );
};

export default GaugeCard;