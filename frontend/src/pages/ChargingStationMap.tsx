import React, { useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { Box, Paper, Typography, Slider, FormControl, InputLabel, Select, MenuItem, Chip, Stack } from '@mui/material';
import 'leaflet/dist/leaflet.css';
import { ChargingStation, FilterOptions } from '../types/ChargingStation';
import { mockStations } from '../data/mockStations';
import L from 'leaflet';

// Fix for default marker icons in Leaflet with React
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
    iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const connectorTypes = ['CCS', 'CHAdeMO', 'Type 2'];
const carrierNetworks = ['ChargePoint', 'Tesla', 'GreenPower'];

const ChargingStationMap: React.FC = () => {
    const [filters, setFilters] = useState<FilterOptions>({
        connectorTypes: [],
        minChargingSpeed: null,
        carrierNetwork: null,
        minRating: null,
        radiusKm: 10
    });

    const filteredStations = mockStations.filter(station => {
        if (filters.connectorTypes.length > 0 && 
            !filters.connectorTypes.some(type => station.connectorTypes.includes(type))) {
            return false;
        }
        if (filters.minChargingSpeed && station.chargingSpeedKw < filters.minChargingSpeed) {
            return false;
        }
        if (filters.carrierNetwork && station.carrierNetwork !== filters.carrierNetwork) {
            return false;
        }
        if (filters.minRating && station.averageRating < filters.minRating) {
            return false;
        }
        return true;
    });

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'AVAILABLE': return 'green';
            case 'IN_USE': return 'orange';
            case 'MAINTENANCE': return 'red';
            case 'OUT_OF_SERVICE': return 'gray';
            default: return 'gray';
        }
    };

    return (
        <Box sx={{ 
            display: 'flex', 
            flexDirection: 'column', 
            height: '100vh',
            width: '100%',
            position: 'relative',
            overflow: 'hidden',
            pt: { xs: 8, sm: 10 }
        }}>
            <Paper elevation={3} sx={{ p: 2, mb: 2 }}>
                <Typography variant="h5" gutterBottom>Find Charging Stations</Typography>
                <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
                    <FormControl sx={{ minWidth: 200 }}>
                        <InputLabel>Connector Types</InputLabel>
                        <Select
                            multiple
                            value={filters.connectorTypes}
                            onChange={(e) => setFilters({ ...filters, connectorTypes: e.target.value as string[] })}
                            renderValue={(selected) => (
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                                    {selected.map((value) => (
                                        <Chip key={value} label={value} />
                                    ))}
                                </Box>
                            )}
                        >
                            {connectorTypes.map((type) => (
                                <MenuItem key={type} value={type}>{type}</MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <FormControl sx={{ minWidth: 200 }}>
                        <InputLabel>Carrier Network</InputLabel>
                        <Select
                            value={filters.carrierNetwork || ''}
                            onChange={(e) => setFilters({ ...filters, carrierNetwork: e.target.value as string })}
                        >
                            <MenuItem value="">Any</MenuItem>
                            {carrierNetworks.map((network) => (
                                <MenuItem key={network} value={network}>{network}</MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <FormControl sx={{ minWidth: 200 }}>
                        <Typography gutterBottom>Min. Charging Speed (kW)</Typography>
                        <Slider
                            value={filters.minChargingSpeed || 0}
                            onChange={(_, value) => setFilters({ ...filters, minChargingSpeed: value as number })}
                            min={0}
                            max={350}
                            step={10}
                            marks
                            valueLabelDisplay="auto"
                        />
                    </FormControl>

                    <FormControl sx={{ minWidth: 200 }}>
                        <Typography gutterBottom>Min. Rating</Typography>
                        <Slider
                            value={filters.minRating || 0}
                            onChange={(_, value) => setFilters({ ...filters, minRating: value as number })}
                            min={0}
                            max={5}
                            step={0.5}
                            marks
                            valueLabelDisplay="auto"
                        />
                    </FormControl>
                </Stack>
            </Paper>

            <Paper elevation={3} sx={{ 
                flex: 1, 
                position: 'relative',
                overflow: 'hidden',
                '& .leaflet-container': {
                    height: '100%',
                    width: '100%'
                }
            }}>
                <MapContainer
                    center={[40.7128, -74.0060]}
                    zoom={14}
                    style={{ height: '100%', width: '100%' }}
                >
                    <TileLayer
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    />
                    {filteredStations.map((station) => (
                        <Marker
                            key={station.id}
                            position={[station.latitude, station.longitude]}
                        >
                            <Popup>
                                <Box sx={{ p: 1 }}>
                                    <Typography variant="h6">{station.name}</Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        {station.location}
                                    </Typography>
                                    <Typography variant="body2">
                                        Status: <span style={{ color: getStatusColor(station.status) }}>
                                            {station.status}
                                        </span>
                                    </Typography>
                                    <Typography variant="body2">
                                        Available Slots: {station.availableSlots}/{station.maxSlots}
                                    </Typography>
                                    <Typography variant="body2">
                                        Price: ${station.pricePerKwh}/kWh
                                    </Typography>
                                    <Typography variant="body2">
                                        Speed: {station.chargingSpeedKw} kW
                                    </Typography>
                                    <Typography variant="body2">
                                        Rating: {station.averageRating}/5
                                    </Typography>
                                    <Typography variant="body2">
                                        Connectors: {station.connectorTypes.join(', ')}
                                    </Typography>
                                </Box>
                            </Popup>
                        </Marker>
                    ))}
                </MapContainer>
            </Paper>
        </Box>
    );
};

export default ChargingStationMap; 