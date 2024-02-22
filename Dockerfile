# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -e -C -B --show-version
COPY src src
RUN mvn package -e -C -B --show-version

# Stage 2: Create the runtime image
FROM azul/zulu-openjdk:21.0.2-jre AS runtime
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
