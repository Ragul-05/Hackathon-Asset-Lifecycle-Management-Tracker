import http from './http.js';

export const fetchMyAssignments = async () => {
  const { data } = await http.get('/api/employee/assets/assigned');
  return data;
};

export const fetchEmployeeAsset = async (assetId) => {
  const { data } = await http.get(`/api/employee/assets/${assetId}`);
  return data;
};

export const fetchEmployeeMaintenance = async (assetId, status) => {
  const { data } = await http.get('/api/employee/maintenance', { params: { assetId, status } });
  return data;
};

export const requestMaintenance = async (payload) => {
  const { data } = await http.post('/api/employee/maintenance', payload);
  return data;
};
