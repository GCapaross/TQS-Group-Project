import React, { useState, useEffect } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import { Icon } from "leaflet";
import "leaflet/dist/leaflet.css";
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Paper,
  Grid,
  Button,
  Slider,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { chargingStationApi } from "../services/api";
import { ChargingStation } from "../types/responseTypes";

// Fix for default marker icons in Leaflet with React
import icon from "leaflet/dist/images/marker-icon.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";

const defaultIcon = new Icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
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
  const [filteredStations, setFilteredStations] = useState<ChargingStation[]>(
    []
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [userLocation, setUserLocation] = useState<[number, number]>([
    38.7223, -9.1393,
  ]); // Default to Lisbon
  const navigate = useNavigate();

  // Filter states
  const [supportedConnectors, setSupportedConnectors] = useState<string[]>([]);
  const [minChargingSpeed, setMinChargingSpeed] = useState<number>(0);

  useEffect(() => {
    // Get user's location
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setUserLocation([position.coords.latitude, position.coords.longitude]);
      },
      (error) => {
        console.error("Error getting location:", error);
      }
    );

    // Fetch stations
    const fetchStations = async () => {
      try {
        const data = await chargingStationApi.getAll();
        console.log("Fetched stations:", data);
        setStations(data);
        setFilteredStations(data);
      } catch (err) {
        setError("Failed to load charging stations");
        console.error("Error fetching stations:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchStations();
  }, []);

  // Apply filters whenever filter states change
  useEffect(() => {
    let filtered = [...stations];

    if (supportedConnectors.length > 0) {
      filtered = filtered.filter((station) =>
        supportedConnectors.some((type) =>
          station.supportedConnectors.includes(type)
        )
      );
    }

    if (minChargingSpeed > 0) {
      filtered = filtered.filter((station) =>
        station.chargers.some(
          (charger) => charger.chargingSpeedKw >= minChargingSpeed
        )
      );
    }

    setFilteredStations(filtered);
  }, [stations, supportedConnectors, minChargingSpeed]);

  const handleStationClick = (stationId: number) => {
    navigate(`/stations/${stationId}/book`);
  };

  if (loading) {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "100vh",
        }}
      >
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
    <Box
      sx={{
        height: "100vh",
        width: "100%",
        position: "relative",
        padding: "80px 20px 20px 20px", // Add padding to account for navbar and edges
        boxSizing: "border-box",
      }}
    >
      <Paper
        elevation={3}
        sx={{
          position: "absolute",
          top: 100, // Adjusted to account for navbar
          right: 40,
          zIndex: 1000,
          p: 2,
          width: 300,
          maxHeight: "calc(100vh - 120px)", // Adjusted to account for padding
          overflowY: "auto",
        }}
      >
        <Typography variant="h6" gutterBottom>
          Filters
        </Typography>
        <Grid container spacing={2}>
          <Grid>
            <FormControl fullWidth>
              <InputLabel>Connector Types</InputLabel>
              <Select
                multiple
                value={supportedConnectors}
                onChange={(e) =>
                  setSupportedConnectors(e.target.value as string[])
                }
                renderValue={(selected) => (
                  <Box sx={{ display: "flex", flexWrap: "wrap", gap: 0.5 }}>
                    {selected.map((value) => (
                      <Chip key={value} label={value} />
                    ))}
                  </Box>
                )}
              >
                {["CCS", "CHAdeMO", "Type 2", "Type 1"].map((type) => (
                  <MenuItem key={type} value={type}>
                    {type}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid>
            <Typography gutterBottom>Min Charging Speed (kW)</Typography>
            <Slider
              value={minChargingSpeed}
              onChange={(_, value) => setMinChargingSpeed(value as number)}
              min={0}
              max={350}
              step={10}
              valueLabelDisplay="auto"
            />
          </Grid>
        </Grid>
      </Paper>

      <Paper
        elevation={3}
        sx={{
          height: "calc(100vh - 100px)", // Adjusted to account for padding
          width: "100%",
          overflow: "hidden",
          borderRadius: "8px",
        }}
      >
        <MapContainer
          center={userLocation}
          zoom={13}
          style={{ height: "100%", width: "100%" }}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          <MapUpdater center={userLocation} />
          {filteredStations.length === 0 ? (
            <Box
              sx={{
                position: "absolute",
                top: "50%",
                left: "50%",
                transform: "translate(-50%, -50%)",
                backgroundColor: "rgba(255, 255, 255, 0.9)",
                padding: "20px",
                borderRadius: "8px",
                boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
                zIndex: 1000,
                textAlign: "center",
              }}
            >
              <Typography variant="h6" color="text.secondary">
                No stations found with these filters
              </Typography>
            </Box>
          ) : (
            filteredStations.map((station) => (
              <Marker
                key={station.id}
                position={[station.latitude, station.longitude]}
                icon={defaultIcon}
              >
                <Popup>
                  <Box sx={{ p: 1, minWidth: 200 }}>
                    <Typography variant="h6" gutterBottom>
                      {station.name}
                    </Typography>
                    <Typography variant="body2" paragraph>
                      {station.location}
                    </Typography>
                    <Typography variant="body2">
                      Price: ${station.pricePerKwh}/kWh
                    </Typography>
                    <Typography variant="body2">
                      Speed: {station.chargers[0].chargingSpeedKw} kW
                    </Typography>
                    <Box
                      sx={{
                        mt: 1,
                        display: "flex",
                        flexWrap: "wrap",
                        gap: 0.5,
                      }}
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
                    <Button
                      variant="contained"
                      size="small"
                      fullWidth
                      sx={{ mt: 2 }}
                      onClick={() => handleStationClick(station.id)}
                    >
                      Book Now
                    </Button>
                  </Box>
                </Popup>
              </Marker>
            ))
          )}
        </MapContainer>
      </Paper>
    </Box>
  );
};

export default ChargingStationMap;
