import axios from 'axios';

const BASE_URL = 'http://localhost:8080';
const api = axios.create({ baseURL: BASE_URL });

// ── Auth ─────────────────────
export const registerCustomer = (data) =>
  api.post('/customers/register', data);

export const loginCustomer = (data) =>
  api.post('/customers/login', data);

// ── Products ─────────────────
export const getAllProducts = () =>
  api.get('/products');

export const getProductsByCategory = (category) =>
  api.get(`/products/category/${category}`);

export const getProductsByStore = (storeId) =>
  api.get(`/products/store/${storeId}`);

// ── Cart ─────────────────────
export const createCart = (customerId) =>
  api.post(`/carts/create/${customerId}`);

export const getCartByCustomer = (customerId) =>
  api.get(`/carts/customer/${customerId}`);

// ── Cart Product ─────────────
export const addProductToCart = (cartId, productId, quantity) =>
  api.post(
    `/cart-product/add?cartId=${cartId}&productId=${productId}&quantity=${quantity}`
  );

// ── Orders ───────────────────
export const createOrder = (customerId, storeId, deliveryId, order) =>
  api.post(
    `/orders?customerId=${customerId}&storeId=${storeId}&deliveryId=${deliveryId}`,
    order
  );

export const getAllOrders = () =>
  api.get('/orders');

export const getOrderById = (id) =>
  api.get(`/orders/${id}`);

// ── Stores ───────────────────
export const getAllStores = () =>
  api.get('/stores');

export default api;