import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const bookingDuration = new Trend('booking_duration');
const loginDuration = new Trend('login_duration');
const stationSearchDuration = new Trend('station_search_duration');

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '30s', target: 10 },
  ],
  thresholds: {
    'errors': ['rate<0.1'],
    'booking_duration': ['p(95)<2000'],
    'login_duration': ['p(95)<1000'],
    'station_search_duration': ['p(95)<1500'],
  },
};

const BASE_URL = 'http://backend:8080';
const TEST_USER = {
  username: `testuser_${__VU}`,
  password: 'testpass123',
  confirmPassword: 'testpass123',
  email: `testuser_${__VU}@test.com`,
  accountType: 'USER'
};

function registerUser() {
  const registerRes = http.post(`${BASE_URL}/api/users/register`, JSON.stringify(TEST_USER), {
    headers: { 'Content-Type': 'application/json' },
  });
  
  console.log(`Registration response: ${registerRes.status} - ${registerRes.body}`);
  
  check(registerRes, {
    'registration successful': (r) => r.status === 200 || r.status === 409,
  });
}

function getToken() {
  const loginRes = http.post(`${BASE_URL}/api/users/login`, JSON.stringify({
    email: TEST_USER.email,
    password: TEST_USER.password
  }), {
    headers: { 'Content-Type': 'application/json' },
  });
  
  console.log(`Login response: ${loginRes.status} - ${loginRes.body}`);
  
  check(loginRes, {
    'login successful': (r) => r.status === 200,
  });
  
  if (loginRes.status === 200) {
    const cookies = loginRes.cookies;
    return cookies.JWT_TOKEN[0].value;
  }
  return null;
}

export default function() {
  registerUser();
  const token = getToken();
  if (!token) {
    errorRate.add(1);
    return;
  }

  const headers = {
    'Content-Type': 'application/json',
    'Cookie': `JWT_TOKEN=${token}`
  };

  const searchStart = new Date();
  const searchRes = http.get(`${BASE_URL}/api/charging-stations`, { headers });
  stationSearchDuration.add(new Date() - searchStart);
  
  console.log(`Search response: ${searchRes.status} - ${searchRes.body}`);
  
  check(searchRes, {
    'search successful': (r) => r.status === 200,
  });

  if (searchRes.status === 200) {
    const stations = searchRes.json();
    if (stations && stations.length > 0) {
      const now = new Date();
      const startTime = new Date(now.getTime() + 3600000); // 1 hour from now
      const endTime = new Date(now.getTime() + 7200000);   // 2 hours from now
      
      const bookingStart = new Date();
      const bookingRes = http.post(`${BASE_URL}/api/bookings`,
        JSON.stringify({
          stationId: stations[0].id,
          startTime: startTime.toISOString(),
          endTime: endTime.toISOString()
        }),
        { headers }
      );
      bookingDuration.add(new Date() - bookingStart);
      
      console.log(`Booking response: ${bookingRes.status} - ${bookingRes.body}`);
      
      check(bookingRes, {
        'booking successful': (r) => r.status === 200,
      });
    }
  }

  sleep(1);
} 