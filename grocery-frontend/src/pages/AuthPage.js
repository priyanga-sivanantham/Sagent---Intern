import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Zap, User, Lock, Mail, Phone, MapPin } from 'lucide-react';
import toast from 'react-hot-toast';
import { registerCustomer, loginCustomer, createCart } from '../api/api';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

export default function AuthPage() {

  const [mode, setMode] = useState('login');
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    customerName: '',
    email: '',
    password: '',
    customerPhone: '',
    customerAddress: '',
  });

  const set = (key) => (e) =>
    setForm(prev => ({ ...prev, [key]: e.target.value }));


  // ================= SUBMIT =================
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {

      // ================= REGISTER =================
      if (mode === 'register') {

        const res = await registerCustomer(form);

        // create cart automatically
        await createCart(res.data.customerId);

        login(res.data);

        toast.success(`Welcome, ${res.data.customerName}! 🎉`);

        navigate('/dashboard');
      }

      // ================= LOGIN =================
      else {

        const res = await loginCustomer({
          email: form.email,
          password: form.password
        });

        if (!res.data || !res.data.customerId) {
          toast.error("Invalid email or password");
          setLoading(false);
          return;
        }

        login(res.data);

        toast.success(`Welcome back, ${res.data.customerName}! 👋`);

        navigate('/dashboard');
      }

    } 
    catch (err) {

      console.log("AUTH ERROR:", err);

      // ✅ show backend error message
      if (err.response && err.response.data) {
        toast.error(err.response.data);
      } else {
        toast.error("Server not reachable");
      }
    }

    setLoading(false);
  };


  return (
    <div className="auth-page">

      {/* LEFT SIDE */}
      <div className="auth-left">
        <div className="auth-brand">
          <div className="auth-logo"><Zap size={28} /></div>
          <h1>Quickcart</h1>
          <p>Fresh groceries at lightning speed ⚡</p>
        </div>

        <div className="auth-features">
          {[
            '🥦 Fresh produce daily',
            '⚡ 10-minute delivery',
            '💳 Secure payments',
            '🎁 Exclusive deals'
          ].map(f => (
            <div key={f} className="auth-feature">{f}</div>
          ))}
        </div>

        <div className="auth-blobs">
          <div className="blob b1" />
          <div className="blob b2" />
        </div>
      </div>


      {/* RIGHT SIDE */}
      <div className="auth-right">
        <div className="auth-card">

          {/* Tabs */}
          <div className="auth-tabs">
            <button
              className={mode === 'login' ? 'active' : ''}
              onClick={() => setMode('login')}
            >
              Sign In
            </button>

            <button
              className={mode === 'register' ? 'active' : ''}
              onClick={() => setMode('register')}
            >
              Register
            </button>
          </div>

          <div className="auth-card-body">

            <h2>
              {mode === 'login'
                ? 'Welcome back!'
                : 'Create account'}
            </h2>

            <p className="auth-sub">
              {mode === 'login'
                ? 'Sign in to continue shopping'
                : 'Join thousands of happy customers'}
            </p>


            {/* FORM */}
            <form onSubmit={handleSubmit} className="auth-form">

              {mode === 'register' && (
                <>
                  <div className="field">
                    <User size={16}/>
                    <input
                      type="text"
                      placeholder="Full Name"
                      value={form.customerName}
                      onChange={set('customerName')}
                      required
                    />
                  </div>

                  <div className="field">
                    <Phone size={16}/>
                    <input
                      type="tel"
                      placeholder="Phone Number"
                      value={form.customerPhone}
                      onChange={set('customerPhone')}
                    />
                  </div>

                  <div className="field">
                    <MapPin size={16}/>
                    <input
                      type="text"
                      placeholder="Delivery Address"
                      value={form.customerAddress}
                      onChange={set('customerAddress')}
                    />
                  </div>
                </>
              )}

              <div className="field">
                <Mail size={16}/>
                <input
                  type="email"
                  placeholder="Email address"
                  value={form.email}
                  onChange={set('email')}
                  required
                />
              </div>

              <div className="field">
                <Lock size={16}/>
                <input
                  type={showPw ? 'text' : 'password'}
                  placeholder="Password"
                  value={form.password}
                  onChange={set('password')}
                  required
                />

                <button
                  type="button"
                  className="eye-btn"
                  onClick={() => setShowPw(p => !p)}
                >
                  {showPw ? <EyeOff size={16}/> : <Eye size={16}/>}
                </button>
              </div>

              <button
                type="submit"
                className="btn btn-green w-full auth-submit"
                disabled={loading}
              >
                {loading
                  ? <span className="spinner"/>
                  : mode === 'login'
                    ? 'Sign In'
                    : 'Create Account'}
              </button>

            </form>

            <p className="auth-switch">
              {mode === 'login'
                ? "Don't have an account? "
                : 'Already have an account? '}

              <button
                onClick={() =>
                  setMode(mode === 'login' ? 'register' : 'login')
                }
              >
                {mode === 'login' ? 'Register' : 'Sign In'}
              </button>
            </p>

          </div>
        </div>
      </div>
    </div>
  );
}