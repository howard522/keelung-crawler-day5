# ===== Build stage =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# 跳過測試加快建置，如要跑測試移除 -DskipTests
RUN mvn -q -DskipTests package

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
