import { ChargingStation } from '../types/responseTypes';

export const mockStations: ChargingStation[] = [
    {
        id: 1,
        name: "Downtown Charging Hub",
        location: "123 Main Street, Downtown",
        latitude: 40.7128,
        longitude: -74.0060,
        status: "AVAILABLE",
        pricePerKwh: 0.45,
        supportedConnectors: ["CCS", "Type 2"],
        companyName: "Downtown Energy Co.",
        workers: [
            { id: 101, username: "alice_w", email: "alice@downtownenergy.com" },
            { id: 102, username: "bob_m",   email: "bob@downtownenergy.com" }
        ],
        chargers: [
            { id: 201, status: "AVAILABLE",     chargingSpeedKw: 50 },
            { id: 202, status: "IN_USE",        chargingSpeedKw: 100 },
            { id: 203, status: "OUT_OF_SERVICE",chargingSpeedKw: 22 }
        ]

    },
    {
        id: 2,
        name: "Shopping Mall Station",
        location: "456 Retail Avenue, Shopping District",
        latitude: 40.7145,
        longitude: -74.0080,
        status: "AVAILABLE",
        pricePerKwh: 0.45,
        supportedConnectors: ["CCS", "CHAdeMO", "Type 2"],
        companyName: "Downtown Energy Co.",
        workers: [
            { id: 101, username: "alice_w", email: "alice@downtownenergy.com" },
            { id: 102, username: "bob_m",   email: "bob@downtownenergy.com" }
        ],
        chargers: [
            { id: 201, status: "AVAILABLE",     chargingSpeedKw: 50 },
            { id: 202, status: "IN_USE",        chargingSpeedKw: 100 },
            { id: 203, status: "OUT_OF_SERVICE",chargingSpeedKw: 22 }
        ]
    },
    {
        id: 3,
        name: "Park & Charge",
        location: "789 Park Road, Green Area",
        latitude: 40.7110,
        longitude: -74.0040,
        status: "AVAILABLE",
        pricePerKwh: 0.35,
        supportedConnectors: ["Type 2"],
        companyName: "Downtown Energy Co.",
        workers: [
            { id: 101, username: "alice_w", email: "alice@downtownenergy.com" },
            { id: 102, username: "bob_m",   email: "bob@downtownenergy.com" }
        ],
        chargers: [
            { id: 201, status: "AVAILABLE",     chargingSpeedKw: 50 },
            { id: 202, status: "IN_USE",        chargingSpeedKw: 100 },
            { id: 203, status: "OUT_OF_SERVICE",chargingSpeedKw: 22 }
        ]
    }
]; 