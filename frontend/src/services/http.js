import axios from 'axios';
import { getAuth, clearAuth } from './storage.js';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8091',
});

api.interceptors.request.use((config) => {
  const auth = getAuth();
  if (auth?.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      clearAuth();
    }
    return Promise.reject(error);
  },
);

export default api;
