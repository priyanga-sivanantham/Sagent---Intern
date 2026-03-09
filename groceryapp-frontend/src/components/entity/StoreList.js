import React, { useEffect, useState } from "react";
import { getStores, deleteStore } from "../../services/storeService";

const StoreList = () => {
    const [stores, setStores] = useState([]);

    useEffect(() => {
        fetchStores();
    }, []);

    const fetchStores = async () => {
        const res = await getStores();
        setStores(res.data);
    };

    const handleDelete = async (id) => {
        await deleteStore(id);
        fetchStores();
    };

    return (
        <div>
            <h2>Stores</h2>
            <table border="1" cellPadding="5">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Address</th>
                    <th>Contact</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {stores.map((s) => (
                    <tr key={s.id}>
                        <td>{s.id}</td>
                        <td>{s.name}</td>
                        <td>{s.address}</td>
                        <td>{s.contact}</td>
                        <td>
                            <button>Edit</button>
                            <button onClick={() => handleDelete(s.id)}>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default StoreList;
