import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';
import Modal from '../Modal';

export default function TeamSection() {
  const [assignments, setAssignments] = useState([]);
  const [events, setEvents] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState({ eventId: '', userId: '', roleInEvent: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [aRes, eRes, uRes] = await Promise.all([
        api.get('/eventteammembers'),
        api.get('/events'),
        api.get('/users'),
      ]);
      setAssignments(extractItems(aRes.data));
      setEvents(extractItems(eRes.data));
      setUsers(extractItems(uRes.data).filter((u) => u.role === 'TEAM_MEMBER'));
    } catch { setAssignments([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      await api.post('/eventteammembers', form);
      setModalOpen(false);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to assign team member.');
    } finally { setSaving(false); }
  };

  const handleDelete = async (item) => {
    if (!window.confirm('Remove this team member assignment?')) return;
    try { await api.delete(`/eventteammembers/${item.id}`); load(); } catch {}
  };

  const getEventName = (id) => events.find((e) => e.id === id || e.eventId === id)?.eventName || id;
  const getUserName = (id) => users.find((u) => u.userId === id || u.id === id)?.name || id;

  const columns = [
    { key: 'eventId', label: 'Event', render: (v) => getEventName(v) },
    { key: 'userId', label: 'Team Member', render: (v) => getUserName(v) },
    { key: 'roleInEvent', label: 'Role in Event' },
  ];

  return (
    <>
      <SectionCard
        title="Team Assignments"
        action={<button className="btn-primary" onClick={() => { setForm({ eventId: '', userId: '', roleInEvent: '' }); setError(''); setModalOpen(true); }}>+ Assign Member</button>}
      >
        <DataTable columns={columns} data={assignments} loading={loading} onDelete={handleDelete} emptyMessage="No team members assigned yet." />
      </SectionCard>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Assign Team Member">
        <form onSubmit={handleSave} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Event</label>
            <select className="input-field" value={form.eventId} onChange={(e) => setForm({ ...form, eventId: e.target.value })} required>
              <option value="">Select event...</option>
              {events.map((ev) => <option key={ev.id || ev.eventId} value={ev.id || ev.eventId}>{ev.eventName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Team Member</label>
            <select className="input-field" value={form.userId} onChange={(e) => setForm({ ...form, userId: e.target.value })} required>
              <option value="">Select member...</option>
              {users.map((u) => <option key={u.userId || u.id} value={u.userId || u.id}>{u.name}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Role in Event</label>
            <input className="input-field" placeholder="e.g. Coordinator" value={form.roleInEvent} onChange={(e) => setForm({ ...form, roleInEvent: e.target.value })} required />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={saving} className="btn-primary flex-1">{saving ? 'Saving...' : 'Assign'}</button>
            <button type="button" onClick={() => setModalOpen(false)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </form>
      </Modal>
    </>
  );
}
