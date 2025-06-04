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
    chargerSpeeds: number[];
    carrierNetwork: string;
    averageRating: number;
}

export interface Booking {
    id: number;
    stationId: number;
    userId: number;
    startTime: string;
    endTime: string;
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