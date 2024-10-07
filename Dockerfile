# Use the Gradle image with JDK 17
FROM gradle:7.5.0-jdk17
# Set the working directory
WORKDIR /opt/app
# Copy the build files into the container
COPY ./build/libs/Ticketwave_S3-0.0.1-SNAPSHOT.jar ./
# Define the entry point
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar Ticketwave_S3-0.0.1-SNAPSHOT.jar"]