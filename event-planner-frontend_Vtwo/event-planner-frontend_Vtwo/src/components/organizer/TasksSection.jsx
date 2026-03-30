import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';
import Modal from '../Modal';

const STATUS_OPTIONS = ['PENDING', 'IN_PROGRESS', 'DONE', 'CANCELLED'];

const statusBadge = (status) => {
  const map = {
    PENDING: 'bg-yellow-100 text-yellow-700',
    IN_PROGRESS: 'bg-blue-100 text-blue-700',
    DONE: 'bg-emerald-100 text-emerald-700',
    CANCELLED: 'bg-red-100 text-red-600',
  };
  return <span className={`badge ${map[status] || 'bg-slate-100 text-slate-600'}`}>{status || 'PENDING'}</span>;
};

export default function TasksSection() {
  const [tasks, setTasks] = useState([]);
  const [events, setEvents] = useState([]);
  const [members, setMembers] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [statusModal, setStatusModal] = useState(null);
  const [form, setForm] = useState({ taskName: '', description: '', eventId: '', teamMemberId: '', vendorId: '' });
  const [newStatus, setNewStatus] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [tRes, eRes, mRes, vRes] = await Promise.all([
        api.get('/tasks'),
        api.get('/events'),
        api.get('/users'),
        api.get('/vendors'),
      ]);
      setTasks(extractItems(tRes.data));
      setEvents(extractItems(eRes.data));
      setMembers(extractItems(mRes.data).filter((u) => u.role === 'TEAM_MEMBER'));
      setVendors(extractItems(vRes.data));
    } catch { setTasks([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true); setError('');
    const params = new URLSearchParams();
    if (form.eventId) params.append('eventId', form.eventId);
    if (form.teamMemberId) params.append('teamMemberId', form.teamMemberId);
    if (form.vendorId) params.append('vendorId', form.vendorId);
    try {
      await api.post(`/tasks/create?${params.toString()}`, { taskName: form.taskName, description: form.description });
      setModalOpen(false);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create task.');
    } finally { setSaving(false); }
  };

  const handleUpdateStatus = async () => {
    if (!statusModal || !newStatus) return;
    try {
      await api.put(`/tasks/${statusModal.id || statusModal.taskId}/status?status=${newStatus}`);
      setStatusModal(null);
      load();
    } catch {}
  };

  const handleDelete = async (item) => {
    if (!window.confirm('Delete this task?')) return;
    try { await api.delete(`/tasks/${item.id || item.taskId}`); load(); } catch {}
  };

  const getEventName = (id) => events.find((e) => (e.id || e.eventId) === id)?.eventName || id;

  const columns = [
    { key: 'taskName', label: 'Task' },
    { key: 'description', label: 'Description' },
    { key: 'eventId', label: 'Event', render: (v) => getEventName(v) },
    { key: 'status', label: 'Status', render: (v) => statusBadge(v) },
  ];

  return (
    <>
      <SectionCard
        title="Tasks"
        action={<button className="btn-primary" onClick={() => { setForm({ taskName: '', description: '', eventId: '', teamMemberId: '', vendorId: '' }); setError(''); setModalOpen(true); }}>+ New Task</button>}
      >
        <DataTable
          columns={columns}
          data={tasks}
          loading={loading}
          onEdit={(item) => { setStatusModal(item); setNewStatus(item.status || 'PENDING'); }}
          onDelete={handleDelete}
          emptyMessage="No tasks yet."
        />
      </SectionCard>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Create Task">
        <form onSubmit={handleSave} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Task Name</label>
            <input className="input-field" value={form.taskName} onChange={(e) => setForm({ ...form, taskName: e.target.value })} required />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Description</label>
            <textarea className="input-field" rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Event</label>
            <select className="input-field" value={form.eventId} onChange={(e) => setForm({ ...form, eventId: e.target.value })}>
              <option value="">Select event...</option>
              {events.map((ev) => <option key={ev.id || ev.eventId} value={ev.id || ev.eventId}>{ev.eventName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Assign to Team Member (optional)</label>
            <select className="input-field" value={form.teamMemberId} onChange={(e) => setForm({ ...form, teamMemberId: e.target.value })}>
              <option value="">None</option>
              {members.map((m) => <option key={m.userId || m.id} value={m.userId || m.id}>{m.name}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Assign to Vendor (optional)</label>
            <select className="input-field" value={form.vendorId} onChange={(e) => setForm({ ...form, vendorId: e.target.value })}>
              <option value="">None</option>
              {vendors.map((v) => <option key={v.id || v.vendorId} value={v.id || v.vendorId}>{v.vendorName}</option>)}
            </select>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={saving} className="btn-primary flex-1">{saving ? 'Saving...' : 'Create Task'}</button>
            <button type="button" onClick={() => setModalOpen(false)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={!!statusModal} onClose={() => setStatusModal(null)} title="Update Task Status" size="sm">
        <div className="space-y-4">
          <p className="text-sm text-slate-600">{statusModal?.taskName}</p>
          <select className="input-field" value={newStatus} onChange={(e) => setNewStatus(e.target.value)}>
            {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <div className="flex gap-3">
            <button onClick={handleUpdateStatus} className="btn-primary flex-1">Update</button>
            <button onClick={() => setStatusModal(null)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </div>
      </Modal>
    </>
  );
}
