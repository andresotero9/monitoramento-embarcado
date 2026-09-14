# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia primeiro o pom.xml para aproveitar o cache de camadas do Docker:
# as dependências só são baixadas novamente se o pom.xml mudar.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Stage 2: runtime ----
FROM eclipse-temurin:17-jre-jammy

# ffmpeg: conversão RTSP -> HLS e captura de frames para o monitoramento de câmeras.
# iputils-ping: usado por PingCheckerImpl para testar conectividade com as câmeras.
# curl: usado apenas pelo HEALTHCHECK abaixo.
RUN apt-get update && \
    apt-get install -y --no-install-recommends ffmpeg iputils-ping curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/monitoramento-embarcado.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
