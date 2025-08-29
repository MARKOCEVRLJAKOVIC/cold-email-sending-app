import axios from 'axios';
import { authStore } from '../store/authStore.js';
import page from 'page';

export const BASE_URL = 'http://localhost:8080';

export const http = axios.create({
  baseURL: BASE_URL,
  withCredentials: false, // ❌ više ne šaljemo cookie
});

// Dodaj Authorization header ako postoji token
http.interceptors.request.use((config) => {
  const token = authStore.token;
  if (token) config.headers['Authorization'] = `Bearer ${token}`;
  return config;
});

// Interceptor za 401
http.interceptors.response.use(
  res => res,
  (error) => {
    const status = error?.response?.status;
    if (status === 401) {
      authStore.clearToken();
      page.redirect('/login');
    }
    const data = error?.response?.data || {};
    return Promise.reject({ error: data.error || data.message || 'Request failed' });
  }
);
