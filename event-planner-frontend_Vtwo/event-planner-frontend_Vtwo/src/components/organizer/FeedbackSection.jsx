import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';

const starRating = (rating) => {
  return (
    <span className="text-amber-400">
      {'★'.repeat(rating || 0)}{'☆'.repeat(5 - (rating || 0))}
    </span>
  );
};

export default function FeedbackSection() {
  const [feedback, setFeedback] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedEvent, setSelectedEvent] = useState('');

  const load = async (eventId) => {
    setLoading(true);
    try {
      const [eRes] = await Promise.all([api.get('/events')]);
      setEvents(extractItems(eRes.data));
      let fRes;
      if (eventId) fRes = await api.get(`/feedback/event/${eventId}`);
      else fRes = await api.get('/feedback');
      setFeedback(extractItems(fRes.data));
    } catch { setFeedback([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(selectedEvent); }, [selectedEvent]);

  const handleDelete = async (item) => {
    if (!window.confirm('Delete this feedback?')) return;
    try { await api.delete(`/feedback/${item.id || item.feedbackId}`); load(selectedEvent); } catch {}
  };

  const getEventName = (id) => events.find((e) => (e.id || e.eventId) === id)?.eventName || id;

  const columns = [
    { key: 'eventId', label: 'Event', render: (v) => getEventName(v) },
    { key: 'rating', label: 'Rating', render: (v) => starRating(v) },
    { key: 'comments', label: 'Comments' },
  ];

  return (
    <SectionCard
      title="Feedback"
      action={
        <select className="input-field w-auto text-xs py-1.5" value={selectedEvent} onChange={(e) => setSelectedEvent(e.target.value)}>
          <option value="">All events</option>
          {events.map((ev) => <option key={ev.id || ev.eventId} value={ev.id || ev.eventId}>{ev.eventName}</option>)}
        </select>
      }
    >
      <DataTable columns={columns} data={feedback} loading={loading} onDelete={handleDelete} emptyMessage="No feedback received yet." />
    </SectionCard>
  );
}
