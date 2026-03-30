import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';

const starRating = (r) => <span className="text-amber-400">{'★'.repeat(r||0)}{'☆'.repeat(5-(r||0))}</span>;

export default function TeamMemberFeedback() {
  const { user } = useAuth();
  const [feedback, setFeedback] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [aRes, eRes] = await Promise.all([api.get('/eventteammembers'), api.get('/events')]);
        const myEventIds = extractItems(aRes.data)
          .filter((a) => String(a.userId) === String(user?.userId || user?.id))
          .map((a) => a.eventId);
        setEvents(extractItems(eRes.data));
        const fbResults = await Promise.all(myEventIds.map((id) => api.get(`/feedback/event/${id}`).catch(() => ({ data: [] }))));
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
