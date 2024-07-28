# Use a base image with Java 17 installed
FROM openjdk:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged jar file into the container at /app
COPY audit-system-parent/retrieval/target/retrieval-1.0.0.jar /app/retrieval-1.0.0.jar

# Specify the command to run your application
CMD ["java", "-jar", "retrieval-1.0.0.jar"]