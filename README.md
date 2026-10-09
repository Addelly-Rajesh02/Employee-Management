<<<<<<< HEAD
# Employee Service

A small production-style Spring Boot REST API using PostgreSQL and Flyway.

## Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Bean Validation
- Swagger/OpenAPI
- Actuator
- JUnit / Spring Boot Test
- Docker / Docker Compose

## 1. Run with Docker Compose

Build the application first:

```bash
mvn clean package
```

Then:

```bash
docker compose up --build
```

API:
http://localhost:8080/api/v1/employees

Swagger:
http://localhost:8080/swagger-ui.html

Health:
http://localhost:8080/actuator/health

## 2. Run locally with PostgreSQL

Start PostgreSQL and create:

- database: employeedb
- user: employee
- password: employee

Or use:

```bash
docker compose up -d postgres
```

Then:

```bash
mvn spring-boot:run
```

## API examples

Create:

```http
POST /api/v1/employees
Content-Type: application/json

{
  "firstName": "Rajesh",
  "lastName": "Kumar",
  "email": "rajesh@example.com",
  "department": "IT",
  "salary": 75000
}
```

List:

```http
GET /api/v1/employees?page=0&size=10&sortBy=createdAt&direction=desc
```

Filter:

```http
GET /api/v1/employees?department=IT&page=0&size=10
```

Get one:

```http
GET /api/v1/employees/{id}
```

Update:

```http
PUT /api/v1/employees/{id}
Content-Type: application/json

{
  "firstName": "Rajesh",
  "lastName": "Kumar",
  "email": "rajesh@example.com",
  "department": "Engineering",
  "salary": 85000
}
```

Delete:

```http
DELETE /api/v1/employees/{id}
```

## Configuration

Database values use environment variables:

DB_URL
DB_USERNAME
DB_PASSWORD
SERVER_PORT

No application code contains a production database password.

## Migration

Flyway runs:

`src/main/resources/db/migration/V1__create_employees.sql`

Hibernate is configured with `ddl-auto=validate`, so Hibernate validates the schema instead of creating/changing tables.

## Test

```bash
mvn test
```

## Docker

```bash
mvn clean package
docker compose up --build
```

Stop:

```bash
docker compose down
```

Delete database volume too:

```bash
docker compose down -v
```
=======
# Employee-Management
A real-world Employee Management System built with Spring Boot and PostgreSQL, providing RESTful APIs for employee CRUD operations, validation, exception handling, database migrations, testing, and production-ready development practices.
>>>>>>> 6169909a295fb19b5c5e64ebec0f5362a686eab4
