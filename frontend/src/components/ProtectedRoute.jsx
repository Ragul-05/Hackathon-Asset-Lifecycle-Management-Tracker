import { Navigate } from 'react-router-dom';
import { getAuth } from '../services/storage.js';

function ProtectedRoute({ children, allowRoles }) {
  const auth = getAuth();
  const role = auth?.role?.toUpperCase();

  if (!auth?.token || !role) {
    return <Navigate to="/login" replace />;
  }

  const allowed = allowRoles?.map((r) => r.toUpperCase());
  if (allowed && !allowed.includes(role)) {
    if (role === 'EMPLOYEE') return <Navigate to="/employee/dashboard" replace />;
    if (role === 'ADMIN') return <Navigate to="/admin/dashboard" replace />;
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;
