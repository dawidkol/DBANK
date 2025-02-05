# User Service

The **User Service** is a Spring Boot-based microservice designed to manage user registration, retrieval, and updates. It integrates with Kafka for event-driven communication and uses PostgreSQL as the primary database. The service also supports H2 for development purposes.

---

## Features

- **User Management**:
    - Register new users.
    - Retrieve user details by ID.
    - Update user information using JSON Merge Patch.
    - Soft delete users (deactivate instead of hard delete).

- **Kafka Integration**:
    - Sends user registration events to a Kafka topic (`user-service-registration-events`).

- **Validation**:
    - Validates user input (e.g., email, phone number, password) using Jakarta Validation.

- **Auditing**:
    - Tracks creation and modification timestamps and users for each entity.

- **Exception Handling**:
    - Custom exceptions for user not found, conflicts, and server errors.

- **Profiles**:
    - Supports `dev` (H2 in-memory database) and `prod` (PostgreSQL) profiles.

---

## Technologies Used

- **Spring Boot 3.4.0**
- **Spring Data JPA**
- **Spring Kafka**
- **PostgreSQL**
- **H2 Database** (for development)
- **Flyway** (for database migrations)
- **Lombok** (for reducing boilerplate code)
- **Jakarta Validation**
- **JSON Patch** (for partial updates)
- **Docker Compose** (for local development with PostgreSQL)
- **Eureka Client** (for service discovery)

---

## Setup Instructions

### Prerequisites

- Java 21
- Maven
- Docker (optional, for running PostgreSQL locally)
- Kafka (optional, for event-driven communication)

### Steps

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd user-service
### Steps:
1. Create a compose.yml file and copy the following section into this file:
```yml
services:
  zoo1:
    image: confluentinc/cp-zookeeper:7.3.2
    hostname: zoo1
    container_name: zoo1
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_SERVER_ID: 1
      ZOOKEEPER_SERVERS: zoo1:2888:3888 
        
  kafka1:
    image: confluentinc/cp-kafka:7.3.2
    hostname: kafka1
    container_name: kafka1
    ports:
      - "9092:9092"
      - "29092:29092"
    environment:
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka1:19092,EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:9092,DOCKER://host.docker.internal:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT,DOCKER:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ZOOKEEPER_CONNECT: "zoo1:2181"
      KAFKA_BROKER_ID: 1
      KAFKA_LOG4J_LOGGERS: "kafka.controller=INFO,kafka.producer.async.DefaultEventHandler=INFO,state.change.logger=INFO"
      KAFKA_AUTHORIZER_CLASS_NAME: kafka.security.authorizer.AclAuthorizer
      KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND: "true"
    depends_on:
      - zoo1

  kafka2:
    image: confluentinc/cp-kafka:7.3.2
    hostname: kafka2
    container_name: kafka2
    ports:
      - "9093:9093"
      - "29093:29093"
    environment:
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka2:19093,EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:9093,DOCKER://host.docker.internal:29093
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT,DOCKER:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ZOOKEEPER_CONNECT: "zoo1:2181"
      KAFKA_BROKER_ID: 2
      KAFKA_LOG4J_LOGGERS: "kafka.controller=INFO,kafka.producer.async.DefaultEventHandler=INFO,state.change.logger=INFO"
      KAFKA_AUTHORIZER_CLASS_NAME: kafka.security.authorizer.AclAuthorizer
      KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND: "true"
    depends_on:
      - zoo1
        
  kafka3:
    image: confluentinc/cp-kafka:7.3.2
    hostname: kafka3
    container_name: kafka3
    ports:
      - "9094:9094"
      - "29094:29094"
    environment:
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka3:19094,EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:9094,DOCKER://host.docker.internal:29094
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT,DOCKER:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ZOOKEEPER_CONNECT: "zoo1:2181"
      KAFKA_BROKER_ID: 3
      KAFKA_LOG4J_LOGGERS: "kafka.controller=INFO,kafka.producer.async.DefaultEventHandler=INFO,state.change.logger=INFO"
      KAFKA_AUTHORIZER_CLASS_NAME: kafka.security.authorizer.AclAuthorizer
      KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND: "true"
    depends_on:
      - zoo1
```
2. Open terminal, navigate to the directory where you create compose.yml file and type:
```bash
   docker compose up -d
```
3. Clone the repository:
```bash
  git clone https://github.com/dawidkol/dbank.git
  cd dbank/user-service
```
4. Build and run project in development mode:
---
 Build: development profile - uses H2 in-memory database without registered as eureka client
```bash
  mvn clean install -Dspring.profiles.active=dev -Deureka.client.register-with-eureka=false
```
Run the Application: development profile
```bash
  java -jar target/user-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev --eureka.client.register-with-eureka=false
```
---
5. Build and run project in production mode

 Build: production profile - uses H2 in-memory database without registered as eureka client
```bash
  mvn clean install -Dspring.profiles.active=prod -Deureka.client.register-with-eureka=false
```
 Run the Application: development profile
```bash
  java -jar target/user-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --eureka.client.register-with-eureka=false
```


[//]: # ()
[//]: # (- **For production** &#40;uses PostgreSQL&#41;)

[//]: # (```bash)

[//]: # (mvn spring-boot:run -Dspring.profiles.active=prod -Dspring.datasource.username=admin -Dspring.datasource.password=password)

[//]: # (```)