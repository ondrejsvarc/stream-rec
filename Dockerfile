# ==========================================
# Build Frontend
# ==========================================
FROM node:20 AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# ==========================================
# Build Backend
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn dependency:go-offline
COPY backend/src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

# ==========================================
# Runtime Environment
# ==========================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && \
    apt-get install -y ffmpeg python3 wget && \
    rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -O /usr/local/bin/yt-dlp && \
    chmod a+rx /usr/local/bin/yt-dlp

COPY --from=backend-build /app/backend/target/*.jar app.jar

RUN mkdir -p /app/recordings /app/data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
