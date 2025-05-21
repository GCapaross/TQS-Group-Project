import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Card,
  CardContent,
  Typography,
  Button,
  Box,
  CircularProgress,
  Alert
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { ChargingStation } from '../types/ChargingStation';

const Stations: React.FC = () => {
  const navigate = useNavigate();
  const [stations, setStations] = useState<ChargingStation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // const fetchStations = async () => {
  //   try {
  //     const response = await fetch('/api/stations');
  //     if (!response.ok) {
  //       throw new Error('Failed to fetch stations');
  //     }
  //     const data = await response.json();
  //     setStations(data);
  //   } catch (err) {
  //     setError('Error loading stations');
  //   } finally {
  //     setLoading(false);

  useEffect(() => {
    // Mock data for testing
    const mockStations: ChargingStation[] = [
      {
        id: 1,
        name: "Downtown Charging Station",
        location: "123 Main St, City Center",
        latitude: 40.7128,
        longitude: -74.0060,
        status: "AVAILABLE",
        availableSlots: 3,
        maxSlots: 5,
        pricePerKwh: 0.35,
        connectorTypes: ["CCS", "Type 2"],
        chargingSpeedKw: 50,
        carrierNetwork: "ChargePoint",
        averageRating: 4.5
      },
      {
        id: 2,
        name: "Shopping Mall Charger",
        location: "456 Retail Ave, Shopping District",
        latitude: 40.7145,
        longitude: -74.0080,
        status: "AVAILABLE",
        availableSlots: 2,
        maxSlots: 4,
        pricePerKwh: 0.40,
        connectorTypes: ["CCS", "CHAdeMO", "Type 2"],
        chargingSpeedKw: 150,
        carrierNetwork: "Tesla",
        averageRating: 4.8
      },
      {
        id: 3,
        name: "Park & Charge",
        location: "789 Park Rd, Green Zone",
        latitude: 40.7110,
        longitude: -74.0040,
        status: "IN_USE",
        availableSlots: 1,
        maxSlots: 3,
        pricePerKwh: 0.30,
        connectorTypes: ["Type 2"],
        chargingSpeedKw: 22,
        carrierNetwork: "GreenPower",
        averageRating: 4.2
      },
      {
        id: 4,
        name: "Highway Rest Stop Charger",
        location: "101 Expressway, Exit 42",
        latitude: 40.7135,
        longitude: -74.0070,
        status: "AVAILABLE",
        availableSlots: 4,
        maxSlots: 6,
        pricePerKwh: 0.45,
        connectorTypes: ["CCS", "CHAdeMO", "Type 2"],
        chargingSpeedKw: 250,
        carrierNetwork: "Electrify America",
        averageRating: 4.6
      },
      {
        id: 5,
        name: "University Campus Station",
        location: "202 Campus Dr, University Area",
        latitude: 40.7150,
        longitude: -74.0090,
        status: "AVAILABLE",
        availableSlots: 2,
        maxSlots: 4,
        pricePerKwh: 0.25,
        connectorTypes: ["Type 2"],
        chargingSpeedKw: 11,
        carrierNetwork: "Campus Power",
        averageRating: 4.0
      },
      {
        id: 6,
        name: "Business District Charger",
        location: "303 Corporate Blvd, Business Park",
        latitude: 40.7160,
        longitude: -74.0100,
        status: "IN_USE",
        availableSlots: 1,
        maxSlots: 3,
        pricePerKwh: 0.50,
        connectorTypes: ["CCS", "Type 2"],
        chargingSpeedKw: 100,
        carrierNetwork: "Business Charge",
        averageRating: 4.7
      }
    ];

    // Simulate API call
    setTimeout(() => {
      setStations(mockStations);
      setLoading(false);
    }, 1000);
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Charging Stations
      </Typography>
      <Grid container spacing={3}>
        {stations.map((station) => (
          <Grid container item xs={12} sm={6} md={4} key={station.id}>
            <Card 
              sx={{ 
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                transition: 'transform 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 3
                }
              }}
            >
              <CardContent>
                <Typography variant="h5" component="h2" gutterBottom>
                  {station.name}
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  {station.location}
                </Typography>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2">
                    Price per kWh: ${station.pricePerKwh}
                  </Typography>
                  <Typography variant="body2">
                    Available Slots: {station.availableSlots} / {station.maxSlots}
                  </Typography>
                  <Typography variant="body2">
                    Status: {station.status}
                  </Typography>
                  <Typography variant="body2">
                    Charging Speed: {station.chargingSpeedKw} kW
                  </Typography>
                  <Typography variant="body2">
                    Connectors: {station.connectorTypes.join(', ')}
                  </Typography>
                  <Typography variant="body2">
                    Network: {station.carrierNetwork}
                  </Typography>
                  <Typography variant="body2">
                    Rating: {station.averageRating}/5
                  </Typography>
                </Box>
                <Button
                  variant="contained"
                  fullWidth
                  onClick={() => navigate(`/stations/${station.id}/book`)}
                  sx={{
                    background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
                    '&:hover': {
                      background: 'linear-gradient(45deg, #1976D2 30%, #1E88E5 90%)'
                    }
                  }}
                >
                  Book Now
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
};

export default Stations;
