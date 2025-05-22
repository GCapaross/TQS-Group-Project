import React from 'react';
import { motion } from 'framer-motion';
import { Box, Typography, Container } from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import BusinessIcon from '@mui/icons-material/Business';

interface AccountTypeSelectionProps {
  onSelect: (type: 'user' | 'operator') => void;
}

const AccountTypeSelection: React.FC<AccountTypeSelectionProps> = ({ onSelect }) => {
  const cardVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { opacity: 1, y: 0 },
    hover: { 
      scale: 1.05,
      boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
      transition: { duration: 0.2 }
    },
    tap: { scale: 0.95 }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
        pt: { xs: 8, sm: 10 }
      }}
    >
      <Container maxWidth="lg">
        <Typography
          variant="h3"
          component="h1"
          align="center"
          sx={{
            mb: 8,
            fontWeight: 'bold',
            background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
            backgroundClip: 'text',
            textFillColor: 'transparent',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent'
          }}
        >
          Choose Your Account Type
        </Typography>

        <Box
          sx={{
            display: 'flex',
            flexDirection: { xs: 'column', md: 'row' },
            gap: 4,
            justifyContent: 'center',
            alignItems: 'stretch'
          }}
        >
          {/* User Card */}
          <Box sx={{ flex: 1, maxWidth: { md: '450px' } }}>
            <motion.div
              variants={cardVariants}
              initial="hidden"
              animate="visible"
              whileHover="hover"
              whileTap="tap"
            >
              <Box
                sx={{
                  bgcolor: 'white',
                  borderRadius: 4,
                  p: 4,
                  height: '100%',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  transition: 'all 0.3s ease',
                  '&:hover': {
                    transform: 'translateY(-8px)'
                  }
                }}
                onClick={() => onSelect('user')}
              >
                <Box
                  sx={{
                    width: 80,
                    height: 80,
                    borderRadius: '50%',
                    bgcolor: 'primary.light',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    mb: 3
                  }}
                >
                  <PersonIcon sx={{ fontSize: 40, color: 'primary.main' }} />
                </Box>
                <Typography variant="h4" component="h2" sx={{ mb: 2, fontWeight: 'bold' }}>
                  User
                </Typography>
                <Typography variant="body1" color="text.secondary" align="center">
                  Create an account to book charging stations and manage your charging sessions.
                  Perfect for electric vehicle owners looking for convenient charging solutions.
                </Typography>
              </Box>
            </motion.div>
          </Box>

          {/* Operator Card */}
          <Box sx={{ flex: 1, maxWidth: { md: '450px' } }}>
            <motion.div
              variants={cardVariants}
              initial="hidden"
              animate="visible"
              whileHover="hover"
              whileTap="tap"
            >
              <Box
                sx={{
                  bgcolor: 'white',
                  borderRadius: 4,
                  p: 4,
                  height: '100%',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  transition: 'all 0.3s ease',
                  '&:hover': {
                    transform: 'translateY(-8px)'
                  }
                }}
                onClick={() => onSelect('operator')}
              >
                <Box
                  sx={{
                    width: 80,
                    height: 80,
                    borderRadius: '50%',
                    bgcolor: 'secondary.light',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    mb: 3
                  }}
                >
                  <BusinessIcon sx={{ fontSize: 40, color: 'secondary.main' }} />
                </Box>
                <Typography variant="h4" component="h2" sx={{ mb: 2, fontWeight: 'bold' }}>
                  Operator
                </Typography>
                <Typography variant="body1" color="text.secondary" align="center">
                  Create an account to manage charging stations and monitor usage.
                  Ideal for businesses and organizations providing charging infrastructure.
                </Typography>
              </Box>
            </motion.div>
          </Box>
        </Box>
      </Container>
    </Box>
  );
};

export default AccountTypeSelection; 