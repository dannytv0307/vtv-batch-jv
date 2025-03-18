FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy Maven wrapper and POM file
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Create file directory
RUN mkdir -p file

# Build application with clean to ensure fresh build
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/target/batch-demo-0.0.1-SNAPSHOT.jar"] 