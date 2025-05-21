import React, { useState } from 'react';
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
  CircularProgress
} from '@mui/material';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ChargingStation } from '../types/ChargingStation';
import { useAuth } from '../contexts/AuthContext';

interface BookingFormProps {
  station: ChargingStation;
  open: boolean;
  onClose: () => void;
  onBook: (startTime: Date, endTime: Date) => Promise<void>;
}

const BookingForm: React.FC<BookingFormProps> = ({ station, open, onClose, onBook }) => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [startTime, setStartTime] = useState<Date | null>(null);
  const [endTime, setEndTime] = useState<Date | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

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

    setError(null);
    setLoading(true);

    try {
      await onBook(startTime, endTime);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to book time slot');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
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
    </Dialog>
  );
};

export default BookingForm; 