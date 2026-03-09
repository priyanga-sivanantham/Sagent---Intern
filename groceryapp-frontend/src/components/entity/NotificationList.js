import React, { useEffect, useState } from "react";
import { getNotifications, deleteNotification } from "../../services/notificationService";

const NotificationList = () => {
    const [notifications, setNotifications] = useState([]);

    useEffect(() => {
        fetchNotifications();
    }, []);

    const fetchNotifications = async () => {
        const res = await getNotifications();
        setNotifications(res.data);
    };

    const handleDelete = async (id) => {
        await deleteNotification(id);
        fetchNotifications();
    };

    return (
        <div>
            <h2>Notifications</h2>
            <table border="1" cellPadding="5">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Message</th>
                    <th>Type</th>
                    <th>Customer</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {notifications.map((n) => (
                    <tr key={n.id}>
                        <td>{n.id}</td>
                        <td>{n.message}</td>
                        <td>{n.type}</td>
                        <td>{n.customerName}</td>
                        <td>
                            <button>Edit</button>
                            <button onClick={() => handleDelete(n.id)}>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default NotificationList;
