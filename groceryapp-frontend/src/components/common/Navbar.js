import React from "react";
import { Link } from "react-router-dom";

const Navbar = () => {
    return (
        <nav style={{ padding: "10px", backgroundColor: "#eee" }}>
            <Link to="/products" style={{ margin: "0 10px" }}>Products</Link>
            <Link to="/customers" style={{ margin: "0 10px" }}>Customers</Link>
            <Link to="/carts" style={{ margin: "0 10px" }}>Carts</Link>
            <Link to="/orders" style={{ margin: "0 10px" }}>Orders</Link>
            <Link to="/order-products" style={{ margin: "0 10px" }}>Order Products</Link>
            <Link to="/payments" style={{ margin: "0 10px" }}>Payments</Link>
            <Link to="/stores" style={{ margin: "0 10px" }}>Stores</Link>
            <Link to="/delivery-persons" style={{ margin: "0 10px" }}>Delivery Persons</Link>
            <Link to="/notifications" style={{ margin: "0 10px" }}>Notifications</Link>
            <Link to="/cart-products" style={{ margin: "0 10px" }}>Cart Products</Link>
        </nav>
    );
};

export default Navbar;
