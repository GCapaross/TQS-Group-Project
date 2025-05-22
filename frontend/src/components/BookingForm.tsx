import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Typography,
  Alert,
  CircularProgress,
  Grid,
  Chip
} from '@mui/material';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ChargingStation } from '../types/ChargingStation';
import { useAuth } from '../contexts/AuthContext';
import { QRCodeSVG } from 'qrcode.react';

interface BookingFormProps {
  station: ChargingStation;
  open: boolean;
  onClose: () => void;
  onBook: (startTime: Date, endTime: Date, estimatedEnergy: number) => Promise<void>;
}

interface TimeSlot {
  start: Date;
  end: Date;
  isAvailable: boolean;
}

const BookingForm: React.FC<BookingFormProps> = ({ station, open, onClose, onBook }) => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [startTime, setStartTime] = useState<Date | null>(null);
  const [endTime, setEndTime] = useState<Date | null>(null);
  const [estimatedEnergy, setEstimatedEnergy] = useState<number>(0);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [availableSlots, setAvailableSlots] = useState<TimeSlot[]>([]);
  const [bookingId, setBookingId] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      fetchAvailableSlots();
    }
  }, [open]);

  const fetchAvailableSlots = async () => {
    try {
      const response = await fetch(`/api/stations/${station.id}/available-slots`);
      if (response.ok) {
        const slots = await response.json();
        setAvailableSlots(slots);
      }
    } catch (err) {
      console.error('Failed to fetch available slots:', err);
    }
  };

  const handleSubmit = async () => {
    if (!isAuthenticated) {
      onClose();
      navigate('/login');
      return;
    }

    if (!startTime || !endTime) {
      setError('Please select both start and end times');
      return;
    }

    if (startTime >= endTime) {
      setError('End time must be after start time');
      return;
    }

    if (startTime < new Date()) {
      setError('Start time cannot be in the past');
      return;
    }

    const durationHours = (endTime.getTime() - startTime.getTime()) / (1000 * 60 * 60);
    if (durationHours > 4) {
      setError('Maximum booking duration is 4 hours');
      return;
    }

    if (estimatedEnergy <= 0) {
      setError('Please enter estimated energy needed');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      await onBook(startTime, endTime, estimatedEnergy);
      setBookingId(crypto.randomUUID());
      setShowConfirmation(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to book time slot');
    } finally {
      setLoading(false);
    }
  };

  const renderBookingConfirmation = () => (
    <Box sx={{ p: 3, textAlign: 'center' }}>
      <Typography variant="h5" gutterBottom>
        Booking Confirmed!
      </Typography>
      <Box sx={{ my: 3 }}>
        <QRCodeSVG value={bookingId || ''} size={200} />
      </Box>
      <Typography variant="body1" gutterBottom>
        Booking ID: {bookingId}
      </Typography>
      <Typography variant="body1" gutterBottom>
        Station: {station.name}
      </Typography>
      <Typography variant="body1" gutterBottom>
        Start Time: {startTime?.toLocaleString()}
      </Typography>
      <Typography variant="body1" gutterBottom>
        End Time: {endTime?.toLocaleString()}
      </Typography>
      <Typography variant="body1" gutterBottom>
        Estimated Energy: {estimatedEnergy} kWh
      </Typography>
      <Button
        variant="contained"
        onClick={onClose}
        sx={{ mt: 2 }}
      >
        Close
      </Button>
    </Box>
  );

  const renderBookingForm = () => (
    <>
      <DialogTitle>
        Book Charging Station
        <Typography variant="subtitle1" color="text.secondary">
          {station.name}
        </Typography>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ mt: 2 }}>
          {!isAuthenticated ? (
            <Alert severity="info" sx={{ mb: 2 }}>
              Please log in to book a charging station.
            </Alert>
          ) : (
            <>
              <LocalizationProvider dateAdapter={AdapterDateFns}>
                <Box sx={{ mb: 3 }}>
                  <DateTimePicker
                    label="Start Time"
                    value={startTime}
                    onChange={(newValue: Date | null) => setStartTime(newValue)}
                    minDateTime={new Date()}
                    sx={{ width: '100%' }}
                  />
                </Box>
                <Box sx={{ mb: 3 }}>
                  <DateTimePicker
                    label="End Time"
                    value={endTime}
                    onChange={(newValue: Date | null) => setEndTime(newValue)}
                    minDateTime={startTime || new Date()}
                    sx={{ width: '100%' }}
                  />
                </Box>
              </LocalizationProvider>

              <TextField
                fullWidth
                label="Estimated Energy Needed (kWh)"
                type="number"
                value={estimatedEnergy}
                onChange={(e) => setEstimatedEnergy(Number(e.target.value))}
                sx={{ mb: 3 }}
              />

              <Typography variant="h6" gutterBottom>
                Available Time Slots
              </Typography>
              <Grid container spacing={1} sx={{ mb: 3 }}>
                {availableSlots.map((slot, index) => (
                  <Grid key={index}> {/* To rewiew - was causing error */ }
                    <Chip
                      label={`${slot.start.toLocaleTimeString()} - ${slot.end.toLocaleTimeString()}`}
                      color={slot.isAvailable ? 'success' : 'error'}
                      onClick={() => {
                        if (slot.isAvailable) {
                          setStartTime(slot.start);
                          setEndTime(slot.end);
                        }
                      }}
                      sx={{ m: 0.5 }}
                    />
                  </Grid>
                ))}
              </Grid>
            </>
          )}

          {error && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {error}
            </Alert>
          )}

          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Price per kWh: ${station.pricePerKwh}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Available Slots: {station.availableSlots} / {station.maxSlots}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Estimated Cost: ${(estimatedEnergy * station.pricePerKwh).toFixed(2)}
            </Typography>
          </Box>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading || (!isAuthenticated && (!startTime || !endTime))}
          sx={{
            background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
            '&:hover': {
              background: 'linear-gradient(45deg, #1976D2 30%, #1E88E5 90%)'
            }
          }}
        >
          {loading ? <CircularProgress size={24} /> : isAuthenticated ? 'Book Now' : 'Login to Book'}
        </Button>
      </DialogActions>
    </>
  );

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      {showConfirmation ? renderBookingConfirmation() : renderBookingForm()}
    </Dialog>
  );
};

export default BookingForm; 