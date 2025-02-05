FROM openjdk:21-slim-bookworm
WORKDIR /app
EXPOSE 9000
COPY target/gateway-server-0.0.1-SNAPSHOT.jar /app/gateway-server.jar
ENTRYPOINT ["java","-jar","gateway-server.jar"]