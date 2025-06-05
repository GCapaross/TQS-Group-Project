import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  TextField,
  InputLabel,
  MenuItem,
  FormControl,
  Select,
  Button,
  Typography,
  CircularProgress,
  Alert,
  Chip,
  Grid,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

interface ChargerInput {
  id: number;
  status: 'AVAILABLE' | 'IN_USE' | 'OUT_OF_SERVICE';
  chargingSpeedKw: number;
}

interface NewStationPayload {
  name: string;
  location: string;
  latitude: number;
  longitude: number;
  pricePerKwh: number;
  supportedConnectors: string[];
  timetable: string;
  companyName: string;
  workerIds: number[];
  chargers: ChargerInput[];
}

const CreateStation: React.FC = () => {
  const [name, setName] = useState('');
  const [location, setLocation] = useState('');
  const [latitude, setLatitude] = useState<number | ''>('');
  const [longitude, setLongitude] = useState<number | ''>('');
  const [pricePerKwh, setPricePerKwh] = useState<number | ''>('');
  const [supportedConnectorsInput, setSupportedConnectorsInput] = useState(''); // comma-separated
  const [timetable, setTimetable] = useState('');
  const [companyName, setCompanyName] = useState('');
  const [workerIdsInput, setWorkerIdsInput] = useState(''); // comma-separated

  const [chargerIdInput, setChargerIdInput] = useState<number | ''>('');
  const [chargerStatusInput, setChargerStatusInput] = useState<ChargerInput['status']>('AVAILABLE');
  const [chargerSpeedInput, setChargerSpeedInput] = useState<number | ''>('');
  const [chargers, setChargers] = useState<ChargerInput[]>([]);

  const [companies, setCompanies] = useState<string[]>([]);
  const [companiesLoading, setCompaniesLoading] = useState(true);
  const [companiesError, setCompaniesError] = useState<string | null>(null);

  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    const fetchCompanies = async () => {
      try {
        const response = await axios.get<string[]>('http://localhost:8080/companies/names', {
          headers: { Accept: '*/*' },
        });
        setCompanies(response.data);
      } catch (err) {
        console.error('Error fetching company names:', err);
        setCompaniesError('Failed to load company names. Please try again later.');
      } finally {
        setCompaniesLoading(false);
      }
    };

    fetchCompanies();
  }, []);

  const handleAddCharger = () => {
    if (
      chargerIdInput === '' ||
      chargerSpeedInput === '' ||
      Number(chargerSpeedInput) <= 0
    ) {
      return;
    }

    const newCharger: ChargerInput = {
      id: Number(chargerIdInput),
      status: chargerStatusInput,
      chargingSpeedKw: Number(chargerSpeedInput),
    };

    setChargers(prev => [...prev, newCharger]);
    setChargerIdInput('');
    setChargerStatusInput('AVAILABLE');
    setChargerSpeedInput('');
  };

  const handleRemoveCharger = (index: number) => {
    setChargers(prev => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitError(null);

    if (
      !name.trim() ||
      !location.trim() ||
      latitude === '' ||
      longitude === '' ||
      pricePerKwh === '' ||
      !supportedConnectorsInput.trim() ||
      !timetable.trim() ||
      !companyName.trim()
    ) {
      setSubmitError('Please fill in all required fields.');
      return;
    }

    const supportedConnectors = supportedConnectorsInput
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0);

    const workerIds = workerIdsInput
      .split(',')
      .map(w => w.trim())
      .filter(w => w !== '')
      .map(w => Number(w))
      .filter(n => !isNaN(n));

    const payload: NewStationPayload = {
      name: name.trim(),
      location: location.trim(),
      latitude: Number(latitude),
      longitude: Number(longitude),
      pricePerKwh: Number(pricePerKwh),
      supportedConnectors,
      timetable: timetable.trim(),
      companyName,
      workerIds,
      chargers,
    };

    setSubmitting(true);
    try {
      await axios.post('http://localhost:8080/api/charging-stations', payload, {
        headers: { 'Content-Type': 'application/json' },
      });
      navigate('/stations');
    } catch (err) {
      console.error('Error creating station:', err);
      setSubmitError('Failied to create station. Please check your input and try again.');
      setSubmitting(false);
    }
  };

  if (companiesLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="sm" sx={{ mt: 4, mb: 4 }}>
      <Box component="form" onSubmit={handleSubmit} noValidate>
        <Typography variant="h4" component="h1" gutterBottom>
          Create New Station
        </Typography>

        {companiesError && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {companiesError}
          </Alert>
        )}

        {submitError && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {submitError}
          </Alert>
        )}

        <TextField
          fullWidth
          label="Name"
          value={name}
          onChange={e => setName(e.target.value)}
          sx={{ mb: 2 }}
        />

        <TextField
          fullWidth
          label="Location"
          value={location}
          onChange={e => setLocation(e.target.value)}
          sx={{ mb: 2 }}
        />

        <TextField
          fullWidth
          label="Latitude"
          type="number"
          value={latitude}
          onChange={e => setLatitude(e.target.value === '' ? '' : Number(e.target.value))}
          sx={{ mb: 2 }}
        />

        <TextField
          fullWidth
          label="Longitude"
          type="number"
          value={longitude}
          onChange={e => setLongitude(e.target.value === '' ? '' : Number(e.target.value))}
          sx={{ mb: 2 }}
        />

        <TextField
          fullWidth
          label="Price Per kWh"
          type="number"
          value={pricePerKwh}
          onChange={e => setPricePerKwh(e.target.value === '' ? '' : Number(e.target.value))}
          sx={{ mb: 2 }}
        />

        <TextField
          fullWidth
          label="Supported Connectors (vírgula-separados)"
          placeholder="ex: Type2, CHAdeMO, CCS"
          value={supportedConnectorsInput}
          onChange={e => setSupportedConnectorsInput(e.target.value)}
          sx={{ mb: 2 }}
        />

        <TextField
          fullWidth
          label="Timetable"
          placeholder="ex: 08:00-18:00"
          value={timetable}
          onChange={e => setTimetable(e.target.value)}
          sx={{ mb: 2 }}
        />

        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel id="company-select-label">Company</InputLabel>
          <Select
            labelId="company-select-label"
            value={companyName}
            label="Company"
            onChange={e => setCompanyName(e.target.value)}
          >
            {companies.map(company => (
              <MenuItem key={company} value={company}>
                {company}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <TextField
          fullWidth
          label="Worker IDs (vírgula-separados)"
          placeholder="ex: 1, 2, 3"
          value={workerIdsInput}
          onChange={e => setWorkerIdsInput(e.target.value)}
          sx={{ mb: 3 }}
        />

        <Typography variant="h6" gutterBottom>
          Adicionar Charger
        </Typography>

        <Grid container spacing={2} sx={{ mb: 2 }}>
          <Grid>
            <TextField
              fullWidth
              label="Charger ID"
              type="number"
              value={chargerIdInput}
              onChange={e => setChargerIdInput(e.target.value === '' ? '' : Number(e.target.value))}
            />
          </Grid>
          <Grid>
            <FormControl fullWidth>
              <InputLabel id="charger-status-label">Status</InputLabel>
              <Select
                labelId="charger-status-label"
                value={chargerStatusInput}
                label="Status"
                onChange={e => setChargerStatusInput(e.target.value as ChargerInput['status'])}
              >
                <MenuItem value="AVAILABLE">AVAILABLE</MenuItem>
                <MenuItem value="CHARGING">CHARGING</MenuItem>
                <MenuItem value="OUT_OF_SERVICE">OUT_OF_SERVICE</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid>
            <TextField
              fullWidth
              label="Speed (kW)"
              type="number"
              value={chargerSpeedInput}
              onChange={e => setChargerSpeedInput(e.target.value === '' ? '' : Number(e.target.value))}
            />
          </Grid>
        </Grid>

        <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
          <Button variant="outlined" onClick={handleAddCharger}>
            Adicionar Charger
          </Button>
        </Box>

        {chargers.length > 0 && (
          <Box sx={{ mb: 3 }}>
            {chargers.map((c, idx) => (
              <Chip
                key={idx}
                label={`ID: ${c.id} – ${c.status}, ${c.chargingSpeedKw} kW`}
                onDelete={() => handleRemoveCharger(idx)}
                sx={{ mr: 1, mb: 1 }}
              />
            ))}
          </Box>
        )}

        <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            type="submit"
            variant="contained"
            color="primary"
            disabled={submitting}
          >
            {submitting ? 'Creating...' : 'Create Station'}
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default CreateStation;
