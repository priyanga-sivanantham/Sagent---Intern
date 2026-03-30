import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';

const fmtDate = (v) => {
  if (!v) return '—';
  return new Date(v).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
};

const daysUntil = (v) => {
  if (!v) return null;
  return Math.ceil((new Date(v) - new Date()) / 86400000);
};

const STATUS_CFG = {
  PENDING:     { label: 'Pending',     dot: 'bg-amber-400',   ring: 'ring-amber-200',   text: 'text-amber-700',   bg: 'bg-amber-50'   },
  IN_PROGRESS: { label: 'In Progress', dot: 'bg-blue-500',    ring: 'ring-blue-200',    text: 'text-blue-700',    bg: 'bg-blue-50'    },
  DONE:        { label: 'Done',        dot: 'bg-emerald-500', ring: 'ring-emerald-200', text: 'text-emerald-700', bg: 'bg-emerald-50' },
  CANCELLED:   { label: 'Cancelled',   dot: 'bg-slate-400',   ring: 'ring-slate-200',   text: 'text-slate-500',   bg: 'bg-slate-50'   },
};

/* mini donut svg */
function DonutChart({ pct, size = 48, stroke = 6, color = '#6272f1' }) {
  const r  = (size - stroke) / 2;
  const cx = size / 2;
  const circumference = 2 * Math.PI * r;
  const dash = (pct / 100) * circumference;
  return (
    <svg width={size} height={size} className="-rotate-90">
      <circle cx={cx} cy={cx} r={r} fill="none" stroke="#e2e8f0" strokeWidth={stroke} />
      <circle
        cx={cx} cy={cx} r={r} fill="none"
        stroke={color} strokeWidth={stroke}
        strokeDasharray={`${dash} ${circumference}`}
        strokeLinecap="round"
      />
    </svg>
  );
}

/* stat tile */
function StatTile({ label, value, sub, icon, accent = 'primary' }) {
  const accentMap = {
    primary: 'from-primary-50 to-primary-100 border-primary-200 text-primary-700',
    emerald: 'from-emerald-50 to-emerald-100 border-emerald-200 text-emerald-700',
    amber:   'from-amber-50 to-amber-100 border-amber-200 text-amber-700',
    rose:    'from-rose-50 to-rose-100 border-rose-200 text-rose-700',
    violet:  'from-violet-50 to-violet-100 border-violet-200 text-violet-700',
  };
  return (
    <div className={`rounded-2xl border bg-gradient-to-br p-5 ${accentMap[accent]}`}>
      <div className="flex items-start justify-between mb-3">
        <span className="text-2xl">{icon}</span>
        {sub && <span className="text-xs font-medium opacity-60 bg-white/60 px-2 py-0.5 rounded-full">{sub}</span>}
      </div>
      <p className="font-display text-3xl font-bold mb-1">{value ?? '—'}</p>
      <p className="text-xs font-semibold uppercase tracking-widest opacity-60">{label}</p>
    </div>
  );
}

/* upcoming event mini-card */
function MiniEventCard({ event, tasks, budget }) {
  const eventId = event.id || event.eventId;
  const evTasks = tasks.filter((t) => String(t.eventId) === String(eventId));
  const done    = evTasks.filter((t) => t.status === 'DONE').length;
  const pct     = evTasks.length > 0 ? Math.round((done / evTasks.length) * 100) : 0;
  const evBudget = budget.find((b) => String(b.eventId) === String(eventId));
  const days    = daysUntil(event.eventDate);
  const isPast  = days !== null && days < 0;

  return (
    <div className={`flex items-center gap-4 p-4 rounded-xl border border-slate-100 hover:border-primary-200 hover:bg-primary-50/40 transition-all group ${isPast ? 'opacity-60' : ''}`}>
      {/* donut */}
      <div className="relative shrink-0">
        <DonutChart pct={pct} size={44} stroke={5} color={pct === 100 ? '#10b981' : pct > 50 ? '#3b82f6' : '#f59e0b'} />
        <span className="absolute inset-0 flex items-center justify-center text-xs font-bold text-slate-600">{pct}%</span>
      </div>

      {/* info */}
      <div className="flex-1 min-w-0">
        <p className="text-sm font-semibold text-slate-800 truncate">{event.eventName}</p>
        <p className="text-xs text-slate-400 truncate">{event.venue || 'No venue'}</p>
        <div className="flex items-center gap-2 mt-1">
          <span className="text-xs text-slate-400">{fmtDate(event.eventDate)}</span>
          {evBudget && (
            <span className="text-xs text-slate-400">
              · ₹{Number(evBudget.remainingBudget).toLocaleString()} left
            </span>
          )}
        </div>
      </div>

      {/* countdown pill */}
      {days !== null && !isPast && (
        <span className={`shrink-0 text-xs font-semibold px-2.5 py-1 rounded-full ${
          days <= 7 ? 'bg-orange-100 text-orange-600' :
          days <= 30 ? 'bg-amber-100 text-amber-600' :
          'bg-emerald-50 text-emerald-600'
        }`}>
          {days === 0 ? 'Today' : `${days}d`}
        </span>
      )}
    </div>
  );
}

/* task breakdown row */
function TaskBreakdown({ tasks }) {
  const counts = { PENDING: 0, IN_PROGRESS: 0, DONE: 0, CANCELLED: 0 };
  tasks.forEach((t) => { const s = t.status || 'PENDING'; if (counts[s] !== undefined) counts[s]++; });
  const total = tasks.length || 1;

  return (
    <div className="space-y-2">
      {Object.entries(counts).map(([status, count]) => {
        const cfg = STATUS_CFG[status];
        const pct = Math.round((count / total) * 100);
        return (
          <div key={status}>
            <div className="flex items-center justify-between mb-1">
              <div className="flex items-center gap-1.5">
                <span className={`w-2 h-2 rounded-full ${cfg.dot}`} />
                <span className="text-xs text-slate-600">{cfg.label}</span>
              </div>
              <span className="text-xs font-semibold text-slate-700">{count}</span>
            </div>
            <div className="h-1.5 rounded-full bg-slate-100 overflow-hidden">
              <div className={`h-full rounded-full ${cfg.dot}`} style={{ width: `${pct}%` }} />
            </div>
          </div>
        );
      })}
    </div>
  );
}

export default function OrganizerOverview() {
  const { user } = useAuth();
  const [data,    setData]    = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [eRes, tRes, vRes, gRes, bRes] = await Promise.all([
          api.get('/events'),
          api.get('/tasks'),
          api.get('/vendors'),
          api.get('/guests'),
          api.get('/budgets'),
        ]);
        const events  = extractItems(eRes.data);
        const tasks   = extractItems(tRes.data);
        const vendors = extractItems(vRes.data);
        const guests  = extractItems(gRes.data);
        const budgets = extractItems(bRes.data);

        const totalBudget     = budgets.reduce((s, b) => s + (Number(b.totalBudget)     || 0), 0);
        const remainingBudget = budgets.reduce((s, b) => s + (Number(b.remainingBudget) || 0), 0);
        const spent = totalBudget - remainingBudget;

        const upcoming = events
          .filter((e) => e.eventDate && new Date(e.eventDate) >= new Date())
          .sort((a, b) => new Date(a.eventDate) - new Date(b.eventDate));

        const rsvpAccepted = guests.filter((g) => g.rsvpStatus === 'Accepted').length;
        const tasksDone    = tasks.filter((t) => t.status === 'DONE').length;

        setData({ events, tasks, vendors, guests, budgets, totalBudget, spent, upcoming, rsvpAccepted, tasksDone });
      } catch {}
      finally { setLoading(false); }
    };
    load();
  }, []);

  if (loading) {
    return (
      <div className="space-y-4 animate-pulse">
        <div className="h-8 w-56 bg-slate-200 rounded-lg" />
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[1,2,3,4].map((i) => <div key={i} className="h-28 bg-slate-200 rounded-2xl" />)}
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          {[1,2,3].map((i) => <div key={i} className="h-64 bg-slate-200 rounded-2xl" />)}
        </div>
      </div>
    );
  }

  if (!data) return null;

  const { events, tasks, budgets, totalBudget, spent, upcoming, rsvpAccepted, tasksDone } = data;
  const budgetPct = totalBudget > 0 ? Math.round((spent / totalBudget) * 100) : 0;
  const taskPct   = tasks.length > 0 ? Math.round((tasksDone / tasks.length) * 100) : 0;

  return (
    <div className="space-y-6">
      {/* greeting */}
      <div className="flex items-end justify-between">
        <div>
          <h2 className="font-display text-2xl text-slate-800">
            Good {new Date().getHours() < 12 ? 'morning' : new Date().getHours() < 17 ? 'afternoon' : 'evening'}, {user?.name?.split(' ')[0]} 👋
          </h2>
          <p className="text-sm text-slate-400 mt-0.5">
            {upcoming.length > 0
              ? `Next event: ${upcoming[0].eventName} — ${new Date(upcoming[0].eventDate).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' })}`
              : 'No upcoming events scheduled.'}
          </p>
        </div>
        <span className="text-xs text-slate-400 hidden sm:block">
          {new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long' })}
        </span>
      </div>

      {/* stat tiles */}
      <div className="grid grid-cols-2 lg:grid-cols-5 gap-3">
        <StatTile label="Events"       value={events.length}                             icon="📅" accent="primary" />
        <StatTile label="Tasks Done"   value={`${tasksDone}/${tasks.length}`}            icon="✅" accent="violet"  sub={`${taskPct}%`} />
        <StatTile label="Vendors"      value={data.vendors.length}                       icon="🏢" accent="amber"   />
        <StatTile label="RSVP Yes"     value={`${rsvpAccepted}/${data.guests.length}`}  icon="🎟️" accent="emerald" />
        <StatTile label="Budget Used"  value={`₹${spent.toLocaleString()}`}             icon="💰" accent="rose"    sub={`${budgetPct}% of ₹${totalBudget.toLocaleString()}`} />
      </div>

      {/* middle row: 2/3 upcoming + 1/3 task breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">

        {/* upcoming events — 2 cols */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200 shadow-card overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
            <h3 className="font-display text-base text-slate-800">Upcoming Events</h3>
            <span className="text-xs text-slate-400 font-medium">{upcoming.length} scheduled</span>
          </div>
          <div className="p-4 space-y-2 max-h-80 overflow-y-auto">
            {upcoming.length === 0 ? (
              <div className="text-center py-10 text-slate-400">
                <span className="text-3xl block mb-2">📅</span>
                <p className="text-sm">No upcoming events. Create one from the Events tab.</p>
              </div>
            ) : (
              upcoming.slice(0, 6).map((ev) => (
                <MiniEventCard key={ev.id || ev.eventId} event={ev} tasks={tasks} budget={budgets} />
              ))
            )}
          </div>
        </div>

        {/* task status breakdown — 1 col */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-card overflow-hidden">
          <div className="px-5 py-4 border-b border-slate-100">
            <h3 className="font-display text-base text-slate-800">Task Overview</h3>
          </div>
          <div className="p-5">
            {tasks.length === 0 ? (
              <div className="text-center py-8 text-slate-400">
                <span className="text-3xl block mb-2">✅</span>
                <p className="text-sm">No tasks created yet.</p>
              </div>
            ) : (
              <>
                {/* big donut */}
                <div className="flex items-center justify-center mb-6">
                  <div className="relative">
                    <DonutChart pct={taskPct} size={100} stroke={10} color={taskPct === 100 ? '#10b981' : '#6272f1'} />
                    <div className="absolute inset-0 flex flex-col items-center justify-center">
                      <span className="text-2xl font-display font-bold text-slate-700">{taskPct}%</span>
                      <span className="text-xs text-slate-400">done</span>
                    </div>
                  </div>
                </div>
                <TaskBreakdown tasks={tasks} />
              </>
            )}
          </div>
        </div>
      </div>

      {/* budget overview bar */}
      {totalBudget > 0 && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-card p-5">
          <div className="flex items-center justify-between mb-3">
            <h3 className="font-display text-base text-slate-800">Overall Budget</h3>
            <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${budgetPct >= 90 ? 'bg-red-100 text-red-600' : budgetPct >= 70 ? 'bg-orange-100 text-orange-600' : 'bg-emerald-50 text-emerald-600'}`}>
              {budgetPct}% used
            </span>
          </div>
          <div className="h-3 bg-slate-100 rounded-full overflow-hidden mb-3">
            <div
              className={`h-full rounded-full transition-all duration-700 ${budgetPct >= 90 ? 'bg-red-500' : budgetPct >= 70 ? 'bg-orange-400' : 'bg-primary-500'}`}
              style={{ width: `${Math.min(budgetPct, 100)}%` }}
            />
          </div>
          <div className="flex items-center justify-between text-xs text-slate-500">
            <span>₹{spent.toLocaleString()} spent</span>
            <span className="font-semibold text-slate-700">Total: ₹{totalBudget.toLocaleString()}</span>
            <span>₹{(totalBudget - spent).toLocaleString()} remaining</span>
          </div>
          {/* per-event budget breakdown */}
          {budgets.length > 1 && (
            <div className="mt-4 grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-2">
              {budgets.map((b) => {
                const evName = events.find((e) => String(e.id || e.eventId) === String(b.eventId))?.eventName || `Event ${b.eventId}`;
                const bPct   = Number(b.totalBudget) > 0 ? Math.round(((Number(b.totalBudget) - Number(b.remainingBudget)) / Number(b.totalBudget)) * 100) : 0;
                return (
                  <div key={b.id || b.budgetId} className="bg-slate-50 rounded-lg p-3">
                    <p className="text-xs font-medium text-slate-700 truncate mb-1.5">{evName}</p>
                    <div className="h-1.5 bg-slate-200 rounded-full overflow-hidden mb-1">
                      <div className={`h-full rounded-full ${bPct >= 90 ? 'bg-red-500' : bPct >= 70 ? 'bg-orange-400' : 'bg-primary-500'}`} style={{ width: `${bPct}%` }} />
                    </div>
                    <p className="text-xs text-slate-400">{bPct}% of ₹{Number(b.totalBudget).toLocaleString()}</p>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
