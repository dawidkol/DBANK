### **Project Description**
The `Transfer Service` project is a Spring Boot application designed to handle inter-account transfers, validate account balances, and manage the status of transfers. It provides features such as transfer creation, cancellation of scheduled transfers, and status updates via Kafka messaging. The service integrates JPA for database operations, Feign for communication with external services, and Resilience4j for fault tolerance.

---

### **Dependencies**
The project relies on the following dependencies:
1. **Spring Boot**:
    - `spring-boot-starter-data-jpa`: For database interactions using JPA.
    - `spring-boot-starter-validation`: For validating transfer data.
    - `spring-boot-starter-web`: For building RESTful APIs.
    - `spring-boot-starter-test`: For testing.
    - `spring-boot-devtools`: For development enhancements (hot reload).
2. **Database**:
    - `h2`: In-memory database for testing.
    - `flyway-core`: For managing database migrations.
3. **Kafka**:
    - `spring-kafka`: For asynchronous messaging.
    - `spring-kafka-test`: For testing Kafka-related components.
4. **Resilience**:
    - `spring-cloud-starter-circuitbreaker-resilience4j`: For fault tolerance.
5. **Service Discovery and Communication**:
    - `spring-cloud-starter-netflix-eureka-client`: For service registration and discovery.
    - `spring-cloud-starter-openfeign`: For inter-service communication.
6. **Utilities**:
    - `lombok`: To reduce boilerplate code in entities and DTOs.

---

### **Features**
1. **Transfer Management**:
    - Create new transfers with validation of sender and recipient account balances.
    - Cancel scheduled transfers if not yet processed.
    - Retrieve transfer details by ID.
    - List all transfers from a specific account or between specific accounts.
2. **Validation**:
    - Ensure sender has sufficient balance before processing transfers.
    - Validate transfer details, including account numbers and currency types.
3. **Messaging**:
    - Kafka-based messaging for processing transfers and updating their statuses.
    - Scheduled execution of pending transfers via Kafka events.
4. **Resilience**:
    - Circuit breakers for fault-tolerant communication with external account services.
5. **Database Management**:
    - Flyway for database migrations and versioning.
    - Hibernate for ORM and schema validation.

---

### **Endpoints**
The application exposes the following RESTful endpoints:

#### **Transfer Management**
1. **POST `/transfers`**:
    - **Description**: Creates a new transfer.
    - **Request Body**: `CreateTransferDto` (senderAccountNumber, recipientAccountNumber, amount, currencyType, transferDate, description).
    - **Response**: Created transfer details (`TransferDto`).

2. **GET `/transfers/{transferId}`**:
    - **Description**: Retrieves details of a transfer by ID.
    - **Response**: Transfer details (`TransferDto`).

3. **GET `/transfers/accounts/{accountNumber}`**:
    - **Description**: Lists all transfers associated with a specific account.
    - **Response**: List of transfers (`TransferDto`).

4. **GET `/transfers`**:
    - **Description**: Lists all transfers between two specific accounts.
    - **Request Params**: `senderAccountNumber`, `recipientAccountNumber`.
    - **Response**: List of transfers (`TransferDto`).

5. **DELETE `/transfers/{transferId}/cancel`**:
    - **Description**: Cancels a scheduled transfer.
    - **Response**: No content.

---

### **Additional Features**
1. **Audit Support**:
    - Tracks created and updated timestamps and users.
2. **Schedulers**:
    - Automated tasks for processing scheduled transfers.
3. **Exception Handling**:
    - Handles common errors like `InsufficientBalanceException`, `AccountNotExistsException`, and `TransferNotFoundException`.

