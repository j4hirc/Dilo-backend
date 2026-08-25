
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/DiloBackend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT java -Xmx280m -XX:MaxMetaspaceSize=100m -XX:ReservedCodeCacheSize=64m -Xss512k \
  -XX:+UseContainerSupport -XX:TieredStopAtLevel=1 -Xshare:auto \
  -Dserver.port=$PORT -jar app.jar
