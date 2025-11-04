# Stage 1 — build the application
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Avoid running as root during mvn build (optional)
WORKDIR /workspace

# Copy only what is needed to download dependencies first (speeds up rebuilds)
COPY pom.xml .
# If you use a multi-module project, copy the parent pom and modules as needed.

# Download dependencies
RUN mvn -B -e -T1C dependency:go-offline

# Copy source files
COPY src ./src

# Build the jar (skip tests for faster build; remove -DskipTests if you want tests)
RUN mvn -B -e -T1C package -DskipTests

# Stage 2 — runtime image
FROM eclipse-temurin:17-jre-alpine

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy jar from build stage (adjust artifact name if different)
# This assumes the built jar is at target/*.jar (Spring Boot's default)
COPY --from=build /workspace/target/*-SNAPSHOT.jar app.jar
# If your artifact name is different, either rename or change the path above.

# Reduce image size a bit
RUN chown appuser:appgroup /app/app.jar

USER appuser

# Expose the port your Spring Boot app uses (default 8080)
EXPOSE 8080

# Health check (optional)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- --timeout=2 http://localhost:8080/actuator/health || exit 1

# JVM options can be provided by Render as environment variables (e.g. JAVA_OPTS)
ENV JAVA_OPTS=""

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/app.jar" ]