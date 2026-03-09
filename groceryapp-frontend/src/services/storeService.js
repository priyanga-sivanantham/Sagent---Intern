import api from "./api";

const BASE_URL = "/stores";

export const getStores = () => api.get(BASE_URL);
export const getStoreById = (id) => api.get(`${BASE_URL}/${id}`);
export const addStore = (data) => api.post(BASE_URL, data);
export const updateStore = (id, data) => api.put(`${BASE_URL}/${id}`, data);
export const deleteStore = (id) => api.delete(`${BASE_URL}/${id}`);
