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

// Mock stations data
const mockStations = [
    {
        id: 1,
        name: "Station A",
        location: "Downtown",
        availableSlots: 2,
        maxSlots: 4,
        pricePerKwh: 0.35,
        chargingSpeedKw: 50,
        connectorTypes: ["Type 2", "CCS"],
        status: "AVAILABLE"
    },
    {
        id: 2,
        name: "Station B",
        location: "Shopping Mall",
        availableSlots: 1,
        maxSlots: 2,
        pricePerKwh: 0.40,
        chargingSpeedKw: 100,
        connectorTypes: ["CCS", "CHAdeMO"],
        status: "AVAILABLE"
    },
    {
        id: 3,
        name: "Station C",
        location: "Airport",
        availableSlots: 3,
        maxSlots: 6,
        pricePerKwh: 0.45,
        chargingSpeedKw: 150,
        connectorTypes: ["Type 2", "CCS", "CHAdeMO"],
        status: "AVAILABLE"
    }
];

const ChargingSessionPage: React.FC = () => {
    const theme = useTheme();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [showStationDialog, setShowStationDialog] = useState(false);
    const [showStopConfirmation, setShowStopConfirmation] = useState(false);
    const [sessionData, setSessionData] = useState<SessionData | null>(null);
    const [powerHistory, setPowerHistory] = useState<number[]>([]);
    const [currentHistory, setCurrentHistory] = useState<number[]>([]);
    const [timeLabels, setTimeLabels] = useState<string[]>([]);
    const navigate = useNavigate();

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

    const startChargingSession = async (station: typeof mockStations[0]) => {
        try {
            // Generate random initial battery level between 10% and 30%
            const initialBatteryLevel = Math.floor(Math.random() * 20) + 10;
            const targetBatteryLevel = 100;
            
            // Calculate estimated time based on charging speed and battery difference
            const batteryCapacity = 75; // kWh (typical EV battery)
            const energyNeeded = (targetBatteryLevel - initialBatteryLevel) * batteryCapacity / 100;
            const estimatedHours = energyNeeded / station.chargingSpeedKw;
            const estimatedEndTime = new Date(Date.now() + estimatedHours * 3600 * 1000);

            const newSession: SessionData = {
                stationId: station.id,
                stationName: station.name,
                batteryLevel: initialBatteryLevel,
                targetBatteryLevel,
                chargingSpeed: station.chargingSpeedKw,
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
        }
    };

    const updateSessionProgress = () => {
        if (!sessionData) return;

        const now = new Date();
        const elapsedHours = (now.getTime() - sessionData.startTime.getTime()) / (1000 * 3600);
        const energyAdded = elapsedHours * sessionData.chargingSpeed;
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
            const newCost = energyAdded * 0.35;
            return {
                ...prev,
                batteryLevel: newBatteryLevel,
                energySupplied: energyAdded,
                cost: newCost,
                isActive: newBatteryLevel < 100
            };
        });

        if (newBatteryLevel >= 100) {
            stopChargingSession();
        }
    };

    const stopChargingSession = async () => {
        if (!sessionData) return;
        setSessionData(null);
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

                <Dialog open={showStationDialog} onClose={() => setShowStationDialog(false)} maxWidth="md" fullWidth>
                    <DialogTitle>Select Charging Station</DialogTitle>
                    <DialogContent>
                        <Grid container spacing={2} sx={{ mt: 1 }}>
                            {mockStations
                                .filter(station => station.availableSlots > 0 && station.status === 'AVAILABLE')
                                .map((station) => (
                                    <Grid item xs={12} sm={6} md={4} key={station.id}>
                                        <Card>
                                            <CardContent>
                                                <Typography variant="h6" gutterBottom>
                                                    {station.name}
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    {station.location}
                                                </Typography>
                                                <Typography variant="body2">
                                                    Available: {station.availableSlots}/{station.maxSlots}
                                                </Typography>
                                                <Typography variant="body2">
                                                    ${station.pricePerKwh}/kWh
                                                </Typography>
                                                <Typography variant="body2">
                                                    {station.chargingSpeedKw} kW
                                                </Typography>
                                                <Typography variant="body2" noWrap>
                                                    {station.connectorTypes.join(', ')}
                                                </Typography>
                                            </CardContent>
                                            <CardActions>
                                                <Button
                                                    fullWidth
                                                    variant="contained"
                                                    color="primary"
                                                    onClick={() => startChargingSession(station)}
                                                >
                                                    Select
                                                </Button>
                                            </CardActions>
                                        </Card>
                                    </Grid>
                                ))}
                        </Grid>
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
                    <Grid item xs={12}>
                        <Box sx={{ mt: 2, mb: 2 }}>
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
                        </Box>
                    </Grid>

                    {/* Power and Current Charts */}
                    <Grid item xs={12} md={6}>
                        <Paper elevation={2} sx={{ p: 2, height: 200 }}>
                            <Typography variant="subtitle2" gutterBottom>
                                Power Consumption
                            </Typography>
                            <Box sx={{ height: 'calc(100% - 30px)' }}>
                                <Line data={powerChartData} options={chartOptions} />
                            </Box>
                        </Paper>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Paper elevation={2} sx={{ p: 2, height: 200 }}>
                            <Typography variant="subtitle2" gutterBottom>
                                Current Flow
                            </Typography>
                            <Box sx={{ height: 'calc(100% - 30px)' }}>
                                <Line data={currentChartData} options={chartOptions} />
                            </Box>
                        </Paper>
                    </Grid>

                    {/* Session Details */}
                    <Grid item xs={12}>
                        <Paper elevation={2} sx={{ p: 2 }}>
                            <Grid container spacing={2}>
                                <Grid item xs={6} sm={3}>
                                    <Typography variant="body2" color="text.secondary">
                                        Energy Supplied
                                    </Typography>
                                    <Typography variant="h6">
                                        {sessionData.energySupplied.toFixed(2)} kWh
                                    </Typography>
                                </Grid>
                                <Grid item xs={6} sm={3}>
                                    <Typography variant="body2" color="text.secondary">
                                        Current Cost
                                    </Typography>
                                    <Typography variant="h6">
                                        ${sessionData.cost.toFixed(2)}
                                    </Typography>
                                </Grid>
                                <Grid item xs={6} sm={3}>
                                    <Typography variant="body2" color="text.secondary">
                                        Charging Speed
                                    </Typography>
                                    <Typography variant="h6">
                                        {sessionData.chargingSpeed} kW
                                    </Typography>
                                </Grid>
                                <Grid item xs={6} sm={3}>
                                    <Typography variant="body2" color="text.secondary">
                                        Time Remaining
                                    </Typography>
                                    <Typography variant="h6">
                                        {sessionData.batteryLevel < 100 
                                            ? new Date(sessionData.estimatedEndTime.getTime() - Date.now()).toISOString().substr(11, 8)
                                            : 'Complete'
                                        }
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Paper>
                    </Grid>

                    {/* Stop Button */}
                    <Grid item xs={12}>
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
                    </Grid>
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