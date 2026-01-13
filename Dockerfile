# Dockerfile
FROM eclipse-temurin:23-jdk-alpine AS build
WORKDIR /app

# Install Maven directly (simpler approach)
RUN apk add --no-cache maven

# Copy pom.xml
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/starcreator-webservice-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]