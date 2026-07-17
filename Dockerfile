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
COPY --from=build /app/target/DiloBackend-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto 8080
EXPOSE 8080

# ENTRYPOINT ajustado OBLIGATORIAMENTE para los 512MB de Render y su puerto dinámico
ENTRYPOINT java -Xmx256m -XX:MaxMetaspaceSize=128m -Xss512k -XX:+UseContainerSupport -Dserver.port=$PORT -jar app.jar