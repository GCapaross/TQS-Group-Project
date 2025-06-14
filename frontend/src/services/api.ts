import axios from "axios";
import { ChargingStation, Booking } from "../types/responseTypes";

const BACKEND_HOST = import.meta.env.VITE_BACKEND_HOST ?? "localhost";
const BACKEND_PORT = import.meta.env.VITE_BACKEND_PORT ?? "8080";

const API_BASE_URL = `http://${BACKEND_HOST}:${BACKEND_PORT}/api`;

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

// Add token to requests if available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const chargingStationApi = {
  getAll: async (): Promise<ChargingStation[]> => {
    const response = await api.get<ChargingStation[]>("/charging-stations");
    return response.data;
  },

  getById: async (id: number): Promise<ChargingStation> => {
    const response = await api.get<ChargingStation>(`/charging-stations/${id}`);
    return response.data;
  },

  getNearby: async (
    latitude: number,
    longitude: number,
    radius: number
  ): Promise<ChargingStation[]> => {
    const response = await api.get<ChargingStation[]>(
      "/charging-stations/nearby",
      {
        params: { latitude, longitude, radius },
      }
    );
    return response.data;
  },
};

export const bookingApi = {
  create: async (
    booking: Omit<
      Booking,
      "id" | "userId" | "status" | "createdAt" | "updatedAt"
    >
  ): Promise<Booking> => {
    const response = await api.post<Booking>("/bookings", booking);
    return response.data;
  },

  getByUser: async (): Promise<Booking[]> => {
    const response = await api.get<Booking[]>("/bookings/user");
    return response.data;
  },

  cancel: async (id: number): Promise<Booking> => {
    const response = await api.post<Booking>(`/bookings/${id}/cancel`);
    return response.data;
  },
};

export const companyApi = {
  getNames: async (): Promise<string[]> => {
    const response = await api.get<string[]>("/companies/names", {
      headers: { Accept: "*/*" },
    });
    return response.data;
  },
};
