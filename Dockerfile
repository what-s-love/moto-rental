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

# Фиксируем UID=1000 — совпадает с типичным ubuntu на хосте и с chown uploads
RUN useradd -u 1000 -ms /bin/bash spring

# jar и стартовые data сразу владельцу spring (иначе процесс не сможет писать в /app/data)
COPY --from=builder --chown=spring:spring /workspace/target/*-SNAPSHOT.jar /app/app.jar
COPY --from=builder --chown=spring:spring /workspace/data /app/data

RUN mkdir -p /app/data && chown -R spring:spring /app/data

USER spring

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-8080} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar" ]
