# --- Build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Copy just the wrapper/pom first so dependency resolution is cached
# separately from source changes.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package && \
    cp target/*.jar app.jar

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
