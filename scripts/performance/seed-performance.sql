-- ============================================================
-- Performance dataset
--
-- 10,000 users
--    500 employees
--     20 services
-- 100,000 bookings
--
-- All generated records are marked with PERF_* / perf-* names.
-- ============================================================

-- ROLLBACK;

BEGIN;

SET perf.user_email_prefix = 'perf-user-';
SET perf.employee_email_prefix = 'perf-employee-';
SET perf.service_prefix = 'PERF_SERVICE_';
SET perf.booking_count = '1000000';

-- ============================================================
-- 1. CLEAN PREVIOUS PERFORMANCE DATA
-- ============================================================

DELETE FROM bookings
WHERE user_id IN (
    SELECT id
    FROM users
    WHERE starts_with(
        email,
        current_setting('perf.user_email_prefix')
    )
)
OR employee_id IN (
    SELECT id
    FROM employees
    WHERE starts_with(
        email,
        current_setting('perf.employee_email_prefix')
    )
)
OR service_id IN (
    SELECT id
    FROM services
    WHERE starts_with(
          name,
          current_setting('perf.service_prefix')
    )
);

DELETE FROM users
WHERE starts_with(
    email,
    current_setting('perf.user_email_prefix')
);


DELETE FROM employee_services
WHERE employee_id IN (
    SELECT id
    FROM employees
    WHERE starts_with(
        email,
        current_setting('perf.employee_email_prefix')
    )
)
OR service_id IN (
    SELECT id
    FROM services
    WHERE starts_with(
        name,
        current_setting('perf.service_prefix')
    )
);


DELETE FROM employees
WHERE starts_with(
    email,
    current_setting('perf.employee_email_prefix')
);

DELETE FROM services
WHERE starts_with(
    name,
    current_setting('perf.service_prefix')
);



-- ============================================================
-- 2. USERS
-- ============================================================

INSERT INTO users (
                   email,
                   password_hash,
                   name,
                   role
)
SELECT
    'perf-user-' || g || '@example.test',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Performance User ' || g,
    CASE
        WHEN g % 1000 = 0 THEN 'ADMIN'
        ELSE 'CUSTOMER'
    END
FROM generate_series(1, 10000) AS g;

-- ============================================================
-- 3. SERVICES
-- ============================================================

INSERT INTO services (
                      name,
                      description,
                      duration_minutes,
                      price
)
SELECT
    'PERF_SERVICE_' || g,
    'Performance test service ' || g,

    CASE
        WHEN g % 4 = 0 THEN 60
        WHEN g % 4 = 1 THEN 15
        WHEN g % 4 = 2 THEN 30
        ELSE 45
    END,

    ((g % 10) + 1) * 25.00

FROM generate_series(1, 20) AS g;

-- ============================================================
-- 4. EMPLOYEES
-- ============================================================

INSERT INTO employees (
                       name,
                       email
)
SELECT
    'Performance Employee ' || g,
    'perf-employee-' || g || '@example.test'
FROM generate_series(1, 500) AS g;

-- ============================================================
-- 5. CREATE TEMP ID LOOKUP TABLES
--
-- This makes generating bookings much cheaper than repeatedly
-- searching the real tables.
-- ============================================================

CREATE TEMP TABLE perf_users AS
SELECT
    ROW_NUMBER() OVER (ORDER BY id) AS rn,
    id
FROM users
WHERE starts_with(
              email,
              current_setting('perf.user_email_prefix')
);

CREATE TEMP TABLE perf_employees AS
SELECT
    ROW_NUMBER() OVER (ORDER BY id) AS rn,
    id
FROM employees
WHERE starts_with(
      email,
      current_setting('perf.employee_email_prefix')
);

CREATE TEMP TABLE perf_services AS
SELECT
    ROW_NUMBER() OVER (ORDER BY id) AS rn,
    id,
    duration_minutes
FROM services
WHERE starts_with(
    name,
    current_setting('perf.service_prefix')
);


CREATE INDEX ON perf_users(rn);
CREATE INDEX ON perf_employees(rn);
CREATE INDEX ON perf_services(rn);

-- ============================================================
-- 6. EMPLOYEE <-> SERVICE RELATIONS
--
-- Each employee provides 3 services.
-- ============================================================

INSERT INTO employee_services (
   employee_id,
   service_id
)
SELECT
    e.id,
    s.id

FROM perf_employees e

CROSS JOIN generate_series(0, 2) AS offset_value

JOIN perf_services s
    ON s.rn =
       (
            ((e.rn + offset_value - 1) % 20) + 1
        );

-- ============================================================
-- 7. BOOKINGS
--
-- Important:
-- We're inserting directly into PostgreSQL.
--
-- We are NOT benchmarking:
--     Spring
--     Hibernate
--     validation
--     HTTP
--
-- while generating the dataset.
--
-- Those are what we'll benchmark AFTER the data exists.
-- ============================================================

INSERT INTO bookings (
                      user_id,
                      employee_id,
                      service_id,
                      start_time,
                      end_time,
                      status,
                      created_at,
                      updated_at
)
SELECT
    u.id,
    e.id,
    s.id,

    booking_start,
    booking_start
        + s.duration_minutes * INTERVAL '1 minute',

    CASE
        WHEN g % 20 = 0 THEN 'CANCELLED'
        WHEN g % 10 = 0 THEN 'COMPLETED'
        ELSE 'CONFIRMED'
    END,

    booking_start - INTERVAL '7 days',
    booking_start - INTERVAL '6 days'

FROM generate_series(
             1,
             CAST(current_setting('perf.booking_count') AS integer)
) AS g

JOIN perf_users u
    ON u.rn = ((g - 1) % 10000) + 1

JOIN perf_employees e
     ON e.rn = ((g - 1) % 500) + 1

JOIN perf_services s
     ON s.rn = ((g - 1) % 20) + 1

CROSS JOIN LATERAL (

    SELECT
        TIMESTAMP '2024-01-01 08:00:00'
            + ((g % 730) * INTERVAL '1 day')
            + ((g % 36) * INTERVAL '15 minutes')
            AS booking_start

    ) AS generated_time;

COMMIT;


-- ============================================================
-- 8. UPDATE POSTGRESQL STATISTICS
--
-- Bulk inserts changed the distribution of the tables.
-- PostgreSQL's planner needs fresh statistics.
-- ============================================================

ANALYZE users;
ANALYZE employees;
ANALYZE services;
ANALYZE employee_services;
ANALYZE bookings;

-- ============================================================
-- 9. VERIFY
-- ============================================================

SELECT
    COUNT(*) as performance_users
FROM users
WHERE starts_with(
      email,
      current_setting('perf.user_email_prefix')
);

SELECT
    COUNT(*) AS performance_employees
FROM employees
WHERE starts_with(
      email,
      current_setting('perf.employee_email_prefix')
);

SELECT
    COUNT(*) AS performance_services
FROM services
WHERE starts_with(
      name,
      current_setting('perf.service_prefix')
);

SELECT
    COUNT(*) AS performance_bookings
FROM bookings b
         JOIN users u ON u.id = b.user_id
WHERE starts_with(
      u.email,
      current_setting('perf.user_email_prefix')
);
