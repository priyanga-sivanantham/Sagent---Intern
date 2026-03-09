import React, { useEffect, useState } from "react";
import { getDeliveryPersons, deleteDeliveryPerson } from "../../services/deliveryPersonService";

const DeliveryPersonList = () => {
    const [deliveryPersons, setDeliveryPersons] = useState([]);

    useEffect(() => {
        fetchDeliveryPersons();
    }, []);

    const fetchDeliveryPersons = async () => {
        const res = await getDeliveryPersons();
        setDeliveryPersons(res.data);
    };

    const handleDelete = async (id) => {
        await deleteDeliveryPerson(id);
        fetchDeliveryPersons();
    };

    return (
        <div>
            <h2>Delivery Persons</h2>
            <table border="1" cellPadding="5">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Contact</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {deliveryPersons.map((d) => (
                    <tr key={d.id}>
                        <td>{d.id}</td>
                        <td>{d.name}</td>
                        <td>{d.contact}</td>
                        <td>{d.status}</td>
                        <td>
                            <button>Edit</button>
                            <button onClick={() => handleDelete(d.id)}>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default DeliveryPersonList;
