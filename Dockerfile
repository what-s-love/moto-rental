FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
COPY data ./data

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy
ENV TZ=UTC
WORKDIR /app

RUN useradd -ms /bin/bash spring
USER spring

COPY --from=builder /workspace/target/*-SNAPSHOT.jar /app/app.jar
COPY --from=builder /workspace/data /app/data

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar" ]