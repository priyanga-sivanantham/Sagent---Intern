import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';

export default function TeamMemberEvents() {
  const { user } = useAuth();
  const [assignments, setAssignments] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [aRes, eRes] = await Promise.all([api.get('/eventteammembers'), api.get('/events')]);
        const all = extractItems(aRes.data);
        const mine = all.filter((a) => String(a.userId) === String(user?.userId || user?.id));
        setAssignments(mine);
        setEvents(extractItems(eRes.data));
      } catch {}
      finally { setLoading(false); }
    };
    load();
  }, [user]);

  const getEventName = (id) => events.find((e) => (e.id || e.eventId) === id)?.eventName || id;
  const getEventDate = (id) => {
    const ev = events.find((e) => (e.id || e.eventId) === id);
    return ev?.eventDate ? new Date(ev.eventDate).toLocaleDateString() : '—';
  };
  const getEventVenue = (id) => events.find((e) => (e.id || e.eventId) === id)?.venue || '—';

  const columns = [
    { key: 'eventId', label: 'Event', render: (v) => getEventName(v) },
    { key: 'eventId', label: 'Date', render: (v) => getEventDate(v) },
    { key: 'eventId', label: 'Venue', render: (v) => getEventVenue(v) },
    { key: 'roleInEvent', label: 'My Role' },
  ];

  return (
    <SectionCard title="My Assigned Events">
      <DataTable columns={columns} data={assignments} loading={loading} emptyMessage="You have not been assigned to any events yet." />
    </SectionCard>
  );
}
