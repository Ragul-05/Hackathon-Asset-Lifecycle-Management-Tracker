import { useEffect, useMemo, useState } from 'react';
import AdminNav from '../components/AdminNav.jsx';
import { createAsset, deleteAsset, fetchAssets, updateAsset } from '../services/assets.js';

const statusClass = {
  AVAILABLE: 'badge-green',
  ASSIGNED: 'badge-blue',
  UNDER_MAINTENANCE: 'badge-orange',
  RETIRED: 'badge-gray',
};

function AssetManagement() {
  const [assets, setAssets] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [locationFilter, setLocationFilter] = useState('');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({
    name: '', category: '', serialNumber: '', status: 'AVAILABLE', purchaseCost: '', purchaseDate: '', vendor: '', location: '',
    usefulLifeMonths: '', salvageValue: '', notes: '',
  });

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const data = await fetchAssets();
        setAssets(data);
      } catch (e) {
        alert(e?.response?.data?.message || 'Failed to load assets');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const categories = useMemo(() => Array.from(new Set(assets.map((a) => a.category).filter(Boolean))).sort(), [assets]);
  const locations = useMemo(() => Array.from(new Set(assets.map((a) => a.location).filter(Boolean))).sort(), [assets]);

  const filtered = useMemo(() => assets.filter((a) => {
    const matchesStatus = !statusFilter || a.status === statusFilter;
    const matchesCategory = !categoryFilter || a.category === categoryFilter;
    const matchesLocation = !locationFilter || a.location === locationFilter;
    const matchesSearch = [a.name, a.serialNumber, a.category].some((field) => (field || '').toLowerCase().includes(search.toLowerCase()));
    return matchesStatus && matchesCategory && matchesLocation && matchesSearch;
  }), [assets, statusFilter, categoryFilter, locationFilter, search]);

  const openAdd = () => {
    setEditing(null);
    setForm({
      name: '', category: '', serialNumber: '', status: 'AVAILABLE', purchaseCost: '', purchaseDate: '', vendor: '', location: '',
      usefulLifeMonths: '', salvageValue: '', notes: '',
    });
    setModalOpen(true);
  };

  const openEdit = (row) => {
    setEditing(row);
    setForm({
      name: row.name || '',
      category: row.category || '',
      serialNumber: row.serialNumber || '',
      status: row.status || 'AVAILABLE',
      purchaseCost: row.purchaseCost || '',
      purchaseDate: row.purchaseDate || '',
      vendor: row.vendor || '',
      location: row.location || '',
      usefulLifeMonths: row.usefulLifeMonths || '',
      salvageValue: row.salvageValue || '',
      notes: row.notes || '',
    });
    setModalOpen(true);
  };

  const save = async () => {
    if (!form.name || !form.category || !form.serialNumber || !form.status) return;
    setSaving(true);
    try {
      if (editing) {
        const updated = await updateAsset(editing.id, {
          ...form,
          purchaseCost: form.purchaseCost ? Number(form.purchaseCost) : null,
          salvageValue: form.salvageValue ? Number(form.salvageValue) : null,
          usefulLifeMonths: form.usefulLifeMonths ? Number(form.usefulLifeMonths) : null,
        });
        setAssets((prev) => prev.map((a) => (a.id === editing.id ? updated : a)));
      } else {
        const created = await createAsset({
          ...form,
          purchaseCost: form.purchaseCost ? Number(form.purchaseCost) : null,
          salvageValue: form.salvageValue ? Number(form.salvageValue) : null,
          usefulLifeMonths: form.usefulLifeMonths ? Number(form.usefulLifeMonths) : null,
        });
        setAssets((prev) => [created, ...prev]);
      }
      setModalOpen(false);
    } catch (e) {
      alert(e?.response?.data?.message || 'Save failed');
    } finally {
      setSaving(false);
    }
  };

  const remove = async (id) => {
    const ok = window.confirm('Delete this asset?');
    if (!ok) return;
    try {
      await deleteAsset(id);
      setAssets((prev) => prev.filter((a) => a.id !== id));
    } catch (e) {
      alert(e?.response?.data?.message || 'Delete failed');
    }
  };

  const formatCurrency = (value) => Number(value || 0).toLocaleString(undefined, { style: 'currency', currency: 'USD', maximumFractionDigits: 0 });

  return (
    <div className="page-shell">
      <div className="page">
        <AdminNav />
        <div className="page-header">
          <div className="page-title">
            <span className="icon-circle">A</span>
            Asset Management
          </div>
          <div className="page-actions">
            <button className="btn btn-teal" onClick={openAdd}>Add Asset</button>
          </div>
        </div>

        <div className="card-surface subtle-hover">
          <div className="filters">
            <select className="select" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="">All statuses</option>
              <option value="AVAILABLE">Available</option>
              <option value="ASSIGNED">Assigned</option>
              <option value="UNDER_MAINTENANCE">Under Maintenance</option>
              <option value="RETIRED">Retired</option>
            </select>
            <select className="select" value={categoryFilter} onChange={(e) => setCategoryFilter(e.target.value)}>
              <option value="">All categories</option>
              {categories.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
            <select className="select" value={locationFilter} onChange={(e) => setLocationFilter(e.target.value)}>
              <option value="">All locations</option>
              {locations.map((loc) => <option key={loc} value={loc}>{loc}</option>)}
            </select>
            <input
              className="search-input"
              placeholder="Search assets"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div className="card-surface" style={{ padding: 0, marginTop: 12 }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Asset Name</th>
                  <th>Category</th>
                  <th>Serial</th>
                  <th>Status</th>
                  <th>Purchase Cost</th>
                  <th>Purchase Date</th>
                  <th>Vendor</th>
                  <th>Location</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((row) => (
                  <tr key={row.id}>
                    <td>{row.name}</td>
                    <td>{row.category}</td>
                    <td>{row.serialNumber}</td>
                    <td><span className={`badge ${statusClass[row.status] || 'badge-gray'}`}><span className="status-dot" style={{ background: 'currentColor' }} />{row.status}</span></td>
                    <td>{formatCurrency(row.purchaseCost)}</td>
                    <td>{row.purchaseDate || '—'}</td>
                    <td>{row.vendor || '—'}</td>
                    <td>{row.location || '—'}</td>
                    <td>
                      <div className="table-actions">
                        <button className="btn btn-blue" onClick={() => openEdit(row)}>Edit</button>
                        <button className="btn btn-red" onClick={() => remove(row.id)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {filtered.length === 0 && (
                  <tr><td colSpan="8" style={{ textAlign: 'center', padding: '18px' }}>{loading ? 'Loading…' : 'No assets match the filters'}</td></tr>
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
              <h3>{editing ? 'Edit Asset' : 'Add Asset'}</h3>
              <button className="btn btn-ghost" onClick={() => setModalOpen(false)}>Close</button>
            </div>
            <div className="form-grid-3">
              <div className="form-group">
                <label>Asset Name</label>
                <input className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Category</label>
                <input className="input" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Serial Number</label>
                <input className="input" value={form.serialNumber} onChange={(e) => setForm({ ...form, serialNumber: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Status</label>
                <select className="select" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                  <option value="AVAILABLE">Available</option>
                  <option value="ASSIGNED">Assigned</option>
                  <option value="UNDER_MAINTENANCE">Under Maintenance</option>
                  <option value="RETIRED">Retired</option>
                </select>
              </div>
              <div className="form-group">
                <label>Purchase Cost</label>
                <input className="input" type="number" value={form.purchaseCost} onChange={(e) => setForm({ ...form, purchaseCost: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Purchase Date</label>
                <input className="input" type="date" value={form.purchaseDate} onChange={(e) => setForm({ ...form, purchaseDate: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Vendor</label>
                <input className="input" value={form.vendor} onChange={(e) => setForm({ ...form, vendor: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Location</label>
                <input className="input" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Useful Life (months)</label>
                <input className="input" type="number" value={form.usefulLifeMonths} onChange={(e) => setForm({ ...form, usefulLifeMonths: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Salvage Value</label>
                <input className="input" type="number" value={form.salvageValue} onChange={(e) => setForm({ ...form, salvageValue: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Notes</label>
                <input className="input" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
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

export default AssetManagement;