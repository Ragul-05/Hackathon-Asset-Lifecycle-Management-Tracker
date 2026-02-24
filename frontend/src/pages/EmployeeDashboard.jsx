import { useEffect, useMemo, useState } from 'react';
import { fetchMyAssignments } from '../services/employee.js';
import EmployeeNav from '../components/EmployeeNav.jsx';

const formatDate = (value) => {
	if (!value) return '—';
	const d = new Date(value);
	if (Number.isNaN(d.getTime())) return '—';
	return d.toLocaleDateString();
};

const computeRisk = (assignment) => {
	if (!assignment) return 'Low';
	if (assignment.status === 'OVERDUE') return 'High';
	if (assignment.dueBackAt) {
		const due = new Date(assignment.dueBackAt).getTime();
		const now = Date.now();
		if (Number.isFinite(due) && due - now < 5 * 24 * 60 * 60 * 1000) return 'Medium';
	}
	return 'Low';
};

function EmployeeDashboard() {
	const [assignments, setAssignments] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	const loadAssignments = async () => {
		setLoading(true);
		setError('');
		try {
			const data = await fetchMyAssignments();
			const list = Array.isArray(data) ? data : data?.content || [];
			setAssignments(list);
		} catch (err) {
			const message = err?.response?.data?.message || 'Could not load your assigned assets right now.';
			setError(message);
		} finally {
			setLoading(false);
		}
	};

	useEffect(() => {
		loadAssignments();
	}, []);

	const stats = useMemo(() => {
		const total = assignments.length;
		const active = assignments.filter((a) => a.status === 'ASSIGNED').length;
		const upcoming = assignments.filter((a) => a.dueBackAt).length;
		return [
			{ label: 'Assigned Assets', value: active || total, tone: 'teal' },
			{ label: 'Upcoming Maintenance', value: upcoming, tone: 'blue' },
			{ label: 'Risk Alerts', value: assignments.filter((a) => computeRisk(a) !== 'Low').length, tone: 'amber' },
		];
	}, [assignments]);

	return (
		<div className="page-shell">
			<div className="page">
				<EmployeeNav />
				<div className="page-header">
					<div className="page-title">
						<span className="icon-circle">E</span>
						Employee Dashboard
					</div>
					<div className="page-actions">
						<button type="button" className="btn" onClick={loadAssignments} disabled={loading}>
							{loading ? 'Refreshing…' : 'Refresh'}
						</button>
					</div>
				</div>

				{error && <div className="alert error">{error}</div>}

				<div className="grid three-columns">
					{stats.map((stat) => (
						<div className="card-surface" key={stat.label}>
							<p className="eyebrow">{stat.label}</p>
							<div className="stat-row">
								<div className={`stat-dot ${stat.tone}`} />
								<div className="stat-value">{stat.value}</div>
							</div>
						</div>
					))}
				</div>

				<div className="card-surface">
					<div className="card-header">
						<div>
							<p className="eyebrow">My Assets</p>
							<h3>Assigned items</h3>
						</div>
						<span className="badge">{assignments.length}</span>
					</div>

					{loading ? (
						<p className="muted">Loading your assets…</p>
					) : assignments.length === 0 ? (
						<p className="muted">No assets assigned yet.</p>
					) : (
						<div className="table">
							<div className="table-head">
								<div>Asset</div>
								<div>Status</div>
								<div>Risk</div>
								<div>Assigned</div>
							</div>
							<div className="table-body">
								{assignments.map((assignment) => {
									const name = assignment.assetName || 'Asset';
									const status = assignment.status || '—';
									const assignedDate = assignment.assignedAt || assignment.assignedDate;
									const risk = computeRisk(assignment);
									return (
										<div className="table-row" key={assignment.id || `${name}-${status}`}>
											<div>
												<div className="strong">{name}</div>
												<div className="muted">SN: {assignment.serialNumber || '—'}</div>
											</div>
											<div>
												<span className="badge subtle">{status}</span>
											</div>
											<div>
												<span className={`badge ${risk === 'High' ? 'danger' : risk === 'Medium' ? 'amber' : 'success'}`}>{risk}</span>
											</div>
											<div>{formatDate(assignedDate)}</div>
										</div>
									);
								})}
							</div>
						</div>
					)}
				</div>
			</div>
		</div>
	);
}

export default EmployeeDashboard;
