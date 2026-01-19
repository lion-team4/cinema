import axios, { type InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from './store';
import { authTokenStorage } from './auth';
import { type ApiResponse, type TokenResponse } from '@/types';

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || '';

type RetriableRequestConfig = InternalAxiosRequestConfig & { _retry?: boolean };

export const publicApi = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor
api.interceptors.request.use((config) => {
  const token =
    useAuthStore.getState().accessToken ?? authTokenStorage.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response Interceptor
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (!axios.isAxiosError(error)) {
      return Promise.reject(error);
    }

    const originalRequest = error.config as RetriableRequestConfig | undefined;
    if (!originalRequest) {
      return Promise.reject(error);
    }

    const requestUrl = originalRequest.url ?? '';
    const isAuthRequest =
      requestUrl.includes('/auth/login') ||
      requestUrl.includes('/auth/signup') ||
      requestUrl.includes('/auth/reissue');
    if (isAuthRequest) {
      return Promise.reject(error);
    }

    // If error is 401 and we haven't retried yet
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const state = useAuthStore.getState();
        const refreshToken =
          state.refreshToken ?? authTokenStorage.getRefreshToken();
        const currentAccessToken =
          state.accessToken ?? authTokenStorage.getAccessToken();
        if (!refreshToken) throw new Error('No refresh token');

        const { data } = await axios.post<ApiResponse<TokenResponse>>(
          `${BASE_URL}/auth/reissue`,
          {
            accessToken: currentAccessToken ?? undefined,
            refreshToken,
          }
        );

        const { accessToken: newAccessToken, refreshToken: newRefresh } =
          data.data;
        useAuthStore.getState().setAuth(newAccessToken, newRefresh);

        // Update current request
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        useAuthStore.getState().logout();
        // Redirect if on client side
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
