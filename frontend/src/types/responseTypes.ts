export interface ChargingStation {
    id: number;
    name: string;
    location: string;
    status: 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'OUT_OF_SERVICE';
    latitude: number;
    longitude: number;
    maxSlots: number;
    availableSlots: number;
    pricePerKwh: number;
    supportedConnectors: string[];
    chargingSpeedKw: number;
    carrierNetwork: string;
    averageRating: number;
    timetable: string;
    company?: {
        id: number;
        name: string;
    };
    workers?: Array<{
        id: number;
        name: string;
    }>;
}

export interface Booking {
    id: number;
    stationId: number;
    userId: number;
    startTime: Date;
    endTime: Date;
    estimatedEnergy: number;
    status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
    createdAt: string;
    updatedAt: string;
}

export interface ApiResponse<T> {
    data: T;
    message?: string;
    error?: string;
} 