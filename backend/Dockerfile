FROM maven:4.0.0-rc-4-eclipse-temurin-24 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -q -e -B clean package -DskipTests

FROM openjdk:24-ea-jdk-slim

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
