import React from 'react';
import './PostCard.css';

const PostCard = ({ image, title, description, buttonText, onClick }) => {
  return (
    <div className="soda-ad">
      <img src={image} alt={title} />
      <div className="soda-ad-content">
        <h2>{title}</h2>
        <p>{description}</p>
        <button className="soda-ad-button" onClick={onClick}>
          {buttonText}
        </button>
      </div>
    </div>
  );
};

export default PostCard;
