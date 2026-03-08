# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Define default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
