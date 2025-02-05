FROM openjdk:21-slim-bookworm
WORKDIR /app
EXPOSE 8086
COPY target/card-service-0.0.1-SNAPSHOT.jar /app/card-service.jar
ENTRYPOINT ["java","-jar","card-service.jar"]