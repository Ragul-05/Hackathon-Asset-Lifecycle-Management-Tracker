import api from './http.js';

export async function fetchInventoryReport() {
  const { data } = await api.get('/api/admin/reports/inventory');
  return data;
}

export async function fetchMaintenanceCostReport() {
  const { data } = await api.get('/api/admin/reports/maintenance-cost');
  return data;
}

export async function fetchDepreciationReport() {
  const { data } = await api.get('/api/admin/reports/depreciation');
  return data;
}

export async function fetchAiRecommendations(useCase) {
  const { data } = await api.get('/api/admin/reports/ai-recommendations', {
    params: useCase ? { useCase } : undefined,
  });
  return data;
}
