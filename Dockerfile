# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package

# Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app

ENV DMS_BASE_URL=https://dms.example.com/api/v1
ENV DMS_API_KEY=simulated-api-key-for-dev

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
