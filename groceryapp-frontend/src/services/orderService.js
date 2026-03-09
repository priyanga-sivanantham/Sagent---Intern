import api from "./api";

const BASE_URL = "/orders";

export const getOrders = () => api.get(BASE_URL);
export const getOrderById = (id) => api.get(`${BASE_URL}/${id}`);
export const addOrder = (data) => api.post(BASE_URL, data);
export const updateOrder = (id, data) => api.put(`${BASE_URL}/${id}`, data);
export const deleteOrder = (id) => api.delete(`${BASE_URL}/${id}`);
