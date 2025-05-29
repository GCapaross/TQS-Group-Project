import React from 'react';
import { AppBar, Toolbar, Button, Box, Typography } from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import ElectricBoltIcon from '@mui/icons-material/ElectricBolt';
import { useAuth } from '../contexts/AuthContext';

const Navbar: React.FC = () => {
    const navigate = useNavigate();
    const { isAuthenticated, logout } = useAuth();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <AppBar 
            position="fixed" 
            sx={{ 
                boxShadow: 2,
                width: '100vw',
                left: 0,
                right: 0,
                top: 0
            }}
        >
            <Toolbar 
                sx={{ 
                    display: 'flex', 
                    justifyContent: 'space-between',
                    width: '100%',
                    maxWidth: '100%',
                    px: { xs: 1, sm: 2 },
                    '& .MuiButton-root': {
                        color: 'white',
                        mx: 1,
                        '&:hover': {
                            backgroundColor: 'rgba(255, 255, 255, 0.1)'
                        }
                    }
                }}
            >
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <Box 
                        component={RouterLink} 
                        to="/" 
                        sx={{ 
                            display: 'flex', 
                            alignItems: 'center', 
                            textDecoration: 'none',
                            color: 'white',
                            mr: 3
                        }}
                    >
                        <ElectricBoltIcon sx={{ fontSize: 32, mr: 1 }} />
                        <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                            EV Charge
                        </Typography>
                    </Box>
                    <Button component={RouterLink} to="/" color="inherit">
                        Home
                    </Button>
                    <Button component={RouterLink} to="/stations" color="inherit">
                        Stations
                    </Button>
                    <Button component={RouterLink} to="/map" color="inherit">
                        Map View
                    </Button>
                    <Button component={RouterLink} to="/charging-session" color="inherit">
                        Charging Session
                    </Button>
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    {isAuthenticated ? (
                        <Button 
                            onClick={handleLogout}
                            color="inherit"
                            variant="outlined"
                            sx={{ 
                                borderColor: 'white',
                                '&:hover': {
                                    borderColor: 'white',
                                    backgroundColor: 'rgba(255, 255, 255, 0.1)'
                                }
                            }}
                        >
                            Logout
                        </Button>
                    ) : (
                        <>
                            <Button 
                                component={RouterLink} 
                                to="/login" 
                                color="inherit"
                                variant="outlined"
                                sx={{ 
                                    borderColor: 'white',
                                    '&:hover': {
                                        borderColor: 'white',
                                        backgroundColor: 'rgba(255, 255, 255, 0.1)'
                                    }
                                }}
                            >
                                Login
                            </Button>
                            <Button 
                                component={RouterLink} 
                                to="/register" 
                                color="inherit"
                                variant="contained"
                                sx={{ 
                                    ml: 2,
                                    background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
                                    '&:hover': {
                                        background: 'linear-gradient(45deg, #1976D2 30%, #1E88E5 90%)'
                                    }
                                }}
                            >
                                Register
                            </Button>
                        </>
                    )}
                </Box>
            </Toolbar>
        </AppBar>
    );
};

export default Navbar;
