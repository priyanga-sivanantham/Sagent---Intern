import React, { createContext, useContext, useState, useEffect } from 'react';
import { useAuth } from './AuthContext';
import { createCart, getCartByCustomer, addProductToCart } from '../api/api';

const CartContext = createContext();

export function CartProvider({ children }) {
  const { user } = useAuth();
  const [cart, setCart]           = useState(null);  // backend cart object
  const [cartItems, setCartItems] = useState([]);     // [{product, quantity}]
  const [loading, setLoading]     = useState(false);

  // Load or create cart when user logs in
  useEffect(() => {
    if (!user) { setCart(null); setCartItems([]); return; }
    initCart();
  }, [user]);

  const initCart = async () => {
    try {
      const res = await getCartByCustomer(user.customerId);
      if (res.data) { setCart(res.data); return; }
    } catch (_) {}
    try {
      const res = await createCart(user.customerId);
      setCart(res.data);
    } catch (e) { console.error('Cart init error', e); }
  };

  const addToCart = async (product, quantity = 1) => {
    if (!cart) return;
    setLoading(true);
    try {
      await addProductToCart(cart.cartId, product.productId, quantity);
      setCartItems(prev => {
        const existing = prev.find(i => i.product.productId === product.productId);
        if (existing) {
          return prev.map(i =>
            i.product.productId === product.productId
              ? { ...i, quantity: i.quantity + quantity }
              : i
          );
        }
        return [...prev, { product, quantity }];
      });
    } catch (e) { console.error('Add to cart error', e); }
    setLoading(false);
  };

  const removeFromCart = (productId) => {
    setCartItems(prev => prev.filter(i => i.product.productId !== productId));
  };

  const updateQuantity = (productId, qty) => {
    if (qty <= 0) { removeFromCart(productId); return; }
    setCartItems(prev =>
      prev.map(i => i.product.productId === productId ? { ...i, quantity: qty } : i)
    );
  };

  const clearCart = () => setCartItems([]);

  const totalItems  = cartItems.reduce((s, i) => s + i.quantity, 0);
  const totalAmount = cartItems.reduce((s, i) => s + i.product.productPrice * i.quantity, 0);

  return (
    <CartContext.Provider value={{
      cart, cartItems, loading,
      addToCart, removeFromCart, updateQuantity, clearCart,
      totalItems, totalAmount
    }}>
      {children}
    </CartContext.Provider>
  );
}

export const useCart = () => useContext(CartContext);
