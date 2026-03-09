import api from "./api";

const BASE_URL = "/deliverypersons";

export const getDeliveryPersons = () => api.get(BASE_URL);
export const getDeliveryPersonById = (id) => api.get(`${BASE_URL}/${id}`);
export const addDeliveryPerson = (data) => api.post(BASE_URL, data);
export const updateDeliveryPerson = (id, data) => api.put(`${BASE_URL}/${id}`, data);
export const deleteDeliveryPerson = (id) => api.delete(`${BASE_URL}/${id}`);
