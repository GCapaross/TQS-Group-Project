import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  Paper,
  Grid,
  Button,
  Alert,
  CircularProgress
} from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import BookingForm from '../components/BookingForm';
import { ChargingStation } from '../types/ChargingStation';

const BookingPage: React.FC = () => {
  const { stationId } = useParams<{ stationId: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [station, setStation] = useState<ChargingStation | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isBookingFormOpen, setIsBookingFormOpen] = useState(false);

  useEffect(() => {
    const fetchStation = async () => {
      try {
        const response = await fetch(`/api/stations/${stationId}`);
        if (!response.ok) {
          throw new Error('Failed to fetch station details');
        }
        const data = await response.json();
        setStation(data);
      } catch (err) {
        setError('Error loading station details');
      } finally {
        setLoading(false);
      }
    };

    fetchStation();
  }, [stationId]);

  const handleBook = async (startTime: Date, endTime: Date) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    try {
      const response = await fetch('/api/bookings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          stationId,
          startTime,
          endTime
        })
      });

      if (!response.ok) {
        throw new Error('Failed to book time slot');
      }

      navigate('/stations');
    } catch (err) {
      setError('Failed to book time slot. Please try again.');
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
        <Grid container spacing={4}>
          <Grid item xs={12} md={6}>
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
            </Box>
          </Grid>
          <Grid item xs={12} md={6}>
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
              <Button
                variant="contained"
                size="large"
                onClick={() => setIsBookingFormOpen(true)}
                sx={{
                  width: '100%',
                  py: 2,
                  background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
                  '&:hover': {
                    background: 'linear-gradient(45deg, #1976D2 30%, #1E88E5 90%)'
                  }
                }}
              >
                Book Now
              </Button>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      <BookingForm
        station={station}
        open={isBookingFormOpen}
        onClose={() => setIsBookingFormOpen(false)}
        onBook={handleBook}
      />
    </Container>
  );
};

export default BookingPage; 