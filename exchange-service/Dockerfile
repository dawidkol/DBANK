FROM openjdk:21-slim-bookworm
WORKDIR /app
EXPOSE 8085
COPY target/exchange-service-0.0.1-SNAPSHOT.jar /app/exchange-service.jar
ENTRYPOINT ["java","-jar","exchange-service.jar"]