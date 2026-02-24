const KEY = 'inventory_auth';

export function setAuth(token, role, remember = true) {
  const normalizedRole = (role || '').toUpperCase();
  const payload = { token, role: normalizedRole };
  const target = remember ? localStorage : sessionStorage;
  target.setItem(KEY, JSON.stringify(payload));
  const other = remember ? sessionStorage : localStorage;
  other.removeItem(KEY);
}

export function getAuth() {
  const raw = localStorage.getItem(KEY) || sessionStorage.getItem(KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (e) {
    return null;
  }
}

export function clearAuth() {
  localStorage.removeItem(KEY);
  sessionStorage.removeItem(KEY);
}
