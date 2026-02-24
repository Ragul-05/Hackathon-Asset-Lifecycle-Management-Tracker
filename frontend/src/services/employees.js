import api from './http.js';

export async function fetchEmployees() {
  const { data } = await api.get('/api/admin/employees');
  return data;
}

export async function createEmployee(payload) {
  const { data } = await api.post('/api/admin/employees', payload);
  return data;
}

export async function updateEmployeeRole(id, role) {
  const { data } = await api.put(`/api/admin/employees/${id}/role`, { role });
  return data;
}

export async function resetEmployeePassword(id, newPassword) {
  await api.put(`/api/admin/employees/${id}/reset-password`, { newPassword });
}
