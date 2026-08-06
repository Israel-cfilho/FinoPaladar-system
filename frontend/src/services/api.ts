import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('finopaladar_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ??
      'Ocorreu um erro inesperado. Tente novamente.';
    return Promise.reject({
      message,
      status: error.response?.status ?? 500,
    });
  },
);
