# ===============================
# BUILD FRONTEND (Angular)
# ===============================
FROM node:20-alpine AS frontend-build

WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install --legacy-peer-deps

COPY frontend/ .
RUN npm run build


# ===============================
# BUILD BACKEND (Spring Boot)
# ===============================
FROM maven:3.9.9-eclipse-temurin-21 AS backend-build

WORKDIR /app/backend

COPY backend/pom.xml ./
RUN mvn dependency:go-offline

COPY backend/ .
COPY --from=frontend-build /app/frontend/dist/*/browser /app/backend/src/main/resources/static
RUN mvn clean package -DskipTests


# ===============================
# RUNTIME (Spring + Angular)
# ===============================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiamos el JAR
COPY --from=backend-build /app/backend/target/*.jar app.jar

# Copiamos Angular dentro de Spring Boot
COPY --from=frontend-build /app/frontend/dist/*/browser /app/static
# Puerto HTTPS
EXPOSE 443

# Variables de entorno (se pasan desde docker-compose)
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]
