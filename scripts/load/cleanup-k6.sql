-- ROLLBACK;
-- ============================================================
-- Collect bookings created for K6 employees before deleting them
-- ============================================================

BEGIN;

DROP TABLE IF EXISTS k6_booking_ids;

CREATE TEMP TABLE k6_booking_ids
    ON COMMIT DROP
AS
SELECT b.id
FROM bookings b
         JOIN employees e ON e.id = b.employee_id
WHERE e.email LIKE 'k6-write-%';

-- ============================================================
-- 1. IDEMPOTENCY RECORDS
-- Must be deleted before bookings because of the FK.
-- ============================================================

DELETE FROM idempotency_records ir
WHERE ir.booking_id IN (
    SELECT id
    FROM k6_booking_ids
);


-- ============================================================
-- 2. BOOKINGS
-- ============================================================

DELETE FROM bookings
WHERE id IN (
    SELECT id
    FROM k6_booking_ids
);


-- ============================================================
-- 3. WORKING HOURS
-- ============================================================

DELETE FROM employee_working_hours wh
    USING employees e
WHERE wh.employee_id = e.id
  AND e.email LIKE 'k6-write-%';


-- ============================================================
-- 4. EMPLOYEE <-> SERVICE ASSIGNMENTS
-- ============================================================

DELETE FROM employee_services es
    USING employees e
WHERE es.employee_id = e.id
  AND e.email LIKE 'k6-write-%';


-- ============================================================
-- 5. K6 EMPLOYEES
-- ============================================================

DELETE FROM employees
WHERE email LIKE 'k6-write-%';


COMMIT;


-- ============================================================
-- VERIFY
-- ============================================================

SELECT COUNT(*) AS remaining_k6_employees
FROM employees
WHERE email LIKE 'k6-write-%';

SELECT COUNT(*) AS remaining_k6_bookings
FROM bookings b
         JOIN employees e ON e.id = b.employee_id
WHERE e.email LIKE 'k6-write-%';