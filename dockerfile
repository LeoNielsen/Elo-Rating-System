# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Kopiér pom og src
COPY pom.xml .
COPY src ./src

# Modtag profil fra workflow (prod/test)
ARG SPRING_PROFILES_ACTIVE=production
ENV SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE

# Byg JAR med korrekt profil
RUN mvn -q -DskipTests package -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# Runtime stage (multi-arch friendly)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
