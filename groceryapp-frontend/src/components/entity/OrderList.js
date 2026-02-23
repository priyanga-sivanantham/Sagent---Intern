import React, { useEffect, useState } from "react";
import { getOrders, deleteOrder } from "../../services/orderService";

const OrderList = () => {
    const [orders, setOrders] = useState([]);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        const res = await getOrders();
        setOrders(res.data);
    };

    const handleDelete = async (id) => {
        await deleteOrder(id);
        fetchOrders();
    };

    return (
        <div>
            <h2>Orders</h2>
            <table border="1" cellPadding="5">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Customer</th>
                    <th>Status</th>
                    <th>Total Price</th>
                    <th>Payment</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {orders.map((o) => (
                    <tr key={o.id}>
                        <td>{o.id}</td>
                        <td>{o.customerName}</td>
                        <td>{o.status}</td>
                        <td>₹{o.totalPrice}</td>
                        <td>{o.paymentMethod}</td>
                        <td>
                            <button>Edit</button>
                            <button onClick={() => handleDelete(o.id)}>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default OrderList;
