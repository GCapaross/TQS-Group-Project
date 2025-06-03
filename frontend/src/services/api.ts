import axios from 'axios';
import { ChargingStation, Booking } from '../types/responseTypes';

const API_BASE_URL = 'http://localhost:8080/api';

export const api = axios.create({
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
        const response = await api.get<ChargingStation[]>('/charging-stations');
        return response.data;
    },

    getById: async (id: number): Promise<ChargingStation> => {
        const response = await api.get<ChargingStation>(`/charging-stations/${id}`);
        return response.data;
    },

    getNearby: async (latitude: number, longitude: number, radius: number): Promise<ChargingStation[]> => {
        const response = await api.get<ChargingStation[]>('/charging-stations/nearby', {
            params: { latitude, longitude, radius }
        });
        return response.data;
    },

    startChargingSession: async (stationId: number, reservationId?: number): Promise<void> => {
        await api.post(`/charging-stations/${stationId}/start-charging`, {
            reservationId
        });
    },

    stopChargingSession: async (stationId: number): Promise<void> => {
        await api.post(`/charging-stations/${stationId}/stop-charging`);
    },

    getActiveSession: async (stationId: number): Promise<{
        energySupplied: number;
        cost: number;
        startTime: string;
        isActive: boolean;
    }> => {
        const response = await api.get(`/charging-stations/${stationId}/active-session`);
        return response.data;
    }
};

export const bookingApi = {
    create: async (booking: Omit<Booking, 'id' | 'userId' | 'status' | 'createdAt' | 'updatedAt'>): Promise<Booking> => {
        const response = await api.post<Booking>('/bookings', {
            ...booking,
            startTime: booking.startTime.toISOString(),
            endTime: booking.endTime.toISOString()
        });
        return {
            ...response.data,
            startTime: new Date(response.data.startTime),
            endTime: new Date(response.data.endTime)
        };
    },

    getByUser: async (): Promise<Booking[]> => {
        const response = await api.get<Booking[]>('/bookings/user');
        return response.data.map(booking => ({
            ...booking,
            startTime: new Date(booking.startTime),
            endTime: new Date(booking.endTime)
        }));
    },

    cancel: async (id: number): Promise<Booking> => {
        const response = await api.post<Booking>(`/bookings/${id}/cancel`);
        return {
            ...response.data,
            startTime: new Date(response.data.startTime),
            endTime: new Date(response.data.endTime)
        };
    },

    startLiveSession: async (stationId: number, initialBatteryLevel: number, targetBatteryLevel: number): Promise<Booking> => {
        const response = await api.post<Booking>(`/api/bookings/live/start`, {
            stationId,
            userId: 1, // TODO: Get from auth context
            initialBatteryLevel,
            targetBatteryLevel
        });
        return {
            ...response.data,
            startTime: new Date(response.data.startTime),
            endTime: new Date(response.data.endTime)
        };
    },

    stopLiveSession: async (id: number): Promise<Booking> => {
        const response = await api.post<Booking>(`/bookings/live/${id}/stop`);
        return {
            ...response.data,
            startTime: new Date(response.data.startTime),
            endTime: new Date(response.data.endTime)
        };
    }
}; 