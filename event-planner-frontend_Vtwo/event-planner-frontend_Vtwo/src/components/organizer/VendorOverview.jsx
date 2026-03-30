import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';
import StatCard from '../StatCard';

export default function VendorOverview() {
  const { user } = useAuth();
  const [stats, setStats] = useState({ events: 0, feedback: 0 });
  const [myVendor, setMyVendor] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [vRes, evRes] = await Promise.all([api.get('/vendors'), api.get('/eventvendors')]);
        const allVendors = extractItems(vRes.data);
        const mine = allVendors.find((v) => String(v.userId) === String(user?.userId || user?.id));
        setMyVendor(mine);
        if (mine) {
          const vid = mine.id || mine.vendorId;
          const evAssignments = extractItems(evRes.data).filter((ev) => String(ev.vendorId) === String(vid));
          const fbRes = await Promise.all(
            [...new Set(evAssignments.map((a) => a.eventId))].map((eid) =>
              api.get(`/feedback/event/${eid}`).catch(() => ({ data: [] }))
            )
          );
          setStats({ events: evAssignments.length, feedback: fbRes.flatMap((r) => extractItems(r.data)).length });
        }
      } catch {}
      finally { setLoading(false); }
    };
    load();
  }, [user]);

  if (loading) return <div className="flex items-center justify-center h-48"><div className="w-7 h-7 border-4 border-primary-500 border-t-transparent rounded-full animate-spin" /></div>;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="font-display text-2xl text-surface-800 mb-1">Welcome, {user?.name}</h2>
        <p className="text-sm text-slate-500">Your vendor profile and event summary.</p>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <StatCard label="Assigned Events" value={stats.events} icon="📅" color="primary" />
        <StatCard label="Feedback Received" value={stats.feedback} icon="⭐" color="amber" />
      </div>
      {myVendor ? (
        <div className="card p-5">
          <h3 className="font-display text-base text-surface-800 mb-4">My Vendor Profile</h3>
          <dl className="grid grid-cols-2 gap-4 text-sm">
            <div><dt className="text-xs text-slate-400 uppercase tracking-wide mb-1">Vendor Name</dt><dd className="font-medium text-slate-700">{myVendor.vendorName}</dd></div>
            <div><dt className="text-xs text-slate-400 uppercase tracking-wide mb-1">Service Type</dt><dd className="font-medium text-slate-700">{myVendor.serviceType || '—'}</dd></div>
            <div><dt className="text-xs text-slate-400 uppercase tracking-wide mb-1">Contact</dt><dd className="font-medium text-slate-700">{myVendor.contact || '—'}</dd></div>
          </dl>
        </div>
      ) : (
        <div className="card p-5 text-center text-slate-400">
          <p className="text-sm">No vendor profile linked to your account yet. Go to "My Profile" to create one.</p>
        </div>
      )}
    </div>
  );
}
