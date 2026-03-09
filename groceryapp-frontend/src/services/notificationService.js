import api from "./api";

const BASE_URL = "/notifications";

export const getNotifications = () => api.get(BASE_URL);
export const getNotificationById = (id) => api.get(`${BASE_URL}/${id}`);
export const addNotification = (data) => api.post(BASE_URL, data);
export const updateNotification = (id, data) => api.put(`${BASE_URL}/${id}`, data);
export const deleteNotification = (id) => api.delete(`${BASE_URL}/${id}`);
