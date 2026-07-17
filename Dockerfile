# ==========================================
# Etapa 1: Construcción (Build)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias para aprovechar el caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y compilamos saltando los tests para mayor velocidad
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Ejecución (Producción)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos ÚNICAMENTE el archivo .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto que usará Spring Boot
EXPOSE 8080

# ENTRYPOINT con límites de memoria (MaxRAMPercentage) vitales para los 512MB de Render
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]