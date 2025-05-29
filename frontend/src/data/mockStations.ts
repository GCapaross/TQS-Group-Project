import { ChargingStation } from '../types/ChargingStation';

export const mockStations: ChargingStation[] = [
    {
        id: 1,
        name: "Downtown Charging Hub",
        location: "123 Main Street, Downtown",
        latitude: 40.7128,
        longitude: -74.0060,
        status: "AVAILABLE",
        maxSlots: 4,
        availableSlots: 2,
        pricePerKwh: 0.45,
        supportedConnectors: ["CCS", "Type 2"],
        chargingSpeedKw: 50,
        carrierNetwork: "ChargePoint",
        averageRating: 4.5
    },
    {
        id: 2,
        name: "Shopping Mall Station",
        location: "456 Retail Avenue, Shopping District",
        latitude: 40.7145,
        longitude: -74.0080,
        status: "IN_USE",
        maxSlots: 6,
        availableSlots: 1,
        pricePerKwh: 0.40,
        supportedConnectors: ["CCS", "CHAdeMO", "Type 2"],
        chargingSpeedKw: 150,
        carrierNetwork: "Tesla",
        averageRating: 4.8
    },
    {
        id: 3,
        name: "Park & Charge",
        location: "789 Park Road, Green Area",
        latitude: 40.7110,
        longitude: -74.0040,
        status: "AVAILABLE",
        maxSlots: 2,
        availableSlots: 2,
        pricePerKwh: 0.35,
        supportedConnectors: ["Type 2"],
        chargingSpeedKw: 22,
        carrierNetwork: "GreenPower",
        averageRating: 4.2
    }
]; 