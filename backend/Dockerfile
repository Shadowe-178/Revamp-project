# =========================
# 1. 建置 Frontend
# =========================
FROM node:20 AS frontend-build

WORKDIR /app/frontend

COPY frontend/package*.json ./

RUN npm ci

COPY frontend/ ./

RUN npm run build


# =========================
# 2. 建置 Backend
# =========================
FROM maven:3.9-eclipse-temurin-17 AS backend-build

WORKDIR /app/backend

COPY backend/pom.xml .

RUN mvn -B dependency:go-offline

COPY backend/src ./src

# 將 Vite build 結果放進 Spring Boot static
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

RUN mvn -B clean package -DskipTests


# =========================
# 3. 執行 Spring Boot
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=backend-build /app/backend/target/*.jar app.jar

ENV PORT=10000

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -Dserver.address=0.0.0.0 -jar app.jar"]