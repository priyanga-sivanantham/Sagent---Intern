import React, { useState } from "react";
import { loginCustomer } from "../services/customerService";
import { useNavigate } from "react-router-dom";

function LoginPage() {

    const navigate = useNavigate();

    const [email, setEmail] = useState("");

    const [password, setPassword] = useState("");

    const handleSubmit = async (e) => {

        e.preventDefault();

        try {

            const res = await loginCustomer(email, password);

            console.log(res.data);

            localStorage.setItem("customer", JSON.stringify(res.data));

            alert("Login successful");

            navigate("/products");

        }

        catch {

            alert("Invalid login");

        }

    };

    return (

        <div>

            <h2>Login</h2>

            <form onSubmit={handleSubmit}>

                <input
                    placeholder="Email"
                    onChange={(e)=>setEmail(e.target.value)}
                />

                <br/><br/>

                <input
                    placeholder="Password"
                    type="password"
                    onChange={(e)=>setPassword(e.target.value)}
                />

                <br/><br/>

                <button type="submit">
                    Login
                </button>

            </form>

            <br/>

            {/* REGISTER BUTTON */}

            <button onClick={() => navigate("/register")}>

                Register

            </button>

        </div>

    );

}

export default LoginPage;
