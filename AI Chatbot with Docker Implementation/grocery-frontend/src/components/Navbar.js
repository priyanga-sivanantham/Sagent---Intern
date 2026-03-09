import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { ShoppingCart, LogOut, User, Zap } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './Navbar.css';

export default function Navbar() {
  const { user, logout } = useAuth();
  const { totalItems }   = useCart();
  const navigate         = useNavigate();
  const location         = useLocation();

  const handleLogout = () => { logout(); navigate('/login'); };

  if (!user) return null;

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        {/* Logo */}
        <Link to="/dashboard" className="navbar-logo">
          <div className="logo-icon"><Zap size={18} /></div>
          <span>Quickcart</span>
        </Link>

        {/* Delivery tag */}
        <div className="delivery-chip">
          <span className="delivery-dot" />
          Delivery in <strong>10 mins</strong>
        </div>

        {/* Right actions */}
        <div className="navbar-actions">
          <div className="user-chip">
            <User size={15} />
            <span>{user.customerName?.split(' ')[0]}</span>
          </div>

          <Link to="/cart" className={`cart-btn ${location.pathname === '/cart' ? 'active' : ''}`}>
            <ShoppingCart size={20} />
            {totalItems > 0 && <span className="cart-badge">{totalItems}</span>}
          </Link>

          <button className="logout-btn" onClick={handleLogout} title="Logout">
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </nav>
  );
}
