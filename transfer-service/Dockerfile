FROM openjdk:21-slim-bookworm
WORKDIR /app
EXPOSE 8083
COPY target/transfer-service-0.0.1-SNAPSHOT.jar /app/transfer-service.jar
ENTRYPOINT ["java","-jar","transfer-service.jar"]