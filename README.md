# Banking REST API

Banking REST API — A production-grade banking backend built with Java 17, Spring Boot, Spring Data JPA, and MySQL, supporting customer management, account operations, and financial transactions (deposit, withdraw, transfer).
Follows a clean 4-layer architecture (Controller → Service → Repository → Database) with Swagger API documentation, proper HTTP status codes, input validation, and @Transactional safety for all money operations.

A production-grade Banking REST API built with Spring Boot, JPA, MySQL and Swagger.

## Tech Stack
- Java 17
- Spring Boot 3.4.3
- Spring Data JPA + Hibernate
- MySQL
- Swagger / OpenAPI (springdoc)
- Maven

## Setup Instructions

### 1. Create MySQL Database
```sql
CREATE DATABASE banking_db;
```

### 2. Update application.properties
```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 3. Run the application
```bash
mvn spring-boot:run
```

### 4. Access Swagger UI
```
http://localhost:8081/swagger-ui.html
```

## API Endpoints

### Customer
| Method | URL | Description |
|--------|-----|-------------|
| POST | /api/customers | Create customer |
| GET | /api/customers | Get all customers |
| GET | /api/customers/{id} | Get customer by ID |

### Account
| Method | URL | Description |
|--------|-----|-------------|
| POST | /api/accounts?customerId=1&accountType=SAVINGS | Open account |
| GET | /api/accounts/{accountNumber} | Get account |
| GET | /api/accounts/customer/{customerId} | Get customer accounts |
| GET | /api/accounts/{accountNumber}/balance | Get balance |

### Transaction
| Method | URL | Description |
|--------|-----|-------------|
| POST | /api/transactions/deposit | Deposit money |
| POST | /api/transactions/withdraw | Withdraw money |
| POST | /api/transactions/transfer | Transfer money |
| GET | /api/transactions/history/{accountNumber} | Transaction history |

## Project Structure
```
com.xbank.banking_api
├── config/
│   └── SwaggerConfig.java
├── controller/
│   ├── CustomerController.java
│   ├── AccountController.java
│   └── TransactionController.java
├── model/
│   ├── Customer.java
│   ├── Account.java
│   └── Transaction.java
├── repository/
│   ├── CustomerRepository.java
│   ├── AccountRepository.java
│   └── TransactionRepository.java
├── service/
│   ├── CustomerService.java
│   ├── AccountService.java
│   └── TransactionService.java
└── BankingApiApplication.java
```
