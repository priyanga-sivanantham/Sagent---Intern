import api from "./api";

const BASE_URL = "/payments";

export const getPayments = () => api.get(BASE_URL);
export const getPaymentById = (id) => api.get(`${BASE_URL}/${id}`);
export const addPayment = (data) => api.post(BASE_URL, data);
export const updatePayment = (id, data) => api.put(`${BASE_URL}/${id}`, data);
export const deletePayment = (id) => api.delete(`${BASE_URL}/${id}`);
