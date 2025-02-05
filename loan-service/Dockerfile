FROM openjdk:21-slim-bookworm
WORKDIR /app
EXPOSE 8084
COPY target/loan-service-0.0.1-SNAPSHOT.jar /app/loan-service.jar
ENTRYPOINT ["java","-jar","loan-service.jar"]