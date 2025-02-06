# Project Overview

This repository contains the source code for a banking system named **dbank**. The system is implemented using **Java Spring Boot** and follows a **microservices architecture**. It includes various services such as **user-service, account-service, loan-service, card-service, transfer-service, notification-service, exchange-service, and gateway-server**.

## Features
- **User Management**: Create, update, delete, and retrieve users.
- **Account Management**: Handle bank accounts and transactions.
- **Loan Processing**: Manage loans, credit score calculations, and repayments.
- **Card Services**: Generate and manage credit/debit cards.
- **Transfers**: Handle fund transfers between accounts.
- **Currency Exchange**: Exchange currency between different accounts using real-time exchange rates.
- **Notifications**: Kafka-based messaging for user interactions.
- **API Gateway**: Centralized routing and security using Spring Cloud Gateway.
- **Service Discovery**: Eureka-based microservice discovery.

## Technologies Used
- **Java 21**, **Spring Boot**, **Spring Cloud**, **Spring Data JPA**, **Hibernate**
- **PostgreSQL**, **H2 Database**
- **Kafka**, **Feign Client**, **Docker**, **JUnit**, **Resilience4j**
- **Maven**, **Lombok**, **Flyway**, **Modulith**
- **GitHub Actions** for CI/CD

## Microservices Overview

### 1. **User Service** (`user-service`)
- Handles user registration, authentication, and profile updates.
- Uses **PostgreSQL** and **Kafka** for event-driven notifications.
- Exposes REST APIs for CRUD operations on users.

### 2. **Account Service** (`account-service`)
- Manages user bank accounts and their transactions.
- Provides APIs for retrieving account balance and transaction history.

### 3. **Loan Service** (`loan-service`)
- Processes loan applications, repayments, and schedules.
- Integrates with **User Service** for identity verification and **Account Service** for transactions.
- Uses **Kafka** for loan-related notifications.

### 4. **Card Service** (`card-service`)
- Generates and manages credit/debit cards.
- Provides APIs for retrieving and updating card details.

### 5. **Transfer Service** (`transfer-service`)
- Handles fund transfers between accounts.
- Ensures secure transactions using **Feign Client** communication.

### 6. **Exchange Service** (`exchange-service`)
- Provides currency exchange between different account balances.
- Uses external APIs for real-time exchange rates.

### 7. **Notification Service** (`notification-service`)
- Publishes user notifications using **Kafka** topics.

### 8. **Gateway Server** (`gateway-server`)
- Routes API requests to respective microservices.
- Provides centralized authentication and security.

### 9. **Eureka Server** (`eureka-server`)
- Enables service discovery and load balancing for microservices.

## Setup & Deployment

### Prerequisites
- Java 21
- Docker
- PostgreSQL
- Kafka (for event-driven services)

### Step 1: Clone the Repository
1. Clone the repository:
   ```sh
   git clone https://github.com/your-repo/dbank.git
   cd dbank
   ```

### Step: 2. Set Up Environment Variables
Create a `.env` file in the root directory of the project and add the following environment variables:
   ```env
   # PostgreSQL Database Configuration
   POSTGRES_USER=your_postgres_user
   POSTGRES_PASSWORD=your_postgres_password

   # JWS Shared Key (for security)
   JWS_SHARED_KEY=your_jws_shared_key

   # Twilio Configuration
   TWILIO_PHONE_NUMBER=your_twilio_phone_number
   TWILIO_ACCOUNT-SID=your_twilio_account_sid
   TWILIO_AUTH-TOKEN=your_twilio_auth-token

   # Email Configuration (for sending emails)
   EMAIL=your_email@gmail.com
   EMAIL_PASSWORD=your_email_password
```
Replace the placeholders (your_postgres_user, your_postgres_password, etc.) with your actual values.

### Step 3: Build and Run the Application
   ```sh
   docker compose up -d
   ```
### Step 4: Access the Application
   ``` http request
   http://localhost:900
   ```
### Step 5: Stopping the Application
To stop the application and remove the containers, run:
   ```shell
   docker compose down
   ```

If you want to remove the volumes (including the database data), use:
   ```shell
   docker compose down -v
   ```

### CI/CD Pipeline
- **GitHub Actions** is configured for automated builds and deployments.
- Uses **Docker Hub** for containerization.
- Runs unit tests and static analysis before pushing to production.