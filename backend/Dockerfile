FROM maven:4.0.0-rc-4-eclipse-temurin-24 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -q -e -B clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]