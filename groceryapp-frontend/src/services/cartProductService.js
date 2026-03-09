import api from "./api";

const BASE_URL = "/cartproducts";

export const getCartProducts = () => api.get(BASE_URL);
export const addCartProduct = (data) => api.post(BASE_URL, data);
export const updateCartProduct = (id, data) => api.put(`${BASE_URL}/${id}`, data);
export const deleteCartProduct = (id) => api.delete(`${BASE_URL}/${id}`);
