# Use an ARM-compatible OpenJDK base image (til Raspberry Pi)
FROM arm64v8/openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
