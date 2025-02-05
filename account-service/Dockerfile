FROM openjdk:21-slim-bookworm
WORKDIR /app
EXPOSE 8080
COPY target/account-service-0.0.1-SNAPSHOT.jar /app/account-service.jar
ENTRYPOINT ["java","-jar","account-service.jar"]