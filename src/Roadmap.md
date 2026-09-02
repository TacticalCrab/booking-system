# Java Booking System — Backend Learning Roadmap

## Goal

Build a real booking/reservation system while progressively learning Java backend development from beginner concepts up to solid mid-level backend topics.

The project should start as a simple monolith and gradually evolve as new concepts are introduced.

The goal is not to use as many technologies as possible.

The goal is to understand **why each technology is needed before introducing it**.

---

# Initial Tech Stack

Start with only:

- Java 25
- Spring Boot
- Maven
- PostgreSQL
- Spring Data JPA
- Flyway
- JUnit
- Testcontainers

Do **not** introduce microservices, Kafka, Kubernetes, CQRS, DDD, or other advanced architecture at the beginning.

---

# Project Concept

Build a booking system where users can reserve services provided by employees or resources.

Examples could include:

- barber appointments
- medical appointments
- restaurant reservations
- equipment rentals
- meeting rooms
- workshops
- consultations

The exact business domain is not important.

The backend concepts are.

---

# Core Domain

The initial system can contain:

## Users

```text
User
├── id
├── email
├── password
├── name
└── role
```

Roles:

```text
CUSTOMER
ADMIN
```

---

## Services

```text
Service
├── id
├── name
├── description
├── duration
└── price
```

Example:

```text
Haircut
Duration: 30 minutes
Price: 80 PLN
```

---

## Employees / Resources

```text
Employee
├── id
├── name
├── email
└── services
```

Employees can provide one or more services.

---

## Bookings

```text
Booking
├── id
├── user
├── employee
├── service
├── startTime
├── endTime
├── status
├── createdAt
└── updatedAt
```

Statuses:

```java
public enum BookingStatus {
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
```

---

# Phase 1 — Basic Spring Boot API

## Goal

Create a working REST API connected to PostgreSQL.

Learn:

- Spring Boot project structure
- Controllers
- Services
- Repositories
- Entities
- DTOs
- Dependency Injection
- Basic HTTP
- JPA
- PostgreSQL

---

## Initial Endpoints

### Users

```http
POST /users
GET /users/{id}
```

### Services

```http
GET /services
GET /services/{id}
POST /services
PUT /services/{id}
DELETE /services/{id}
```

### Employees

```http
GET /employees
GET /employees/{id}
POST /employees
PUT /employees/{id}
```

### Bookings

```http
GET /bookings
GET /bookings/{id}
POST /bookings
DELETE /bookings/{id}
```

---

# First Milestone

The first milestone should be extremely simple:

> A user can create a booking and the booking is persisted in PostgreSQL.

Architecture:

```text
Client
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
PostgreSQL
```

Do not add Redis, Kafka, authentication, or microservices yet.

---

# Phase 2 — Build a Proper REST API

Once basic CRUD works, improve the API.

Learn:

- validation
- HTTP status codes
- DTO mapping
- global exception handling
- database constraints
- pagination
- filtering
- sorting
- database migrations

---

## Validation

Examples:

```text
Email must be valid.

Service duration must be greater than zero.

Price cannot be negative.

Booking start time cannot be in the past.

Booking end time must occur after start time.
```

Use:

```text
Jakarta Validation
```

---

## Exception Handling

Create centralized exception handling.

Example responses:

```json
{
  "status": 404,
  "message": "Booking not found",
  "timestamp": "2026-09-01T12:00:00Z"
}
```

Handle errors such as:

```text
404 Not Found

400 Bad Request

409 Conflict

401 Unauthorized

403 Forbidden
```

---

# Phase 3 — Database Migrations

Introduce:

```text
Flyway
```

Do not rely on Hibernate automatically modifying your production database schema.

Create migrations such as:

```text
V1__create_users.sql

V2__create_services.sql

V3__create_employees.sql

V4__create_bookings.sql
```

Learn:

- schema migrations
- versioning
- indexes
- foreign keys
- unique constraints

---

# Phase 4 — Authentication and Authorization

Introduce:

```text
Spring Security
```

Users should be able to:

```text
Register

Login

View their bookings

Create bookings

Cancel their own bookings
```

Admins should be able to:

```text
Manage services

Manage employees

View all bookings

Cancel bookings

Manage users
```

---

## Concepts to Learn

Understand:

- authentication
- authorization
- password hashing
- sessions
- JWT
- access tokens
- refresh tokens
- Spring Security filters
- security contexts
- role-based access control

Do not only copy a JWT implementation.

Understand the complete authentication flow.

---

# Phase 5 — Booking Availability

Now start implementing real business logic.

Before creating a booking, verify:

```text
Does the employee exist?

Does the service exist?

Can the employee perform the service?

Is the employee working at this time?

Is the requested slot available?

Does another booking overlap with it?
```

Example:

```text
Employee: John

Existing booking:
10:00 ───────── 10:30

Requested booking:
10:15 ───────── 10:45

Result:

CONFLICT
```

Return:

```http
409 Conflict
```

---

# Phase 6 — Concurrency and Transactions

This is where the project becomes significantly more interesting.

Consider:

```text
User A ──────┐
             │
             ▼
         10:00 slot
             ▲
             │
User B ──────┘
```

Both requests arrive almost simultaneously.

Only one booking should succeed.

---

## Topics to Learn

Study:

- transactions
- race conditions
- transaction isolation
- database locks
- optimistic locking
- pessimistic locking
- unique constraints
- atomic operations

Understand:

```java
@Transactional
```

rather than simply using it because Spring tutorials do.

---

# Phase 7 — Testing

Introduce proper automated testing.

Use:

- JUnit
- Mockito
- Spring Boot Test
- Testcontainers

---

## Tests to Implement

```text
✓ User can create a booking

✓ Booking is saved in PostgreSQL

✓ Booking cannot be created in the past

✓ Booking cannot overlap another booking

✓ User cannot cancel another user's booking

✓ User can cancel their own booking

✓ Admin can cancel any booking

✓ Invalid service returns 404

✓ Concurrent booking requests do not create duplicate reservations
```

---

## Integration Testing

Use Testcontainers to launch a real PostgreSQL database while running tests.

Architecture:

```text
JUnit
   │
   ▼
Spring Boot
   │
   ▼
PostgreSQL Testcontainer
```

This avoids relying entirely on mocked repositories.

---

# Phase 8 — Add a Simple Frontend

The frontend should remain intentionally small.

It only needs enough UI to make the application usable.

Example:

```text
Calendar
   │
   ▼
Choose employee
   │
   ▼
Choose service
   │
   ▼
Choose time
   │
   ▼
Book
```

Admin interface:

```text
Bookings

Employees

Services

Users
```

The backend should contain the business rules.

The frontend should primarily:

```text
display data

collect input

send requests

display errors

display loading states
```

---

# Phase 9 — Redis

Once the normal application works, introduce Redis.

Do not introduce Redis simply because it appears on backend roadmaps.

Use it to solve an actual problem.

---

## Example: Availability Cache

Instead of calculating available slots on every request:

```text
GET /employees/12/availability
          │
          ▼
        Redis
```

Cache availability for a short period.

Example:

```text
TTL: 30 seconds
```

---

## Learn Cache Invalidation

When a booking is created:

```text
Booking created
      │
      ▼
Database updated
      │
      ▼
Availability cache invalidated
```

Learn:

- caching
- TTL
- cache invalidation
- cache consistency
- cache misses

---

# Phase 10 — Background Jobs

Introduce asynchronous processing.

Example:

When someone creates a booking, send a confirmation notification.

Do not do:

```text
POST /booking
      │
      ▼
Save booking
      │
      ▼
Call email provider
      │
      ▼
Wait...
      │
      ▼
Return response
```

Instead:

```text
POST /booking
      │
      ▼
Save booking
      │
      ▼
BookingCreated event
      │
      ▼
Background worker
      │
      ▼
Send notification
```

Initially, this can be implemented without external message infrastructure.

Learn the concept first.

---

# Phase 11 — Message Queue

Once background jobs work, introduce:

```text
RabbitMQ
```

or eventually:

```text
Kafka
```

Architecture:

```text
Spring Boot API
      │
      ▼
BookingCreated
      │
      ▼
Message Queue
      │
      ▼
Notification Worker
      │
      ▼
Email / SMS
```

Learn:

- producers
- consumers
- acknowledgements
- retries
- dead-letter queues
- duplicate messages
- idempotency

---

# Phase 12 — Reliable Events

Introduce the problem:

```text
Database transaction succeeds

BUT

publishing BookingCreated fails
```

Now the booking exists, but the notification event was never sent.

Research and implement the:

```text
Transactional Outbox Pattern
```

Architecture:

```text
Database Transaction

├── INSERT booking
└── INSERT outbox_event

        │
        ▼

Outbox Worker

        │
        ▼

Message Queue
```

This introduces real distributed-system reliability concepts.

---

# Phase 13 — Idempotency

Consider:

```text
POST /booking
```

The client sends the request.

The server succeeds.

The network connection fails before the client receives the response.

The client retries.

Without protection:

```text
Booking #1
Booking #2
```

Implement:

```text
Idempotency-Key
```

Example:

```http
Idempotency-Key: 07e1295d-...
```

Learn how APIs protect themselves from duplicated operations.

---

# Phase 14 — Rate Limiting

Protect selected endpoints.

Example:

```text
POST /login

Maximum:

5 requests / minute
```

Other examples:

```text
Booking creation

Password reset

Registration

Availability queries
```

Learn:

- rate limiting
- token bucket
- sliding windows
- Redis-backed distributed rate limits

---

# Phase 15 — Observability

Add:

```text
Spring Boot Actuator

Micrometer

Prometheus

Grafana
```

Track things like:

```text
Requests per second

HTTP latency

Error rate

Database connection usage

JVM memory

Garbage collection

Booking creation rate

Failed bookings

Queue depth
```

Architecture:

```text
Spring Boot
     │
     ▼
Micrometer
     │
     ▼
Prometheus
     │
     ▼
Grafana
```

---

# Phase 16 — Logging

Implement structured logging.

Avoid logs like:

```text
Something failed
```

Prefer:

```json
{
  "level": "ERROR",
  "event": "booking_creation_failed",
  "userId": 123,
  "employeeId": 42,
  "serviceId": 7,
  "reason": "slot_already_reserved"
}
```

Learn about:

- log levels
- correlation IDs
- request IDs
- structured logs
- distributed tracing concepts

---

# Phase 17 — Performance

Generate large datasets.

Example:

```text
1,000,000 bookings

100,000 users

5,000 employees
```

Investigate:

```text
Which endpoints become slow?

Which queries perform badly?

Are indexes being used?

Are too many SQL queries being generated?
```

Learn:

- PostgreSQL EXPLAIN
- EXPLAIN ANALYZE
- indexes
- composite indexes
- query plans
- N+1 queries
- connection pooling
- pagination strategies

---

# Phase 18 — Load Testing

Use something like:

```text
k6
```

Simulate:

```text
100 users

1,000 users

10,000 concurrent booking attempts
```

Measure:

```text
latency

throughput

error rate

database load

CPU usage

memory usage
```

Try to discover where the application breaks.

---

# Phase 19 — Docker

Containerize the application.

Eventually:

```text
docker-compose.yml

├── booking-api
├── postgres
├── redis
├── rabbitmq
├── prometheus
└── grafana
```

Learn:

- Dockerfiles
- multi-stage builds
- Docker networking
- volumes
- environment variables
- health checks

---

# Phase 20 — CI/CD

Create a pipeline.

Example:

```text
Push
  │
  ▼
Compile
  │
  ▼
Unit tests
  │
  ▼
Integration tests
  │
  ▼
Docker build
  │
  ▼
Deploy
```

Use:

```text
GitHub Actions
```

or another CI/CD platform.

---

# Phase 21 — Deployment

Deploy the application as a normal monolith first.

Do **not** immediately deploy Kubernetes.

Example:

```text
Internet
   │
   ▼
Reverse Proxy
   │
   ▼
Spring Boot Docker Container
   │
   ├── PostgreSQL
   └── Redis
```

Learn:

- Linux deployment
- reverse proxies
- HTTPS
- environment configuration
- secrets
- backups
- health checks
- deployment strategies

---

# Phase 22 — Architecture Improvements

Once the system becomes large enough, investigate architectural patterns.

Possible topics:

- modular monolith
- hexagonal architecture
- clean architecture
- domain-driven design
- domain events

Do not restructure the application simply because a tutorial says that every project needs these patterns.

Introduce architecture when the existing structure creates an actual problem.

---

# Phase 23 — Distributed Systems

Only after understanding the monolith well should you explore distributing parts of it.

Possible separation:

```text
                    API Gateway
                         │
             ┌───────────┼───────────┐
             │           │           │
             ▼           ▼           ▼
          Booking      Users    Notifications
          Service      Service      Service
             │
             ▼
         PostgreSQL
```

Do this to learn distributed systems — not because microservices are automatically better.

---

# Distributed Systems Topics

Research:

- partial failures
- network timeouts
- retries
- exponential backoff
- circuit breakers
- eventual consistency
- distributed transactions
- service discovery
- message duplication
- idempotency
- distributed tracing

---

# Advanced Topics

Once the project is mature, investigate the following individually.

## Optimistic Locking

Useful when conflicts are relatively rare.

```text
Read entity version 5

Update entity

WHERE version = 5

Set version = 6
```

If another request already changed the row:

```text
update fails
```

---

## Pessimistic Locking

Useful when conflicts are likely.

Conceptually:

```sql
SELECT *
FROM booking_slot
WHERE id = ?
FOR UPDATE;
```

Understand when pessimistic locking is appropriate and what problems it can introduce.

---

## Distributed Locks

If multiple application instances operate on the same logical resource:

```text
Instance A ─┐
            ├── shared resource
Instance B ─┘
```

Investigate distributed locking using Redis or database mechanisms.

Also understand why distributed locks are easy to implement incorrectly.

---

# Recommended Learning Order

```text
Java fundamentals
        │
        ▼
Spring Boot
        │
        ▼
REST
        │
        ▼
PostgreSQL
        │
        ▼
JPA
        │
        ▼
Flyway
        │
        ▼
Validation
        │
        ▼
Authentication
        │
        ▼
Transactions
        │
        ▼
Testing
        │
        ▼
Docker
        │
        ▼
Redis
        │
        ▼
Background jobs
        │
        ▼
RabbitMQ / Kafka
        │
        ▼
Observability
        │
        ▼
Performance
        │
        ▼
Cloud deployment
        │
        ▼
Distributed systems
```

---

# Technologies to Avoid Initially

Do not start with:

```text
Microservices

Kubernetes

Kafka

Spring Cloud

CQRS

Event Sourcing

DDD everywhere

Hexagonal Architecture

WebFlux

GraphQL

Elasticsearch

MongoDB

Multiple databases

Service mesh
```

They can be introduced later when the project provides a reason to learn them.

---

# AI-Assisted Development Rule

AI can generate implementation code, but every significant piece of generated code should be understandable.

For generated code, ask:

```text
Why is this class needed?

Why does this annotation exist?

Where does this transaction start and end?

What happens if this request fails halfway through?

What happens when two requests execute simultaneously?

What SQL does Hibernate generate?

What happens when PostgreSQL goes offline?

What happens when Redis loses the cached value?

What happens if a queue sends the same message twice?
```

The objective is not to memorize syntax.

The objective is to understand the system.

---

# Final Architecture

A mature version of the project could eventually look like:

```text
                         Internet
                            │
                            ▼
                     Reverse Proxy
                            │
                            ▼
                     Spring Boot API
                  ┌─────────┼──────────┐
                  │         │          │
                  ▼         ▼          ▼
             PostgreSQL    Redis    RabbitMQ
                  │                    │
                  │                    ▼
                  │             Notification Worker
                  │
                  ▼
              Outbox Events


Spring Boot
     │
     ▼
Micrometer
     │
     ▼
Prometheus
     │
     ▼
Grafana
```

---

# What This Project Should Teach

By the end, you should understand much more than Java syntax.

You should understand:

## Java

- object-oriented programming
- interfaces
- generics
- collections
- streams
- exceptions
- concurrency
- JVM basics

## Spring

- dependency injection
- Spring Boot
- Spring MVC
- Spring Data
- Spring Security
- transactions
- configuration
- Actuator

## Databases

- PostgreSQL
- SQL
- transactions
- isolation levels
- indexes
- locks
- query optimization
- migrations

## Backend Engineering

- REST
- authentication
- authorization
- validation
- caching
- background jobs
- queues
- retries
- idempotency
- rate limiting

## Testing

- unit tests
- integration tests
- Testcontainers
- concurrency tests
- load tests

## Infrastructure

- Docker
- Linux
- CI/CD
- deployment
- monitoring
- logging

## Distributed Systems

- message delivery
- eventual consistency
- partial failures
- outbox pattern
- retries
- distributed locks
- observability

---

# Final Goal

Do not aim to create the most architecturally complicated booking application possible.

The progression should be:

```text
Simple working application
          │
          ▼
Correct application
          │
          ▼
Well-tested application
          │
          ▼
Reliable application
          │
          ▼
Observable application
          │
          ▼
Performant application
          │
          ▼
Scalable application
```

The project should grow because you understand increasingly difficult backend problems — not because you keep adding technologies.

The most important first milestone remains:

> **Create a booking through a Spring Boot endpoint and persist it in PostgreSQL.**

Everything else comes afterward.