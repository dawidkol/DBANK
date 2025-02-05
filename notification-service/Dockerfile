FROM openjdk:21-slim-bookworm
WORKDIR /app
EXPOSE 8081
COPY target/notification-service-0.0.1-SNAPSHOT.jar /app/notification-service.jar
ENTRYPOINT ["java","-jar","notification-service.jar"]