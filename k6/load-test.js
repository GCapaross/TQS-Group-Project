import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';

const errorRate = new Rate('errors');
const userCounter = new Counter('user_counter');

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '30s', target: 10 },
  ],
  thresholds: {
    'errors': ['rate<0.1'],
  },
};

const BASE_URL = 'http://backend:8080';

let userCount = 0;

function formatDate(date) {
  // Format as YYYY-MM-DDTHH:mm:ss without milliseconds
  return date.toISOString().split('.')[0];
}

export default function() {
  userCount++;
  userCounter.add(1);
  
  const userId = `${__VU}_${__ITER}_${Date.now()}_${userCount}`;
  
  const payload = {
    username: `testuser_${userId}`,
    password: 'testpass123',
    confirmPassword: 'testpass123',
    email: `testuser_${userId}@test.com`,
    accountType: 'user'
  };
  
  const registerRes = http.post(`${BASE_URL}/api/users/register`, JSON.stringify(payload), {
    headers: { 'Content-Type': 'application/json' },
  });
  
  console.log(`Registration response: ${registerRes.status} - ${registerRes.body}`);
  
  check(registerRes, {
    'registration successful': (r) => r.status === 200 || r.status === 409,
  });
  
  const loginPayload = {
    email: payload.email,
    password: payload.password
  };
  
  const loginRes = http.post(`${BASE_URL}/api/users/login`, JSON.stringify(loginPayload), {
    headers: { 'Content-Type': 'application/json' },
  });
  
  console.log(`Login response: ${loginRes.status} - ${loginRes.body}`);
  
  check(loginRes, {
    'login successful': (r) => r.status === 200,
  });

  if (loginRes.status === 200) {
    let token = null;
    let headers = { 'Content-Type': 'application/json' };
    
    if (loginRes.cookies && loginRes.cookies.JWT_TOKEN && loginRes.cookies.JWT_TOKEN.length > 0) {
      token = loginRes.cookies.JWT_TOKEN[0].value;
      headers['Cookie'] = `JWT_TOKEN=${token}`;
    } else if (loginRes.headers && loginRes.headers.Authorization) {
      headers['Authorization'] = loginRes.headers.Authorization;
    } else {
      try {
        const responseBody = loginRes.json();
        if (responseBody.token) {
          headers['Authorization'] = `Bearer ${responseBody.token}`;
        }
      } catch (e) {
        console.log('Could not parse login response for token');
      }
    }
    
    const searchRes = http.get(`${BASE_URL}/api/charging-stations`, { headers });
    console.log(`Search response: ${searchRes.status} - ${searchRes.body}`);
    
    check(searchRes, {
      'search successful': (r) => r.status === 200,
    });
    
    if (searchRes.status === 200) {
      const stations = searchRes.json();
      if (stations && stations.length > 0) {
        // Start from tomorrow and space out bookings by days
        // Each station has 2 chargers, so we can have 2 bookings per day
        const now = new Date();
        const daysToAdd = Math.floor(userCount / 2); // 2 bookings per day
        const hourOffset = (userCount % 2) * 12; // First booking at 00:00, second at 12:00
        
        const startTime = new Date(now.getTime() + 86400000 + (daysToAdd * 86400000) + (hourOffset * 3600000));
        const endTime = new Date(startTime.getTime() + 3600000); // 1 hour duration
        
        const bookingPayload = {
          stationId: stations[0].id,
          startTime: formatDate(startTime),
          endTime: formatDate(endTime)
        };
        
        console.log(`Booking payload: ${JSON.stringify(bookingPayload)}`);
        
        const bookingRes = http.post(`${BASE_URL}/api/bookings`, JSON.stringify(bookingPayload), { headers });
        console.log(`Booking response: ${bookingRes.status} - ${bookingRes.body}`);
        
        if (bookingRes.status !== 200) {
          console.log(`Booking response headers: ${JSON.stringify(bookingRes.headers)}`);
          try {
            const errorData = JSON.parse(bookingRes.body);
            console.log(`Booking error details: ${JSON.stringify(errorData, null, 2)}`);
          } catch (e) {
            console.log('Could not parse error response');
          }
        }
        
        check(bookingRes, {
          'booking successful': (r) => r.status === 200,
        });
        
        if (bookingRes.status !== 200) {
          errorRate.add(1);
        }
      }
    }
  }
  
  sleep(1);
}