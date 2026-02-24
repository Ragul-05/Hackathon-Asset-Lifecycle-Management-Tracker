import { Link, useLocation, useNavigate } from 'react-router-dom';
import { clearAuth } from '../services/storage.js';

const navItems = [
  { label: 'Dashboard', to: '/admin/dashboard' },
  { label: 'Departments', to: '/admin/departments' },
  { label: 'Employees', to: '/admin/employees' },
  { label: 'Assets', to: '/admin/assets' },
  { label: 'Assignments', to: '/admin/assignments' },
  { label: 'Maintenance', to: '/admin/maintenance' },
  { label: 'Reports', to: '/admin/reports' },
];

function AdminNav() {
  const { pathname } = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    clearAuth();
    navigate('/login', { replace: true });
  };
  return (
    <div className="admin-nav card-surface subtle-hover">
      <div className="admin-nav-links">
        {navItems.map((item) => {
          const active = pathname.startsWith(item.to);
          return (
            <Link key={item.to} to={item.to} className={`admin-nav-item ${active ? 'active' : ''}`}>
              {item.label}
            </Link>
          );
        })}
      </div>
      <button className="btn btn-ghost" onClick={handleLogout}>Logout</button>
    </div>
  );
}

export default AdminNav;