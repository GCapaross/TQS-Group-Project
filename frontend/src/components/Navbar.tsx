import React from 'react';
import { AppBar, Toolbar, Button, Box, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import ElectricBoltIcon from '@mui/icons-material/ElectricBolt';

const Navbar: React.FC = () => {
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
                </Box>
            </Toolbar>
        </AppBar>
    );
};

export default Navbar;
