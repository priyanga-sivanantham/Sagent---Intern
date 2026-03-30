import { useState, useEffect } from 'react';
import api from '../../services/api';
import { extractItems } from '../../utils/auth';
import SectionCard from '../SectionCard';
import DataTable from '../DataTable';
import Modal from '../Modal';

export default function BudgetSection() {
  const [budgets, setBudgets] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [budgetModal, setBudgetModal] = useState(false);
  const [expenseModal, setExpenseModal] = useState(false);
  const [editBudget, setEditBudget] = useState(null);
  const [editExpense, setEditExpense] = useState(null);
  const [bForm, setBForm] = useState({ totalBudget: '', remainingBudget: '', eventId: '' });
  const [eForm, setEForm] = useState({ expenseName: '', amount: '', description: '', budgetId: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [bRes, exRes, evRes] = await Promise.all([api.get('/budgets'), api.get('/expenses'), api.get('/events')]);
      setBudgets(extractItems(bRes.data));
      setExpenses(extractItems(exRes.data));
      setEvents(extractItems(evRes.data));
    } catch {}
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const openBudget = (item = null) => {
    setEditBudget(item);
    setBForm(item ? { totalBudget: item.totalBudget, remainingBudget: item.remainingBudget, eventId: item.eventId } : { totalBudget: '', remainingBudget: '', eventId: '' });
    setError(''); setBudgetModal(true);
  };

  const openExpense = (item = null) => {
    setEditExpense(item);
    setEForm(item ? { expenseName: item.expenseName, amount: item.amount, description: item.description, budgetId: item.budgetId } : { expenseName: '', amount: '', description: '', budgetId: '' });
    setError(''); setExpenseModal(true);
  };

  const saveBudget = async (e) => {
    e.preventDefault(); setSaving(true); setError('');
    try {
      if (editBudget) await api.put(`/budgets/${editBudget.id || editBudget.budgetId}`, bForm);
      else await api.post('/budgets', bForm);
      setBudgetModal(false); load();
    } catch (err) { setError(err.response?.data?.message || 'Failed.'); }
    finally { setSaving(false); }
  };

  const saveExpense = async (e) => {
    e.preventDefault(); setSaving(true); setError('');
    try {
      if (editExpense) await api.put(`/expenses/${editExpense.id || editExpense.expenseId}`, eForm);
      else await api.post('/expenses', eForm);
      setExpenseModal(false); load();
    } catch (err) { setError(err.response?.data?.message || 'Failed.'); }
    finally { setSaving(false); }
  };

  const delBudget = async (item) => {
    if (!window.confirm('Delete budget?')) return;
    try { await api.delete(`/budgets/${item.id || item.budgetId}`); load(); } catch {}
  };

  const delExpense = async (item) => {
    if (!window.confirm('Delete expense?')) return;
    try { await api.delete(`/expenses/${item.id || item.expenseId}`); load(); } catch {}
  };

  const getEventName = (id) => events.find((e) => (e.id || e.eventId) === id)?.eventName || id;
  const getBudgetName = (id) => { const b = budgets.find((b) => (b.id || b.budgetId) === id); return b ? `Budget #${b.id || b.budgetId}` : id; };

  const budgetCols = [
    { key: 'eventId', label: 'Event', render: (v) => getEventName(v) },
    { key: 'totalBudget', label: 'Total', render: (v) => `₹${Number(v).toLocaleString()}` },
    { key: 'remainingBudget', label: 'Remaining', render: (v) => `₹${Number(v).toLocaleString()}` },
  ];

  const expenseCols = [
    { key: 'expenseName', label: 'Expense' },
    { key: 'amount', label: 'Amount', render: (v) => `₹${Number(v).toLocaleString()}` },
    { key: 'description', label: 'Description' },
    { key: 'budgetId', label: 'Budget', render: (v) => getBudgetName(v) },
  ];

  return (
    <div className="space-y-6">
      <SectionCard title="Budgets" action={<button className="btn-primary" onClick={() => openBudget()}>+ Add Budget</button>}>
        <DataTable columns={budgetCols} data={budgets} loading={loading} onEdit={openBudget} onDelete={delBudget} emptyMessage="No budgets created yet." />
      </SectionCard>

      <SectionCard title="Expenses" action={<button className="btn-primary" onClick={() => openExpense()}>+ Add Expense</button>}>
        <DataTable columns={expenseCols} data={expenses} loading={loading} onEdit={openExpense} onDelete={delExpense} emptyMessage="No expenses recorded yet." />
      </SectionCard>

      <Modal isOpen={budgetModal} onClose={() => setBudgetModal(false)} title={editBudget ? 'Edit Budget' : 'Add Budget'}>
        <form onSubmit={saveBudget} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Event</label>
            <select className="input-field" value={bForm.eventId} onChange={(e) => setBForm({ ...bForm, eventId: e.target.value })} required>
              <option value="">Select event...</option>
              {events.map((ev) => <option key={ev.id || ev.eventId} value={ev.id || ev.eventId}>{ev.eventName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Total Budget</label>
            <input type="number" className="input-field" value={bForm.totalBudget} onChange={(e) => setBForm({ ...bForm, totalBudget: e.target.value })} required />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Remaining Budget</label>
            <input type="number" className="input-field" value={bForm.remainingBudget} onChange={(e) => setBForm({ ...bForm, remainingBudget: e.target.value })} required />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={saving} className="btn-primary flex-1">{saving ? 'Saving...' : editBudget ? 'Update' : 'Add Budget'}</button>
            <button type="button" onClick={() => setBudgetModal(false)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={expenseModal} onClose={() => setExpenseModal(false)} title={editExpense ? 'Edit Expense' : 'Add Expense'}>
        <form onSubmit={saveExpense} className="space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Expense Name</label>
            <input className="input-field" value={eForm.expenseName} onChange={(e) => setEForm({ ...eForm, expenseName: e.target.value })} required />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Amount</label>
            <input type="number" className="input-field" value={eForm.amount} onChange={(e) => setEForm({ ...eForm, amount: e.target.value })} required />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Description</label>
            <textarea className="input-field" rows={2} value={eForm.description} onChange={(e) => setEForm({ ...eForm, description: e.target.value })} />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5 uppercase tracking-wide">Budget</label>
            <select className="input-field" value={eForm.budgetId} onChange={(e) => setEForm({ ...eForm, budgetId: e.target.value })} required>
              <option value="">Select budget...</option>
              {budgets.map((b) => <option key={b.id || b.budgetId} value={b.id || b.budgetId}>{getEventName(b.eventId)} - Budget #{b.id || b.budgetId}</option>)}
            </select>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={saving} className="btn-primary flex-1">{saving ? 'Saving...' : editExpense ? 'Update' : 'Add Expense'}</button>
            <button type="button" onClick={() => setExpenseModal(false)} className="btn-secondary flex-1">Cancel</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
