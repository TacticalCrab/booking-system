import { request } from './client'; import type { Page, Role, User } from './types';
export const users = () => request<Page<User>>('/api/users'); export const createUser = (email: string, password: string, name: string, role: Role) => request<User>('/api/users', { method: 'POST', body: JSON.stringify({ email, password, name, role }) });
