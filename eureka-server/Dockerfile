FROM openjdk:21-slim-bookworm
WORKDIR /app
EXPOSE 8000
COPY target/eureka-server-0.0.1-SNAPSHOT.jar /app/eureka-server.jar
ENTRYPOINT ["java","-jar","eureka-server.jar"]