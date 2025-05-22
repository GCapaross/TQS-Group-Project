FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/chargingplatform-0.0.1-SNAPSHOT.jar"] 