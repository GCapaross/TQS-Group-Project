import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
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
} from "@mui/material";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import { ChargingStation } from "../types/responseTypes";
import { useAuth } from "../contexts/AuthContext";
import { QRCodeSVG } from "qrcode.react";

interface BookingFormProps {
  station: ChargingStation;
  open: boolean;
  onClose: () => void;
  onBook: (
    startTime: Date,
    endTime: Date,
    estimatedEnergy: number
  ) => Promise<void>;
}

const BookingForm: React.FC<BookingFormProps> = ({
  station,
  open,
  onClose,
  onBook,
}) => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [startTime, setStartTime] = useState<Date | null>(null);
  const [endTime, setEndTime] = useState<Date | null>(null);
  const [estimatedEnergy, setEstimatedEnergy] = useState<number>(0);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [bookingId, setBookingId] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      setShowConfirmation(false);
      setBookingId(null);
      setStartTime(null);
      setEndTime(null);
      setEstimatedEnergy(0);
      setError(null);
      setLoading(false);
    }
  }, [open]);

  const handleSubmit = async () => {
    if (!isAuthenticated) {
      onClose();
      navigate("/login");
      return;
    }

    if (!startTime || !endTime) {
      setError("Please select both start and end times");
      return;
    }

    if (startTime >= endTime) {
      setError("End time must be after start time");
      return;
    }

    if (startTime < new Date()) {
      setError("Start time cannot be in the past");
      return;
    }

    const durationHours =
      (endTime.getTime() - startTime.getTime()) / (1000 * 60 * 60);
    if (durationHours > 4) {
      setError("Maximum booking duration is 4 hours");
      return;
    }

    if (estimatedEnergy <= 0) {
      setError("Please enter estimated energy needed");
      return;
    }

    setError(null);
    setLoading(true);

    try {
      await onBook(startTime, endTime, estimatedEnergy);
      setBookingId(crypto.randomUUID());
      setShowConfirmation(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to book time slot");
    } finally {
      setLoading(false);
    }
  };

  const renderBookingConfirmation = () => (
    <Box sx={{ p: 3, textAlign: "center" }}>
      <Typography variant="h5" gutterBottom>
        Booking Confirmed!
      </Typography>
      <Box sx={{ my: 3 }}>
        <QRCodeSVG value={bookingId || ""} size={200} />
      </Box>
      <Typography variant="body1" gutterBottom>
        <span id="booking-id">Booking ID: {bookingId}</span>
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
      <Button variant="contained" onClick={onClose} sx={{ mt: 2 }}>
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
                    sx={{ width: "100%" }}
                    slotProps={{
                      textField: { id: "start-date" },
                    }}
                  />
                </Box>
                <Box sx={{ mb: 3 }}>
                  <DateTimePicker
                    slotProps={{
                      textField: { id: "end-date" },
                    }}
                    label="End Time"
                    value={endTime}
                    onChange={(newValue: Date | null) => setEndTime(newValue)}
                    minDateTime={startTime || new Date()}
                    sx={{ width: "100%" }}
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
                id="estimated-energy"
              />
            </>
          )}

          {error && (
            <Alert severity="error" sx={{ mt: 2 }} id="error-message">
              {error}
            </Alert>
          )}

          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Price per kWh: ${station.pricePerKwh}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Estimated Cost: $
              {(estimatedEnergy * station.pricePerKwh).toFixed(2)}
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
          id="confirmation-button"
          disabled={loading || (!isAuthenticated && (!startTime || !endTime))}
          sx={{
            background: "linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)",
            "&:hover": {
              background: "linear-gradient(45deg, #1976D2 30%, #1E88E5 90%)",
            },
          }}
        >
          {loading ? (
            <CircularProgress size={24} />
          ) : isAuthenticated ? (
            "Book Now"
          ) : (
            "Login to Book"
          )}
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
