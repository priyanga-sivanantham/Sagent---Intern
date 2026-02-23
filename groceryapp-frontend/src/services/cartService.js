import api from "./api";

const BASE_URL = "/carts";

export const getCarts = () => api.get(BASE_URL);
export const getCartById = (id) => api.get(`${BASE_URL}/${id}`);
export const addCart = (data) => api.post(BASE_URL, data);
export const updateCart = (id, data) => api.put(`${BASE_URL}/${id}`, data);
export const deleteCart = (id) => api.delete(`${BASE_URL}/${id}`);
