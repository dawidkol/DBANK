### **Project Description**
The `Account Service` project is a Spring Boot application designed to manage user accounts, handle transactions, and manage account balances. The service provides functionality for account creation, balance updates, fetching account details, and more. It integrates with Kafka for messaging, Feign for inter-service communication, and JPA for database management. The application also supports resiliency using Resilience4j.

---

### **Dependencies**
The project relies on the following dependencies:
1. **Spring Boot**:
    - `spring-boot-starter-data-jpa`: For database interactions using JPA.
    - `spring-boot-starter-validation`: For validating request data.
    - `spring-boot-starter-web`: For building RESTful APIs.
    - `spring-boot-starter-test`: For unit and integration testing.
    - `spring-boot-devtools`: For development convenience (hot reload).
2. **Database**:
    - `h2`: In-memory database for testing.
    - `flyway-core`: For database migrations.
3. **Kafka**:
    - `spring-kafka`: To enable Kafka messaging.
    - `spring-kafka-test`: For testing Kafka-related components.
4. **Resilience**:
    - `spring-cloud-starter-circuitbreaker-resilience4j`: For implementing circuit breakers.
5. **Service Discovery and Communication**:
    - `spring-cloud-starter-netflix-eureka-client`: For service registration and discovery.
    - `spring-cloud-starter-openfeign`: For making HTTP requests to other services.
6. **Utilities**:
    - `lombok`: To reduce boilerplate code in data classes.
    - `spring-modulith`: For building modular monoliths.

---

### **Features**
1. **Account Management**:
    - Create accounts with unique account numbers and types (e.g., SAVINGS, CHECKING).
    - Retrieve account details by account number.
    - Soft delete accounts by marking them as inactive.
    - Fetch all accounts associated with a user.
2. **Account Balance Management**:
    - Update account balances based on transactions.
    - Fetch account balances by account number and currency type.
3. **Transactions**:
    - Handle inter-account transfers and maintain transaction records.
    - Calculate the average balance for the last 12 months of a user’s transactions.
4. **Validation**:
    - Ensure data integrity using constraints like `@NotNull` and `@Pattern`.
5. **Messaging**:
    - Kafka-based messaging to handle events like loan account creation and transfer processing.
6. **Resilience**:
    - Circuit breakers for fault tolerance and graceful degradation during inter-service communication failures.
7. **Auditing**:
    - Automatically track created and updated timestamps and users.
8. **Database Schema Management**:
    - Uses Flyway for managing schema migrations.

---

### **Endpoints**
The application exposes the following RESTful endpoints:

#### **Account Management**
1. **POST `/accounts`**:
    - **Description**: Creates a new account.
    - **Request Body**: `CreateAccountDto` (accountType, userId).
    - **Response**: Created account details (`AccountDto`).

2. **GET `/accounts/{accountId}`**:
    - **Description**: Retrieves details of an account by ID.
    - **Response**: Account details (`AccountDto`).

3. **DELETE `/accounts/{accountId}`**:
    - **Description**: Marks an account as inactive (soft delete).
    - **Response**: No content.

4. **GET `/accounts/{userId}/all`**:
    - **Description**: Retrieves all accounts associated with a user.
    - **Response**: List of account details (`AccountDto`).

#### **Account Balance Management**
1. **PATCH `/accounts/{accountNumber}`**:
    - **Description**: Updates the balance of an account.
    - **Request Body**: `UpdateAccountBalanceDto` (currencyType, updateByValue).
    - **Response**: Updated account balance (`AccountBalanceDto`).

2. **GET `/accounts/{accountNumber}/balance`**:
    - **Description**: Retrieves the balance of an account by account number and currency type.
    - **Response**: Account balance (`AccountBalanceDto`).

#### **Transactions**
1. **GET `/transactions/last-12-months/{userId}`**:
    - **Description**: Calculates the average balance of a user’s accounts over the last 12 months.
    - **Response**: Average balance as a decimal value.