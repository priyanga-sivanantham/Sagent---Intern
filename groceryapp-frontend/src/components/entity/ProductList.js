import React, { useEffect, useState } from "react";
import { getAllProducts } from "../../services/productService";

function ProductList() {

    const [products, setProducts] = useState([]);

    useEffect(() => {

        getAllProducts()
            .then(response => {

                setProducts(response.data);

            })
            .catch(error => {

                console.error(error);

            });

    }, []);

    return (

        <div>

            <h2>Product Management</h2>

            <table>

                <thead>

                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Available</th>
                </tr>

                </thead>

                <tbody>

                {products.map(product => (

                    <tr key={product.productId}>

                        <td>{product.productId}</td>
                        <td>{product.productName}</td>
                        <td>{product.productCategory}</td>
                        <td>{product.productPrice}</td>
                        <td>{product.stockQuantity}</td>

                    </tr>

                ))}

                </tbody>

            </table>

        </div>

    );

}

export default ProductList;