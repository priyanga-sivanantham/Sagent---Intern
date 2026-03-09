import React, { useEffect, useState } from "react";
import { getCarts, deleteCart } from "../../services/cartService";

const CartList = () => {
    const [carts, setCarts] = useState([]);

    useEffect(() => {
        fetchCarts();
    }, []);

    const fetchCarts = async () => {
        const res = await getCarts();
        setCarts(res.data);
    };

    const handleDelete = async (id) => {
        await deleteCart(id);
        fetchCarts();
    };

    return (
        <div>
            <h2>Carts</h2>
            <table border="1" cellPadding="5">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Customer</th>
                    <th>Total Items</th>
                    <th>Total Price</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {carts.map((c) => (
                    <tr key={c.id}>
                        <td>{c.id}</td>
                        <td>{c.customerName}</td>
                        <td>{c.totalItems}</td>
                        <td>₹{c.totalPrice}</td>
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

export default CartList;
