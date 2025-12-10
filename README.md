

# Bank Card Management System — Backend Service

This project is a backend service built with **Java (Spring Boot)** that provides secure management of bank cards, including card creation, lifecycle management, filtering, pagination, role-based access, JWT authentication, and internal transfers between cards.

The system is structured using a clean layered architecture (Controller → Service → Repository → Domain → DTO → Security → Exception → Mapper) with strict separation of responsibilities.

---

## 1. Project Overview

The application implements a complete card management workflow for both administrators and end-users. It provides secure handling of card data, encrypted storage of card numbers, transactional operations, error handling, and complete API documentation.

---

## 2. Functional Requirements

### 2.1 Card Management

* Create, update, block, activate, and delete cards
* View card details
* Encrypted storage of card numbers
* Masked card number representation in API responses (e.g., `**** **** **** 1234`)
* Support for card statuses:

    * ACTIVE
    * BLOCKED
    * EXPIRED
* Automatic expiration detection
* Balance management

### 2.2 User Features

* View own cards with filtering, sorting, and pagination
* Search cards by the last four digits
* Request card blocking
* Perform internal transfers between user’s own cards
* View balances and card information

### 2.3 Administrator Features

* Manage users
* View all cards
* Create, block, activate, or delete any card
* Manage card lifecycle

---

## 3. Security Requirements

* Authentication via **JWT**
* Authorization via **Spring Security**
* Role-based access control (RBAC)

    * **ADMIN**
    * **USER**
* Secure password hashing (BCrypt)
* Encrypted storage of card numbers
* Controlled exposure of sensitive fields through DTOs

---

## 4. API Requirements

The backend exposes a RESTful API supporting:

* CRUD operations for cards
* Internal transfers
* Pagination (`page`, `size`)
* Sorting (`sort=field,asc|desc`)
* Filtering by:

    * Status
    * Expiration date
    * Last 4 digits
* Input validation
* Detailed error messages
* Unified error response format

---



## 5. Database and Persistence

* PostgreSQL or MySQL as the primary relational database
* Schema migrations handled via **Liquibase**

    * Located in `src/main/resources/db/migration`
* JPA/Hibernate for ORM
* Efficient filtered queries using Spring Data JPA and `Pageable`

---

## 6. Documentation

The project includes:

* **OpenAPI/Swagger** specification for all endpoints
* Swagger UI for interactive testing
* OpenAPI YAML file (`docs/openapi.yaml`)
* Comprehensive README with architecture and setup instructions

---

## 7. Deployment and Infrastructure

* Docker Compose for local development environment
* Application container + database container
* Automatic execution of Liquibase migrations on startup
* Environment-based configuration (`application.yml`, `application-prod.yml`)

---

## 8. Testing

The system must include:

* Unit tests for service-level logic
* Tests for card transfer operations
* Validation and error handling tests
* Repository-level tests (optional but recommended)

---

## 9. Technologies Used

* Java 17+
* Spring Boot 3+
* Spring Security
* JWT (JSON Web Tokens)
* Spring Data JPA
* PostgreSQL
* Liquibase
* Docker & Docker Compose
* Lombok
* MapStruct (or manual mappers)
* OpenAPI / Swagger



![CI Maven](https://github.com/rukaiia/Bank_rest/actions/workflows/ci.yml/badge.svg)

---

