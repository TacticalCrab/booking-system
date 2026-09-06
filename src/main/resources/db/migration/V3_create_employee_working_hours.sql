CREATE TABLE employee_working_hours (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    CONSTRAINT fk_employee_working_hours_employee
        FOREIGN KEY (employee_id)
            REFERENCES employees(id),

    CONSTRAINT uq_employee_working_day
        UNIQUE (employee_id, day_of_week),

    CONSTRAINT chk_employee_working_hours
        CHECK (end_time > start_time)
)