import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  Paper,
  Button,
  Alert,
  CircularProgress,
  Snackbar
} from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import BookingForm from '../components/BookingForm';
import { chargingStationApi, bookingApi } from '../services/api';
import { ChargingStation } from '../types/responseTypes';

const BookingPage: React.FC = () => {
  const { stationId } = useParams<{ stationId: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [station, setStation] = useState<ChargingStation | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isBookingFormOpen, setIsBookingFormOpen] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);

  useEffect(() => {
    const fetchStation = async () => {
      if (!stationId) return;
      
      try {
        const data = await chargingStationApi.getById(parseInt(stationId));
        setStation(data);
      } catch (err) {
        setError('Error loading station details');
        console.error('Error fetching station:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchStation();
  }, [stationId]);

  const handleBook = async (startTime: Date, endTime: Date, estimatedEnergy: number) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    if (!stationId) return;

    try {
      await bookingApi.create({
        stationId: parseInt(stationId),
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString(),
        estimatedEnergy
      });

      setShowSuccess(true);
      // Refresh station data to update available slots
      const updatedStation = await chargingStationApi.getById(parseInt(stationId));
      setStation(updatedStation);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to book time slot');
      throw err;
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error || !station) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">{error || 'Station not found'}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 4 }}>
          <Box sx={{ flex: 1 }}>
            <Typography variant="h4" component="h1" gutterBottom>
              {station.name}
            </Typography>
            <Typography variant="body1" color="text.secondary" paragraph>
              {station.location}
            </Typography>
            <Box sx={{ mt: 2 }}>
              <Typography variant="h6" gutterBottom>
                Station Details
              </Typography>
              <Typography variant="body1">
                Price per kWh: ${station.pricePerKwh}
              </Typography>
              <Typography variant="body1">
                Available Slots: {station.availableSlots} / {station.maxSlots}
              </Typography>
              <Typography variant="body1">
                Status: {station.status}
              </Typography>
              <Typography variant="body1">
                Charging Speed: {station.chargingSpeedKw} kW
              </Typography>
              <Typography variant="body1">
                Connector Types: {station.connectorTypes.join(', ')}
              </Typography>
              <Typography variant="body1">
                Network: {station.carrierNetwork}
              </Typography>
              <Typography variant="body1">
                Rating: {station.averageRating}/5
              </Typography>
            </Box>
          </Box>
          <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
            <Button
              variant="contained"
              size="large"
              onClick={() => setIsBookingFormOpen(true)}
              disabled={station.status !== 'AVAILABLE' || station.availableSlots === 0}
              sx={{
                width: '100%',
                py: 2,
                background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
                '&:hover': {
                  background: 'linear-gradient(45deg, #1976D2 30%, #1E88E5 90%)'
                }
              }}
            >
              {station.status !== 'AVAILABLE' ? 'Station Unavailable' :
               station.availableSlots === 0 ? 'No Slots Available' : 'Book Now'}
            </Button>
          </Box>
        </Box>
      </Paper>

      <BookingForm
        station={station}
        open={isBookingFormOpen}
        onClose={() => setIsBookingFormOpen(false)}
        onBook={handleBook}
      />

      <Snackbar
        open={showSuccess}
        autoHideDuration={6000}
        onClose={() => setShowSuccess(false)}
        message="Booking successful! Check your bookings page for details."
      />
    </Container>
  );
};

export default BookingPage; 