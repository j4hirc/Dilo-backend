# ==========================================
# Etapa 1: Construcción (Build)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos solo el pom.xml primero para aprovechar el caché de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Ejecución (Producción)
# ==========================================
# Usamos alpine para que el contenedor sea lo más liviano posible
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el archivo JAR (el asterisco ayuda a que no importe el nombre de la versión)
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto 8080
EXPOSE 8080

# ENTRYPOINT con banderas de memoria limitadas para los 512MB de Render
# -Xmx400m limita la RAM al máximo (400mb) para evitar el error Out of Memory
ENTRYPOINT ["java", "-Xmx400m", "-Xss512k", "-XX:+UseContainerSupport", "-jar", "app.jar"]