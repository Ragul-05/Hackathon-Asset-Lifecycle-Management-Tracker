import api from './http.js';

export async function fetchAssets(params = {}) {
  const { data } = await api.get('/api/assets', { params });
  return data;
}

export async function createAsset(payload) {
  const { data } = await api.post('/api/admin/assets', payload);
  return data;
}

export async function updateAsset(id, payload) {
  const { data } = await api.put(`/api/admin/assets/${id}`, payload);
  return data;
}

export async function deleteAsset(id) {
  await api.delete(`/api/admin/assets/${id}`);
}

export async function updateAssetStatus(id, status) {
  const { data } = await api.put(`/api/admin/assets/${id}/status`, { status });
  return data;
}
