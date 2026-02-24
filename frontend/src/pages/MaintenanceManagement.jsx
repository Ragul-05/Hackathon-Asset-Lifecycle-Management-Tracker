import { useMemo, useState } from 'react';
import AdminNav from '../components/AdminNav.jsx';

const seedRecords = [
  { id: 'm1', asset: 'Forklift A12', date: '2024-03-02', type: 'Preventive', cost: 620, vendor: 'LiftCare', nextDue: '2024-04-02', description: 'Quarterly check' },
  { id: 'm2', asset: 'Server Rack 4', date: '2024-02-20', type: 'Repair', cost: 1100, vendor: 'DataCore', nextDue: '2024-05-20', description: 'Fan replacement' },
  { id: 'm3', asset: 'Pickup Truck', date: '2024-01-15', type: 'Oil Change', cost: 180, vendor: 'AutoHub', nextDue: '2024-03-15', description: 'Oil + filters' },
];

function MaintenanceManagement() {
  const [records, setRecords] = useState(seedRecords);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ asset: '', date: '', type: '', cost: '', vendor: '', nextDue: '', description: '' });

  const assets = useMemo(() => ['Forklift A12', 'Server Rack 4', 'Pickup Truck', 'Laptop Elite'], []);
  const today = useMemo(() => new Date().toISOString().slice(0, 10), []);

  const openAdd = () => {
    setEditing(null);
    setForm({ asset: '', date: today, type: '', cost: '', vendor: '', nextDue: '', description: '' });
    setModalOpen(true);
  };

  const openEdit = (row) => {
    setEditing(row);
    setForm({ ...row });
    setModalOpen(true);
  };

  const save = () => {
    if (!form.asset || !form.date || !form.type || !form.cost) return;
    const next = { ...form, cost: Number(form.cost) };
    if (editing) {
      setRecords((prev) => prev.map((r) => (r.id === editing.id ? { ...r, ...next } : r)));
    } else {
      setRecords((prev) => [...prev, { ...next, id: crypto.randomUUID() }]);
    }
    setModalOpen(false);
  };

  const remove = (id) => setRecords((prev) => prev.filter((r) => r.id !== id));

  const isOverdue = (nextDue) => nextDue && nextDue < today;

  const formatCurrency = (value) => Number(value || 0).toLocaleString(undefined, { style: 'currency', currency: 'USD', maximumFractionDigits: 0 });

  return (
    <div className="page-shell">
      <div className="page">
        <AdminNav />
        <div className="page-header">
          <div className="page-title">
            <span className="icon-circle">M</span>
            Maintenance Management
          </div>
          <div className="page-actions">
            <button className="btn btn-teal" onClick={openAdd}>Add Maintenance Record</button>
          </div>
        </div>

        <div className="card-surface subtle-hover">
          <div className="card-surface" style={{ padding: 0 }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Cost</th>
                  <th>Vendor</th>
                  <th>Next Due</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {records.map((row) => (
                  <tr key={row.id}>
                    <td>{row.asset}</td>
                    <td>{row.date}</td>
                    <td>{row.type}</td>
                    <td style={{ fontWeight: 800 }}>{formatCurrency(row.cost)}</td>
                    <td>{row.vendor}</td>
                    <td style={{ color: isOverdue(row.nextDue) ? '#b91c1c' : undefined, fontWeight: isOverdue(row.nextDue) ? 700 : 500 }}>
                      {row.nextDue || '—'}
                    </td>
                    <td>
                      <div className="table-actions">
                        <button className="btn btn-blue" onClick={() => openEdit(row)}>Edit</button>
                        <button className="btn btn-red" onClick={() => remove(row.id)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {records.length === 0 && (
                  <tr><td colSpan="7" style={{ textAlign: 'center', padding: '18px' }}>No maintenance records yet</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {modalOpen && (
        <div className="modal-backdrop">
          <div className="modal">
            <div className="modal-header">
              <h3>{editing ? 'Edit Maintenance' : 'Add Maintenance Record'}</h3>
              <button className="btn btn-ghost" onClick={() => setModalOpen(false)}>Close</button>
            </div>
            <div className="form-grid-3">
              <div className="form-group">
                <label>Select Asset</label>
                <select className="select" value={form.asset} onChange={(e) => setForm({ ...form, asset: e.target.value })}>
                  <option value="">Choose asset</option>
                  {assets.map((a) => <option key={a} value={a}>{a}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Maintenance Date</label>
                <input className="input" type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Type</label>
                <input className="input" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Cost</label>
                <input className="input" type="number" value={form.cost} onChange={(e) => setForm({ ...form, cost: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Vendor</label>
                <input className="input" value={form.vendor} onChange={(e) => setForm({ ...form, vendor: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Next Due Date</label>
                <input className="input" type="date" value={form.nextDue} onChange={(e) => setForm({ ...form, nextDue: e.target.value })} />
              </div>
              <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                <label>Description</label>
                <textarea className="input" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
              </div>
            </div>
            <div className="modal-actions">
              <button className="btn btn-gray" onClick={() => setModalOpen(false)}>Cancel</button>
              <button className="btn btn-teal" onClick={save}>Save</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MaintenanceManagement;