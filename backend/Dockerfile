# ── Stage 1: Build ─────────────────────────────────────────────────────────────
# Use Maven + JDK 17 to compile and package the JAR.
# We copy pom.xml first and download dependencies in a separate layer — this
# means Docker's layer cache avoids re-downloading dependencies on every code
# change (only re-downloads if pom.xml changes).
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy POM first — cache the dependency download layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source and build
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
# Use a slim JRE (not full JDK) for the runtime image to minimise attack surface.
# eclipse-temurin:17-jre is a well-maintained, production-quality image.
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy only the fat JAR from the build stage
COPY --from=builder /build/target/payroute-backend-*.jar app.jar

# Expose the Spring Boot port
EXPOSE 8080

# SPRING_DATASOURCE_URL is overridden by docker-compose.yml to point to the
# postgres service. All other config comes from application.properties baked
# into the JAR, which docker-compose can further override with env vars.
ENTRYPOINT ["java", "-jar", "app.jar"]
