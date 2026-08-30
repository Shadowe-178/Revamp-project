# =========================
# 1. 建置前端
# =========================
FROM node:20 AS frontend-build

WORKDIR /app/frontend

COPY frontend/package*.json ./

RUN npm ci

COPY frontend/ ./

RUN npm run build -- --base=/


# =========================
# 2. 建置 Spring Boot
# =========================
FROM maven:3.9-eclipse-temurin-25 AS backend-build

WORKDIR /app/backend

COPY backend/pom.xml ./

RUN mvn dependency:go-offline

COPY backend/src ./src

# 將 Vite build 後的前端檔案放入 Spring Boot static
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

RUN mvn clean package -DskipTests


# =========================
# 3. 執行 Spring Boot
# =========================
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=backend-build /app/backend/target/*.jar app.jar

ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=render -Dserver.port=${PORT:-8080} -Dserver.address=0.0.0.0 -jar app.jar"]