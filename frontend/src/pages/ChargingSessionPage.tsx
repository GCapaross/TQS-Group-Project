import React, { useState, useEffect } from 'react';
import {
    Box,
    Container,
    Typography,
    Paper,
    Button,
    Alert,
    CircularProgress,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Grid,
    Card,
    CardContent,
    CardActions,
    LinearProgress,
    useTheme
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import AddIcon from '@mui/icons-material/Add';
import { Line } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
} from 'chart.js';
import { useAuth } from '../contexts/AuthContext';
import { chargingStationApi } from '../services/api';
import { ChargingStation } from '../types/ChargingStation';
import { bookingApi } from '../services/api';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
);

interface SessionData {
    stationId: number;
    stationName: string;
    batteryLevel: number;
    targetBatteryLevel: number;
    chargingSpeed: number;
    startTime: Date;
    estimatedEndTime: Date;
    energySupplied: number;
    cost: number;
    isActive: boolean;
}

const ChargingSessionPage: React.FC = () => {
    const theme = useTheme();
    const navigate = useNavigate();
    const { isAuthenticated } = useAuth();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [showStationDialog, setShowStationDialog] = useState(false);
    const [showStopConfirmation, setShowStopConfirmation] = useState(false);
    const [sessionData, setSessionData] = useState<SessionData | null>(null);
    const [powerHistory, setPowerHistory] = useState<number[]>([]);
    const [currentHistory, setCurrentHistory] = useState<number[]>([]);
    const [timeLabels, setTimeLabels] = useState<string[]>([]);
    const [stations, setStations] = useState<ChargingStation[]>([]);

    useEffect(() => {
        const fetchStations = async () => {
            try {
                console.log('Fetching stations...');
                const data = await chargingStationApi.getAll();
                console.log('Stations fetched:', data);
                if (!data || data.length === 0) {
                    console.warn('No stations returned from API');
                }
                setStations(data);
            } catch (err) {
                console.error('Error fetching stations:', err);
                if (err instanceof Error) {
                    setError(`Failed to load charging stations: ${err.message}`);
                } else {
                    setError('Failed to load charging stations');
                }
            }
        };

        fetchStations();
    }, []); // Fetch on component mount

    useEffect(() => {
        let interval: number | undefined;
        if (sessionData?.isActive) {
            interval = window.setInterval(() => {
                updateSessionProgress();
            }, 1000);
        }
        return () => {
            if (interval) clearInterval(interval);
        };
    }, [sessionData]);

    const startChargingSession = async (station: ChargingStation) => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }

        try {
            setLoading(true);
            
            // Create a booking for immediate use
            const startTime = new Date();
            const endTime = new Date(startTime.getTime() + 2 * 60 * 60 * 1000); // 2 hours from now
            
            const booking = await bookingApi.create({
                stationId: station.id,
                startTime: startTime,
                endTime: endTime,
                estimatedEnergy: 50 // Default estimated energy
            });

            // Start the charging session with the booking ID
            await chargingStationApi.startChargingSession(station.id, booking.id);

            // Generate random initial battery level between 10% and 30%
            const initialBatteryLevel = Math.floor(Math.random() * 20) + 10;
            const targetBatteryLevel = 100;
            
            // Calculate estimated time based on charging speed and battery difference
            const batteryCapacity = 75; // kWh (typical EV battery)
            const energyNeeded = (targetBatteryLevel - initialBatteryLevel) * batteryCapacity / 100;
            const estimatedHours = energyNeeded / 50; // Using a default charging speed of 50kW
            const estimatedEndTime = new Date(Date.now() + estimatedHours * 3600 * 1000);

            const newSession: SessionData = {
                stationId: station.id,
                stationName: station.name,
                batteryLevel: initialBatteryLevel,
                targetBatteryLevel,
                chargingSpeed: 50, // Default charging speed
                startTime: new Date(),
                estimatedEndTime,
                energySupplied: 0,
                cost: 0,
                isActive: true
            };

            setSessionData(newSession);
            setShowStationDialog(false);
        } catch (err) {
            setError('Failed to start charging session');
            console.error('Error starting session:', err);
        } finally {
            setLoading(false);
        }
    };

    const updateSessionProgress = async () => {
        if (!sessionData) return;

        try {
            const activeSession = await chargingStationApi.getActiveSession(sessionData.stationId);
            
            const now = new Date();
            const elapsedHours = (now.getTime() - sessionData.startTime.getTime()) / (1000 * 3600);
            const energyAdded = activeSession.energySupplied;
            const batteryCapacity = 75; // kWh
            const newBatteryLevel = Math.min(
                100,
                sessionData.batteryLevel + (energyAdded / batteryCapacity) * 100
            );

            // Update power and current history
            const currentPower = sessionData.chargingSpeed;
            const current = (currentPower * 1000) / 400; // Assuming 400V system
            const timeLabel = now.toLocaleTimeString();

            setPowerHistory(prev => [...prev, currentPower].slice(-20));
            setCurrentHistory(prev => [...prev, current].slice(-20));
            setTimeLabels(prev => [...prev, timeLabel].slice(-20));

            setSessionData(prev => {
                if (!prev) return null;
                return {
                    ...prev,
                    batteryLevel: newBatteryLevel,
                    energySupplied: energyAdded,
                    cost: activeSession.cost,
                    isActive: activeSession.isActive
                };
            });

            if (!activeSession.isActive) {
                stopChargingSession();
            }
        } catch (err) {
            console.error('Error updating session progress:', err);
        }
    };

    const stopChargingSession = async () => {
        if (!sessionData) return;
        
        try {
            await chargingStationApi.stopChargingSession(sessionData.stationId);
            setSessionData(null);
        } catch (err) {
            setError('Failed to stop charging session');
            console.error('Error stopping session:', err);
        }
    };

    const handleStopClick = () => {
        setShowStopConfirmation(true);
    };

    const handleStopConfirm = async () => {
        await stopChargingSession();
        setShowStopConfirmation(false);
    };

    const powerChartData = {
        labels: timeLabels,
        datasets: [
            {
                label: 'Power (kW)',
                data: powerHistory,
                borderColor: theme.palette.primary.main,
                backgroundColor: theme.palette.primary.light,
                tension: 0.4,
                fill: true
            }
        ]
    };

    const currentChartData = {
        labels: timeLabels,
        datasets: [
            {
                label: 'Current (A)',
                data: currentHistory,
                borderColor: theme.palette.secondary.main,
                backgroundColor: theme.palette.secondary.light,
                tension: 0.4,
                fill: true
            }
        ]
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            y: {
                beginAtZero: true
            }
        },
        animation: {
            duration: 0
        }
    };

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
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

    if (!sessionData) {
        return (
            <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
                <Box sx={{ textAlign: 'center', mt: 4 }}>
                    {!isAuthenticated ? (
                        <Alert severity="info" sx={{ mb: 2 }}>
                            Please log in to start a charging session.
                        </Alert>
                    ) : null}
                    <Button
                        variant="contained"
                        color="primary"
                        size="large"
                        startIcon={<AddIcon />}
                        onClick={() => setShowStationDialog(true)}
                    >
                        Start Charging Session
                    </Button>
                </Box>

                <Dialog open={showStationDialog} onClose={() => setShowStationDialog(false)} maxWidth="xl" fullWidth>
                    <DialogTitle>Select Charging Station</DialogTitle>
                    <DialogContent>
                        <Box sx={{ 
                            display: 'grid', 
                            gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)', lg: 'repeat(4, 1fr)' },
                            gap: 2,
                            p: 2
                        }}>
                            {stations.map((station) => (
                                <Card key={station.id} sx={{ 
                                    height: '100%',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    transition: 'transform 0.2s, box-shadow 0.2s',
                                    '&:hover': {
                                        transform: 'translateY(-4px)',
                                        boxShadow: 6
                                    }
                                }}>
                                    <CardContent sx={{ flexGrow: 1 }}>
                                        <Typography variant="h6" gutterBottom noWrap>
                                            {station.name}
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary" gutterBottom>
                                            {station.location}
                                        </Typography>
                                        <Box sx={{ mt: 2 }}>
                                            <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                                <strong>Price:</strong>&nbsp;${station.pricePerKwh}/kWh
                                            </Typography>
                                            <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                                <strong>Connectors:</strong>&nbsp;{station.supportedConnectors.join(', ')}
                                            </Typography>
                                            <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center' }}>
                                                <strong>Schedule:</strong>&nbsp;{station.timetable}
                                            </Typography>
                                        </Box>
                                    </CardContent>
                                    <CardActions sx={{ p: 2, pt: 0 }}>
                                        <Button
                                            fullWidth
                                            variant="contained"
                                            color="primary"
                                            onClick={() => startChargingSession(station)}
                                            sx={{
                                                py: 1,
                                                background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
                                                '&:hover': {
                                                    background: 'linear-gradient(45deg, #1976D2 30%, #1E88E5 90%)'
                                                }
                                            }}
                                        >
                                            Select Station
                                        </Button>
                                    </CardActions>
                                </Card>
                            ))}
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setShowStationDialog(false)}>Cancel</Button>
                    </DialogActions>
                </Dialog>
            </Container>
        );
    }

    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
            {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                </Alert>
            )}

            <Paper elevation={3} sx={{ p: 3 }}>
                <Typography variant="h5" gutterBottom>
                    Active Charging Session
                </Typography>
                <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                    {sessionData.stationName}
                </Typography>

                <Grid container spacing={3}>
                    {/* Battery Level */}
                    <Box sx={{ width: '100%', p: 1 }}>
                        <Paper sx={{ p: 2 }}>
                            <Typography variant="body2" color="text.secondary" gutterBottom>
                                Battery Level
                            </Typography>
                            <LinearProgress 
                                variant="determinate" 
                                value={sessionData.batteryLevel} 
                                sx={{ height: 10, borderRadius: 5 }}
                            />
                            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                                {sessionData.batteryLevel.toFixed(1)}%
                            </Typography>
                        </Paper>
                    </Box>

                    {/* Power and Current Charts */}
                    <Box sx={{ width: { xs: '100%', md: '50%' }, p: 1 }}>
                        <Paper sx={{ p: 2, height: '100%' }}>
                            <Typography variant="subtitle2" gutterBottom>
                                Power Consumption
                            </Typography>
                            <Box sx={{ height: 'calc(100% - 30px)' }}>
                                <Line data={powerChartData} options={chartOptions} />
                            </Box>
                        </Paper>
                    </Box>

                    <Box sx={{ width: { xs: '100%', md: '50%' }, p: 1 }}>
                        <Paper sx={{ p: 2, height: '100%' }}>
                            <Typography variant="subtitle2" gutterBottom>
                                Current Flow
                            </Typography>
                            <Box sx={{ height: 'calc(100% - 30px)' }}>
                                <Line data={currentChartData} options={chartOptions} />
                            </Box>
                        </Paper>
                    </Box>

                    {/* Session Details */}
                    <Box sx={{ width: '100%', p: 1 }}>
                        <Paper sx={{ p: 2 }}>
                            <Grid container spacing={2}>
                                <Box sx={{ width: { xs: '50%', sm: '25%' }, p: 1 }}>
                                    <Typography variant="body2" color="text.secondary">
                                        Energy Supplied
                                    </Typography>
                                    <Typography variant="h6">
                                        {sessionData.energySupplied.toFixed(2)} kWh
                                    </Typography>
                                </Box>
                                <Box sx={{ width: { xs: '50%', sm: '25%' }, p: 1 }}>
                                    <Typography variant="body2" color="text.secondary">
                                        Current Cost
                                    </Typography>
                                    <Typography variant="h6">
                                        ${sessionData.cost.toFixed(2)}
                                    </Typography>
                                </Box>
                                <Box sx={{ width: { xs: '50%', sm: '25%' }, p: 1 }}>
                                    <Typography variant="body2" color="text.secondary">
                                        Charging Speed
                                    </Typography>
                                    <Typography variant="h6">
                                        {sessionData.chargingSpeed} kW
                                    </Typography>
                                </Box>
                                <Box sx={{ width: { xs: '50%', sm: '25%' }, p: 1 }}>
                                    <Typography variant="body2" color="text.secondary">
                                        Time Remaining
                                    </Typography>
                                    <Typography variant="h6">
                                        {sessionData.batteryLevel < 100 
                                            ? new Date(sessionData.estimatedEndTime.getTime() - Date.now()).toISOString().substr(11, 8)
                                            : 'Complete'
                                        }
                                    </Typography>
                                </Box>
                            </Grid>
                        </Paper>
                    </Box>

                    {/* Stop Button */}
                    <Box sx={{ width: '100%', p: 1 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                            <Button
                                variant="contained"
                                color="error"
                                size="large"
                                onClick={handleStopClick}
                                disabled={!sessionData.isActive}
                                sx={{ 
                                    minWidth: 200,
                                    '&:hover': {
                                        backgroundColor: 'error.dark'
                                    }
                                }}
                            >
                                Stop Charging Session
                            </Button>
                        </Box>
                    </Box>
                </Grid>
            </Paper>

            {/* Stop Confirmation Dialog */}
            <Dialog 
                open={showStopConfirmation} 
                onClose={() => setShowStopConfirmation(false)}
            >
                <DialogTitle>Stop Charging Session?</DialogTitle>
                <DialogContent>
                    <Typography>
                        Are you sure you want to stop the charging session? This action cannot be undone.
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                        Current battery level: {sessionData?.batteryLevel.toFixed(1)}%
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setShowStopConfirmation(false)}>Cancel</Button>
                    <Button 
                        onClick={handleStopConfirm} 
                        color="error" 
                        variant="contained"
                    >
                        Stop Session
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
};

export default ChargingSessionPage; 