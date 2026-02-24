import api from './http.js';

export async function fetchDepartments() {
  const { data } = await api.get('/api/departments');
  return data;
}

export async function createDepartment(payload) {
  const { data } = await api.post('/api/departments', payload);
  return data;
}

export async function updateDepartment(id, payload) {
  const { data } = await api.put(`/api/departments/${id}`, payload);
  return data;
}

export async function deleteDepartment(id) {
  await api.delete(`/api/departments/${id}`);
}
