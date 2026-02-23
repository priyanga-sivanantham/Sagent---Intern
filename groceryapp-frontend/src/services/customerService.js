import api from "./api";

export const registerCustomer = (customer) =>
    api.post("/customers/register", customer);

export const loginCustomer = (email, password) =>
    api.post("/customers/login", {
        email,
        password
    });