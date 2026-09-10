import { request } from "./client";
import type { Booking, BookingStatus, Page } from "./types";
export const createBooking = (
  employeeId: number,
  serviceId: number,
  startTime: string,
) =>
  request<Booking>("/api/bookings", {
    method: "POST",
    body: JSON.stringify({ employeeId, serviceId, startTime }),
  });
export const myBookings = () => request<Page<Booking>>("/api/bookings/me");
export const allBookings = () => request<Page<Booking>>("/api/bookings");
export const cancelBooking = (id: number) =>
  request<Booking>(`/api/bookings/${id}/cancel`, { method: "PATCH" });
export const updateBookingStatus = (id: number, status: BookingStatus) =>
  request<Booking>(`/api/bookings/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status }),
  });
export const deleteBooking = (id: number) =>
  request<void>(`/api/bookings/${id}`, { method: "DELETE" });
