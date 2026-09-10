import { browser } from "$app/environment";
import { auth } from "$lib/stores/auth.svelte";
import type { ApiErrorBody } from "./types";
export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}
const API_URL = import.meta.env.VITE_API_URL ?? "";
let refreshInFlight: Promise<boolean> | null = null;

function refreshSession() {
  if (!refreshInFlight)
    refreshInFlight = auth.refresh().finally(() => {
      refreshInFlight = null;
    });
  return refreshInFlight;
}

async function messageFor(response: Response) {
  const fallback = `${response.status} ${response.statusText}`.trim();
  try {
    const body = (await response.json()) as ApiErrorBody;
    return body.message ?? fallback;
  } catch {
    return fallback;
  }
}
export async function request<T>(
  path: string,
  options: RequestInit = {},
  retry = true,
): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body) headers.set("Content-Type", "application/json");
  const accessToken = browser ? auth.accessToken : null;
  if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);
  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, { ...options, headers });
  } catch {
    throw new ApiError(
      0,
      "Could not reach the booking server. Is it running on port 8080?",
    );
  }
  if (response.status === 401 && retry && browser) {
    // Another request may already have rotated the access token while this one
    // was in flight. Retry with that token instead of rotating again.
    if (accessToken !== auth.accessToken && auth.accessToken)
      return request<T>(path, options, false);

    if (await refreshSession()) return request<T>(path, options, false);
  }
  if (!response.ok)
    throw new ApiError(response.status, await messageFor(response));
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}
