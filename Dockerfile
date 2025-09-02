# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copy only pom first to warm the dependency cache
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
# Copy the rest of the source
COPY . .
# Build a runnable jar (tests skipped to speed up)
RUN mvn -q -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the fat jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Port will be provided by platform (Railway/Render). Default to 8080 for local runs.
ENV PORT=8080
EXPOSE 8080

# Respect PORT and bind to 0.0.0.0
ENTRYPOINT ["java","-jar","/app/app.jar"]
