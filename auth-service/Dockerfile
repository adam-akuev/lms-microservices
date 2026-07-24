FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем всё, чтобы Maven видел структуру всех модулей
COPY . .

# Собираем только discovery-server
RUN mvn -pl auth-service -am clean package -DskipTests

# Этап запуска
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/auth-service/target/*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]