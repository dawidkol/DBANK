FROM openjdk:21-slim-bookworm
WORKDIR /app
EXPOSE 8080
COPY target/user-service-0.0.1-SNAPSHOT.jar /app/user-service.jar
ENTRYPOINT ["java","-jar","user-service.jar"]