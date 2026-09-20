BEGIN;

SET perf.user_email_prefix = 'perf-user-';
SET perf.employee_email_prefix = 'perf-employee-';
SET perf.service_prefix = 'PERF_SERVICE_';


-- bookings first
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


-- employee-service join table before employees/services
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


-- if Phase 17 employees somehow have working hours
DELETE FROM employee_working_hours
WHERE employee_id IN (
    SELECT id
    FROM employees
    WHERE starts_with(
                  email,
                  current_setting('perf.employee_email_prefix')
          )
);


DELETE FROM employees
WHERE starts_with(
              email,
              current_setting('perf.employee_email_prefix')
      );


DELETE FROM users
WHERE starts_with(
              email,
              current_setting('perf.user_email_prefix')
      );


DELETE FROM services
WHERE starts_with(
              name,
              current_setting('perf.service_prefix')
      );


COMMIT;