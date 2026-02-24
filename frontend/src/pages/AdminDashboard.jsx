import { useEffect, useMemo, useState } from 'react';
import AdminNav from '../components/AdminNav.jsx';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  Cell,
} from 'recharts';
import { format, parseISO } from 'date-fns';
import { fetchDashboardSummary } from '../services/adminDashboard.js';
import { fetchInventoryReport, fetchMaintenanceCostReport, fetchDepreciationReport } from '../services/reports.js';
import { fetchDepartments } from '../services/departments.js';
import { fetchEmployees } from '../services/employees.js';

const palette = {
  primary: '#1E3A8A',
  teal: '#0D9488',
  grayBg: '#F3F4F6',
  text: '#111827',
  success: '#10B981',
  warning: '#F59E0B',
  danger: '#EF4444',
};

const statusColors = {
  AVAILABLE: '#10B981',
  ASSIGNED: '#3B82F6',
  UNDER_MAINTENANCE: '#F59E0B',
  RETIRED: '#9CA3AF',
};

const categoryColors = ['#1E3A8A', '#0D9488', '#3B82F6', '#22D3EE', '#818CF8', '#C084FC'];

function AdminDashboard() {
  const [summary, setSummary] = useState(null);
  const [inventory, setInventory] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [maintenanceCosts, setMaintenanceCosts] = useState([]);
  const [depreciation, setDepreciation] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [windowDays, setWindowDays] = useState(30);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const [sum, inv, deps, emps, maint, deprec] = await Promise.all([
          fetchDashboardSummary({ windowDays }),
          fetchInventoryReport(),
          fetchDepartments(),
          fetchEmployees(),
          fetchMaintenanceCostReport(),
          fetchDepreciationReport(),
        ]);
        setSummary(sum);
        setInventory(inv);
        setDepartments(deps);
        setEmployees(emps);
        setMaintenanceCosts(maint);
        setDepreciation(deprec);
      } catch (e) {
        const message = e?.response?.data?.message || 'Failed to load dashboard data.';
        setError(message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [windowDays]);

  const metrics = useMemo(() => {
    const totalAssets = inventory.length;
    const totalDepartments = departments.length;
    const totalEmployees = employees.length;
    const totalMaintenance = maintenanceCosts.reduce((sum, item) => sum + Number(item.totalCost || 0), 0);
    return [
      {
        title: 'Total Assets',
        value: totalAssets,
        color: '#1E3A8A',
        delta: '+0.0%'
      },
      {
        title: 'Total Departments',
        value: totalDepartments,
        color: '#0D9488',
        delta: '+0.0%'
      },
      {
        title: 'Total Employees',
        value: totalEmployees,
        color: '#3B82F6',
        delta: '+0.0%'
      },
      {
        title: 'Total Maintenance Cost',
        value: totalMaintenance.toLocaleString(undefined, { style: 'currency', currency: 'USD', minimumFractionDigits: 0 }),
        color: '#F59E0B',
        delta: '+0.0%'
      },
    ];
  }, [inventory, departments, employees, maintenanceCosts]);

  const assetsByCategory = useMemo(() => {
    if (!summary?.assetsByCategory) return [];
    return Object.entries(summary.assetsByCategory).map(([name, value], idx) => ({
      name,
      value,
      fill: categoryColors[idx % categoryColors.length],
    }));
  }, [summary]);

  const assetsByStatus = useMemo(() => {
    if (!summary?.assetsByStatus) return [];
    return Object.entries(summary.assetsByStatus).map(([status, count]) => ({
      status,
      count,
      fill: statusColors[status] || palette.primary,
    }));
  }, [summary]);

  const depreciationTrend = useMemo(() => {
    const bucket = new Map();
    depreciation.forEach((asset) => {
      asset.schedule?.forEach((entry) => {
        const key = entry.periodStart;
        const amount = Number(entry.depreciationAmount || 0);
        bucket.set(key, (bucket.get(key) || 0) + amount);
      });
    });
    return Array.from(bucket.entries())
      .sort((a, b) => new Date(a[0]) - new Date(b[0]))
      .map(([date, amount]) => ({ date: format(parseISO(date), 'MMM yyyy'), amount }));
  }, [depreciation]);

  const maintenanceDue = useMemo(() => {
    const list = summary?.maintenanceDue || [];
    if (!search) return list;
    return list.filter((m) => m.assetName?.toLowerCase().includes(search.toLowerCase()) || m.type?.toLowerCase().includes(search.toLowerCase()));
  }, [summary, search]);

  const highMaintenanceAssets = useMemo(() => summary?.highMaintenanceAssets || [], [summary]);
  const aiRisk = useMemo(() => summary?.aiRiskAssets || [], [summary]);

  if (loading) {
    return <div className="placeholder"><p>Loading dashboard…</p></div>;
  }
  if (error) {
    return <div className="placeholder"><p>{error}</p></div>;
  }

  return (
    <div className="page-shell">
      <div className="page">
        <AdminNav />

        <div className="page-header">
          <div className="page-title">
            <span className="icon-circle">D</span>
            Dashboard
          </div>
          <div className="page-actions" style={{ gap: 8 }}>
            <input
              className="search-input"
              placeholder="Search maintenance or assets"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <select className="select" value={windowDays} onChange={(e) => setWindowDays(Number(e.target.value))}>
              <option value={15}>Next 15 days</option>
              <option value={30}>Next 30 days</option>
              <option value={60}>Next 60 days</option>
            </select>
          </div>
        </div>

        <div className="metrics-grid">
          {metrics.map((m) => (
            <div key={m.title} className="card-surface subtle-hover metric-card">
              <div className="metric-top">
                <span className="metric-label">{m.title}</span>
                <span className="metric-dot" style={{ background: m.color }} />
              </div>
              <div className="metric-value">{m.value}</div>
              <div className="metric-delta">{m.delta}</div>
            </div>
          ))}
        </div>

        <div className="charts-grid" style={{ marginTop: 16 }}>
          <div className="card-surface subtle-hover chart-card">
            <div className="card-header">
              <h3>Assets by Category</h3>
            </div>
            <div className="chart-wrap">
              <ResponsiveContainer>
                <PieChart>
                  <Pie data={assetsByCategory} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80} paddingAngle={2} />
                  <Tooltip />
                  <Legend verticalAlign="bottom" height={36} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="card-surface subtle-hover chart-card">
            <div className="card-header">
              <h3>Assets by Status</h3>
            </div>
            <div className="chart-wrap">
              <ResponsiveContainer>
                <BarChart data={assetsByStatus}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="status" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                    {assetsByStatus.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.fill} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="card-surface subtle-hover chart-card">
            <div className="card-header">
              <h3>Depreciation Trend</h3>
            </div>
            <div className="chart-wrap">
              <ResponsiveContainer>
                <LineChart data={depreciationTrend}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Line type="monotone" dataKey="amount" stroke={palette.primary} strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        <div className="tables-grid" style={{ marginTop: 16 }}>
          <div className="card-surface subtle-hover table-card">
            <div className="card-header"><h3>Maintenance Due (Next {windowDays} Days)</h3></div>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Type</th>
                  <th>Scheduled</th>
                </tr>
              </thead>
              <tbody>
                {maintenanceDue.slice(0, 5).map((m) => (
                  <tr key={m.maintenanceId}>
                    <td>{m.assetName}</td>
                    <td>{m.type}</td>
                    <td>{m.scheduledFor}</td>
                  </tr>
                ))}
                {maintenanceDue.length === 0 && (
                  <tr><td colSpan="3" className="empty">No upcoming maintenance</td></tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="card-surface subtle-hover table-card">
            <div className="card-header"><h3>High Maintenance Assets</h3></div>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Total Cost</th>
                </tr>
              </thead>
              <tbody>
                {highMaintenanceAssets.slice(0, 5).map((a) => (
                  <tr key={a.assetId}>
                    <td>{a.assetName}</td>
                    <td>{Number(a.totalCost || 0).toLocaleString()}</td>
                  </tr>
                ))}
                {highMaintenanceAssets.length === 0 && (
                  <tr><td colSpan="2" className="empty">No high-cost assets</td></tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="card-surface subtle-hover table-card">
            <div className="card-header"><h3>AI Risk Alerts</h3></div>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Insight</th>
                  <th>When</th>
                </tr>
              </thead>
              <tbody>
                {aiRisk.slice(0, 5).map((r) => (
                  <tr key={r.insightId}>
                    <td>{r.assetName || '—'}</td>
                    <td>{r.result}</td>
                    <td>{r.generatedAt ? new Date(r.generatedAt).toLocaleDateString() : '—'}</td>
                  </tr>
                ))}
                {aiRisk.length === 0 && (
                  <tr><td colSpan="3" className="empty">No AI risk alerts</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;
