import axios from 'axios';
import { ChargingStation, Booking, ApiResponse } from '../types/api';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add token to requests if available
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const chargingStationApi = {
    getAll: async (): Promise<ChargingStation[]> => {
        const response = await api.get<ApiResponse<ChargingStation[]>>('/stations');
        return response.data.data;
    },

    getById: async (id: number): Promise<ChargingStation> => {
        const response = await api.get<ApiResponse<ChargingStation>>(`/stations/${id}`);
        return response.data.data;
    },

    getNearby: async (latitude: number, longitude: number, radius: number): Promise<ChargingStation[]> => {
        const response = await api.get<ApiResponse<ChargingStation[]>>('/stations/nearby', {
            params: { latitude, longitude, radius }
        });
        return response.data.data;
    }
};

export const bookingApi = {
    create: async (booking: Omit<Booking, 'id' | 'userId' | 'status' | 'createdAt' | 'updatedAt'>): Promise<Booking> => {
        const response = await api.post<ApiResponse<Booking>>('/bookings', booking);
        return response.data.data;
    },

    getByUser: async (): Promise<Booking[]> => {
        const response = await api.get<ApiResponse<Booking[]>>('/bookings/user');
        return response.data.data;
    },

    cancel: async (id: number): Promise<Booking> => {
        const response = await api.post<ApiResponse<Booking>>(`/bookings/${id}/cancel`);
        return response.data.data;
    }
}; 