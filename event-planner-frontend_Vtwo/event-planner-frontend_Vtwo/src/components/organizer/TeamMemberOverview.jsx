import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';
import StatCard from '../StatCard';

export default function TeamMemberOverview() {
  const { user } = useAuth();
  const [stats, setStats] = useState({ events: 0, tasks: 0, pendingTasks: 0 });
  const [recentTasks, setRecentTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [tRes, etRes] = await Promise.all([api.get('/tasks'), api.get('/eventteammembers')]);
        const tasks = extractItems(tRes.data);
        const myAssignments = extractItems(etRes.data).filter(
          (a) => String(a.userId) === String(user?.userId || user?.id)
        );
        const myTasks = tasks.filter((t) => String(t.teamMemberId) === String(user?.userId || user?.id));
        setStats({
          events: myAssignments.length,
          tasks: myTasks.length,
          pendingTasks: myTasks.filter((t) => t.status === 'PENDING' || !t.status).length,
        });
        setRecentTasks(myTasks.slice(0, 5));
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
        <p className="text-sm text-slate-500">Your task and event summary.</p>
      </div>
      <div className="grid grid-cols-3 gap-4">
        <StatCard label="My Events" value={stats.events} icon="📅" color="primary" />
        <StatCard label="My Tasks" value={stats.tasks} icon="✅" color="violet" />
        <StatCard label="Pending Tasks" value={stats.pendingTasks} icon="⏳" color="amber" />
      </div>
      <div className="card">
        <div className="px-5 py-4 border-b border-surface-100">
          <h3 className="font-display text-base text-surface-800">Recent Tasks</h3>
        </div>
        <div className="p-5">
          {recentTasks.length === 0 ? (
            <p className="text-sm text-slate-400 text-center py-8">No tasks assigned yet.</p>
          ) : (
            <ul className="divide-y divide-surface-100">
              {recentTasks.map((t) => (
                <li key={t.id || t.taskId} className="py-3 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-slate-700">{t.taskName}</p>
                    <p className="text-xs text-slate-400">{t.description}</p>
                  </div>
                  <span className={`badge text-xs ${t.status === 'DONE' ? 'bg-emerald-100 text-emerald-700' : t.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-700' : 'bg-yellow-100 text-yellow-700'}`}>
                    {t.status || 'PENDING'}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
