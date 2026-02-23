import api from "./api";

const BASE_URL = "/orderproducts";

export const getOrderProducts = () => api.get(BASE_URL);
export const addOrderProduct = (data) => api.post(BASE_URL, data);
export const updateOrderProduct = (id, data) => api.put(`${BASE_URL}/${id}`, data);
export const deleteOrderProduct = (id) => api.delete(`${BASE_URL}/${id}`);
