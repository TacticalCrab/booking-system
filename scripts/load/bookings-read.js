import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
    stages: [
        { duration: '30s', target: 25 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '30s', target: 150 },
        { duration: '30s', target: 200 },
        { duration: '30s', target: 0 },
    ],

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    const token = __ENV.JWT;

    const response = http.get(
        `${BASE_URL}/api/bookings?size=100`,
        {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        }
    )

    check(response, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1);
}