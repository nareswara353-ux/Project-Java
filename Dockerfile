# Stage 1: Build dengan Maven (menggunakan image dengan JDK 21)
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Salin pom.xml dan download dependencies (layer cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Salin seluruh source code dan build aplikasi
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime dengan JRE 21 slim (hanya untuk menjalankan JAR)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Buat user non-root untuk keamanan
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Salin JAR dari stage build (gunakan layered JAR untuk cache)
COPY --from=build /app/target/java-enterprise-portfolio-0.0.1-SNAPSHOT.jar app.jar

# Expose port aplikasi
EXPOSE 8080

# Healthcheck untuk Actuator (opsional)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# Jalankan aplikasi
ENTRYPOINT ["java", "-jar", "app.jar"]
