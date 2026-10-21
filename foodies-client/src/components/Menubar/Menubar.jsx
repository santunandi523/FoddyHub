import React, { useContext, useState, useRef, useEffect } from 'react';
import './Menubar.css';
import { assets } from '../../assets/assets';
import { Link, useNavigate } from 'react-router-dom';
import { StoreContext } from '../context/StoreContext';

const Menubar = () => {
  const [active, setActive] = useState('home');
  const [dropOpen, setDropOpen] = useState(false);

  const navigate = useNavigate();
  const { quantities, token, setToken ,setQuantities} = useContext(StoreContext);
  const uniqueItemsInCart = Object.values(quantities || {}).filter(
    (qty) => qty > 0
  ).length;

  const dropRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handler = (e) => {
      if (dropRef.current && !dropRef.current.contains(e.target)) {
        setDropOpen(false);
      }
    };

    document.addEventListener('mousedown', handler);

    return () => {
      document.removeEventListener('mousedown', handler);
    };
  }, []);

  const logout = () => {
    localStorage.removeItem('token');
    setToken("");
    setQuantities({});
    setDropOpen(false);
    navigate("/");
  };

  const goToOrders = () => {
    setDropOpen(false);
    navigate('/orders');
  };
  const goToMyOrders=()=>{
    setDropOpen(false);
    navigate('/myorders');
  }

  return (
    <nav className="navbar navbar-expand-lg site-navbar">
      <div className="container">

        {/* Brand */}
        <Link
          className="navbar-brand p-0"
          to="/"
          onClick={() => setActive('home')}
        >
          <img
            src={assets.foodies}
            alt="Foodies"
            height={44}
            width={112}
          />
        </Link>

        {/* Mobile Toggle */}
        <button
          className="navbar-toggler border-0"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarMain"
          aria-controls="navbarMain"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarMain">

          {/* Navigation */}
          <ul className="navbar-nav me-auto mb-2 mb-lg-0 gap-1">

            <li className="nav-item">
              <Link
                className={active === 'home' ? 'nav-link active' : 'nav-link'}
                to="/"
                onClick={() => setActive('home')}
              >
                Home
              </Link>
            </li>

            <li className="nav-item">
              <Link
                className={active === 'explore' ? 'nav-link active' : 'nav-link'}
                to="/explore"
                onClick={() => setActive('explore')}
              >
                Explore
              </Link>
            </li>

            <li className="nav-item">
              <Link
                className={active === 'contactUs' ? 'nav-link active' : 'nav-link'}
                to="/contact"
                onClick={() => setActive('contactUs')}
              >
                Contact Us
              </Link>
            </li>

            <li className="nav-item">
              <Link
                className={active === 'assistant' ? 'nav-link active fw-bold' : 'nav-link fw-semibold'}
                to="/assistant"
                onClick={() => setActive('assistant')}
              >
                <i className="bi bi-stars text-warning me-1"></i>
                Foodie AI
              </Link>
            </li>

          </ul>

          {/* Right Side */}
          <div className="d-flex align-items-center gap-3">

            {/* Cart */}
            <Link to="/cart" style={{ textDecoration: 'none' }}>
              <div className="cart-wrapper">
                <img
                  src={assets.cart}
                  alt="Cart"
                  height={28}
                  width={28}
                />

                {uniqueItemsInCart > 0 && (
                  <span className="cart-badge">
                    {uniqueItemsInCart}
                  </span>
                )}
              </div>
            </Link>

            {/* Login/User */}
            {token ? (
              <div className="user-menu" ref={dropRef}>

                {/* Avatar Button */}
                <button
                  className={`user-avatar-btn ${dropOpen ? 'open' : ''}`}
                  onClick={() => setDropOpen(!dropOpen)}
                >
                  <img
                    src={assets.profile}
                    alt="Profile"
                    className="user-avatar-circle"
                  />

                  <i
                    className={`bi bi-chevron-down user-chevron ${
                      dropOpen ? 'rotated' : ''
                    }`}
                  ></i>
                </button>

                {/* Dropdown */}
                <div className={`user-dropdown ${dropOpen ? 'open' : ''}`}>

                  <div className="user-dropdown-header">

                    <img
                      src={assets.profile}
                      alt="Profile"
                      className="user-avatar-large"
                    />

                    <span className="user-dropdown-greeting">
                      My Account
                    </span>

                  </div>

                  <div className="user-dropdown-divider"></div>
                    <button
                    className="user-dropdown-item"
                    onClick={goToMyOrders}
                  >
                    <i className="bi bi-bag-check"></i>
                    <span>My Orders</span>
                  </button>


                  <button
                    className="user-dropdown-item"
                    onClick={goToOrders}
                  >
                    <i className="bi bi-bag-check"></i>
                    <span>Place Order</span>
                  </button>

                  <div className="user-dropdown-divider"></div>

                  <button
                    className="user-dropdown-item danger"
                    onClick={logout}
                  >
                    <i className="bi bi-box-arrow-right"></i>
                    <span>Logout</span>
                  </button>

                </div>

              </div>
            ) : (
              <>
                <Link className="btn-nav-login" to="/login">
                  Login
                </Link>

                <Link className="btn-nav-register" to="/register">
                  Register
                </Link>
              </>
            )}

          </div>
        </div>
      </div>
    </nav>
  );
};

export default Menubar;