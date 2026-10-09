CREATE TABLE employees (
    id UUID PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    department VARCHAR(80) NOT NULL,
    salary NUMERIC(12,2) NOT NULL CHECK (salary > 0),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_employees_department ON employees(department);
