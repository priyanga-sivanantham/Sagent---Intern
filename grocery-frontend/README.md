# 🛒 Quickcart — Grocery App Frontend

A Blinkit-inspired React frontend connected to your Spring Boot backend.

---

## 🚀 Quick Start

### 1. Install dependencies
```bash
npm install
```

### 2. Start the app
```bash
npm start
```
Runs on **http://localhost:3000**

Your Spring Boot backend must be running on **http://localhost:8080**

---

## 🔗 Backend API Mapping

| Frontend Action        | Backend Endpoint                          |
|------------------------|-------------------------------------------|
| Register               | POST `/customers/register`                |
| Login                  | POST `/customers/login`                   |
| Get all products       | GET `/api/products`                       |
| Get products by cat    | GET `/api/products/category/{category}`   |
| Create cart            | POST `/api/carts/create/{customerId}`     |
| Get customer cart      | GET `/api/carts/customer/{customerId}`    |
| Add to cart            | POST `/api/cart-product/add?...`          |
| Place order            | POST `/api/orders?...`                    |
| Get stores             | GET `/api/stores`                         |

---

## 🔐 Password Encryption

Passwords are hashed client-side using **bcryptjs** before being sent to the backend.

> ⚠️ **Important Backend Note:** Since the frontend sends a bcrypt hash on registration,
> your backend's `findByEmailAndPassword` login query does a plain string match.
> For login to work correctly, you should update `CustomerService` / repository to use 
> `BCryptPasswordEncoder.matches(rawPassword, storedHash)` instead of exact string match.
>
> **Quick fix in Spring Boot:**
> ```java
> // In CustomerController login:
> Customer customer = customerRepository.findByEmail(loginRequest.getEmail());
> if (customer != null && passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
>     return customer;
> }
> ```
> Add `BCryptPasswordEncoder` bean to your Spring config.

---

## 📁 Folder Structure

```
src/
├── api/          → All Axios API calls
├── context/      → Auth & Cart state (React Context)
├── components/   → Navbar, ProductCard, ProtectedRoute
├── pages/        → AuthPage, Dashboard, CartPage, CheckoutPage
└── index.css     → Global styles & design tokens
```

## 🎨 Features

- ✅ Login / Register with bcrypt password hashing
- ✅ Product grid with category filter + search + sort
- ✅ Add to cart / quantity controls
- ✅ Cart page with bill summary & free delivery logic
- ✅ Checkout with address, store selection, payment method
- ✅ Order placement via backend API
- ✅ Auto-rotating hero banner
- ✅ Protected routes (redirect to login if not authenticated)
- ✅ Toast notifications

## 🛠️ CORS

Backend already has `@CrossOrigin(origins = "*")` on controllers.
Make sure your `CorsConfig.java` allows `http://localhost:3000`.
