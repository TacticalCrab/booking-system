import { request } from './client'; import type { User } from './types';
export interface LoginResponse { accessToken: string; refreshToken: string; user: User }
export const login = (email: string, password: string) => request<LoginResponse>('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });
export const register = (email: string, password: string, name: string) => request<User>('/api/auth/register', { method: 'POST', body: JSON.stringify({ email, password, name }) });
export const refresh = (refreshToken: string) => request<{ accessToken: string; refreshToken: string }>('/api/auth/refresh', { method: 'POST', body: JSON.stringify({ refreshToken }) });
export const logout = (refreshToken: string) => request<void>('/api/auth/logout', { method: 'POST', body: JSON.stringify({ refreshToken }) }); export const me = () => request<User>('/api/users/me');
