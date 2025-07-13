# Use official OpenJDK image with Python support
FROM openjdk:17-slim

# Install Python
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    apt-get clean

# Set working directory
WORKDIR /app

# Copy Maven build output (after building locally or use Maven image in multi-stage)
COPY target/compiler-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 9090

# Run Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
