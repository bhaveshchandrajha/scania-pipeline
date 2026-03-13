# Warranty Claim Management - UAT Container
# Build from project root: docker build -t warranty-claim-uat:1.0.0 .

# Stage 1: Build Angular UI
FROM node:20-alpine AS angular-build
WORKDIR /build

COPY warranty-ui/package*.json ./
RUN npm ci

COPY warranty-ui/ ./
RUN npm run build -- --base-href /angular/

# Stage 2: Build Spring Boot
FROM maven:3.9-eclipse-temurin-17-alpine AS maven-build
WORKDIR /build

# Copy Maven project
COPY warranty_demo/ ./warranty_demo/

# Overwrite with fresh Angular build
COPY --from=angular-build /build/dist/warranty-ui/browser/ ./warranty_demo/src/main/resources/static/angular/

WORKDIR /build/warranty_demo
RUN mvn package -DskipTests -q

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN adduser -D -u 1000 appuser && mkdir -p /data && chown -R appuser:appuser /data

COPY --from=maven-build /build/warranty_demo/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8081

USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
