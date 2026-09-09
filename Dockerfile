# Multi-stage Dockerfile for IPL Auction Software Backend
# Stage 1: Build JAR
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper & pom.xml for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copy application source code and compile
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Production Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create non-root system user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy built JAR artifact from build stage
COPY --from=build /app/target/ipl-auction-software-*.jar app.jar

# Expose standard application port & WebSocket port
EXPOSE 8080

# Configure JVM memory flags and launch
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
