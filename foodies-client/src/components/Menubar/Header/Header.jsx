import React from 'react'
import './Header.css'
import { Link } from 'react-router-dom'

const Header = () => {
  return (
    <main className='container'>
      <div className='hero-section'>
        <div className="hero-badge">🔥 Fresh &amp; Delicious</div>
        <h1 className='hero-title'>
          Order your <span>favourite food</span><br />right here
        </h1>
        <p className='hero-sub'>
          Discover the best foods and drinks from West Bengal — delivered hot to your door.
        </p>
        <Link to="/explore" className='btn-hero'>
          Explore Menu <i className="bi bi-arrow-right"></i>
        </Link>
        <div className='hero-stats'>
          <div className='hero-stat'>
            <div className='hero-stat-num'>50+</div>
            <div className='hero-stat-label'>Dishes</div>
          </div>
          <div className='hero-stat'>
            <div className='hero-stat-num'>4.8★</div>
            <div className='hero-stat-label'>Rating</div>
          </div>
          <div className='hero-stat'>
            <div className='hero-stat-num'>30min</div>
            <div className='hero-stat-label'>Delivery</div>
          </div>
        </div>
      </div>
    </main>
  )
}

export default Header
