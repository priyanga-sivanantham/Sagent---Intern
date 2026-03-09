import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, CreditCard, Truck, CheckCircle, ChevronDown } from 'lucide-react';
import toast from 'react-hot-toast';
import { createOrder, getAllStores } from '../api/api';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './Checkout.css';

const PAYMENT_METHODS = [
  { id: 'upi', label: 'UPI / QR Code', icon: '📱' },
  { id: 'card', label: 'Credit / Debit Card', icon: '💳' },
  { id: 'netbanking', label: 'Net Banking', icon: '🏦' },
  { id: 'cod', label: 'Cash on Delivery', icon: '💵' },
];

export default function CheckoutPage() {
  const { user }                          = useAuth();
  const { cartItems, totalAmount, clearCart } = useCart();
  const navigate                          = useNavigate();

  const [stores, setStores]       = useState([]);
  const [storeId, setStoreId]     = useState('');
  const [payMethod, setPayMethod] = useState('cod');
  const [address, setAddress]     = useState(user?.customerAddress || '');
  const [placing, setPlacing]     = useState(false);
  const [success, setSuccess]     = useState(false);
  const [orderId, setOrderId]     = useState(null);

  const delivery    = totalAmount > 499 ? 0 : 40;
  const discount    = totalAmount > 999 ? Math.round(totalAmount * 0.05) : 0;
  const grandTotal  = totalAmount + delivery - discount;

  useEffect(() => {
    getAllStores()
      .then(r => { setStores(r.data || []); if (r.data?.length) setStoreId(r.data[0].storeId); })
      .catch(() => {});
  }, []);

  const handlePlaceOrder = async () => {
    if (!address.trim()) { toast.error('Please enter a delivery address'); return; }
    if (!storeId) { toast.error('Please select a store'); return; }

    setPlacing(true);
    try {
      const orderPayload = {
        orderStatus: 'PENDING',
        totalAmount: grandTotal,
        paymentMethod: payMethod,
        deliveryAddress: address,
      };
      // deliveryId = 1 as a default (required by backend)
      const res = await createOrder(user.customerId, storeId, 1, orderPayload);
      setOrderId(res.data?.orderId || res.data?.id || '—');
      clearCart();
      setSuccess(true);
    } catch (err) {
      toast.error('Failed to place order. Please try again.');
    }
    setPlacing(false);
  };

  if (success) {
    return (
      <div className="success-page fade-up">
        <div className="success-icon"><CheckCircle size={56} /></div>
        <h2>Order Placed! 🎉</h2>
        <p>Your order #{orderId} has been confirmed and will arrive in <strong>10 minutes</strong>.</p>
        <div className="order-track-info">
          <Truck size={20} />
          <span>Your delivery is on its way!</span>
        </div>
        <button className="btn btn-green" onClick={() => navigate('/dashboard')}>
          Continue Shopping
        </button>
      </div>
    );
  }

  return (
    <div className="checkout-page">
      <h2 className="checkout-title">Checkout</h2>

      <div className="checkout-grid">
        {/* Left column */}
        <div className="checkout-left">

          {/* Delivery address */}
          <section className="checkout-section">
            <div className="section-title">
              <MapPin size={18} />
              <h3>Delivery Address</h3>
            </div>
            <div className="addr-box">
              <div className="addr-tag">HOME</div>
              <textarea
                value={address}
                onChange={e => setAddress(e.target.value)}
                rows={3}
                placeholder="Enter your full delivery address..."
              />
            </div>
          </section>

          {/* Store selection */}
          {stores.length > 0 && (
            <section className="checkout-section">
              <div className="section-title">
                <span>🏪</span>
                <h3>Fulfilling Store</h3>
              </div>
              <div className="select-wrap">
                <select value={storeId} onChange={e => setStoreId(e.target.value)}>
                  {stores.map(s => (
                    <option key={s.storeId} value={s.storeId}>{s.storeName || `Store #${s.storeId}`}</option>
                  ))}
                </select>
                <ChevronDown size={16} />
              </div>
            </section>
          )}

          {/* Payment method */}
          <section className="checkout-section">
            <div className="section-title">
              <CreditCard size={18} />
              <h3>Payment Method</h3>
            </div>
            <div className="payment-options">
              {PAYMENT_METHODS.map(m => (
                <label key={m.id} className={`pay-option ${payMethod === m.id ? 'active' : ''}`}>
                  <input
                    type="radio"
                    name="payment"
                    value={m.id}
                    checked={payMethod === m.id}
                    onChange={() => setPayMethod(m.id)}
                  />
                  <span className="pay-icon">{m.icon}</span>
                  <span>{m.label}</span>
                </label>
              ))}
            </div>
          </section>

          {/* Order items */}
          <section className="checkout-section">
            <div className="section-title">
              <span>🛒</span>
              <h3>Order Items ({cartItems.length})</h3>
            </div>
            <div className="order-items">
              {cartItems.map(({ product, quantity }) => (
                <div key={product.productId} className="order-item">
                  <span className="oi-name">{product.productName}</span>
                  <span className="oi-qty">× {quantity}</span>
                  <span className="oi-price">₹{(product.productPrice * quantity).toFixed(2)}</span>
                </div>
              ))}
            </div>
          </section>
        </div>

        {/* Right summary */}
        <div className="checkout-summary">
          <div className="summary-card">
            <h3>Order Summary</h3>
            <div className="summary-rows">
              <div className="summary-row"><span>Items total</span><span>₹{totalAmount.toFixed(2)}</span></div>
              <div className="summary-row"><span>Delivery</span>{delivery === 0 ? <span className="green">FREE</span> : <span>₹{delivery}</span>}</div>
              {discount > 0 && (
                <div className="summary-row green"><span>Discount</span><span>-₹{discount}</span></div>
              )}
              <div className="summary-divider" />
              <div className="summary-row grand"><span>Grand Total</span><span>₹{grandTotal.toFixed(2)}</span></div>
            </div>

            <button
              className="btn btn-green w-full place-order-btn"
              onClick={handlePlaceOrder}
              disabled={placing}
            >
              {placing ? <><span className="spinner" /> Placing Order…</> : `Place Order • ₹${grandTotal.toFixed(2)}`}
            </button>
            <p className="tnc">By placing your order, you agree to our Terms & Conditions</p>
          </div>
        </div>
      </div>
    </div>
  );
}
