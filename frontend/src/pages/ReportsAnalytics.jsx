import { useMemo, useState } from 'react';
import AdminNav from '../components/AdminNav.jsx';

const tabDefs = [
  { key: 'inventory', label: 'Inventory Report' },
  { key: 'maintenance', label: 'Maintenance Report' },
  { key: 'depreciation', label: 'Depreciation Summary' },
  { key: 'ai', label: 'AI Recommendations' },
];

const sampleData = {
  inventory: {
    cards: [
      { title: 'Total Assets', value: '142' },
      { title: 'Active', value: '118' },
      { title: 'Under Maintenance', value: '12' },
      { title: 'Retired', value: '12' },
    ],
    table: [
      { name: 'Forklift A12', type: 'Equipment', status: 'Active', value: '$32,000' },
      { name: 'Server Rack 4', type: 'IT Hardware', status: 'Under Maintenance', value: '$5,800' },
      { name: 'Pickup Truck', type: 'Vehicle', status: 'Active', value: '$18,500' },
    ],
  },
  maintenance: {
    cards: [
      { title: 'Mtd Spend', value: '$4,200' },
      { title: 'Upcoming Jobs', value: '7' },
      { title: 'Overdue', value: '2' },
      { title: 'Vendors', value: '5' },
    ],
    table: [
      { asset: 'Forklift A12', type: 'Preventive', cost: '$620', due: '2024-04-02' },
      { asset: 'Server Rack 4', type: 'Repair', cost: '$1,100', due: '2024-05-20' },
    ],
  },
  depreciation: {
    cards: [
      { title: 'Total Book Value', value: '$1.2M' },
      { title: 'Avg Useful Life', value: '5.8 yrs' },
      { title: 'Accum. Depreciation', value: '$480k' },
      { title: 'Monthly Depreciation', value: '$22k' },
    ],
    table: [
      { asset: '3D Printer', method: 'Straight-line', monthly: '$240', remaining: '$1,200' },
      { asset: 'Laptop Elite', method: 'Straight-line', monthly: '$85', remaining: '$1,530' },
    ],
  },
  ai: {
    cards: [
      { title: 'AI Insights', value: '6' },
      { title: 'High-Risk Assets', value: '3' },
      { title: 'Optimization Ideas', value: '4' },
      { title: 'Adoption', value: '76%' },
    ],
    table: [
      { asset: 'Server Rack 4', insight: 'Predicted fan failure within 30 days' },
      { asset: 'Forklift A12', insight: 'Increase maintenance interval to 45 days' },
    ],
  },
};

function ReportsAnalytics() {
  const [tab, setTab] = useState('inventory');
  const data = useMemo(() => sampleData[tab], [tab]);

  return (
    <div className="page-shell">
      <div className="page">
        <AdminNav />
        <div className="page-header">
          <div className="page-title">
            <span className="icon-circle">R</span>
            Reports & Analytics
          </div>
          <div className="page-actions">
            <button className="btn btn-ghost">Export</button>
          </div>
        </div>

        <div className="card-surface subtle-hover">
          <div className="tablist">
            {tabDefs.map((t) => (
              <button key={t.key} className={`tab ${tab === t.key ? 'active' : ''}`} onClick={() => setTab(t.key)}>
                {t.label}
              </button>
            ))}
          </div>

          <div style={{ padding: 12 }}>
            <div className="summary-cards">
              {data.cards.map((c) => (
                <div key={c.title} className="summary-card">
                  <h4>{c.title}</h4>
                  <div className="value">{c.value}</div>
                </div>
              ))}
            </div>

            <div className="card-surface" style={{ marginTop: 14 }}>
              <div className="card-header">
                <h3>Trend</h3>
                <button className="btn btn-gray">Export</button>
              </div>
              <div className="chart-placeholder" />
            </div>

            <div className="card-surface" style={{ marginTop: 14, padding: 0 }}>
              {tab === 'inventory' && (
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Asset</th><th>Type</th><th>Status</th><th>Value</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.table.map((row, idx) => (
                      <tr key={idx}>
                        <td>{row.name}</td>
                        <td>{row.type}</td>
                        <td>{row.status}</td>
                        <td>{row.value}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              {tab === 'maintenance' && (
                <table className="data-table">
                  <thead>
                    <tr><th>Asset</th><th>Type</th><th>Cost</th><th>Next Due</th></tr>
                  </thead>
                  <tbody>
                    {data.table.map((row, idx) => (
                      <tr key={idx}>
                        <td>{row.asset}</td>
                        <td>{row.type}</td>
                        <td>{row.cost}</td>
                        <td>{row.due}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              {tab === 'depreciation' && (
                <table className="data-table">
                  <thead>
                    <tr><th>Asset</th><th>Method</th><th>Monthly Depreciation</th><th>Remaining Value</th></tr>
                  </thead>
                  <tbody>
                    {data.table.map((row, idx) => (
                      <tr key={idx}>
                        <td>{row.asset}</td>
                        <td>{row.method}</td>
                        <td>{row.monthly}</td>
                        <td>{row.remaining}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              {tab === 'ai' && (
                <table className="data-table">
                  <thead>
                    <tr><th>Asset</th><th>Insight</th></tr>
                  </thead>
                  <tbody>
                    {data.table.map((row, idx) => (
                      <tr key={idx}>
                        <td>{row.asset}</td>
                        <td>{row.insight}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ReportsAnalytics;