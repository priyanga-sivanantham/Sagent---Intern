import React, { useEffect, useState } from "react";
import { getOrderProducts, deleteOrderProduct } from "../../services/orderProductService";

const OrderProductList = () => {
    const [orderProducts, setOrderProducts] = useState([]);

    useEffect(() => {
        fetchOrderProducts();
    }, []);

    const fetchOrderProducts = async () => {
        const res = await getOrderProducts();
        setOrderProducts(res.data);
    };

    const handleDelete = async (id) => {
        await deleteOrderProduct(id);
        fetchOrderProducts();
    };

    return (
        <div>
            <h2>Order Products</h2>
            <table border="1" cellPadding="5">
                <thead>
                <tr>
                    <th>Order ID</th>
                    <th>Product</th>
                    <th>Quantity</th>
                    <th>Price</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {orderProducts.map((op) => (
                    <tr key={`${op.orderId}-${op.productId}`}>
                        <td>{op.orderId}</td>
                        <td>{op.productName}</td>
                        <td>{op.quantity}</td>
                        <td>₹{op.price}</td>
                        <td>
                            <button>Edit</button>
                            <button onClick={() => handleDelete(op.id)}>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default OrderProductList;
