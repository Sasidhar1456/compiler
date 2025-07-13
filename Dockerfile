# ====================
# Single Stage: Just run the existing JAR
# ====================
FROM openjdk:21-slim

# Optional: Install Python (if needed for your app)
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    apt-get clean

WORKDIR /app

# 🟢 Copy the already-built JAR from your local target folder
COPY target/Compiler-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
