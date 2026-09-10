import { request } from "./client";
import type { Employee, EmployeeAvailability, Page } from "./types";
export const employees = () => request<Page<Employee>>("/api/employees");
export const availability = (employeeId: number, serviceId: number, date: string) =>
  request<EmployeeAvailability>(
    `/api/employees/${employeeId}/availability?serviceId=${serviceId}&date=${encodeURIComponent(date)}`,
  );
export const createEmployee = (
  name: string,
  email: string,
  servicesIds: number[],
) =>
  request<Employee>("/api/employees", {
    method: "POST",
    body: JSON.stringify({ name, email, servicesIds }),
  });
export const updateEmployee = (
  id: number,
  name: string,
  email: string,
  servicesIds: number[],
) =>
  request<Employee>(`/api/employees/${id}`, {
    method: "PUT",
    body: JSON.stringify({ name, email, servicesIds }),
  });
