CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

CREATE TABLE employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE services (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    price NUMERIC(38, 2) NOT NULL
);

CREATE TABLE employee_services (
    employee_id INTEGER NOT NULL,
    service_id INTEGER NOT NULL,
    PRIMARY KEY (employee_id, service_id),
    CONSTRAINT fk_employee_services_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_employee_services_service
        FOREIGN KEY (service_id) REFERENCES services (id)
);

CREATE TABLE bookings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    employee_id INTEGER NOT NULL,
    service_id INTEGER NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL CHECK (status IN ('CONFIRMED', 'CANCELLED', 'COMPLETED')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_booking_time CHECK (end_time > start_time),
    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_bookings_service
        FOREIGN KEY (service_id) REFERENCES services (id)
);

CREATE INDEX idx_employee_services_service_id
    ON employee_services (service_id);

CREATE INDEX idx_bookings_user_id
    ON bookings (user_id);

CREATE INDEX idx_bookings_employee_id
    ON bookings (employee_id);

CREATE INDEX idx_bookings_service_id
    ON bookings (service_id);
