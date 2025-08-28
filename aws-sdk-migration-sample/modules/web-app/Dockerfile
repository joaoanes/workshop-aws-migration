FROM openjdk:21-slim
WORKDIR /app
COPY modules/web-app/build/libs/web-app-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
