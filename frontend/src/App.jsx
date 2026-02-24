import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { clearAuth } from './services/storage.js';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import AdminDashboard from './pages/AdminDashboard.jsx';
import DepartmentManagement from './pages/DepartmentManagement.jsx';
import EmployeeManagement from './pages/EmployeeManagement.jsx';
import AssetManagement from './pages/AssetManagement.jsx';
import AssetAssignment from './pages/AssetAssignment.jsx';
import MaintenanceManagement from './pages/MaintenanceManagement.jsx';
import ReportsAnalytics from './pages/ReportsAnalytics.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import EmployeeDashboard from './pages/EmployeeDashboard.jsx';
import MyAssets from './pages/MyAssets.jsx';
import MaintenanceRequest from './pages/MaintenanceRequest.jsx';

const Placeholder = ({ title }) => {
  const navigate = useNavigate();
  const handleLogout = () => {
    clearAuth();
    navigate('/login', { replace: true });
  };
  return (
    <div className="page-shell">
      <div className="page">
        <div className="page-header">
          <div className="page-title">
            <span className="icon-circle">U</span>
            {title}
          </div>
          <div className="page-actions">
            <button className="btn btn-ghost" onClick={handleLogout}>Logout</button>
          </div>
        </div>
        <div className="card-surface">
          <p>Page scaffolding is ready. Hook up real content next.</p>
        </div>
      </div>
    </div>
  );
};

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/admin/dashboard"
        element={(
          <ProtectedRoute allowRoles={['ADMIN']}>
            <AdminDashboard />
          </ProtectedRoute>
        )}
      />
      <Route
        path="/admin/departments"
        element={(
          <ProtectedRoute allowRoles={['ADMIN']}>
            <DepartmentManagement />
          </ProtectedRoute>
        )}
      />
      <Route
        path="/admin/employees"
        element={(
          <ProtectedRoute allowRoles={['ADMIN']}>
            <EmployeeManagement />
          </ProtectedRoute>
        )}
      />
      <Route
        path="/admin/assets"
        element={(
          <ProtectedRoute allowRoles={['ADMIN']}>
            <AssetManagement />
          </ProtectedRoute>
        )}
      />
      <Route
        path="/admin/assignments"
        element={(
          <ProtectedRoute allowRoles={['ADMIN']}>
            <AssetAssignment />
          </ProtectedRoute>
        )}
      />
      <Route
        path="/admin/maintenance"
        element={(
          <ProtectedRoute allowRoles={['ADMIN']}>
            <MaintenanceManagement />
          </ProtectedRoute>
        )}
      />
      <Route
        path="/admin/reports"
        element={(
          <ProtectedRoute allowRoles={['ADMIN']}>
            <ReportsAnalytics />
          </ProtectedRoute>
        )}
      />
      <Route
        path="/employee/dashboard"
        element={(
          <ProtectedRoute allowRoles={['EMPLOYEE', 'ADMIN']}>
            <EmployeeDashboard />
          </ProtectedRoute>
        )}
      />
      <Route
        path="/employee/assets"
        element={(
          <ProtectedRoute allowRoles={['EMPLOYEE', 'ADMIN']}>
            <MyAssets />
          </ProtectedRoute>
        )}
      />
      <Route
        path="/employee/maintenance"
        element={(
          <ProtectedRoute allowRoles={['EMPLOYEE', 'ADMIN']}>
            <MaintenanceRequest />
          </ProtectedRoute>
        )}
      />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
