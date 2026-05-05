FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Expect the JAR to be built before Docker build
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]