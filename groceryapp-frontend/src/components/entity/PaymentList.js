import React, { useEffect, useState } from "react";
import { getPayments, deletePayment } from "../../services/paymentService";

const PaymentList = () => {
    const [payments, setPayments] = useState([]);

    useEffect(() => {
        fetchPayments();
    }, []);

    const fetchPayments = async () => {
        const res = await getPayments();
        setPayments(res.data);
    };

    const handleDelete = async (id) => {
        await deletePayment(id);
        fetchPayments();
    };

    return (
        <div>
            <h2>Payments</h2>
            <table border="1" cellPadding="5">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Order ID</th>
                    <th>Amount</th>
                    <th>Method</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {payments.map((p) => (
                    <tr key={p.id}>
                        <td>{p.id}</td>
                        <td>{p.orderId}</td>
                        <td>₹{p.amount}</td>
                        <td>{p.method}</td>
                        <td>{p.status}</td>
                        <td>
                            <button>Edit</button>
                            <button onClick={() => handleDelete(p.id)}>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default PaymentList;
