import api from './http.js';

export async function fetchMaintenance(params = {}) {
  const { data } = await api.get('/api/admin/maintenance', { params });
  return data;
}

export async function createMaintenance(payload) {
  const { data } = await api.post('/api/admin/maintenance', payload);
  return data;
}

export async function updateMaintenance(id, payload) {
  const { data } = await api.put(`/api/admin/maintenance/${id}`, payload);
  return data;
}

export async function deleteMaintenance(id) {
  await api.delete(`/api/admin/maintenance/${id}`);
}
