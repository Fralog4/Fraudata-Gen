# ==========================================
# STAGE 1: BUILD (The "Builder" environment)
# ==========================================

FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src

# --no-daemon velocizza la build su CI/CD spegnendo i processi in background di Gradle
RUN gradle shadowJar --no-daemon
# ==========================================
# STAGE 2: RUN (The "Production" environment)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*-all.jar fraudgen-api.jar

EXPOSE 8080

# Comando di avvio del microservizio
ENTRYPOINT ["java", "-jar", "fraudgen-api.jar"]

# Il Dockerfile è strutturato in due fasi: la prima fase (builder) compila l'applicazione usando Gradle 
# e la seconda fase (production) esegue l'applicazione in un ambiente più leggero basato su Alpine Linux. 
# Questo approccio riduce significativamente la dimensione dell'immagine finale, 
# includendo solo ciò che è necessario per eseguire l'applicazione.