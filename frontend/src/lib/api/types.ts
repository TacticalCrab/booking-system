export type Role = "CUSTOMER" | "ADMIN";
export type BookingStatus = "CONFIRMED" | "CANCELLED" | "COMPLETED";
export interface User {
  id: number;
  email: string;
  name: string;
  role: Role;
}
export interface Service {
  id: number;
  name: string;
  description: string;
  durationMinutes: number;
  price: number;
}
export interface Employee {
  id: number;
  name: string;
  email: string;
  services: Service[];
}
export interface Booking {
  id: number;
  user: User;
  employee: Employee;
  service: Service;
  startTime: string;
  endTime: string;
  status: BookingStatus;
  createdAt: string;
  updatedAt: string;
}
export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}
export interface ApiErrorBody {
  status?: number;
  message?: string;
  timestamp?: string;
}
