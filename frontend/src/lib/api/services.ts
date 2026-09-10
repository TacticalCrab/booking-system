import { request } from "./client";
import type { Page, Service } from "./types";
export const services = () => request<Page<Service>>("/api/services");
export const createService = (data: Omit<Service, "id">) =>
  request<Service>("/api/services", {
    method: "POST",
    body: JSON.stringify(data),
  });
export const updateService = (id: number, data: Omit<Service, "id">) =>
  request<Service>(`/api/services/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
export const deleteService = (id: number) =>
  request<void>(`/api/services/${id}`, { method: "DELETE" });
