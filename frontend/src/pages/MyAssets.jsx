import { useEffect, useState } from 'react';
import { fetchMyAssignments } from '../services/employee.js';
import EmployeeNav from '../components/EmployeeNav.jsx';

const columns = [
  { key: 'assetName', label: 'Asset Name' },
  { key: 'status', label: 'Status' },
  { key: 'currentValue', label: 'Current Value' },
  { key: 'risk', label: 'Risk Level' },
];

const formatCurrency = (value) => {
  if (value == null) return '—';
  const num = Number(value);
  if (Number.isNaN(num)) return '—';
  return num.toLocaleString(undefined, { style: 'currency', currency: 'USD', maximumFractionDigits: 0 });
};

const inferRisk = (assignment) => {
  if (!assignment) return 'Low';
  if (assignment.status === 'OVERDUE') return 'High';
  if (assignment.dueBackAt) return 'Medium';
  return 'Low';
};

function MyAssets() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchMyAssignments();
      const list = Array.isArray(data) ? data : data?.content || [];
      setRows(list);
    } catch (err) {
      const message = err?.response?.data?.message || 'Unable to load assets right now.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <div className="page-shell">
      <div className="page">
        <EmployeeNav />
        <div className="page-header">
          <div className="page-title">
            <span className="icon-circle">A</span>
            My Assets
          </div>
          <div className="page-actions">
            <button className="btn" onClick={load} disabled={loading}>{loading ? 'Refreshing…' : 'Refresh'}</button>
          </div>
        </div>

        {error && <div className="alert error">{error}</div>}

        <div className="card-surface">
          <table className="data-table">
            <thead>
              <tr>
                {columns.map((c) => (
                  <th key={c.key}>{c.label}</th>
                ))}
                <th>View</th>
              </tr>
            </thead>
            <tbody>
              {loading && (
                <tr><td colSpan={columns.length + 1} style={{ textAlign: 'center' }}>Loading…</td></tr>
              )}
              {!loading && rows.length === 0 && (
                <tr><td colSpan={columns.length + 1} style={{ textAlign: 'center' }}>No assets assigned yet</td></tr>
              )}
              {rows.map((row) => {
                const risk = inferRisk(row);
                return (
                  <tr key={row.id}>
                    <td>{row.assetName || 'Asset'}</td>
                    <td><span className="badge subtle">{row.status || '—'}</span></td>
                    <td>{formatCurrency(row.currentValue)}</td>
                    <td><span className={`badge ${risk === 'High' ? 'danger' : risk === 'Medium' ? 'amber' : 'success'}`}>{risk}</span></td>
                    <td>
                      <button className="btn btn-ghost" disabled title="Coming soon">View</button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default MyAssets;
