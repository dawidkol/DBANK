### **Project Description**
The `Loan Service` project is a Spring Boot application designed to manage loan operations, calculate credit scores, and process payments. It supports features like loan creation, installment payments, loan schedules, and reminders for due payments. The application integrates Kafka for messaging, Feign for inter-service communication, and JPA for database interactions. Resilience is achieved with Resilience4j and scheduling support is provided for automated tasks.

---

### **Dependencies**
The project relies on the following dependencies:
1. **Spring Boot**:
   - `spring-boot-starter-data-jpa`: For database interactions using JPA.
   - `spring-boot-starter-validation`: For input validation.
   - `spring-boot-starter-web`: For building RESTful APIs.
   - `spring-boot-starter-test`: For testing.
   - `spring-boot-devtools`: For development enhancements (hot reload).
2. **Database**:
   - `h2`: In-memory database for testing.
   - `flyway-core`: For managing database migrations.
3. **Kafka**:
   - `spring-kafka`: To enable Kafka messaging.
   - `spring-kafka-test`: For Kafka-related testing.
4. **Resilience**:
   - `spring-cloud-starter-circuitbreaker-resilience4j`: For fault tolerance.
5. **Service Discovery and Communication**:
   - `spring-cloud-starter-netflix-eureka-client`: For service discovery and registration.
   - `spring-cloud-starter-openfeign`: For inter-service communication.
6. **Utilities**:
   - `lombok`: To reduce boilerplate code in entities and DTOs.

---

### **Features**
1. **Loan Management**:
   - Create loans with user-specific details and currency types.
   - Retrieve loan details by ID.
   - Calculate monthly installments based on loan amount, interest rate, and duration.
   - Fetch all loans associated with a user.
2. **Loan Schedules**:
   - Generate and manage loan repayment schedules.
   - Update payment statuses (e.g., unpaid, overdue, paid on time).
   - Send reminders for upcoming or overdue payments.
3. **Payments**:
   - Process loan installment payments with validations.
   - Communicate with external transfer services for payment processing.
4. **Messaging**:
   - Kafka-based messaging for loan account creation and payment updates.
   - Scheduled notifications for payment reminders.
5. **Validation and Resilience**:
   - Input validations for loan creation and payments.
   - Resilience4j-based circuit breakers for fault-tolerant inter-service communication.
6. **Database Management**:
   - Flyway for database schema migration and versioning.
   - Hibernate for ORM and schema validation.

---

### **Endpoints**
The application exposes the following RESTful endpoints:

#### **Loan Management**
1. **POST `/loans`**:
   - **Description**: Creates a new loan.
   - **Request Body**: `CreateLoanDto` (userId, amount, interestRate, startDate, endDate, description, avgIncome, avgExpenses, existingLoanRepayments, currencyType).
   - **Response**: Created loan details (`LoanDto`).

2. **GET `/loans/{loanId}`**:
   - **Description**: Retrieves loan details by ID.
   - **Response**: Loan details (`LoanDto`).

3. **GET `/loans/monthly-installment`**:
   - **Description**: Calculates the monthly installment for a loan.
   - **Request Params**: `amount`, `interestRate`, `months`.
   - **Response**: Calculated installment amount.

4. **GET `/loans/{userId}/all`**:
   - **Description**: Fetches all loans associated with a user.
   - **Response**: List of loans (`LoanDto`).

#### **Payments**
1. **POST `/loans/{loanScheduleId}/pay`**:
   - **Description**: Processes a loan installment payment.
   - **Request Body**: `CreateLoanInstallmentTransfer` (senderAccountNumber, transferDate, description).
   - **Response**: Transfer details (`TransferDto`).

#### **Loan Schedules**
1. **GET `/loan-schedules/{loanId}`**:
   - **Description**: Retrieves the repayment schedule for a loan.
   - **Response**: List of schedule entries (`LoanScheduleDto`).

---

### **Additional Features**
1. **Audit Support**:
   - Tracks created and updated timestamps and users.
2. **Exception Handling**:
   - Handles common exceptions like `LoanNotExistsException`, `UserNotFoundException`, and validation errors.
3. **Schedulers**:
   - Automated tasks for updating payment statuses and sending reminders.