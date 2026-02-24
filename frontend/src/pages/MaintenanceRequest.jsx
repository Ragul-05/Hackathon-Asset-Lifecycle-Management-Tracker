import { useEffect, useMemo, useState } from 'react';
import { fetchMyAssignments, fetchEmployeeMaintenance, requestMaintenance } from '../services/employee.js';
import EmployeeNav from '../components/EmployeeNav.jsx';

const priorities = ['Low', 'Medium', 'High'];

function MaintenanceRequest() {
  const [assignments, setAssignments] = useState([]);
  const [selectedAsset, setSelectedAsset] = useState('');
  const [issue, setIssue] = useState('');
  const [priority, setPriority] = useState('Medium');
  const [scheduledFor, setScheduledFor] = useState('');
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [history, setHistory] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(false);

  const loadAssignments = async () => {
    try {
      const data = await fetchMyAssignments();
      const list = Array.isArray(data) ? data : data?.content || [];
      setAssignments(list);
      if (!selectedAsset && list.length > 0) {
        setSelectedAsset(String(list[0].assetId));
      }
    } catch (err) {
      const message = err?.response?.data?.message || 'Unable to load your assets right now.';
      setError(message);
    }
  };

  const loadHistory = async (assetId) => {
    if (!assetId) return;
    setLoadingHistory(true);
    setError('');
    try {
      const data = await fetchEmployeeMaintenance(assetId);
      setHistory(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      const message = err?.response?.data?.message || 'Unable to load maintenance history.';
      setError(message);
    } finally {
      setLoadingHistory(false);
    }
  };

  useEffect(() => {
    loadAssignments();
  }, []);

  useEffect(() => {
    if (selectedAsset) {
      loadHistory(selectedAsset);
    }
  }, [selectedAsset]);

  const submit = async (e) => {
    e.preventDefault();
    if (!selectedAsset || !issue.trim()) {
      setError('Select an asset and describe the issue.');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      await requestMaintenance({
        assetId: selectedAsset,
        type: issue.trim(),
        scheduledFor: scheduledFor || null,
        notes: `Priority: ${priority}\n${notes || ''}`.trim(),
      });
      setIssue('');
      setNotes('');
      setScheduledFor('');
      await loadHistory(selectedAsset);
    } catch (err) {
      const message = err?.response?.data?.message || 'Could not submit maintenance request.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  const selectedAssetName = useMemo(
    () => assignments.find((a) => String(a.assetId) === String(selectedAsset))?.assetName || 'Asset',
    [assignments, selectedAsset],
  );

  return (
    <div className="page-shell">
      <div className="page">
        <EmployeeNav />
        <div className="page-header">
          <div className="page-title">
            <span className="icon-circle">M</span>
            Maintenance Request
          </div>
          <div className="page-actions">
            <button className="btn" onClick={loadAssignments} disabled={submitting}>Refresh Assets</button>
          </div>
        </div>

        {error && <div className="alert error">{error}</div>}

        <div className="grid two-columns">
          <div className="card-surface">
            <div className="card-header">
              <div>
                <p className="eyebrow">New Request</p>
                <h3>Report an issue</h3>
              </div>
            </div>
            <form className="form-grid-2" onSubmit={submit}>
              <label className="form-group">
                <span>Asset</span>
                <select className="select" value={selectedAsset} onChange={(e) => setSelectedAsset(e.target.value)}>
                  <option value="">Select assigned asset</option>
                  {assignments.map((a) => (
                    <option key={a.id} value={String(a.assetId)}>{a.assetName}</option>
                  ))}
                </select>
              </label>
              <label className="form-group">
                <span>Priority</span>
                <select className="select" value={priority} onChange={(e) => setPriority(e.target.value)}>
                  {priorities.map((p) => <option key={p}>{p}</option>)}
                </select>
              </label>
              <label className="form-group" style={{ gridColumn: '1 / -1' }}>
                <span>Issue Description</span>
                <textarea className="input" rows="3" placeholder="Describe the problem" value={issue} onChange={(e) => setIssue(e.target.value)} />
              </label>
              <label className="form-group">
                <span>Preferred Date</span>
                <input className="input" type="date" value={scheduledFor} onChange={(e) => setScheduledFor(e.target.value)} />
              </label>
              <label className="form-group">
                <span>Additional Notes</span>
                <textarea className="input" rows="2" placeholder="Optional details" value={notes} onChange={(e) => setNotes(e.target.value)} />
              </label>
              <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
                <button type="submit" className="btn btn-teal" disabled={submitting}>
                  {submitting ? 'Submitting…' : 'Submit Request'}
                </button>
              </div>
            </form>
          </div>

          <div className="card-surface">
            <div className="card-header">
              <div>
                <p className="eyebrow">Request History</p>
                <h3>{selectedAssetName}</h3>
              </div>
            </div>
            {loadingHistory ? (
              <p className="muted">Loading history…</p>
            ) : history.length === 0 ? (
              <p className="muted">No maintenance requests yet.</p>
            ) : (
              <div className="table">
                <div className="table-head">
                  <div>Date</div>
                  <div>Status</div>
                  <div>Notes</div>
                </div>
                <div className="table-body">
                  {history.map((row) => (
                    <div className="table-row" key={row.id}>
                      <div>{row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '—'}</div>
                      <div><span className="badge subtle">{row.status}</span></div>
                      <div className="muted">{row.notes || '—'}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default MaintenanceRequest;
