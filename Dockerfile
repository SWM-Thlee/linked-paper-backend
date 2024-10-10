# Use Gradle to build the app with OpenJDK 21
FROM gradle:jdk21-alpine AS build
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src ./src

ARG SENTRY_AUTH_TOKEN
ENV SENTRY_AUTH_TOKEN=${SENTRY_AUTH_TOKEN}

# Grant permission to Gradle wrapper
RUN chmod +x gradlew

RUN ./gradlew spotlessApply

# Build the project without running tests
RUN ./gradlew build -x test

# Use JDK 21 to run the app
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/build/libs/linked-paper-api-server-0.0.1-SNAPSHOT.jar /app/linked-paper-api-server.jar

# Set the environment variable for active profile (can be overridden at runtime)
ARG ACTIVE_PROFILE=prod
ENV SPRING_PROFILES_ACTIVE=${ACTIVE_PROFILE}

ARG SENTRY_DSN
ENV SENTRY_DSN=${SENTRY_DSN}

# Expose port 8080
EXPOSE 8080

# Run the app with the specified profile
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/linked-paper-api-server.jar"]
