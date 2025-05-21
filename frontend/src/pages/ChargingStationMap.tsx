import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import { Icon } from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { Box, Typography, CircularProgress, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { chargingStationApi } from '../services/api';
import { ChargingStation } from '../types/api';

// Fix for default marker icons in Leaflet with React
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

const defaultIcon = new Icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});

const MapUpdater: React.FC<{ center: [number, number] }> = ({ center }) => {
    const map = useMap();
    useEffect(() => {
        map.setView(center);
    }, [center, map]);
    return null;
};

const ChargingStationMap: React.FC = () => {
    const [stations, setStations] = useState<ChargingStation[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [userLocation, setUserLocation] = useState<[number, number]>([38.7223, -9.1393]); // Default to Lisbon
    const navigate = useNavigate();

    useEffect(() => {
        // Get user's location
        navigator.geolocation.getCurrentPosition(
            (position) => {
                setUserLocation([position.coords.latitude, position.coords.longitude]);
            },
            (error) => {
                console.error('Error getting location:', error);
            }
        );

        // Fetch stations
        const fetchStations = async () => {
            try {
                const data = await chargingStationApi.getAll();
                setStations(data);
            } catch (err) {
                setError('Failed to load charging stations');
                console.error('Error fetching stations:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchStations();
    }, []);

    const handleStationClick = (stationId: number) => {
        navigate(`/stations/${stationId}`);
    };

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <CircularProgress />
            </Box>
        );
    }

    if (error) {
        return (
            <Box sx={{ p: 2 }}>
                <Alert severity="error">{error}</Alert>
            </Box>
        );
    }

    return (
        <Box sx={{ height: '100vh', width: '100%' }}>
            <MapContainer
                center={userLocation}
                zoom={13}
                style={{ height: '100%', width: '100%' }}
            >
                <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                />
                <MapUpdater center={userLocation} />
                {stations.map((station) => (
                    <Marker
                        key={station.id}
                        position={[station.latitude, station.longitude]}
                        icon={defaultIcon}
                        eventHandlers={{
                            click: () => handleStationClick(station.id)
                        }}
                    >
                        <Popup>
                            <Box sx={{ p: 1 }}>
                                <Typography variant="h6">{station.name}</Typography>
                                <Typography variant="body2">{station.location}</Typography>
                                <Typography variant="body2">
                                    Available Slots: {station.availableSlots}/{station.maxSlots}
                                </Typography>
                                <Typography variant="body2">
                                    Price: ${station.pricePerKwh}/kWh
                                </Typography>
                                <Typography variant="body2">
                                    Status: {station.status}
                                </Typography>
                            </Box>
                        </Popup>
                    </Marker>
                ))}
            </MapContainer>
        </Box>
    );
};

export default ChargingStationMap; 