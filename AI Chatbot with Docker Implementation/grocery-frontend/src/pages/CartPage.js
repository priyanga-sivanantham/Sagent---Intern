import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Minus, Plus, Trash2, ShoppingBag, ArrowLeft } from 'lucide-react';
import { useCart } from '../context/CartContext';
import './Cart.css';

export default function CartPage() {
  const { cartItems, removeFromCart, updateQuantity, totalItems, totalAmount } = useCart();
  const navigate = useNavigate();

  const delivery  = totalAmount > 499 ? 0 : 40;
  const discount  = totalAmount > 999 ? Math.round(totalAmount * 0.05) : 0;
  const grandTotal = totalAmount + delivery - discount;

  if (cartItems.length === 0) {
    return (
      <div className="cart-empty">
        <div className="empty-icon"><ShoppingBag size={48} /></div>
        <h2>Your cart is empty</h2>
        <p>Add items to get started with your order</p>
        <Link to="/dashboard" className="btn btn-green">Browse Products</Link>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <div className="cart-main">
        <div className="cart-header">
          <button className="back-btn" onClick={() => navigate(-1)}>
            <ArrowLeft size={18} /> Continue Shopping
          </button>
          <h2>My Cart <span>({totalItems} items)</span></h2>
        </div>

        {/* Delivery promise */}
        <div className="delivery-promise">
          ⚡ Delivery in <strong>10 minutes</strong> — Order before stock runs out!
        </div>

        {/* Items */}
        <div className="cart-items">
          {cartItems.map(({ product, quantity }) => (
            <div key={product.productId} className="cart-item">
              <div className="cart-item-img">
                <span>{getCategoryEmoji(product.productCategory)}</span>
              </div>
              <div className="cart-item-info">
                <p className="item-cat">{product.productCategory}</p>
                <h4 className="item-name">{product.productName}</h4>
                <p className="item-price">₹{product.productPrice?.toFixed(2)} each</p>
              </div>
              <div className="cart-item-right">
                <div className="qty-ctrl">
                  <button onClick={() => updateQuantity(product.productId, quantity - 1)}>
                    <Minus size={14} />
                  </button>
                  <span>{quantity}</span>
                  <button onClick={() => updateQuantity(product.productId, quantity + 1)}>
                    <Plus size={14} />
                  </button>
                </div>
                <p className="item-total">₹{(product.productPrice * quantity).toFixed(2)}</p>
                <button className="remove-btn" onClick={() => removeFromCart(product.productId)}>
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Bill summary */}
      <div className="cart-sidebar">
        <div className="bill-card">
          <h3>Bill Summary</h3>

          <div className="bill-rows">
            <div className="bill-row">
              <span>Subtotal ({totalItems} items)</span>
              <span>₹{totalAmount.toFixed(2)}</span>
            </div>
            <div className="bill-row">
              <span>Delivery charges</span>
              {delivery === 0
                ? <span className="free">FREE</span>
                : <span>₹{delivery}</span>
              }
            </div>
            {discount > 0 && (
              <div className="bill-row discount">
                <span>Discount (5% on ₹999+)</span>
                <span>- ₹{discount}</span>
              </div>
            )}
            <div className="bill-divider" />
            <div className="bill-row total">
              <span>Total</span>
              <span>₹{grandTotal.toFixed(2)}</span>
            </div>
          </div>

          {totalAmount <= 499 && (
            <div className="free-delivery-tip">
              🚚 Add items worth ₹{(499 - totalAmount).toFixed(2)} more for free delivery
            </div>
          )}
          {totalAmount > 499 && (
            <div className="free-delivery-tip success">
              🎉 You've unlocked free delivery!
            </div>
          )}

          <button className="btn btn-green w-full checkout-btn" onClick={() => navigate('/checkout')}>
            Proceed to Checkout →
          </button>
        </div>

        <div className="safe-badge">🔒 100% Safe & Secure Checkout</div>
      </div>
    </div>
  );
}

function getCategoryEmoji(cat) {
  const map = { fruits:'🍎', vegetables:'🥦', dairy:'🥛', bakery:'🍞', meat:'🥩', beverages:'🧃', snacks:'🍿', frozen:'🧊' };
  if (!cat) return '🛒';
  const key = Object.keys(map).find(k => cat.toLowerCase().includes(k));
  return key ? map[key] : '🛒';
}
