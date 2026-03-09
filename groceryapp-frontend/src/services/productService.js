import api from "./api";

export const getAllProducts = () => api.get("/products");

export const addProduct = (product, storeId) =>
    api.post(`/products/store/${storeId}`, product);

export const updateProduct = (id, product) =>
    api.put(`/products/${id}`, product);

export const deleteProduct = (id) =>
    api.delete(`/products/${id}`);