# ====================
# 1st Stage: Build with Maven
# ====================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build and list target directory to debug JAR output
RUN mvn clean package -DskipTests && ls -lh target/

# ====================
# 2nd Stage: Run Spring Boot app with Python
# ====================
FROM openjdk:21-slim

# Install Python
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    apt-get clean

WORKDIR /app

# 🟡 Use correct JAR name from above step!
COPY --from=build /app/target/compiler-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
