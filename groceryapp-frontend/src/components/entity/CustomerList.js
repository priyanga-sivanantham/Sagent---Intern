import React, { useEffect, useState } from "react";
import { getCustomers, deleteCustomer } from "../../services/customerService";

const CustomerList = () => {
    const [customers, setCustomers] = useState([]);

    useEffect(() => {
        fetchCustomers();
    }, []);

    const fetchCustomers = async () => {
        const res = await getCustomers();
        setCustomers(res.data);
    };

    const handleDelete = async (id) => {
        await deleteCustomer(id);
        fetchCustomers();
    };

    return (
        <div>
            <h2>Customers</h2>
            <table border="1" cellPadding="5">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Address</th>
                    <th>Contact</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {customers.map((c) => (
                    <tr key={c.id}>
                        <td>{c.id}</td>
                        <td>{c.name}</td>
                        <td>{c.email}</td>
                        <td>{c.address}</td>
                        <td>{c.contact}</td>
                        <td>
                            <button>Edit</button>
                            <button onClick={() => handleDelete(c.id)}>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default CustomerList;
