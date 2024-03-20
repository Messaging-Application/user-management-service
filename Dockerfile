# Use an official Maven image with Java 17 as a parent image for the build stage
FROM maven:3.8.4-openjdk-17 as build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml file into the container at /app
COPY pom.xml /app/

# Copy the project source into the container at /app
COPY src /app/src

# Package the application
RUN mvn -f /app/pom.xml clean package -DskipTests

# For the final image, use an OpenJDK 17 runtime
FROM openjdk:17-jdk-slim

# Set the Spring profiles active environment variable
ENV SPRING_PROFILES_ACTIVE=dev

# Copy the jar from the build stage to the final image
COPY --from=build /app/target/*.jar /usr/local/lib/app.jar

# Expose the port the app runs on
EXPOSE 8081

# Run the jar file 
ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]
