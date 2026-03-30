import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';
import Modal from '../Modal';

const STATUS_OPTIONS = ['PENDING', 'IN_PROGRESS', 'DONE', 'CANCELLED'];

const statusBadge = (s) => {
  const map = { PENDING: 'bg-yellow-100 text-yellow-700', IN_PROGRESS: 'bg-blue-100 text-blue-700', DONE: 'bg-emerald-100 text-emerald-700', CANCELLED: 'bg-red-100 text-red-600' };
  return <span className={`badge ${map[s] || 'bg-slate-100 text-slate-600'}`}>{s || 'PENDING'}</span>;
};

export default function TeamMemberTasks() {
  const { user } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusModal, setStatusModal] = useState(null);
  const [newStatus, setNewStatus] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [tRes, eRes] = await Promise.all([api.get('/tasks'), api.get('/events')]);
      const all = extractItems(tRes.data);
      setTasks(all.filter((t) => String(t.teamMemberId) === String(user?.userId || user?.id)));
      setEvents(extractItems(eRes.data));
    } catch {}
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [user]);

  const handleUpdate = async () => {
    if (!statusModal || !newStatus) return;
    try {
      await api.put(`/tasks/${statusModal.id || statusModal.taskId}/status?status=${newStatus}`);
      setStatusModal(null); load();
    } catch {}
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
      <SectionCard title="My Tasks">
        <DataTable columns={columns} data={tasks} loading={loading}
          onEdit={(item) => { setStatusModal(item); setNewStatus(item.status || 'PENDING'); }}
          emptyMessage="No tasks assigned to you yet." />
      </SectionCard>

      <Modal isOpen={!!statusModal} onClose={() => setStatusModal(null)} title="Update Task Status" size="sm">
        <div className="space-y-4">
          <p className="text-sm text-slate-600">{statusModal?.taskName}</p>
          <select className="input-field" value={newStatus} onChange={(e) => setNewStatus(e.target.value)}>
            {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <div className="flex gap-3">
            <button onClick={handleUpdate} className="btn-primary flex-1">Update</button>
            <button onClick={() => setStatusModal(null)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </div>
      </Modal>
    </>
  );
}
