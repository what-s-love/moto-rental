# Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Зависимости
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Исходники и данные
COPY src ./src
COPY data ./data

# Сборка
RUN mvn -q -DskipTests package

# Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Пользователь
RUN useradd -ms /bin/bash spring
USER spring

# Копируем артефакт и данные
COPY --from=builder /workspace/target/*-SNAPSHOT.jar /app/app.jar
COPY --from=builder /workspace/data /app/data

# Создаём директорию для H2 БД
RUN mkdir -p /app/data

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
EXPOSE 8080

# Запуск с профилем H2
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-8080} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-h2} -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar" ]