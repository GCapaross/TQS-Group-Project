import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Container,
  Typography,
  Paper,
  Button,
  Alert,
  CircularProgress,
  Snackbar,
  Grid,
} from "@mui/material";
import { useAuth } from "../contexts/AuthContext";
import BookingForm from "../components/BookingForm";
import { chargingStationApi, bookingApi } from "../services/api";
import { ChargingStation } from "../types/responseTypes";
import { AxiosError } from "axios";

const BookingPage: React.FC = () => {
  const { stationId } = useParams<{ stationId: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [station, setStation] = useState<ChargingStation | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isBookingFormOpen, setIsBookingFormOpen] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [bookingError, setBookingError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStation = async () => {
      if (!stationId) return;

      try {
        const data = await chargingStationApi.getById(parseInt(stationId));
        setStation(data);
      } catch (err) {
        setError("Error loading station details");
        console.error("Error fetching station:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchStation();
  }, [stationId]);

  const handleBook = async (
    startTime: Date,
    endTime: Date,
    estimatedEnergy: number
  ) => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    if (!stationId) return;
    try {
      const toLocalISOString = (d: Date) => d.toISOString().slice(0, 19);

      await bookingApi.create({
        stationId: parseInt(stationId),
        startTime: toLocalISOString(startTime),
        endTime: toLocalISOString(endTime),
        estimatedEnergy,
      });

      setShowSuccess(true);
      setBookingError(null);
      const updated = await chargingStationApi.getById(parseInt(stationId));
      setStation(updated);
      // setIsBookingFormOpen(false);
    } catch (err: unknown) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 400) {
          setBookingError("There are not slots available for this time range.");
        } else {
          setBookingError(
            "Failed to make a reservation: " + (err.message || "unknown error")
          );
        }
      } else {
        setBookingError(
          "Failed to make a reservation: " +
            (err instanceof Error ? err.message : "unknown error")
        );
      }
    }
  };

  if (loading) {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "100vh",
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  if (error || !station) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">{error || "Station not found"}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
        <Box
          sx={{
            display: "flex",
            flexDirection: { xs: "column", md: "row" },
            gap: 4,
          }}
        >
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
                Connector Types: {station.supportedConnectors.join(", ")}
              </Typography>
            </Box>
          </Box>
          <Box
            sx={{
              flex: 1,
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              gap: 2,
            }}
          >
            <Button
              variant="contained"
              size="large"
              onClick={() => setIsBookingFormOpen(true)}
              sx={{
                width: "100%",
                py: 2,
                background: "linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)",
                "&:hover": {
                  background:
                    "linear-gradient(45deg, #1976D2 30%, #1E88E5 90%)",
                },
              }}
              id="book-button"
            >
              Book Now
            </Button>
          </Box>
        </Box>
        <Typography variant="h6" mt={4} gutterBottom>
          Chargers
        </Typography>
        {station.chargers && station.chargers.length > 0 ? (
          <Grid container spacing={3} mt={5}>
            {station.chargers.map((charger) => (
              <Grid key={charger.id}>
                <Paper
                  elevation={2}
                  sx={{ p: 2, borderRadius: 2, textAlign: "center" }}
                >
                  <Typography variant="h6">{`Charger ${charger.id}`}</Typography>
                  <Typography variant="body1">
                    Status: {charger.status}
                  </Typography>
                  <Typography variant="body1">
                    Speed: {charger.chargingSpeedKw} kW
                  </Typography>
                </Paper>
              </Grid>
            ))}
          </Grid>
        ) : (
          <Typography variant="body2" color="text.secondary" mt={5}>
            No chargers available.
          </Typography>
        )}

        <Typography variant="h6" mt={4} gutterBottom>
          Workers
        </Typography>
        {station.workers && station.workers.length > 0 ? (
          <Grid container spacing={2}>
            {station.workers.map((worker) => (
              <Grid key={worker.id}>
                <Paper elevation={2} sx={{ p: 2, borderRadius: 2 }}>
                  <Typography variant="body1">{worker.username}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {worker.email}
                  </Typography>
                </Paper>
              </Grid>
            ))}
          </Grid>
        ) : (
          <Typography variant="body2" color="text.secondary" mb={2}>
            No workers assigned to this station.
          </Typography>
        )}
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
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
      />

      {/* Error handling for booking */}
      <Snackbar
        open={!!bookingError}
        autoHideDuration={6000}
        onClose={() => setBookingError(null)}
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
      >
        <Alert
          onClose={() => setBookingError(null)}
          severity="error"
          sx={{ width: "100%" }}
        >
          {bookingError}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default BookingPage;
