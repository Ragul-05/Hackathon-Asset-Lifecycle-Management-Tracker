import { useEffect, useState } from 'react';
import AdminNav from '../components/AdminNav.jsx';
import { assignAsset, fetchAssignments, returnAssignment } from '../services/assignments.js';
import { fetchAssets } from '../services/assets.js';
import { fetchEmployees } from '../services/employees.js';

const statusBadge = {
  ASSIGNED: 'badge-blue',
  RETURNED: 'badge-green',
};

const sortAssignments = (list) => [...list].sort((a, b) => new Date(b.assignedAt || 0) - new Date(a.assignedAt || 0));

function AssetAssignment() {
  const [assignments, setAssignments] = useState([]);
  const [assets, setAssets] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [returningId, setReturningId] = useState(null);

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState({ assetId: '', employeeId: '', dueBackAt: '', notes: '' });

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [assignmentData, assetData, employeeData] = await Promise.all([
          fetchAssignments(),
          fetchAssets({ status: 'AVAILABLE' }),
          fetchEmployees(),
        ]);
        setAssignments(sortAssignments(assignmentData));
        setAssets(assetData);
        setEmployees(employeeData.filter((e) => e.role === 'EMPLOYEE'));
      } catch (e) {
        alert(e?.response?.data?.message || 'Failed to load assignments');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const assign = async () => {
    if (!form.assetId || !form.employeeId) return;
    setSaving(true);
    try {
      const created = await assignAsset({
        assetId: form.assetId,
        employeeId: form.employeeId,
        dueBackAt: form.dueBackAt || null,
        notes: form.notes || null,
      });
      setAssignments((prev) => sortAssignments([created, ...prev]));
      setAssets((prev) => prev.filter((a) => a.id !== form.assetId));
      setModalOpen(false);
      setForm({ assetId: '', employeeId: '', dueBackAt: '', notes: '' });
    } catch (e) {
      alert(e?.response?.data?.message || 'Failed to assign asset');
    } finally {
      setSaving(false);
    }
  };

  const markReturned = async (id) => {
    setReturningId(id);
    try {
      const updated = await returnAssignment(id);
      setAssignments((prev) => prev.map((row) => (row.id === id ? updated : row)));
      const availableAssets = await fetchAssets({ status: 'AVAILABLE' });
      setAssets(availableAssets);
    } catch (e) {
      alert(e?.response?.data?.message || 'Failed to mark returned');
    } finally {
      setReturningId(null);
    }
  };

  const formatDate = (value) => (value ? new Date(value).toLocaleDateString() : '—');

  return (
    <div className="page-shell">
      <div className="page">
        <AdminNav />
        <div className="page-header">
          <div className="page-title">
            <span className="icon-circle">AS</span>
            Asset Assignment
          </div>
          <div className="page-actions">
            <button className="btn btn-teal" onClick={() => setModalOpen(true)}>Assign Asset</button>
          </div>
        </div>

        <div className="card-surface subtle-hover">
          <div className="table-toolbar" style={{ marginBottom: 10 }}>
            <div>
              <div className="page-title" style={{ fontSize: 16, gap: 8 }}>
                <span className="icon-circle" style={{ width: 28, height: 28, fontSize: 12 }}>F</span>
                Assignment Form
              </div>
              <p style={{ margin: 4, color: '#6b7280', fontSize: 14 }}>Select asset and employee to create an assignment.</p>
            </div>
            <button className="btn btn-teal" onClick={() => setModalOpen(true)}>Open Form</button>
          </div>

          <div className="card-surface" style={{ padding: 0, marginTop: 12 }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Employee</th>
                  <th>Assigned Date</th>
                  <th>Returned Date</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {assignments.map((row) => (
                  <tr key={row.id}>
                    <td>{row.assetName}</td>
                    <td>{row.employeeName}</td>
                    <td>{formatDate(row.assignedAt)}</td>
                    <td>{formatDate(row.returnedAt)}</td>
                    <td><span className={`badge ${statusBadge[row.status] || 'badge-gray'}`}>{row.status}</span></td>
                    <td>
                      <div className="table-actions">
                        {row.status === 'ASSIGNED' && (
                          <button className="btn btn-blue" disabled={returningId === row.id} onClick={() => markReturned(row.id)}>
                            {returningId === row.id ? 'Returning…' : 'Return'}
                          </button>
                        )}
                        {row.status === 'RETURNED' && <span className="table-note">Completed</span>}
                      </div>
                    </td>
                  </tr>
                ))}
                {assignments.length === 0 && (
                  <tr><td colSpan="6" style={{ textAlign: 'center', padding: '18px' }}>{loading ? 'Loading…' : 'No assignments yet'}</td></tr>
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
              <h3>Assign Asset</h3>
              <button className="btn btn-ghost" onClick={() => setModalOpen(false)}>Close</button>
            </div>
            <div className="form-grid-2">
              <div className="form-group">
                <label>Select Asset</label>
                <select className="select" value={form.assetId} onChange={(e) => setForm({ ...form, assetId: e.target.value })}>
                  <option value="">Choose asset</option>
                  {assets.map((a) => <option key={a.id} value={a.id}>{a.name} {a.serialNumber ? `(${a.serialNumber})` : ''}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Select Employee</label>
                <select className="select" value={form.employeeId} onChange={(e) => setForm({ ...form, employeeId: e.target.value })}>
                  <option value="">Choose employee</option>
                  {employees.map((e) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
                </select>
              </div>
            </div>
            <div className="form-grid-2">
              <div className="form-group">
                <label>Due Back (optional)</label>
                <input className="input" type="date" value={form.dueBackAt} onChange={(e) => setForm({ ...form, dueBackAt: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Notes (optional)</label>
                <input className="input" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
              </div>
            </div>
            <div className="modal-actions">
              <button className="btn btn-gray" onClick={() => setModalOpen(false)}>Cancel</button>
              <button className="btn btn-teal" disabled={saving} onClick={assign}>{saving ? 'Saving…' : 'Assign'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AssetAssignment;