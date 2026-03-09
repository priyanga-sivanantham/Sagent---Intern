// src/pages/RegisterPage.js
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { registerCustomer } from "../services/customerService";

function RegisterPage() {

    const navigate = useNavigate();

    const [customer, setCustomer] = useState({

        customerName: "",
        customerAddress: "",
        customerPhone: "",
        email: "",
        password: ""

    });

    const handleChange = (e) => {

        setCustomer({

            ...customer,
            [e.target.name]: e.target.value

        });

    };

    const handleSubmit = async (e) => {

        e.preventDefault();

        try {

            await registerCustomer(customer);

            alert("Registration successful");

            navigate("/");

        }

        catch (error) {

            alert("Registration failed");

        }

    };

    return (

        <div>

            <h2>Register</h2>

            <form onSubmit={handleSubmit}>

                <input
                    name="customerName"
                    placeholder="Name"
                    onChange={handleChange}
                />

                <input
                    name="customerAddress"
                    placeholder="Address"
                    onChange={handleChange}
                />

                <input
                    name="customerPhone"
                    placeholder="Phone"
                    onChange={handleChange}
                />

                <input
                    name="email"
                    placeholder="Email"
                    onChange={handleChange}
                />

                <input
                    name="password"
                    placeholder="Password"
                    onChange={handleChange}
                />

                <button type="submit">

                    Register

                </button>

            </form>

        </div>

    );

}

export default RegisterPage;

