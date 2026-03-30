import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';
import SectionCard from '../SectionCard';

export default function VendorProfile() {
  const { user } = useAuth();
  const [vendor, setVendor] = useState(null);
  const [form, setForm] = useState({ vendorName: '', serviceType: '', contact: '', userId: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const res = await api.get('/vendors');
        const all = extractItems(res.data);
        const mine = all.find((v) => String(v.userId) === String(user?.userId || user?.id));
        if (mine) {
          setVendor(mine);
          setForm({ vendorName: mine.vendorName, serviceType: mine.serviceType || '', contact: mine.contact || '', userId: mine.userId });
        } else {
          setForm((f) => ({ ...f, userId: user?.userId || user?.id || '' }));
        }
      } catch {}
      finally { setLoading(false); }
    };
    load();
  }, [user]);

  const handleSave = async (e) => {
    e.preventDefault(); setSaving(true); setError(''); setSuccess('');
    try {
      if (vendor) await api.put(`/vendors/${vendor.id || vendor.vendorId}`, form);
      else await api.post('/vendors', form);
      setSuccess('Profile saved successfully!');
      const res = await api.get('/vendors');
      const mine = extractItems(res.data).find((v) => String(v.userId) === String(user?.userId || user?.id));
      if (mine) setVendor(mine);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save profile.');
    } finally { setSaving(false); }
  };

  if (loading) return <div className="flex items-center justify-center h-48"><div className="w-7 h-7 border-4 border-primary-500 border-t-transparent rounded-full animate-spin" /></div>;

  return (
    <SectionCard title="My Vendor Profile">
      <form onSubmit={handleSave} className="space-y-4 max-w-md">
        {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>}
        {success && <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-700 text-sm">{success}</div>}
        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Vendor / Business Name</label>
          <input className="input-field" value={form.vendorName} onChange={(e) => setForm({ ...form, vendorName: e.target.value })} required />
        </div>
        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Service Type</label>
          <input className="input-field" placeholder="e.g. Catering, Photography, Decor" value={form.serviceType} onChange={(e) => setForm({ ...form, serviceType: e.target.value })} />
        </div>
        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Contact Info</label>
          <input className="input-field" placeholder="Phone or email" value={form.contact} onChange={(e) => setForm({ ...form, contact: e.target.value })} />
        </div>
        <button type="submit" disabled={saving} className="btn-primary">
          {saving ? 'Saving...' : vendor ? 'Update Profile' : 'Create Profile'}
        </button>
      </form>
    </SectionCard>
  );
}
