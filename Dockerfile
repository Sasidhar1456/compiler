# ===========================
# Stage 1: Build the JAR
# ===========================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy everything and build the project
COPY . .
RUN mvn clean package -DskipTests


# ===========================
# Stage 2: Run the JAR
# ===========================
FROM openjdk:21-slim

WORKDIR /app

# ✅ Install Python and create symbolic link
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    ln -s /usr/bin/python3 /usr/bin/python && \
    apt-get clean

# Copy the JAR from the build stage
COPY --from=build /app/target/Compiler-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 3000

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
    