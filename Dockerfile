# --- Stage 1: Build the application ---
# Sử dụng image chứa Maven/Gradle và JDK để build code
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy file cấu hình dependency trước để tận dụng cache layer của Docker
COPY pom.xml .
# (Nếu dùng Gradle thì copy build.gradle và settings.gradle)

# Tải dependencies (giúp các lần build sau nhanh hơn nếu không đổi thư viện)
RUN mvn dependency:go-offline

# Copy toàn bộ source code vào
COPY src ./src

# Build ra file JAR (bỏ qua test để build nhanh hơn)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file JAR từ Stage 1 sang Stage 2
COPY --from=build /app/target/*.jar app.jar

# Expose port mà Spring Boot chạy (mặc định là 8080)
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]