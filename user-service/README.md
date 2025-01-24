### **Project Description**
The `User Service` project is a Spring Boot application designed for managing user information and performing operations such as user registration, retrieval, updates, and deletion. It integrates Kafka for asynchronous messaging and uses JPA for database interactions. This service also includes validation and exception handling to ensure data integrity and provide meaningful error feedback.

---

### **Dependencies**
The project relies on the following dependencies:
1. **Spring Boot**:
    - `spring-boot-starter-data-jpa`: For database interactions using JPA.
    - `spring-boot-starter-validation`: For validation of incoming data.
    - `spring-boot-starter-web`: For building RESTful web services.
    - `spring-boot-starter-test`: For unit and integration testing.
    - `spring-boot-devtools`: For development convenience (hot reload).
2. **Database**:
    - `h2`: An in-memory database for testing purposes.
    - `flyway-core`: For database migrations.
3. **Kafka**:
    - `spring-kafka`: To enable Kafka messaging.
    - `spring-kafka-test`: For testing Kafka-related components.
4. **Netflix Eureka**:
    - `spring-cloud-starter-netflix-eureka-client`: For service discovery and registration.
5. **Utilities**:
    - `lombok`: To reduce boilerplate code for data objects.
    - `json-patch`: To support JSON patch updates.

---

### **Features**
1. **User Management**:
    - User Registration: Creates a new user while ensuring no duplicates for email or phone.
    - Retrieve User by ID: Fetch user details by their unique ID.
    - Update User: Modify specific user details using JSON merge patches.
    - Soft Delete: Marks users as inactive without removing them from the database.
2. **Validation**:
    - Ensures input data adheres to specific constraints like valid email, phone numbers, and password format.
3. **Auditing**:
    - Tracks created/updated timestamps and users.
4. **Asynchronous Messaging**:
    - Publishes user registration events to a Kafka topic.
5. **Database Schema Management**:
    - Uses Flyway for managing database schema and migrations.
6. **Exception Handling**:
    - Custom exceptions for server errors, user conflicts, and not-found scenarios.

---

### **Endpoints**
The application provides the following RESTful endpoints:
1. **POST `/users`**:
    - **Description**: Registers a new user.
    - **Request Body**: `SaveUserDto` (firstName, lastName, email, phone, password, dateOfBirth).
    - **Response**: Created user details (`UserDto`).
2. **GET `/users/{userId}`**:
    - **Description**: Retrieves user details by ID.
    - **Response**: User details (`UserDto`).
3. **DELETE `/users/{userId}`**:
    - **Description**: Deletes a user (soft delete).
    - **Response**: No content.
4. **PATCH `/users/{userId}`**:
    - **Description**: Updates a user using a JSON merge patch.
    - **Request Body**: JSON Merge Patch.
    - **Response**: No content.

