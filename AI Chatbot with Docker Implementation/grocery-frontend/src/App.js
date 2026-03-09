import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';

import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';

import ProtectedRoute from './components/ProtectedRoute';
import Navbar        from './components/Navbar';
import AuthPage      from './pages/AuthPage';
import Dashboard     from './pages/Dashboard';
import CartPage      from './pages/CartPage';
import CheckoutPage  from './pages/CheckoutPage';

function AppLayout() {
  return (
    <>
      <Navbar />
      <Outlet />
    </>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <BrowserRouter>
          <Toaster
            position="top-right"
            toastOptions={{
              style: { fontFamily: "'DM Sans', sans-serif", borderRadius: '12px', fontSize: '14px' },
              success: { iconTheme: { primary: '#0C831F', secondary: 'white' } },
            }}
          />
          <Routes>
            <Route path="/login" element={<AuthPage />} />
            <Route path="/" element={<Navigate to="/dashboard" replace />} />

            <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/cart"      element={<CartPage />} />
              <Route path="/checkout"  element={<CheckoutPage />} />
            </Route>

            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
}
