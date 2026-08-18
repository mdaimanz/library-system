FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package \
    && cp target/management-*.jar /workspace/application.jar

FROM eclipse-temurin:17-jre-jammy AS runtime

RUN groupadd --system spring \
    && useradd --system --gid spring --create-home spring

WORKDIR /app

COPY --from=builder --chown=spring:spring /workspace/application.jar ./application.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
