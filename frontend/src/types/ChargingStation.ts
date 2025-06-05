export interface ChargingStation {
    id: number;
    name: string;
    location: string;
    latitude: number;
    longitude: number;
    status: 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'OUT_OF_SERVICE';
    maxSlots: number;
    availableSlots: number;
    pricePerKwh: number;
    supportedConnectors: string[];
    chargerSpeeds: number;
    carrierNetwork: string;
    averageRating: number;
}

export interface FilterOptions {
    connectorTypes: string[];
    minChargingSpeed: number | null;
    carrierNetwork: string | null;
    minRating: number | null;
    radiusKm: number;
} 