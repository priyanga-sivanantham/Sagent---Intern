import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';
import Modal from '../Modal';

export default function VendorsSection() {
  const [vendors, setVendors] = useState([]);
  const [events, setEvents] = useState([]);
  const [eventVendors, setEventVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [vendorModal, setVendorModal] = useState(false);
  const [assignModal, setAssignModal] = useState(false);
  const [editItem, setEditItem] = useState(null);
  const [form, setForm] = useState({ vendorName: '', serviceType: '', contact: '', userId: '' });
  const [assignForm, setAssignForm] = useState({ eventId: '', vendorId: '', serviceDetails: '' });
  const [users, setUsers] = useState([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [vRes, eRes, evRes, uRes] = await Promise.all([
        api.get('/vendors'),
        api.get('/events'),
        api.get('/eventvendors'),
        api.get('/users'),
      ]);
      setVendors(extractItems(vRes.data));
      setEvents(extractItems(eRes.data));
      setEventVendors(extractItems(evRes.data));
      setUsers(extractItems(uRes.data).filter((u) => u.role === 'VENDOR'));
    } catch { setVendors([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const openCreate = () => { setEditItem(null); setForm({ vendorName: '', serviceType: '', contact: '', userId: '' }); setError(''); setVendorModal(true); };
  const openEdit = (item) => { setEditItem(item); setForm({ vendorName: item.vendorName, serviceType: item.serviceType, contact: item.contact, userId: item.userId }); setError(''); setVendorModal(true); };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      if (editItem) { await api.put(`/vendors/${editItem.id || editItem.vendorId}`, form); }
      else { await api.post('/vendors', form); }
      setVendorModal(false); load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save vendor.');
    } finally { setSaving(false); }
  };

  const handleDelete = async (item) => {
    if (!window.confirm('Delete this vendor?')) return;
    try { await api.delete(`/vendors/${item.id || item.vendorId}`); load(); } catch {}
  };

  const handleAssign = async (e) => {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      await api.post(`/eventvendors/assign?eventId=${assignForm.eventId}&vendorId=${assignForm.vendorId}&serviceDetails=${encodeURIComponent(assignForm.serviceDetails)}`);
      setAssignModal(false); load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to assign vendor.');
    } finally { setSaving(false); }
  };

  const handleUnassign = async (item) => {
    if (!window.confirm('Remove vendor from this event?')) return;
    try { await api.delete(`/eventvendors/${item.id}`); load(); } catch {}
  };

  const getEventName = (id) => events.find((e) => (e.id || e.eventId) === id)?.eventName || id;
  const getVendorName = (id) => vendors.find((v) => (v.id || v.vendorId) === id)?.vendorName || id;

  const vendorCols = [
    { key: 'vendorName', label: 'Vendor' },
    { key: 'serviceType', label: 'Service' },
    { key: 'contact', label: 'Contact' },
  ];

  const assignCols = [
    { key: 'eventId', label: 'Event', render: (v) => getEventName(v) },
    { key: 'vendorId', label: 'Vendor', render: (v) => getVendorName(v) },
    { key: 'serviceDetails', label: 'Service Details' },
  ];

  return (
    <div className="space-y-6">
      <SectionCard title="Vendors" action={
        <div className="flex gap-2">
          <button className="btn-secondary" onClick={() => { setAssignForm({ eventId: '', vendorId: '', serviceDetails: '' }); setError(''); setAssignModal(true); }}>Assign to Event</button>
          <button className="btn-primary" onClick={openCreate}>+ Add Vendor</button>
        </div>
      }>
        <DataTable columns={vendorCols} data={vendors} loading={loading} onEdit={openEdit} onDelete={handleDelete} emptyMessage="No vendors yet." />
      </SectionCard>

      <SectionCard title="Event-Vendor Assignments">
        <DataTable columns={assignCols} data={eventVendors} loading={loading} onDelete={handleUnassign} emptyMessage="No vendor assignments yet." />
      </SectionCard>

      <Modal isOpen={vendorModal} onClose={() => setVendorModal(false)} title={editItem ? 'Edit Vendor' : 'Add Vendor'}>
        <form onSubmit={handleSave} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Vendor Name</label>
            <input className="input-field" value={form.vendorName} onChange={(e) => setForm({ ...form, vendorName: e.target.value })} required />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Service Type</label>
            <input className="input-field" placeholder="e.g. Catering, Photography" value={form.serviceType} onChange={(e) => setForm({ ...form, serviceType: e.target.value })} />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Contact</label>
            <input className="input-field" value={form.contact} onChange={(e) => setForm({ ...form, contact: e.target.value })} />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Linked User (optional)</label>
            <select className="input-field" value={form.userId} onChange={(e) => setForm({ ...form, userId: e.target.value })}>
              <option value="">None</option>
              {users.map((u) => <option key={u.userId || u.id} value={u.userId || u.id}>{u.name}</option>)}
            </select>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={saving} className="btn-primary flex-1">{saving ? 'Saving...' : editItem ? 'Update' : 'Add Vendor'}</button>
            <button type="button" onClick={() => setVendorModal(false)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={assignModal} onClose={() => setAssignModal(false)} title="Assign Vendor to Event">
        <form onSubmit={handleAssign} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Event</label>
            <select className="input-field" value={assignForm.eventId} onChange={(e) => setAssignForm({ ...assignForm, eventId: e.target.value })} required>
              <option value="">Select event...</option>
              {events.map((ev) => <option key={ev.id || ev.eventId} value={ev.id || ev.eventId}>{ev.eventName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Vendor</label>
            <select className="input-field" value={assignForm.vendorId} onChange={(e) => setAssignForm({ ...assignForm, vendorId: e.target.value })} required>
              <option value="">Select vendor...</option>
              {vendors.map((v) => <option key={v.id || v.vendorId} value={v.id || v.vendorId}>{v.vendorName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Service Details</label>
            <input className="input-field" value={assignForm.serviceDetails} onChange={(e) => setAssignForm({ ...assignForm, serviceDetails: e.target.value })} />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={saving} className="btn-primary flex-1">{saving ? 'Assigning...' : 'Assign'}</button>
            <button type="button" onClick={() => setAssignModal(false)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
