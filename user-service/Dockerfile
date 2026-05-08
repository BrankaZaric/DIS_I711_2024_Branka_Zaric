FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/user-service-1.0.0-SNAPSHOT.jar user-service.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "user-service.jar"]