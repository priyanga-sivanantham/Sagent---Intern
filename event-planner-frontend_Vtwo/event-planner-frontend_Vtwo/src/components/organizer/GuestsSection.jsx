import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';
import Modal from '../Modal';

const RSVP_OPTIONS = ['Pending', 'Accepted', 'Declined'];

const rsvpBadge = (status) => {
  const map = { Accepted: 'bg-emerald-100 text-emerald-700', Declined: 'bg-red-100 text-red-600', Pending: 'bg-yellow-100 text-yellow-700' };
  return <span className={`badge ${map[status] || 'bg-slate-100 text-slate-600'}`}>{status || 'Pending'}</span>;
};

export default function GuestsSection() {
  const [guests, setGuests] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editItem, setEditItem] = useState(null);
  const [form, setForm] = useState({ guestName: '', email: '', phone: '', rsvpStatus: 'Pending', eventId: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [selectedEvent, setSelectedEvent] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [gRes, eRes] = await Promise.all([api.get('/guests'), api.get('/events')]);
      setGuests(extractItems(gRes.data));
      setEvents(extractItems(eRes.data));
    } catch { setGuests([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const openCreate = () => { setEditItem(null); setForm({ guestName: '', email: '', phone: '', rsvpStatus: 'Pending', eventId: '' }); setError(''); setModalOpen(true); };
  const openEdit = (item) => { setEditItem(item); setForm({ guestName: item.guestName, email: item.email, phone: item.phone, rsvpStatus: item.rsvpStatus, eventId: item.eventId }); setError(''); setModalOpen(true); };

  const handleSave = async (e) => {
    e.preventDefault(); setSaving(true); setError('');
    try {
      if (editItem) await api.put(`/guests/${editItem.id || editItem.guestId}`, form);
      else await api.post('/guests', form);
      setModalOpen(false); load();
    } catch (err) { setError(err.response?.data?.message || 'Failed to save guest.'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (item) => {
    if (!window.confirm('Delete this guest?')) return;
    try { await api.delete(`/guests/${item.id || item.guestId}`); load(); } catch {}
  };

  const sendInvites = async (eventId) => {
    if (!eventId) return alert('Select an event first.');
    try {
      await api.post(`/guests/send-invitations/${eventId}`);
      alert('Invitations sent!');
    } catch { alert('Failed to send invitations.'); }
  };

  const sendFeedback = async (eventId) => {
    if (!eventId) return alert('Select an event first.');
    try {
      await api.post(`/guests/${eventId}/send-feedback-mail`);
      alert('Feedback emails sent!');
    } catch { alert('Failed to send feedback emails.'); }
  };

  const getEventName = (id) => events.find((e) => (e.id || e.eventId) === id)?.eventName || id;
  const filtered = selectedEvent ? guests.filter((g) => String(g.eventId) === String(selectedEvent)) : guests;

  const columns = [
    { key: 'guestName', label: 'Name' },
    { key: 'email', label: 'Email' },
    { key: 'phone', label: 'Phone' },
    { key: 'eventId', label: 'Event', render: (v) => getEventName(v) },
    { key: 'rsvpStatus', label: 'RSVP', render: (v) => rsvpBadge(v) },
  ];

  return (
    <>
      <SectionCard
        title="Guests"
        action={
          <div className="flex flex-wrap gap-2">
            <select className="input-field w-auto text-xs py-1.5" value={selectedEvent} onChange={(e) => setSelectedEvent(e.target.value)}>
              <option value="">All events</option>
              {events.map((ev) => <option key={ev.id || ev.eventId} value={ev.id || ev.eventId}>{ev.eventName}</option>)}
            </select>
            <button className="btn-secondary text-xs py-1.5" onClick={() => sendInvites(selectedEvent)}>Send Invites</button>
            <button className="btn-secondary text-xs py-1.5" onClick={() => sendFeedback(selectedEvent)}>Send Feedback</button>
            <button className="btn-primary" onClick={openCreate}>+ Add Guest</button>
          </div>
        }
      >
        <DataTable columns={columns} data={filtered} loading={loading} onEdit={openEdit} onDelete={handleDelete} emptyMessage="No guests added yet." />
      </SectionCard>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editItem ? 'Edit Guest' : 'Add Guest'}>
        <form onSubmit={handleSave} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Guest Name</label>
            <input className="input-field" value={form.guestName} onChange={(e) => setForm({ ...form, guestName: e.target.value })} required />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Email</label>
            <input type="email" className="input-field" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Phone</label>
            <input className="input-field" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Event</label>
            <select className="input-field" value={form.eventId} onChange={(e) => setForm({ ...form, eventId: e.target.value })} required>
              <option value="">Select event...</option>
              {events.map((ev) => <option key={ev.id || ev.eventId} value={ev.id || ev.eventId}>{ev.eventName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">RSVP Status</label>
            <select className="input-field" value={form.rsvpStatus} onChange={(e) => setForm({ ...form, rsvpStatus: e.target.value })}>
              {RSVP_OPTIONS.map((r) => <option key={r}>{r}</option>)}
            </select>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={saving} className="btn-primary flex-1">{saving ? 'Saving...' : editItem ? 'Update' : 'Add Guest'}</button>
            <button type="button" onClick={() => setModalOpen(false)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </form>
      </Modal>
    </>
  );
}
