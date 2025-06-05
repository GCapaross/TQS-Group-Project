export interface ChargingStation {
    id: number;
    name: string;
    location: string;
    status: 'AVAILABLE' | 'OUT_OF_SERVICE';
    latitude: number;
    longitude: number;
    pricePerKwh: number;
    supportedConnectors: string[];
    companyName: string;
    workers: Worker[];
    chargers: Charger[];
}

export interface Charger {
    id: number;
    status: 'AVAILABLE' | 'IN_USE' | 'OUT_OF_SERVICE';
    chargingSpeedKw: number;
}

export interface Worker {
    id: number;
    username: string;
    email: string;
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