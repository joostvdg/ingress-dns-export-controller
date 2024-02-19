# Stage 1: Build the application
FROM --platform=linux/amd64 maven:3.8.4-jdk-11-slim AS build-amd64
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -f pom.xml clean package -DskipTests

FROM --platform=linux/arm64 maven:3.8.4-jdk-11-slim AS build-arm64
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -f pom.xml clean package -DskipTests

# Stage 2: Create the runtime image
FROM --platform=linux/amd64 openjdk:11-jre-slim AS runtime-amd64

FROM --platform=linux/arm64 openjdk:11-jre-slim AS runtime-arm64

FROM runtime-${TARGETARCH}

COPY --from=build-${TARGETARCH} /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
