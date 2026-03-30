import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';

export default function VendorEvents() {
  const { user } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [vRes, evRes, eRes] = await Promise.all([api.get('/vendors'), api.get('/eventvendors'), api.get('/events')]);
        const allVendors = extractItems(vRes.data);
        const mine = allVendors.find((v) => String(v.userId) === String(user?.userId || user?.id));
        if (!mine) { setRows([]); return; }
        const vid = mine.id || mine.vendorId;
        const assignments = extractItems(evRes.data).filter((a) => String(a.vendorId) === String(vid));
        const events = extractItems(eRes.data);
        const enriched = assignments.map((a) => {
          const ev = events.find((e) => (e.id || e.eventId) === a.eventId);
          return { ...a, eventName: ev?.eventName || a.eventId, eventDate: ev?.eventDate, venue: ev?.venue };
        });
        setRows(enriched);
      } catch {}
      finally { setLoading(false); }
    };
    load();
  }, [user]);

  const columns = [
    { key: 'eventName', label: 'Event' },
    { key: 'eventDate', label: 'Date', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
    { key: 'venue', label: 'Venue' },
    { key: 'serviceDetails', label: 'Service Details' },
  ];

  return (
    <SectionCard title="My Assigned Events">
      <DataTable columns={columns} data={rows} loading={loading} emptyMessage="No events assigned to you yet." />
    </SectionCard>
  );
}
