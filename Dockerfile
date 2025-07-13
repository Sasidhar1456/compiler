# ===========================
# Stage 1: Build the JAR
# ===========================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy everything and build
COPY . .
RUN mvn clean package -DskipTests

# ===========================
# Stage 2: Run the JAR
# ===========================
FROM openjdk:21-slim

WORKDIR /app

# Install Python only if needed
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    apt-get clean

# Copy JAR from build stage
COPY --from=build /app/target/Compiler-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
