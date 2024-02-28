# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -e -C -B --show-version --no-transfer-progress
COPY src src
COPY ./.mvn/jvm.config ./.mvn/jvm.config
RUN mvn package -e -C -B --show-version --no-transfer-progress -DskipTests  # we assume tests are run before we get here

# Stage 2: Create the runtime image
# FROM azul/zulu-openjdk:21.0.2-jre AS runtime
FROM cgr.dev/chainguard/jre as runtime

LABEL maintainer="Joost van der Griendt"
LABEL description="This exports DNS records from Kubernetes resources, such as Istio's VirtualServices."
LABEL org.opencontainers.image.source="https://github.com/joostvdg/ingress-dns-export-controller"
LABEL org.opencontainers.image.title="ingress-dns-export-controller"
LABEL org.opencontainers.image.description="This exports DNS records from Kubernetes resources, such as Istio's VirtualServices."
LABEL org.opencontainers.image.authors="Joost van der Griendt<joostvdg@gmail.com>"

# https://pretius.com/blog/jvm-kubernetes/
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
USER 1001
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75", "-XX:+UseParallelGC", "-XX:ActiveProcessorCount=1", "-jar","/app/app.jar"]
