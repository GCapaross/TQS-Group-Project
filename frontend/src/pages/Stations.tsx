import React, { useState, useEffect } from "react";
import {
  Box,
  Container,
  Grid,
  Card,
  CardContent,
  Typography,
  Chip,
  CircularProgress,
  Alert,
  TextField,
  InputAdornment,
  Button,
} from "@mui/material";
import { Search as SearchIcon } from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { chargingStationApi } from "../services/api";
import { ChargingStation } from "../types/responseTypes";

const Stations: React.FC = () => {
  const [stations, setStations] = useState<ChargingStation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const fetchStations = async () => {
      try {
        const data = await chargingStationApi.getAll();
        console.log("Fetched stations:", data);
        setStations(data);
      } catch (err) {
        setError("Failed to load charging stations");
        console.error("Error fetching stations:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchStations();
  }, []);

  const filteredStations = stations.filter(
    (station) =>
      station.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      station.location.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getStatusColor = (status: string) => {
    switch (status) {
      case "AVAILABLE":
        return "success";
      case "IN_USE":
        return "warning";
      case "MAINTENANCE":
        return "error";
      case "OUT_OF_SERVICE":
        return "default";
      default:
        return "default";
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

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Charging Stations
        </Typography>
        <TextField
          fullWidth
          variant="outlined"
          placeholder="Search stations by name or location..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
          sx={{ mb: 4 }}
        />

        <Button
          variant="contained"
          color="primary"
          sx={{ mb: 4 }}
          onClick={() => navigate("/stations/create")}
        >
          Create New Station
        </Button>

        <Grid container spacing={3}>
          {filteredStations.map((station) => (
            <Grid key={station.id}>
              <Card
                sx={{
                  height: "100%",
                  display: "flex",
                  flexDirection: "column",
                  cursor: "pointer",
                  "&:hover": {
                    boxShadow: 6,
                    transform: "translateY(-4px)",
                    transition: "all 0.3s ease-in-out",
                  },
                }}
                onClick={() => navigate(`/stations/${station.id}/book`)}
              >
                <CardContent>
                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "flex-start",
                      mb: 2,
                    }}
                  >
                    <Typography variant="h6" component="h2" gutterBottom>
                      {station.name}
                    </Typography>
                    <Chip
                      label={station.status}
                      color={getStatusColor(station.status)}
                      size="small"
                    />
                  </Box>
                  <Typography variant="body2" color="text.secondary" paragraph>
                    {station.location}
                  </Typography>
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2">
                      Price: ${station.pricePerKwh}/kWh
                    </Typography>
                    <Typography variant="body2">
                      Speed: {station.chargers[0].chargingSpeedKw} kW
                    </Typography>
                  </Box>
                  <Box
                    sx={{ mt: 2, display: "flex", flexWrap: "wrap", gap: 0.5 }}
                  >
                    {station.supportedConnectors.map((type) => (
                      <Chip
                        key={type}
                        label={type}
                        size="small"
                        variant="outlined"
                      />
                    ))}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>
    </Container>
  );
};

export default Stations;
