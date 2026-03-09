import React, { useEffect, useState } from "react";
import { getCartProducts, deleteCartProduct } from "../../services/cartProductService";

const CartProductList = () => {
    const [cartProducts, setCartProducts] = useState([]);

    useEffect(() => {
        fetchCartProducts();
    }, []);

    const fetchCartProducts = async () => {
        const res = await getCartProducts();
        setCartProducts(res.data);
    };

    const handleDelete = async (id) => {
        await deleteCartProduct(id);
        fetchCartProducts();
    };

    return (
        <div>
            <h2>Cart Products</h2>
            <table border="1" cellPadding="5">
                <thead>
                <tr>
                    <th>Cart ID</th>
                    <th>Product</th>
                    <th>Quantity</th>
                    <th>Price</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {cartProducts.map((cp) => (
                    <tr key={`${cp.cartId}-${cp.productId}`}>
                        <td>{cp.cartId}</td>
                        <td>{cp.productName}</td>
                        <td>{cp.quantity}</td>
                        <td>₹{cp.price}</td>
                        <td>
                            <button>Edit</button>
                            <button onClick={() => handleDelete(cp.id)}>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default CartProductList;
