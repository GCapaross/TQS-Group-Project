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
  
  sleep(1);
}