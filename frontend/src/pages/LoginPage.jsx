import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login } from '../services/auth.js';
import { setAuth } from '../services/storage.js';

const ROLE_ROUTES = {
  ADMIN: '/admin/dashboard',
  EMPLOYEE: '/employee/dashboard',
};

function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [remember, setRemember] = useState(true);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await login({ email, password });
      setAuth(data.token, data.role, remember);
      const target = ROLE_ROUTES[data.role] || '/login';
      navigate(target, { replace: true });
    } catch (err) {
      const message = err?.response?.data?.message || 'Login failed. Check your credentials.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-shell">
      <div className="login-card">
        <div className="login-brand">
          <div className="brand-badge">AI</div>
          <h1>Asset Lifecycle Management System</h1>
          <p>AI-Powered Inventory & Maintenance Optimization</p>
          <div className="brand-illustration">
            <div className="orb orb-1" />
            <div className="orb orb-2" />
            <div className="orb orb-3" />
            <div className="device laptop" />
            <div className="device vehicle" />
            <div className="device gear" />
          </div>
        </div>

        <div className="login-form">
          <div className="auth-header">
            <h2>Welcome back</h2>
            <p>Sign in to manage assets, maintenance, and insights.</p>
          </div>

          <form className="auth-form" onSubmit={handleSubmit}>
            <label>
              <span>Email</span>
              <input
                type="email"
                name="email"
                autoComplete="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </label>
            <label>
              <span>Password</span>
              <input
                type="password"
                name="password"
                autoComplete="current-password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </label>

            <div className="auth-row">
              <label className="checkbox">
                <input
                  type="checkbox"
                  checked={remember}
                  onChange={(e) => setRemember(e.target.checked)}
                />
                <span>Remember me</span>
              </label>
              <Link className="link-muted" to="/register">Create account</Link>
            </div>

            {error && <div className="auth-error">{error}</div>}

            <button type="submit" disabled={loading} className="primary-btn">
              {loading ? <span className="spinner" /> : 'Sign In'}
            </button>
          </form>

          <div className="auth-footer">
            <span>Don’t have an account?</span>
            <Link className="link-primary" to="/register">Register</Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
