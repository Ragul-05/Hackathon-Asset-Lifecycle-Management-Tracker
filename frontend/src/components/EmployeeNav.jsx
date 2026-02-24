import { Link, useLocation, useNavigate } from 'react-router-dom';
import { clearAuth } from '../services/storage.js';

const navItems = [
  { label: 'Dashboard', to: '/employee/dashboard' },
  { label: 'My Assets', to: '/employee/assets' },
  { label: 'Maintenance', to: '/employee/maintenance' },
];

function EmployeeNav() {
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

export default EmployeeNav;
