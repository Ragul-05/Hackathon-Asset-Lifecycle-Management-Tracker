import { useEffect, useMemo, useState } from 'react';
import AdminNav from '../components/AdminNav.jsx';
import { fetchDepartments } from '../services/departments.js';
import { createEmployee, fetchEmployees, resetEmployeePassword, updateEmployeeRole } from '../services/employees.js';

const roleBadge = {
  ADMIN: 'badge-blue',
  EMPLOYEE: 'badge-teal',
};

function EmployeeManagement() {
  const [employees, setEmployees] = useState([]);
  const [roleFilter, setRoleFilter] = useState('');
  const [deptFilter, setDeptFilter] = useState('');
  const [search, setSearch] = useState('');

  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'EMPLOYEE', departmentId: '' });

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [emps, deps] = await Promise.all([fetchEmployees(), fetchDepartments()]);
        setEmployees(emps);
        setDepartments(deps);
      } catch (e) {
        alert(e?.response?.data?.message || 'Failed to load employees');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const filtered = useMemo(() => {
    return employees.filter((e) => {
      const matchesRole = !roleFilter || e.role === roleFilter;
      const matchesDept = !deptFilter || e.departmentId === deptFilter;
      const matchesSearch = [e.fullName, e.email].some((field) => (field || '').toLowerCase().includes(search.toLowerCase()));
      return matchesRole && matchesDept && matchesSearch;
    });
  }, [employees, roleFilter, deptFilter, search]);

  const openAdd = () => {
    setEditing(null);
    setForm({ name: '', email: '', password: '', role: 'EMPLOYEE', departmentId: '' });
    setModalOpen(true);
  };

  const openEdit = (row) => {
    setEditing(row);
    setForm({
      name: row.fullName,
      email: row.email,
      password: '',
      role: row.role,
      departmentId: row.departmentId || '',
    });
    setModalOpen(true);
  };

  const save = async () => {
    const isNew = !editing;
    if (!form.name || !form.email || !form.role || !form.departmentId) return;
    if (isNew && !form.password) return;
    setSaving(true);
    try {
      if (!isNew) {
        if (form.role !== editing.role) {
          const updated = await updateEmployeeRole(editing.id, form.role);
          setEmployees((prev) => prev.map((e) => (e.id === editing.id ? updated : e)));
        }
        if (form.password) {
          await resetEmployeePassword(editing.id, form.password);
        }
      } else {
        const created = await createEmployee({
          fullName: form.name,
          email: form.email,
          password: form.password,
          departmentId: form.departmentId,
        });
        let newEmployee = created;
        if (form.role && form.role !== created.role) {
          newEmployee = await updateEmployeeRole(created.id, form.role);
        }
        setEmployees((prev) => [newEmployee, ...prev]);
      }
      setModalOpen(false);
    } catch (e) {
      alert(e?.response?.data?.message || 'Save failed');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page-shell">
      <div className="page">
        <AdminNav />
        <div className="page-header">
          <div className="page-title">
            <span className="icon-circle">E</span>
            Employee Management
          </div>
          <div className="page-actions">
            <button className="btn btn-teal" onClick={openAdd}>Add Employee</button>
          </div>
        </div>

        <div className="card-surface subtle-hover">
          <div className="filters">
            <select className="select" value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
              <option value="">All roles</option>
              <option value="ADMIN">Admin</option>
              <option value="EMPLOYEE">Employee</option>
            </select>
            <select className="select" value={deptFilter} onChange={(e) => setDeptFilter(e.target.value)}>
              <option value="">All departments</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
            <input
              className="search-input"
              placeholder="Search by name or email"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div className="card-surface" style={{ padding: 0, marginTop: 12 }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Employee Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Department</th>
                  <th>Created Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((row) => (
                  <tr key={row.id}>
                    <td>{row.fullName}</td>
                    <td>{row.email}</td>
                    <td><span className={`badge ${roleBadge[row.role]}`}>{row.role}</span></td>
                    <td>{row.departmentName || '—'}</td>
                    <td>{row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '—'}</td>
                    <td>
                      <div className="table-actions">
                        <button className="btn btn-blue" onClick={() => openEdit(row)}>Edit</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {filtered.length === 0 && (
                  <tr><td colSpan="6" style={{ textAlign: 'center', padding: '18px' }}>{loading ? 'Loading…' : 'No employees found'}</td></tr>
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
              <h3>{editing ? 'Edit Employee' : 'Add Employee'}</h3>
              <button className="btn btn-ghost" onClick={() => setModalOpen(false)}>Close</button>
            </div>
            <div className="form-grid-2">
              <div className="form-group">
                <label>Name</label>
                <input className="input" value={form.name} disabled={!!editing} onChange={(e) => setForm({ ...form, name: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input className="input" type="email" value={form.email} disabled={!!editing} onChange={(e) => setForm({ ...form, email: e.target.value })} />
              </div>
              <div className="form-group">
                <label>{editing ? 'Reset Password (optional)' : 'Password'}</label>
                <input className="input" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Role</label>
                <select className="select" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                  <option value="ADMIN">Admin</option>
                  <option value="EMPLOYEE">Employee</option>
                </select>
              </div>
              <div className="form-group">
                <label>Department</label>
                <select className="select" value={form.departmentId} onChange={(e) => setForm({ ...form, departmentId: e.target.value })}>
                  <option value="">Select department</option>
                  {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
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

export default EmployeeManagement;