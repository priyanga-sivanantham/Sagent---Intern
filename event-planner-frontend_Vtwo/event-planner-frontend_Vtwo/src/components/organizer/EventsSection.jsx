import { useState, useEffect, useCallback } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import Modal from '../Modal';

/* ─── helpers ─── */
const fmtDate = (v) => {
  if (!v) return '—';
  const d = new Date(v);
  return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
};

const daysUntil = (v) => {
  if (!v) return null;
  const diff = Math.ceil((new Date(v) - new Date()) / 86400000);
  return diff;
};

const statusConfig = {
  PENDING:     { label: 'Pending',     dot: 'bg-amber-400',   bar: 'bg-amber-400',   text: 'text-amber-700',  bg: 'bg-amber-50'  },
  IN_PROGRESS: { label: 'In Progress', dot: 'bg-blue-500',    bar: 'bg-blue-500',    text: 'text-blue-700',   bg: 'bg-blue-50'   },
  DONE:        { label: 'Done',        dot: 'bg-emerald-500', bar: 'bg-emerald-500', text: 'text-emerald-700',bg: 'bg-emerald-50'},
  CANCELLED:   { label: 'Cancelled',   dot: 'bg-slate-400',   bar: 'bg-slate-400',   text: 'text-slate-500',  bg: 'bg-slate-50'  },
};

const countdownPill = (days) => {
  if (days === null) return null;
  if (days < 0)  return <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-slate-100 text-slate-500">Past</span>;
  if (days === 0) return <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-red-100 text-red-600">Today</span>;
  if (days <= 7)  return <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-orange-100 text-orange-600">{days}d away</span>;
  if (days <= 30) return <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-amber-100 text-amber-600">{days}d away</span>;
  return <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-600">{days}d away</span>;
};

/* ─── Event Card ─── */
function EventCard({ event, tasks, budget, onEdit, onDelete, onViewTimeline }) {
  const eventId = event.id || event.eventId;
  const eventTasks = tasks.filter((t) => String(t.eventId) === String(eventId));
  const done  = eventTasks.filter((t) => t.status === 'DONE').length;
  const total = eventTasks.length;
  const pct   = total > 0 ? Math.round((done / total) * 100) : 0;

  const evBudget = budget.find((b) => String(b.eventId) === String(eventId));
  const spent    = evBudget ? Number(evBudget.totalBudget) - Number(evBudget.remainingBudget) : null;
  const budgetPct = evBudget && Number(evBudget.totalBudget) > 0
    ? Math.round((spent / Number(evBudget.totalBudget)) * 100) : null;

  const days = daysUntil(event.eventDate);
  const isPast = days !== null && days < 0;

  return (
    <div className={`bg-white rounded-2xl border border-slate-200 shadow-card hover:shadow-card-hover transition-shadow flex flex-col overflow-hidden ${isPast ? 'opacity-70' : ''}`}>
      {/* colour bar accent */}
      <div className={`h-1 w-full ${isPast ? 'bg-slate-200' : days !== null && days <= 7 ? 'bg-orange-400' : 'bg-primary-500'}`} />

      <div className="p-5 flex-1 flex flex-col gap-4">
        {/* header */}
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0">
            <h3 className="font-display text-base text-slate-800 truncate">{event.eventName}</h3>
            <div className="flex items-center gap-1.5 mt-1 text-xs text-slate-400">
              <svg className="w-3.5 h-3.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span className="truncate">{event.venue || 'No venue'}</span>
            </div>
          </div>
          {countdownPill(days)}
        </div>

        {/* date row */}
        <div className="flex items-center gap-1.5 text-xs font-medium text-slate-600">
          <svg className="w-3.5 h-3.5 text-primary-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          {fmtDate(event.eventDate)}
        </div>

        {/* task progress */}
        <div>
          <div className="flex items-center justify-between mb-1.5">
            <span className="text-xs text-slate-500 font-medium">Task Progress</span>
            <span className="text-xs font-semibold text-slate-700">{done}/{total} done</span>
          </div>
          <div className="h-2 rounded-full bg-slate-100 overflow-hidden">
            <div
              className={`h-full rounded-full transition-all duration-500 ${pct === 100 ? 'bg-emerald-500' : pct > 50 ? 'bg-blue-500' : 'bg-amber-400'}`}
              style={{ width: `${pct}%` }}
            />
          </div>
          <p className="text-xs text-slate-400 mt-1">{pct}% complete</p>
        </div>

        {/* budget summary */}
        {evBudget ? (
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <span className="text-xs text-slate-500 font-medium">Budget Used</span>
              <span className="text-xs font-semibold text-slate-700">
                ₹{Number(spent).toLocaleString()} / ₹{Number(evBudget.totalBudget).toLocaleString()}
              </span>
            </div>
            <div className="h-2 rounded-full bg-slate-100 overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-500 ${budgetPct >= 90 ? 'bg-red-500' : budgetPct >= 70 ? 'bg-orange-400' : 'bg-primary-500'}`}
                style={{ width: `${Math.min(budgetPct, 100)}%` }}
              />
            </div>
            <p className="text-xs text-slate-400 mt-1">
              ₹{Number(evBudget.remainingBudget).toLocaleString()} remaining
            </p>
          </div>
        ) : (
          <p className="text-xs text-slate-400 italic">No budget set</p>
        )}
      </div>

      {/* action footer */}
      <div className="border-t border-slate-100 px-5 py-3 flex items-center justify-between gap-2 bg-slate-50/60">
        <button
          onClick={() => onViewTimeline(event)}
          className="text-xs font-medium text-primary-600 hover:text-primary-800 flex items-center gap-1"
        >
          <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          Timeline
        </button>
        <div className="flex gap-1.5">
          <button
            onClick={() => onEdit(event)}
            className="px-3 py-1.5 text-xs rounded-lg bg-primary-50 text-primary-700 hover:bg-primary-100 font-medium"
          >
            Edit
          </button>
          <button
            onClick={() => onDelete(event)}
            className="px-3 py-1.5 text-xs rounded-lg bg-red-50 text-red-600 hover:bg-red-100 font-medium"
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}

/* ─── Timeline Modal ─── */
function TimelineModal({ event, tasks, isOpen, onClose }) {
  if (!event) return null;
  const eventId = event.id || event.eventId;
  const eventTasks = tasks
    .filter((t) => String(t.eventId) === String(eventId))
    .sort((a, b) => {
      const order = { PENDING: 0, IN_PROGRESS: 1, DONE: 2, CANCELLED: 3 };
      return (order[a.status] ?? 0) - (order[b.status] ?? 0);
    });

  const total = eventTasks.length;
  const done  = eventTasks.filter((t) => t.status === 'DONE').length;

  const STATUS_OPTIONS = ['PENDING', 'IN_PROGRESS', 'DONE', 'CANCELLED'];

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Timeline — ${event.eventName}`} size="lg">
      {/* mini summary bar */}
      <div className="flex items-center gap-4 p-4 bg-slate-50 rounded-xl mb-6 text-sm">
        <div className="flex items-center gap-1.5 text-slate-600">
          <svg className="w-4 h-4 text-primary-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <span className="font-medium">{fmtDate(event.eventDate)}</span>
        </div>
        <div className="flex items-center gap-1.5 text-slate-600">
          <svg className="w-4 h-4 text-primary-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
          </svg>
          <span className="font-medium">{event.venue || '—'}</span>
        </div>
        <div className="ml-auto text-xs font-semibold text-slate-700 bg-white border border-slate-200 px-3 py-1.5 rounded-lg">
          {done}/{total} tasks done
        </div>
      </div>

      {/* status legend */}
      <div className="flex flex-wrap gap-3 mb-5">
        {STATUS_OPTIONS.map((s) => {
          const cfg = statusConfig[s] || statusConfig.PENDING;
          const count = eventTasks.filter((t) => (t.status || 'PENDING') === s).length;
          return (
            <div key={s} className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium ${cfg.bg} ${cfg.text}`}>
              <span className={`w-2 h-2 rounded-full ${cfg.dot}`} />
              {cfg.label} <span className="opacity-60 ml-0.5">({count})</span>
            </div>
          );
        })}
      </div>

      {/* timeline */}
      {eventTasks.length === 0 ? (
        <div className="text-center py-12 text-slate-400">
          <span className="text-3xl block mb-2">📋</span>
          <p className="text-sm">No tasks for this event yet.</p>
        </div>
      ) : (
        <div className="relative">
          {/* vertical line */}
          <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-slate-200" />

          <ul className="space-y-4 pl-12">
            {eventTasks.map((task, i) => {
              const s   = task.status || 'PENDING';
              const cfg = statusConfig[s] || statusConfig.PENDING;
              return (
                <li key={task.id || task.taskId || i} className="relative">
                  {/* dot */}
                  <span className={`absolute -left-8 top-2 w-4 h-4 rounded-full border-2 border-white ${cfg.dot} shadow-sm`} />
                  <div className={`rounded-xl border border-slate-200 p-4 ${cfg.bg}`}>
                    <div className="flex items-start justify-between gap-2">
                      <div className="min-w-0">
                        <p className={`text-sm font-semibold ${cfg.text}`}>{task.taskName}</p>
                        {task.description && (
                          <p className="text-xs text-slate-500 mt-0.5 leading-relaxed">{task.description}</p>
                        )}
                      </div>
                      <span className={`shrink-0 text-xs font-medium px-2.5 py-1 rounded-full ${cfg.bg} ${cfg.text} border border-current border-opacity-20`}>
                        {cfg.label}
                      </span>
                    </div>
                  </div>
                </li>
              );
            })}
          </ul>
        </div>
      )}
    </Modal>
  );
}

/* ─── Main EventsSection ─── */
export default function EventsSection() {
  const [events,  setEvents]  = useState([]);
  const [tasks,   setTasks]   = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [loading, setLoading] = useState(true);

  const [modalOpen,   setModalOpen]   = useState(false);
  const [editItem,    setEditItem]    = useState(null);
  const [form,        setForm]        = useState({ eventName: '', eventDate: '', venue: '' });
  const [saving,      setSaving]      = useState(false);
  const [error,       setError]       = useState('');

  const [timelineEvent, setTimelineEvent] = useState(null);
  const [view, setView]  = useState('grid'); // 'grid' | 'list'
  const [search, setSearch] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [eRes, tRes, bRes] = await Promise.all([
        api.get('/events'),
        api.get('/tasks'),
        api.get('/budgets'),
      ]);
      setEvents(extractItems(eRes.data));
      setTasks(extractItems(tRes.data));
      setBudgets(extractItems(bRes.data));
    } catch { setEvents([]); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const openCreate = () => {
    setEditItem(null);
    setForm({ eventName: '', eventDate: '', venue: '' });
    setError('');
    setModalOpen(true);
  };

  const openEdit = (item) => {
    setEditItem(item);
    setForm({ eventName: item.eventName, eventDate: item.eventDate?.slice(0, 10) || '', venue: item.venue });
    setError('');
    setModalOpen(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      if (editItem) await api.put(`/events/${editItem.id || editItem.eventId}`, form);
      else          await api.post('/events', form);
      setModalOpen(false);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save event.');
    } finally { setSaving(false); }
  };

  const handleDelete = async (item) => {
    if (!window.confirm(`Delete "${item.eventName}"?`)) return;
    try { await api.delete(`/events/${item.id || item.eventId}`); load(); } catch {}
  };

  const filtered = events.filter((e) =>
    e.eventName?.toLowerCase().includes(search.toLowerCase()) ||
    e.venue?.toLowerCase().includes(search.toLowerCase())
  );

  const upcoming = filtered.filter((e) => !e.eventDate || new Date(e.eventDate) >= new Date());
  const past     = filtered.filter((e) => e.eventDate && new Date(e.eventDate) < new Date());

  if (loading) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-48 bg-slate-200 rounded-lg animate-pulse" />
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1,2,3].map((i) => (
            <div key={i} className="bg-white rounded-2xl border border-slate-200 p-5 space-y-4 animate-pulse">
              <div className="h-5 bg-slate-200 rounded w-3/4" />
              <div className="h-3 bg-slate-100 rounded w-1/2" />
              <div className="h-2 bg-slate-100 rounded" />
              <div className="h-2 bg-slate-100 rounded w-5/6" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  const renderCards = (list, emptyMsg) =>
    list.length === 0 ? (
      <div className="col-span-full text-center py-16 text-slate-400">
        <span className="text-4xl block mb-3">📭</span>
        <p className="text-sm">{emptyMsg}</p>
      </div>
    ) : (
      list.map((ev) => (
        <EventCard
          key={ev.id || ev.eventId}
          event={ev}
          tasks={tasks}
          budget={budgets}
          onEdit={openEdit}
          onDelete={handleDelete}
          onViewTimeline={setTimelineEvent}
        />
      ))
    );

  const renderList = (list, emptyMsg) =>
    list.length === 0 ? (
      <div className="text-center py-8 text-slate-400 text-sm">{emptyMsg}</div>
    ) : (
      <div className="divide-y divide-slate-100">
        {list.map((ev) => {
          const eventId    = ev.id || ev.eventId;
          const evTasks    = tasks.filter((t) => String(t.eventId) === String(eventId));
          const done       = evTasks.filter((t) => t.status === 'DONE').length;
          const pct        = evTasks.length > 0 ? Math.round((done / evTasks.length) * 100) : 0;
          const days       = daysUntil(ev.eventDate);
          return (
            <div key={eventId} className="flex items-center gap-4 py-3 px-1 hover:bg-slate-50 rounded-lg transition-colors">
              <div className="w-2 h-2 rounded-full shrink-0 bg-primary-400" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-slate-800 truncate">{ev.eventName}</p>
                <p className="text-xs text-slate-400">{ev.venue || '—'} · {fmtDate(ev.eventDate)}</p>
              </div>
              <div className="hidden sm:flex items-center gap-2 w-32">
                <div className="flex-1 h-1.5 rounded-full bg-slate-100 overflow-hidden">
                  <div className="h-full rounded-full bg-primary-500" style={{ width: `${pct}%` }} />
                </div>
                <span className="text-xs text-slate-500 w-8 text-right">{pct}%</span>
              </div>
              <div className="shrink-0">{countdownPill(days)}</div>
              <div className="flex items-center gap-1.5 shrink-0">
                <button onClick={() => setTimelineEvent(ev)} className="text-xs text-primary-600 hover:text-primary-800 font-medium">Timeline</button>
                <button onClick={() => openEdit(ev)} className="px-2.5 py-1 text-xs rounded-lg bg-primary-50 text-primary-700 hover:bg-primary-100 font-medium">Edit</button>
                <button onClick={() => handleDelete(ev)} className="px-2.5 py-1 text-xs rounded-lg bg-red-50 text-red-600 hover:bg-red-100 font-medium">Del</button>
              </div>
            </div>
          );
        })}
      </div>
    );

  return (
    <>
      {/* page header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
        <div>
          <h2 className="font-display text-2xl text-slate-800">Events</h2>
          <p className="text-sm text-slate-400 mt-0.5">{events.length} total · {upcoming.length} upcoming</p>
        </div>
        <div className="flex items-center gap-2 flex-wrap">
          {/* search */}
          <div className="relative">
            <svg className="absolute left-2.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              className="pl-8 pr-3 py-2 text-sm border border-slate-200 rounded-lg bg-white w-44 focus:outline-none focus:ring-2 focus:ring-primary-500"
              placeholder="Search events..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          {/* view toggle */}
          <div className="flex items-center border border-slate-200 rounded-lg overflow-hidden bg-white">
            <button
              onClick={() => setView('grid')}
              className={`px-3 py-2 text-xs font-medium transition-colors ${view === 'grid' ? 'bg-primary-600 text-white' : 'text-slate-500 hover:bg-slate-50'}`}
            >
              ⊞ Grid
            </button>
            <button
              onClick={() => setView('list')}
              className={`px-3 py-2 text-xs font-medium transition-colors ${view === 'list' ? 'bg-primary-600 text-white' : 'text-slate-500 hover:bg-slate-50'}`}
            >
              ≡ List
            </button>
          </div>
          <button className="btn-primary" onClick={openCreate}>+ New Event</button>
        </div>
      </div>

      {/* upcoming */}
      <section className="mb-8">
        <h3 className="text-xs font-bold uppercase tracking-widest text-slate-400 mb-3">
          Upcoming ({upcoming.length})
        </h3>
        {view === 'grid' ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {renderCards(upcoming, 'No upcoming events. Create your first event.')}
          </div>
        ) : (
          <div className="bg-white rounded-xl border border-slate-200 p-4">
            {renderList(upcoming, 'No upcoming events.')}
          </div>
        )}
      </section>

      {/* past */}
      {past.length > 0 && (
        <section>
          <h3 className="text-xs font-bold uppercase tracking-widest text-slate-400 mb-3">
            Past Events ({past.length})
          </h3>
          {view === 'grid' ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {renderCards(past, '')}
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-slate-200 p-4">
              {renderList(past, '')}
            </div>
          )}
        </section>
      )}

      {/* create / edit modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editItem ? 'Edit Event' : 'Create Event'}>
        <form onSubmit={handleSave} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Event Name</label>
            <input className="input-field" value={form.eventName} onChange={(e) => setForm({ ...form, eventName: e.target.value })} required />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Date</label>
            <input type="date" className="input-field" value={form.eventDate} onChange={(e) => setForm({ ...form, eventDate: e.target.value })} required />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Venue</label>
            <input className="input-field" value={form.venue} onChange={(e) => setForm({ ...form, venue: e.target.value })} required />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={saving} className="btn-primary flex-1">
              {saving ? 'Saving...' : editItem ? 'Update Event' : 'Create Event'}
            </button>
            <button type="button" onClick={() => setModalOpen(false)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </form>
      </Modal>

      {/* timeline modal */}
      <TimelineModal
        event={timelineEvent}
        tasks={tasks}
        isOpen={!!timelineEvent}
        onClose={() => setTimelineEvent(null)}
      />
    </>
  );
}
