import React from 'react';
import { Box, Typography, Paper, Button, Container } from '@mui/material';
import { Grid } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import ElectricBoltIcon from '@mui/icons-material/ElectricBolt';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import SpeedIcon from '@mui/icons-material/Speed';
import SecurityIcon from '@mui/icons-material/Security';

const Home: React.FC = () => {
    const navigate = useNavigate();

    const features = [
        {
            icon: <ElectricBoltIcon sx={{ fontSize: 40, color: '#2196f3' }} />,
            title: 'Fast Charging',
            description: 'Access high-speed charging stations across the city'
        },
        {
            icon: <LocationOnIcon sx={{ fontSize: 40, color: '#4caf50' }} />,
            title: 'Easy to Find',
            description: 'Locate charging stations near you with our interactive map'
        },
        {
            icon: <SpeedIcon sx={{ fontSize: 40, color: '#ff9800' }} />,
            title: 'Real-time Status',
            description: 'Check station availability and charging speed in real-time'
        },
        {
            icon: <SecurityIcon sx={{ fontSize: 40, color: '#f44336' }} />,
            title: 'Secure Payment',
            description: 'Safe and convenient payment options for all users'
        }
    ];

  return (
        <Box sx={{ 
            minHeight: '100vh',
            background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
            pt: { xs: 8, sm: 10 },
            pb: 8
        }}>
            {/* Hero Section */}
            <Container maxWidth="lg">
                <Box sx={{ 
                    textAlign: 'center', 
                    mb: 8,
                    position: 'relative',
                    overflow: 'hidden'
                }}>
                    <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.8 }}
                    >
                        <Typography 
                            variant="h2" 
                            component="h1" 
                            sx={{ 
                                fontWeight: 'bold',
                                mb: 2,
                                background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
                                backgroundClip: 'text',
                                textFillColor: 'transparent',
                                WebkitBackgroundClip: 'text',
                                WebkitTextFillColor: 'transparent'
                            }}
                        >
                            EV Charging Platform
                        </Typography>
                        <Typography 
                            variant="h5" 
                            color="text.secondary" 
                            sx={{ mb: 4, maxWidth: '800px', mx: 'auto' }}
                        >
                            Power your journey with our extensive network of charging stations.
                            Find, charge, and go with ease.
                        </Typography>
                        <Button 
                            variant="contained" 
                            size="large"
                            onClick={() => navigate('/map')}
                            sx={{ 
                                px: 4, 
                                py: 1.5,
                                borderRadius: 2,
                                background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
                                '&:hover': {
                                    background: 'linear-gradient(45deg, #1976D2 30%, #1E88E5 90%)'
                                }
                            }}
                        >
                            Find Charging Stations
                        </Button>
                    </motion.div>
                </Box>

                {/* Features Grid */}
                <Grid container spacing={4} sx={{ mb: 8 }}>
                    {features.map((feature, index) => (
                        <Grid component="div" key={index}>
                            <motion.div
                                initial={{ opacity: 0, y: 20 }}
                                animate={{ opacity: 1, y: 0 }}
                                transition={{ duration: 0.5, delay: index * 0.1 }}
                            >
                                <Paper 
                                    elevation={3}
                                    sx={{ 
                                        p: 3, 
                                        height: '100%',
                                        display: 'flex',
                                        flexDirection: 'column',
                                        alignItems: 'center',
                                        textAlign: 'center',
                                        transition: 'transform 0.2s',
                                        '&:hover': {
                                            transform: 'translateY(-5px)'
                                        }
                                    }}
                                >
                                    {feature.icon}
                                    <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
                                        {feature.title}
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        {feature.description}
                                    </Typography>
                                </Paper>
                            </motion.div>
                        </Grid>
                    ))}
                </Grid>

                {/* Stats Section */}
                <Box sx={{ 
                    display: 'flex', 
                    justifyContent: 'space-around',
                    flexWrap: 'wrap',
                    gap: 4,
                    mb: 8
                }}>
                    {[
                        { value: '100+', label: 'Charging Stations' },
                        { value: '24/7', label: 'Availability' },
                        { value: '50kW+', label: 'Charging Speed' },
                        { value: '1000+', label: 'Happy Users' }
                    ].map((stat, index) => (
                        <motion.div
                            key={index}
                            initial={{ opacity: 0, scale: 0.8 }}
                            animate={{ opacity: 1, scale: 1 }}
                            transition={{ duration: 0.5, delay: index * 0.1 }}
                        >
                            <Box sx={{ textAlign: 'center' }}>
                                <Typography 
                                    variant="h3" 
                                    sx={{ 
                                        fontWeight: 'bold',
                                        color: '#2196f3',
                                        mb: 1
                                    }}
                                >
                                    {stat.value}
                                </Typography>
                                <Typography variant="body1" color="text.secondary">
                                    {stat.label}
                                </Typography>
                            </Box>
                        </motion.div>
                    ))}
                </Box>

                {/* CTA Section */}
                <Paper 
                    elevation={3}
                    sx={{ 
                        p: 6, 
                        textAlign: 'center',
                        background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
                        color: 'white'
                    }}
                >
                    <Typography variant="h4" sx={{ mb: 2 }}>
                        Ready to Start Charging?
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, maxWidth: '600px', mx: 'auto' }}>
                        Join thousands of EV owners who trust our platform for their charging needs.
                    </Typography>
                    <Button 
                        variant="contained" 
                        size="large"
                        onClick={() => navigate('/map')}
                        sx={{ 
                            px: 4, 
                            py: 1.5,
                            borderRadius: 2,
                            backgroundColor: 'white',
                            color: '#2196f3',
                            '&:hover': {
                                backgroundColor: 'rgba(255, 255, 255, 0.9)'
                            }
                        }}
                    >
                        Get Started Now
                    </Button>
                </Paper>
            </Container>
        </Box>
    );
};

export default Home;
