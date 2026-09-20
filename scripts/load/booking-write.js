import http from 'k6/http';
import { check, fail } from 'k6';
import { Counter } from 'k6/metrics';

const bookingsCreated = new Counter('bookings_created');
const conflicts = new Counter('booking_conflicts');
const rateLimited = new Counter('booking_rate_limited');
const unexpected = new Counter('booking_unexpected');

http.setResponseCallback(
    http.expectedStatuses(200, 201)
);

export const options = {
    scenarios: {
        successful_bookings: {
            executor: 'constant-vus',
            vus: 50,
            duration: '30s',
        },
    },

    thresholds: {
        booking_unexpected: ['count==0'],
        booking_conflicts: ['count==0'],
        booking_rate_limited: ['count==0'],
    },
};

const BASE_URL =
    __ENV.BASE_URL || 'http://localhost:8080';

function requireEnv(name) {
    if (!__ENV[name]) {
        fail(`Missing environment variable: ${name}`);
    }

    return __ENV[name];
}

function addDays(baseDate, days) {
    const date = new Date(`${baseDate}T00:00:00Z`);

    date.setUTCDate(
        date.getUTCDate() + days
    );

    return date
        .toISOString()
        .substring(0, 10);
}

export function setup() {
    const response = http.get(
        `${BASE_URL}/api/employees?size=100`
    );

    if (response.status !== 200) {
        fail(
            `Failed to load employees. Status=${response.status}`
        );
    }

    const body = response.json();

    const employees = Array.isArray(body)
        ? body
        : body.content;

    const loadTestEmployees = employees
        .filter(employee =>
            employee.email.startsWith('k6-write-')
        )
        .sort((a, b) =>
            a.email.localeCompare(b.email)
        );

    if (loadTestEmployees.length < 50) {
        fail(
            `Expected 50 K6 employees, found ${loadTestEmployees.length}`
        );
    }

    return {
        employeeIds:
            loadTestEmployees.map(e => e.id),
    };
}

export default function (data) {
    const token = requireEnv('JWT');

    const serviceId = Number(
        requireEnv('SERVICE_ID')
    );

    const baseDate = requireEnv('BASE_DATE');

    const employeeId =
        data.employeeIds[__VU - 1];

    /*
     * Each VU owns one employee.
     *
     * Each iteration uses another DATE:
     *
     * VU 1 iteration 0:
     * employee A, 2026-10-01 14:00
     *
     * VU 1 iteration 1:
     * employee A, 2026-10-02 14:00
     *
     * VU 2 iteration 0:
     * employee B, 2026-10-01 14:00
     *
     * etc.
     *
     * Therefore bookings never overlap.
     */
    const bookingDate =
        addDays(baseDate, __ITER);

    const startTime =
        `${bookingDate}T14:00:00`;

    const payload = JSON.stringify({
        employeeId,
        serviceId,
        startTime,
    });

    const idempotencyKey =
        `k6-write-${__VU}-${__ITER}-${Date.now()}`;

    const response = http.post(
        `${BASE_URL}/api/bookings`,
        payload,
        {
            headers: {
                'Content-Type': 'application/json',

                'Authorization':
                    `Bearer ${token}`,

                'Idempotency-Key':
                idempotencyKey,
            },

            tags: {
                test_type: 'booking_write',
            },
        }
    );

    if (response.status === 201) {
        bookingsCreated.add(1);

        check(response, {
            'booking created':
                r => r.status === 201,
        });

        return;
    }

    if (response.status === 409) {
        conflicts.add(1);
    } else if (response.status === 429) {
        rateLimited.add(1);
    } else {
        unexpected.add(1);
    }

    console.error(
        `VU=${__VU} ` +
        `ITER=${__ITER} ` +
        `employee=${employeeId} ` +
        `start=${startTime} ` +
        `status=${response.status} ` +
        `body=${response.body}`
    );
}