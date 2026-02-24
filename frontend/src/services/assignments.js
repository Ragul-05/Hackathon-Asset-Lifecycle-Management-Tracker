import api from './http.js';

export async function fetchAssignments(params = {}) {
  const { data } = await api.get('/api/admin/assignments', { params });
  return data;
}

export async function assignAsset(payload) {
  const { data } = await api.post('/api/admin/assignments', payload);
  return data;
}

export async function reassignAsset(id, payload) {
  const { data } = await api.put(`/api/admin/assignments/${id}/reassign`, payload);
  return data;
}

export async function returnAssignment(id) {
  const { data } = await api.put(`/api/admin/assignments/${id}/return`);
  return data;
}
