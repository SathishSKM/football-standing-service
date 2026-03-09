FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -s /root/.m2/settings.xml

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar football-standing-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "football-standing-service.jar"]