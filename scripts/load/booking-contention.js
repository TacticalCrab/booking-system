import http from 'k6/http';
import { check, fail } from 'k6';

// For this test, conflicts are EXPECTED.
// k6 normally treats 409 as a failed HTTP request.
http.setResponseCallback(
    http.expectedStatuses(200, 201, 409)
);

export const options = {
    scenarios: {
        booking_contention: {
            executor: 'per-vu-iterations',

            // 50 users attempt the same booking concurrently
            vus: 50,

            // Exactly one request from each VU
            iterations: 1,

            maxDuration: '30s',
        },
    },

    thresholds: {
        // 400, 401, 429, 500 etc. should still fail this threshold.
        // 200/201 and 409 are considered expected above.
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

function requireEnvironmentVariable(name) {
    if (!__ENV[name]) {
        fail(`Missing environment variable: ${name}`);
    }

    return __ENV[name];
}

export default function () {
    const token = requireEnvironmentVariable('JWT');
    const employeeId = Number(
        requireEnvironmentVariable('EMPLOYEE_ID')
    );
    const serviceId = Number(
        requireEnvironmentVariable('SERVICE_ID')
    );
    const startTime = requireEnvironmentVariable('START_TIME');

    const payload = JSON.stringify({
        employeeId,
        serviceId,
        startTime,
    });

    console.log(`payload=${payload}`);

    // Every request represents a DIFFERENT logical booking attempt.
    // Therefore every request needs its own idempotency key.
    const idempotencyKey =
        `k6-contention-${__VU}-${__ITER}-${Date.now()}`;

    const response = http.post(
        `${BASE_URL}/api/bookings`,
        payload,
        {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
                'Idempotency-Key': idempotencyKey,
            },

            tags: {
                test_type: 'booking_contention',
            },
        }
    );

    const validResult = check(response, {
        'booking created or conflict': (r) =>
            r.status === 200 ||
            r.status === 201 ||
            r.status === 409,
    });

    // Only print useful diagnostic information.
    if (!validResult) {
        console.error(
            `VU=${__VU} ` +
            `status=${response.status} ` +
            `body=${response.body}`
        );

        return;
    }

    console.log(
        `VU=${__VU} status=${response.status}`
    );
}