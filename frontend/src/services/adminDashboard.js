import api from './http.js';

export async function fetchDashboardSummary({ threshold = 1000, windowDays = 30 } = {}) {
  const { data } = await api.get('/api/admin/dashboard/summary', {
    params: {
      highMaintenanceThreshold: threshold,
      maintenanceWindowDays: windowDays,
    },
  });
  return data;
}
