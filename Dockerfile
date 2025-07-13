# ====================
# 1st Stage: Build with Maven
# ====================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ====================
# 2nd Stage: Run Spring Boot app with Python
# ====================
FROM openjdk:17-slim

# Install Python
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    apt-get clean

# Set working directory
WORKDIR /app

# Copy built jar from stage 1
COPY --from=build /app/target/compiler-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 3000

# Run Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
