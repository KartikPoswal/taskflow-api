# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/taskflow-api-*.jar app.jar
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:8085/actuator/health || exit 1
ENTRYPOINT ["java","-jar","app.jar"]