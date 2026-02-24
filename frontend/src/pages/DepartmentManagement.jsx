import { useEffect, useMemo, useState } from 'react';
import AdminNav from '../components/AdminNav.jsx';
import { createDepartment, deleteDepartment, fetchDepartments, updateDepartment } from '../services/departments.js';

function DepartmentManagement() {
  const [departments, setDepartments] = useState([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const pageSize = 8;

  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ name: '', code: '', description: '' });

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await fetchDepartments();
        setDepartments(data);
      } catch (e) {
        const msg = e?.response?.data?.message || 'Failed to load departments';
        setError(msg);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const filtered = useMemo(() => {
    return departments.filter((d) => {
      const term = search.toLowerCase();
      return d.name.toLowerCase().includes(term) || d.code.toLowerCase().includes(term);
    });
  }, [departments, search]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const start = (page - 1) * pageSize;
  const current = filtered.slice(start, start + pageSize);

  const openAdd = () => {
    setEditing(null);
    setForm({ name: '', code: '', description: '' });
    setModalOpen(true);
  };

  const openEdit = (row) => {
    setEditing(row);
    setForm({ name: row.name, code: row.code, description: row.description || '' });
    setModalOpen(true);
  };

  const save = async () => {
    if (!form.name || !form.code) return;
    setSaving(true);
    try {
      if (editing) {
        const updated = await updateDepartment(editing.id, form);
        setDepartments((prev) => prev.map((d) => (d.id === editing.id ? updated : d)));
      } else {
        const created = await createDepartment(form);
        setDepartments((prev) => [created, ...prev]);
      }
      setModalOpen(false);
    } catch (e) {
      alert(e?.response?.data?.message || 'Save failed');
    } finally {
      setSaving(false);
    }
  };

  const remove = async (id) => {
    const ok = window.confirm('Delete this department?');
    if (!ok) return;
    try {
      await deleteDepartment(id);
      setDepartments((prev) => prev.filter((d) => d.id !== id));
    } catch (e) {
      alert(e?.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <div className="page-shell">
      <div className="page">
        <AdminNav />
        <div className="page-header">
          <div className="page-title">
            <span className="icon-circle">D</span>
            Department Management
          </div>
          <div className="page-actions">
            <button className="btn btn-teal" onClick={openAdd}>Add Department</button>
          </div>
        </div>

        <div className="card-surface table-toolbar subtle-hover">
          <div className="filters">
            <input
              className="search-input"
              placeholder="Search name or code"
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(1); }}
            />
          </div>

          <div className="card-surface" style={{ padding: 0, marginTop: 12 }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Code</th>
                  <th>Description</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {current.map((row) => (
                  <tr key={row.id}>
                    <td>{row.name}</td>
                    <td>{row.code}</td>
                    <td>{row.description || '—'}</td>
                    <td>{row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '—'}</td>
                    <td>
                      <div className="table-actions">
                        <button className="btn btn-blue" onClick={() => openEdit(row)}>Edit</button>
                        <button className="btn btn-red" onClick={() => remove(row.id)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {current.length === 0 && (
                  <tr><td colSpan="5" style={{ textAlign: 'center', padding: '18px' }}>{loading ? 'Loading…' : 'No departments found'}</td></tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="pagination">
            <button disabled={page === 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>Prev</button>
            <span className="current">Page {page} of {totalPages}</span>
            <button disabled={page === totalPages} onClick={() => setPage((p) => Math.min(totalPages, p + 1))}>Next</button>
          </div>
        </div>
      </div>

      {modalOpen && (
        <div className="modal-backdrop">
          <div className="modal">
            <div className="modal-header">
              <h3>{editing ? 'Edit Department' : 'Add Department'}</h3>
              <button className="btn btn-ghost" onClick={() => setModalOpen(false)}>Close</button>
            </div>
            <div className="form-grid-2">
              <div className="form-group">
                <label>Name</label>
                <input className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Code</label>
                <input className="input" value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Description</label>
                <input className="input" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
              </div>
            </div>
            <div className="modal-actions">
              <button className="btn btn-gray" onClick={() => setModalOpen(false)}>Cancel</button>
              <button className="btn btn-teal" disabled={saving} onClick={save}>{saving ? 'Saving…' : 'Save'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default DepartmentManagement;