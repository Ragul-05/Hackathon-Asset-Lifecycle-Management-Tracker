import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register as registerApi } from '../services/auth.js';
import { fetchDepartments } from '../services/departments.js';
import { setAuth } from '../services/storage.js';

const ROLE_OPTIONS = [
  { value: 'ADMIN', label: 'Admin' },
  { value: 'EMPLOYEE', label: 'Employee' },
];

function strengthLabel(score) {
  if (score >= 4) return 'Strong';
  if (score >= 3) return 'Good';
  if (score >= 2) return 'Fair';
  return 'Weak';
}

function passwordScore(pw) {
  let score = 0;
  if (!pw) return 0;
  if (pw.length >= 8) score += 1;
  if (/[A-Z]/.test(pw)) score += 1;
  if (/[a-z]/.test(pw)) score += 1;
  if (/[0-9]/.test(pw)) score += 1;
  if (/[^A-Za-z0-9]/.test(pw)) score += 1;
  return Math.min(score, 4);
}

function RegisterPage() {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState('EMPLOYEE');
  const [departmentId, setDepartmentId] = useState('');
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDepartments()
      .then((data) => setDepartments(data))
      .catch(() => setDepartments([]));
  }, []);

  const pwScore = useMemo(() => passwordScore(password), [password]);
  const pwLabel = useMemo(() => strengthLabel(pwScore), [pwScore]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    setLoading(true);
    try {
      const payload = {
        fullName,
        email,
        password,
        role,
        departmentId: departmentId || null,
      };
      const data = await registerApi(payload);
      setAuth(data.token, data.role, true);
      navigate(role === 'ADMIN' ? '/admin/dashboard' : '/employee/dashboard', { replace: true });
    } catch (err) {
      const message = err?.response?.data?.message || 'Registration failed. Please check the details.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-shell">
      <div className="login-card single">
        <div className="login-form">
          <div className="auth-header">
            <h2>Create your account</h2>
            <p>Join the AI-powered Asset Lifecycle Management System.</p>
          </div>

          <form className="auth-form" onSubmit={handleSubmit}>
            <label>
              <span>Full Name</span>
              <input
                type="text"
                name="fullName"
                autoComplete="name"
                placeholder="Jane Doe"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
            </label>

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

            <div className="two-col">
              <label>
                <span>Password</span>
                <input
                  type="password"
                  name="password"
                  autoComplete="new-password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </label>
              <label>
                <span>Confirm Password</span>
                <input
                  type="password"
                  name="confirmPassword"
                  autoComplete="new-password"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                />
              </label>
            </div>

            <div className="pw-strength">
              <div className={`bar score-${pwScore}`} />
              <span>{pwLabel}</span>
            </div>

            <div className="two-col">
              <label>
                <span>Role</span>
                <select value={role} onChange={(e) => setRole(e.target.value)}>
                  {ROLE_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>Department</span>
                <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
                  <option value="">Select department</option>
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            {error && <div className="auth-error">{error}</div>}

            <button type="submit" disabled={loading} className="primary-btn teal">
              {loading ? <span className="spinner" /> : 'Register'}
            </button>
          </form>

          <div className="auth-footer">
            <span>Back to</span>
            <Link className="link-primary" to="/login">
              Login
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default RegisterPage;
