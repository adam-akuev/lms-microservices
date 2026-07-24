# Этап 1: Сборка в контейнере
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем ВЕСЬ проект, чтобы Maven видел структуру всех модулей
COPY . .

# Собираем api-gateway и зависимый lms-security
RUN mvn -pl api-gateway -am clean package -DskipTests

# Этап 2: Минимальный образ для запуска
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем собранный JAR из этапа build
COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]