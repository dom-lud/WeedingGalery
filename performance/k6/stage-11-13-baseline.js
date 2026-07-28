import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const healthPath = __ENV.HEALTH_PATH || '/actuator/health';

export const options = {
  scenarios: {
    baseline: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 5),
      timeUnit: '1s',
      duration: __ENV.DURATION || '30s',
      preAllocatedVUs: Number(__ENV.PREALLOCATED_VUS || 5),
      maxVUs: Number(__ENV.MAX_VUS || 20),
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

export default function () {
  const response = http.get(`${baseUrl}${healthPath}`, {
    tags: { scenario: 'stage-11-13-baseline' },
  });
  check(response, {
    'health endpoint responds': (res) => res.status === 200,
    'health endpoint is JSON': (res) => res.headers['Content-Type']?.includes('application/json'),
  });
  sleep(0.1);
}
