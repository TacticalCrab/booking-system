-- ============================================================
-- CLEANUP
-- ============================================================
BEGIN;

SET perf.k6_employee_prefix = 'k6-write-';
SET perf.k6_employee_count = '50';
SET perf.k6_service_id = '1';

-- ============================================================
-- 1. CREATE LOAD-TEST EMPLOYEES
-- ============================================================

INSERT INTO employees (
    name,
    email
)
SELECT
    'K6 Write Employee ' || g,
    current_setting('perf.k6_employee_prefix')
        || LPAD(g::text, 3, '0')
        || '@example.test'
FROM generate_series(
             1,
             current_setting('perf.k6_employee_count')::int
     ) AS g
WHERE NOT EXISTS (
    SELECT 1
    FROM employees e
    WHERE e.email =
          current_setting('perf.k6_employee_prefix')
              || LPAD(g::text, 3, '0')
              || '@example.test'
);


-- ============================================================
-- 2. ASSIGN SERVICE 1 TO EVERY EMPLOYEE
-- ============================================================

INSERT INTO employee_services (
    employee_id,
    service_id
)
SELECT
    e.id,
    current_setting('perf.k6_service_id')::bigint
FROM employees e
WHERE starts_with(
        e.email,
        current_setting('perf.k6_employee_prefix')
      )
  AND NOT EXISTS (
    SELECT 1
    FROM employee_services es
    WHERE es.employee_id = e.id
      AND es.service_id =
          current_setting('perf.k6_service_id')::bigint
);


-- ============================================================
-- 3. WORKING HOURS
-- ============================================================

INSERT INTO employee_working_hours (
    employee_id,
    day_of_week,
    start_time,
    end_time
)
SELECT
    e.id,
    d.day_of_week,
    TIME '08:00',
    TIME '20:00'
FROM employees e
         CROSS JOIN (
    VALUES
        ('MONDAY'),
        ('TUESDAY'),
        ('WEDNESDAY'),
        ('THURSDAY'),
        ('FRIDAY'),
        ('SATURDAY'),
        ('SUNDAY')
) AS d(day_of_week)
WHERE starts_with(
        e.email,
        current_setting('perf.k6_employee_prefix')
      )
  AND NOT EXISTS (
    SELECT 1
    FROM employee_working_hours wh
    WHERE wh.employee_id = e.id
      AND wh.day_of_week = d.day_of_week
);


COMMIT;


-- ============================================================
-- VERIFY
-- ============================================================

SELECT
    COUNT(*) AS k6_employees
FROM employees
WHERE starts_with(
              email,
              'k6-write-'
      );

SELECT
    COUNT(*) AS k6_working_hours
FROM employee_working_hours wh
         JOIN employees e
              ON e.id = wh.employee_id
WHERE starts_with(
              e.email,
              'k6-write-'
      );