export interface ChargingStation {
    id: number;
    name: string;
    location: string;
    latitude: number;
    longitude: number;
    pricePerKwh: number;
    supportedConnectors: string[];
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

export interface FilterOptions {
    connectorTypes: string[];
    minChargingSpeed: number | null;
    carrierNetwork: string | null;
    minRating: number | null;
    radiusKm: number;
} 