import React, { useState } from 'react';
import { Plus, Minus, ShoppingBag } from 'lucide-react';
import { useCart } from '../context/CartContext';
import './ProductCard.css';

// Category to emoji map for visual flair
const categoryEmoji = {
  fruits:      '🍎',
  vegetables:  '🥦',
  dairy:       '🥛',
  bakery:      '🍞',
  meat:        '🥩',
  beverages:   '🧃',
  snacks:      '🍿',
  frozen:      '🧊',
  personal:    '🧴',
  household:   '🧹',
  default:     '🛒',
};

const getEmoji = (cat) => {
  if (!cat) return categoryEmoji.default;
  const key = Object.keys(categoryEmoji).find(k => cat.toLowerCase().includes(k));
  return key ? categoryEmoji[key] : categoryEmoji.default;
};

// Pastel backgrounds per category
const cardColors = ['#FFF9EC','#F0FFF4','#EDF2FF','#FFF5F5','#F0F9FF','#FDF4FF'];
const getColor = (id) => cardColors[id % cardColors.length];

export default function ProductCard({ product }) {
  const { cartItems, addToCart, updateQuantity } = useCart();
  const [adding, setAdding] = useState(false);

  const cartItem = cartItems.find(i => i.product.productId === product.productId);
  const qty = cartItem?.quantity || 0;

  const handleAdd = async () => {
    setAdding(true);
    await addToCart(product, 1);
    setAdding(false);
  };

  const handleInc = () => updateQuantity(product.productId, qty + 1);
  const handleDec = () => updateQuantity(product.productId, qty - 1);

  return (
    <div className="product-card fade-up" style={{ '--card-bg': getColor(product.productId) }}>
      <div className="product-img-box">
        <span className="product-emoji">{getEmoji(product.productCategory)}</span>
        {product.stockQuantity <= 5 && product.stockQuantity > 0 && (
          <span className="stock-warn">Only {product.stockQuantity} left</span>
        )}
        {product.stockQuantity === 0 && (
          <span className="out-of-stock">Out of Stock</span>
        )}
      </div>

      <div className="product-info">
        <p className="product-category">{product.productCategory || 'General'}</p>
        <h3 className="product-name">{product.productName}</h3>
        <div className="product-footer">
          <span className="product-price">₹{product.productPrice?.toFixed(2)}</span>

          {qty === 0 ? (
            <button
              className="add-btn"
              onClick={handleAdd}
              disabled={adding || product.stockQuantity === 0}
            >
              {adding ? <span className="mini-spin" /> : <Plus size={16} />}
              Add
            </button>
          ) : (
            <div className="qty-control">
              <button onClick={handleDec}><Minus size={14} /></button>
              <span>{qty}</span>
              <button onClick={handleInc}><Plus size={14} /></button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
