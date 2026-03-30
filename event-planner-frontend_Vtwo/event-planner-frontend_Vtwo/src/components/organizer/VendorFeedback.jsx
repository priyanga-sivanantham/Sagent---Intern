import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';

const starRating = (r) => <span className="text-amber-400">{'★'.repeat(r||0)}{'☆'.repeat(5-(r||0))}</span>;

export default function VendorFeedback() {
  const { user } = useAuth();
  const [feedback, setFeedback] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [vRes, evRes, eRes] = await Promise.all([api.get('/vendors'), api.get('/eventvendors'), api.get('/events')]);
        const mine = extractItems(vRes.data).find((v) => String(v.userId) === String(user?.userId || user?.id));
        setEvents(extractItems(eRes.data));
        if (!mine) { setFeedback([]); return; }
        const vid = mine.id || mine.vendorId;
        const assignments = extractItems(evRes.data).filter((a) => String(a.vendorId) === String(vid));
        const eventIds = [...new Set(assignments.map((a) => a.eventId))];
        const fbResults = await Promise.all(eventIds.map((id) => api.get(`/feedback/event/${id}`).catch(() => ({ data: [] }))));
        setFeedback(fbResults.flatMap((r) => extractItems(r.data)));
      } catch {}
      finally { setLoading(false); }
    };
    load();
  }, [user]);

  const getEventName = (id) => events.find((e) => (e.id || e.eventId) === id)?.eventName || id;
  const columns = [
    { key: 'eventId', label: 'Event', render: (v) => getEventName(v) },
    { key: 'rating', label: 'Rating', render: (v) => starRating(v) },
    { key: 'comments', label: 'Comments' },
  ];

  return (
    <SectionCard title="Feedback for My Events">
      <DataTable columns={columns} data={feedback} loading={loading} emptyMessage="No feedback for your events yet." />
    </SectionCard>
  );
}
