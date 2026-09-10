import { request } from "./client";
import type { Employee, Page } from "./types";
export const employees = () => request<Page<Employee>>("/api/employees");
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
